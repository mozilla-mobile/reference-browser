import datetime
import os

import git
import jsone
import slugid
import taskcluster
import yaml

from decisionlib.common import SlugId


def schedule_hook(html_url: str, task_id: SlugId):
    if not html_url.startswith('https://github.com/'):
        raise ValueError('expected repository to be a GitHub repository (accessed via HTTPs)')

    html_url = html_url[:-4] if html_url.endswith('.git') else html_url
    html_url = html_url[:-1] if html_url.endswith('/') else html_url
    repo = git.Repo.clone_from(html_url, 'repository', depth=1)

    repository_full_name = html_url[len('https://github.com/'):]
    ref = str(repo.head.ref)
    commit = str(repo.head.ref.commit)

    with open(os.path.join('repository', '.taskcluster.yml'), 'rb') as f:
        taskcluster_yml = yaml.safe_load(f)

    # provide a similar JSON-e context to what taskcluster-github provides
    slugids = {}

    def as_slugid(name):
        if name not in slugids:
            slugids[name] = slugid.nice()
        return slugids[name]

    context = {
        'tasks_for': 'cron',
        'cron': {
            'task_id': task_id,
        },
        'now': datetime.datetime.utcnow().isoformat()[:23] + 'Z',
        'as_slugid': as_slugid,
        'event': {
            'repository': {
                'html_url': html_url,
                'full_name': repository_full_name,
            },
            'release': {
                'tag_name': commit,
                'target_commitish': ref,
            },
            'sender': {
                'login': 'TaskclusterHook',
            },
        },
    }

    rendered = jsone.render(taskcluster_yml, context)
    if len(rendered['tasks']) != 1:
        raise RuntimeError('Expected .taskcluster.yml in {} to only produce one cron task'
                           .format(html_url))

    task = rendered['tasks'][0]
    queue = taskcluster.Queue({'baseUrl': 'http://taskcluster/queue/v1'})
    queue.createTask(task['taskId', task])




