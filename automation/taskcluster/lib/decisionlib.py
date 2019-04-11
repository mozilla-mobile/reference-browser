import datetime
from enum import Enum
from typing import Optional, Tuple, List, Any, Dict, Callable

import taskcluster

SlugId = str


class TrustLevel(Enum):
    L1 = 1
    L3 = 3


class Treeherder:
    def __init__(
            self,
            symbol: str,
            job_kind: str,
            tier: int,
            machine_platform: str,
            group_symbol: str = None,
    ):
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
    repo_url: str
    repo_ref: str
    repo_commit: str
    product_name: str
    level: TrustLevel

    def __init__(
            self,
            repo_url: str,
            repo_ref: str,
            repo_commit: str,
            product_name: str,
            level: TrustLevel,
    ):
        self.repo_url = repo_url
        self.repo_ref = repo_ref
        self.repo_commit = repo_commit
        self.product_name = product_name
        self.level = level


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
    configurations: List[Callable[['Task', ConfigurationContext], None]]

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
        self.configurations = []

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

    def with_treeherder(self, treeherder: Treeherder):
        self.treeherder = treeherder
        return self.configure(lambda builder, context: builder.append_route(
            'tc-treeherder.v2.{}.{}'.format(context.product_name, context.commit)))

    def configure(self, configuration: Callable[['Task', ConfigurationContext], None]):
        self.configurations.append(configuration)
        return self

    def compile(
            self,
            task_id: SlugId,
            task_group_id: SlugId,
            product_name: str,
            level: TrustLevel,
            scheduler_id: str,
            owner: str,
            source: str,
            repo_url: str,
            repo_ref: str,
            repo_commit: str,
    ):
        context = ConfigurationContext(repo_url, repo_ref, repo_commit, product_name, level)
        worker_type = self.decide_worker_type(level)
        for configuration in self.configurations:
            configuration(self, context)

        return {
            'scheduler_id': scheduler_id,
            'taskGroupId': task_group_id,
            'provisionerId': self.provisioner_id,
            'workerType': worker_type,
            'metadata': {
                'name': self.name,
                'description': self.description,
                'owner': owner,
                'source': source,
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


class SigningType(Enum):
    DEP = 'dep'
    RELEASE = 'release'


def signing_task(
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

    return Task(name, 'aws-provisioner-v1', decide_worker_type, payload) \
        .with_dependencies([assemble_task_id for assemble_task_id, _ in artifacts]) \
        .configure(
        lambda builder, context: builder.with_scopes([
            'project:mobile:{}:releng:signing:format:{}'.format(
                context.product_name, signing_format),
            'project:mobile:{}:releng:signing:cert:{}'.format(
                context.product_name, signing_type.value),
        ]))


class ArtifactType(Enum):
    FILE = 'file'
    DIRECTORY = 'directory'


class Artifact:
    public_path: str
    fs_path: str
    type: ArtifactType

    def __init__(self, public_path: str, fs_path: str, type: ArtifactType = ArtifactType.FILE):
        self.public_path = public_path
        self.fs_path = fs_path
        self.type = type


class ShellTask:
    task: Task
    image: str
    commands: str
    artifacts: List[Artifact]
    secrets: List[Tuple[str, str, str]]

    def __init__(
            self,
            name: str,
            provisioner_id: str,
            decide_worker_type: Callable[[TrustLevel], str],
            image: str,
            commands: str,
            artifacts: List[Artifact]
    ):
        self.task = Task(name, provisioner_id, decide_worker_type)
        self.image = image
        self.commands = commands
        self.artifacts = artifacts
        self.secrets = []

    def append_secret(self, secret, key, target_file):
        self.secrets.append((secret, key, target_file))
        self.task.append_scope('secrets:get:{}'.format(secret))
        return self

    def to_task(self):
        fetch_secrets_commands = [
            'python automation/taskcluster/helper/get-secret.py -s {} -k {} -f {}'.format(
                secret, key, target_file
            ) for secret, key, target_file in self.secrets
        ]

        def configuration(builder: Task, context: ConfigurationContext):
            commands = ' && '.join([
                'export TERM=dumb',
                'git fetch {} {}'.format(context.repo_url, context.repo_ref),
                'git config advice.detachedHead false',
                'git checkout FETCH_HEAD',
                *fetch_secrets_commands,
                self.commands,
            ])

            builder.with_payload({
                'features': {
                    'chainOfTrust': True if self.artifacts else False,
                    'taskclusterProxy': True if self.secrets else False,
                },
                'image': self.image,
                'command': [
                    '/bin/bash',
                    '--login',
                    '-cx',
                    commands
                ],
                'artifacts': {
                    taskcluster_path: {
                        'type': 'file',
                        'path': fs_path,
                    }
                    for taskcluster_path, fs_path in self.artifacts.items()
                }
            })

        return self.task.configure(configuration)


def shell_task(
        name: str,
        image: str,
        commands: str,
        artifacts: List[Artifact],
):
    def decide_worker_type(level: TrustLevel):
        return 'mobile-{}-b-fenix'.format(level.value)

    return ShellTask(
        name,
        'aws-provisioner-v1',
        decide_worker_type,
        image,
        commands,
        artifacts
    )

# trust-domain/product-name = product


class Scheduler:
    _task_builders: List[Tuple[SlugId, Task]]

    def __init__(self):
        self._task_builders = []

    def append_task(self, builder: Task):
        task_id = 'task_id'
        self._task_builders.append((task_id, builder))
        return 'task_id'

    def bonk(self, level: TrustLevel):

    def schedule_tasks(
            self,
            task_group_id: SlugId,
            product_name: str,
            level: TrustLevel,
            scheduler_id: str,
            owner: str,
            source: str,
            repo_url: str,
            repo_ref: str,
            repo_commit: str
    ):
        for task_id, builder in self._task_builders:
            _task = builder.compile(task_id, task_group_id, product_name, level, scheduler_id,
                                    owner, source, repo_url, repo_ref, repo_commit)


def main():
    scheduler = Scheduler()
    is_staging = False

    prefix_secret = '{}/project/mobile'.format('garbage/staging' if is_staging else '/')
    sentry_secret = '{}/fenix/sentry'.format(prefix_secret)
    leanplum_secret = '{}/fenix/leanplum'.format(prefix_secret)
    notify_route = 'notify.email.fenix-eng-notifications@mozilla.com.on-failed'
    assemble_task_id = scheduler.append_task(
        shell_task(
            'Assemble',
            'mozillamobile/fenix:1.3',
            './gradlew --no-daemon -PcrashReports=true clean test assembleRelease',
            [Artifact('public/target.apk', 'build/outputs/apk/release/aarch64/release.apk')],
        )
        .append_secret(sentry_secret, 'dsn', '.sentry_token')
        .append_secret(leanplum_secret, 'production', '.leanplum_token')
        .to_task()
        .with_routes([] if is_staging else [notify_route])
        .with_treeherder(Treeherder('NA', 'build', 1, 'android-all'))
    )

    scheduler.append_task(signing_task(
        'Signing',
        'autograph_apk',
        SigningType.DEP,
        [(assemble_task_id, ['public/target.apk'])],
    ).with_treeherder(Treeherder('A', 'build', 1, 'android-arm-debug')))
