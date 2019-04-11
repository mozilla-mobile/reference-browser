import datetime
from enum import Enum
import os

from git import Repo
from typing import Optional, Tuple, List, Any, Callable

import taskcluster

from decisionlib.scheduler import Scheduler

SlugId = str


class TrustLevel(Enum):
    L1 = 1
    L3 = 3


class Trigger:
    def __init__(self, task_group_id: SlugId, level: TrustLevel, owner: str, source: str):
        self.task_group_id = task_group_id
        self.level = level
        self.owner = owner
        self.source = source


class Checkout:
    def __init__(self, product_id, html_url: str, ref: str, commit: str):
        self.product_id = product_id
        self.html_url = html_url
        self.ref = ref
        self.commit = commit

    @staticmethod
    def from_cwd():
        repo = Repo(os.getcwd())
        remote = repo.remote()
        ref = repo.head.ref

        if not remote.url.startswith('https:/github.com'):
            raise RuntimeError('Expected remote to be a GitHub repository (accessed via HTTPs)')

        if remote.url.endswith('.git'):
            html_url = remote.url[:-4]
        elif remote.url.endswith('/'):
            html_url = remote.url[:-1]
        else:
            html_url = remote.url

        product_id = remote.url.split('/')[-1]
        return Checkout(product_id, html_url, str(ref), str(ref.commit))


class Treeherder:
    def __init__(self, job_kind: str, machine_platform: str, tier: int, symbol: str,
                 group_symbol: str = None):
        self.symbol = symbol
        self.job_kind = job_kind
        self.tier = tier
        self.machine_platform = machine_platform
        self.group_symbol = group_symbol


class Priority(Enum):
    HIGHEST = 'highest'
    VERY_HIGH = 'very-high'
    HIGH = 'high'
    MEDIUM = 'medium'
    LOW = 'low'
    VERY_LOW = 'very-low'
    LOWEST = 'lowest'
    NORMAL = 'normal'


class ConfigurationContext:
    product_id: str
    checkout: Checkout
    trigger: Trigger

    def __init__(
            self,
            product_id: str,
            checkout: Checkout,
            trigger: Trigger,
    ):
        self.product_id = product_id
        self.checkout = checkout
        self.trigger = trigger


class Task:
    name: str
    provisioner_id: str
    payload: Any
    decide_worker_type: Callable[[TrustLevel], str]
    description: str
    priority: Optional[Priority]
    treeherder: Optional[Treeherder]
    routes: List[str]
    dependencies: List[SlugId]
    scopes: List[str]
    map_functions: List[Callable[['Task', ConfigurationContext], None]]

    def __init__(
            self,
            name: str,
            provisioner_id: str,
            decide_worker_type: Callable[[TrustLevel], str],
            payload: Any = None,
    ):
        self.name = name
        self.provisioner_id = provisioner_id
        self.payload = payload
        self.decide_worker_type = decide_worker_type
        self.description = ''
        self.routes = []
        self.dependencies = []
        self.scopes = []
        self.map_functions = []

    def with_description(self, description: str):
        self.description = description
        return self

    def with_priority(self, priority: Priority):
        self.priority = priority
        return self

    def with_payload(self, payload: Any):
        self.payload = payload
        return self

    def with_routes(self, routes: List[str]):
        self.routes = routes
        return self

    def append_route(self, route: str):
        self.routes.append(route)
        return self

    def with_scopes(self, scopes: List[str]):
        self.scopes = scopes
        return self

    def append_scope(self, scope: str):
        self.scopes.append(scope)
        return self

    def with_dependencies(self, dependencies: List[SlugId]):
        self.dependencies = dependencies
        return self

    def append_dependency(self, dependency: SlugId):
        self.dependencies.append(dependency)
        return self

    def with_notify_owner(self):
        self.map(lambda builder, context: builder.append_route(
            'notify.email.{}.on-failed'.format(context.trigger.owner)))

    def with_treeherder(self, job_kind: str, machine_platform: str, tier: int, symbol: str,
                 group_symbol: str = None):
        self.treeherder = Treeherder(job_kind, machine_platform, tier, symbol, group_symbol)
        return self.map(lambda builder, context: builder.append_route(
            'tc-treeherder.v2.{}.{}'.format(context.product_id, context.checkout.commit)))

    def map(self, configuration: Callable[['Task', ConfigurationContext], None]):
        self.map_functions.append(configuration)
        return self

    def schedule(self, scheduler: Scheduler):
        return scheduler.append_task(self)

    def compile(
            self,
            task_id: SlugId,
            trigger: Trigger,
            checkout: Checkout,
    ):
        context = ConfigurationContext(checkout.product_id, checkout, trigger)
        worker_type = self.decide_worker_type(trigger.level)
        for map_function in self.map_functions:
            map_function(self, context)

        return {
            'scheduler_id': '{}-level-{}'.format(checkout.product_id, trigger.level),
            'taskGroupId': trigger.task_group_id,
            'provisionerId': self.provisioner_id,
            'workerType': worker_type,
            'metadata': {
                'name': self.name,
                'description': self.description,
                'owner': trigger.owner,
                'source': trigger.source,
            },
            'routes': self.routes,
            'dependencies': [task_id] + self.dependencies,
            'scopes': self.scopes,
            'payload': self.payload or {},
            'priority': self.priority.value if self.priority else None,
            'extra': {
                'treeherder': {
                    'symbol': self.treeherder.symbol,
                    'groupSymbol': self.treeherder.group_symbol,
                    'jobKind': self.treeherder.job_kind,
                    'tier': self.treeherder.tier,
                    'machine': {
                        'platform': self.treeherder.machine_platform
                    },
                } if self.treeherder else {}
            },
            'created': taskcluster.stringDate(datetime.datetime.now()),
            'deadline': taskcluster.fromNow('1 day'),
        }


class ArtifactType(Enum):
    FILE = 'file'
    DIRECTORY = 'directory'


class AndroidArtifact:
    taskcluster_path: str
    output_path: str
    type: ArtifactType

    def __init__(self, public_path: str, output_path: str):
        self.taskcluster_path = public_path
        self.output_path = output_path
        self.type = ArtifactType.FILE

    def fs_path(self, product_id: str):
        return '/build/{}/app/build/outputs/apk/{}'.format(product_id, self.output_path)


class ShellTask(Task):
    task: Task
    image: str
    commands: str
    artifacts: List[AndroidArtifact]
    file_secrets: List[Tuple[str, str, str]]

    def __init__(
            self,
            name: str,
            provisioner_id: str,
            decide_worker_type: Callable[[TrustLevel], str],
            image: str,
            commands: str,
            artifacts: List[AndroidArtifact]
    ):
        super().__init__(name, provisioner_id, decide_worker_type)
        self.image = image
        self.commands = commands
        self.artifacts = artifacts
        self.file_secrets = []

    def append_secret(self, secret):
        self.task.append_scope('secrets:get:{}'.format(secret))
        return self

    def append_file_secret(self, secret, key, target_file):
        self.file_secrets.append((secret, key, target_file))
        return self.append_secret(secret)

    def compile(
            self,
            task_id: SlugId,
            trigger: Trigger,
            checkout: Checkout,
    ):
        fetch_file_secrets_commands = [
            'python automation/taskcluster/helper/get-secret.py -s {} -k {} -f {}'.format(
                secret, key, target_file
            ) for secret, key, target_file in self.file_secrets
        ]

        def configuration(builder: Task, context: ConfigurationContext):
            commands = ' && '.join([
                'export TERM=dumb',
                'git fetch {} {}'.format(context.checkout.html_url, context.checkout.ref),
                'git config advice.detachedHead false',
                'git checkout FETCH_HEAD',
                *fetch_file_secrets_commands,
                self.commands,
            ])

            builder.with_payload({
                'features': {
                    'chainOfTrust': True if self.artifacts else False,
                    'taskclusterProxy': True if self.file_secrets else False,
                },
                'image': self.image,
                'command': [
                    '/bin/bash',
                    '--login',
                    '-cx',
                    commands
                ],
                'artifacts': {
                    artifact.taskcluster_path: {
                        'type': artifact.type.value,
                        'path': artifact.fs_path(context.product_id),
                    }
                    for artifact in self.artifacts
                }
            })

        self.task.map(configuration)
        return super().compile(task_id, trigger, checkout)


def shell_task(
        name: str,
        image: str,
        commands: str,
        artifacts: List[AndroidArtifact] = (),
):
    def decide_worker_type(level: TrustLevel):
        return 'mobile-{}-b-ref-browser'.format(level.value)

    return ShellTask(
        name,
        'aws-provisioner-v1',
        decide_worker_type,
        image,
        commands,
        artifacts
    )


class SigningType(Enum):
    DEP = 'dep'
    RELEASE = 'release'


def sign_task(
        name: str,
        signing_format: str,
        signing_type: SigningType,
        artifacts: List[Tuple[SlugId, List[str]]],
):
    payload = {
        'upstreamArtifacts': [{
            'paths': apk_paths,
            'formats': [signing_format],
            'taskId': assemble_task_id,
            'taskType': 'build'
        } for assemble_task_id, apk_paths in artifacts]
    }

    def decide_worker_type(level: TrustLevel):
        if level == TrustLevel.L1 and signing_type == SigningType.RELEASE:
            raise RuntimeError('Cannot use RELEASE signing type with a trust level of 1')

        return 'mobile-signing-dep-v1' if signing_type == SigningType.DEP else 'mobile-signing-v1'

    return Task(name, 'scriptworker-prov-v1', decide_worker_type, payload) \
        .with_dependencies([assemble_task_id for assemble_task_id, _ in artifacts]) \
        .map(
        lambda builder, context: builder.with_scopes([
            'project:mobile:{}:releng:signing:format:{}'.format(
                context.product_id, signing_format),
            'project:mobile:{}:releng:signing:cert:{}'.format(
                context.product_id, signing_type.value),
        ]))


def google_play_task(
        name: str,
        track: str,
        artifacts: List[Tuple[SlugId, List[str]]],
):
    payload = {
        'commit': True,
        'google_play_track': track,
        'upstreamArtifacts': [{
            'paths': apk_paths,
            'taskId': signing_task_id,
            'taskType': 'signing'
        } for signing_task_id, apk_paths in artifacts]
    }

    def decide_worker_type(level: TrustLevel):
        return 'mobile-pushapk-dep-v1' if level == TrustLevel.L1 else 'mobile-pushapk-v1'

    return Task(name, 'scriptworker-prov-v1', decide_worker_type, payload) \
        .with_dependencies([signing_task_id for signing_task_id, _ in artifacts]) \
        .map(
        lambda builder, context: builder.with_scopes([
            'project:mobile:{name}:releng:googleplay:product:{name}{type}'.format(
                name=name,
                type=':dep' if context.trigger.level == TrustLevel.L1 else ''
            )
        ]))
