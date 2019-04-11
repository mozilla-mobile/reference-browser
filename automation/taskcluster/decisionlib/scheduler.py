import json
from typing import Tuple, List

from decisionlib.__init__ import SlugId, Task, Trigger, Checkout


class Scheduler:
    _task_builders: List[Tuple[SlugId, Task]]

    def __init__(self):
        self._task_builders = []

    def append_task(self, builder: Task):
        task_id = 'task_id'
        self._task_builders.append((task_id, builder))
        return 'task_id'

    @staticmethod
    def write_cot_files(full_task_graph):
        with open('task-graph.json', 'w') as f:
            json.dump(full_task_graph, f)

        # These files are needed to keep chainOfTrust happy. However, they are not needed
        # for many projects at the moment. For more details, see:
        # https://github.com/mozilla-releng/scriptworker/pull/209/files#r184180585
        for file_names in ('actions.json', 'parameters.yml'):
            with open(file_names, 'w') as f:
                json.dump({}, f)

    def schedule_tasks(
            self,
            queue,
            trigger: Trigger,
            checkout: Checkout,
            write_cot_files=write_cot_files
    ):
        full_task_graph = {}

        for task_id, builder in self._task_builders:
            task = builder.compile(task_id, trigger, checkout)
            queue.createTask(task_id, task)
            full_task_graph[task_id] = {
                'task': queue.task(task_id)
            }

        write_cot_files(full_task_graph)
