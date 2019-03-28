import taskcluster

from lib import tasks


class TaskGraph:
    def __init__(self, queue):
        self._task_graph = {}
        self._queue = queue

    def schedule_new_task(self, task):
        task_id = taskcluster.slugId()
        tasks.schedule_task(self._queue, task_id, task)

        self._task_graph[task_id] = {
            'task': self._queue.task(task_id)
        }
        return task_id

    def get_raw_graph(self):
        return self._task_graph

