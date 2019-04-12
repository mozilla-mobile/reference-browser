import argparse

from decisionlib.hook.hook import schedule_hook


def main():
    parser = argparse.ArgumentParser(
        description='Schedule tasks or request secrets from taskcluster'
    )

    command_subparser = parser.add_subparsers(dest='command')
    hook_parser = command_subparser.add_parser('schedule-hook')

    hook_parser.add_argument('repository')
    hook_parser.add_argument('task_id')

    result = parser.parse_args()
    if result.command == 'schedule-hook':
        schedule_hook(result.repository, result.task_id)


if __name__ == '__main__':
    main()
