# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

"""
Decision task
"""

from __future__ import print_function

import os

import argparse
import datetime
import taskcluster

from lib.gradle import get_variant
from lib.tasks import (
    TaskBuilder,
    schedule_task_graph,
    fetch_mozharness_task_id,
)
from lib.chain_of_trust import (
    populate_chain_of_trust_task_graph,
    populate_chain_of_trust_required_but_unused_files
)
from lib.variant import Variant

REPO_URL = os.environ.get('MOBILE_HEAD_REPOSITORY')
COMMIT = os.environ.get('MOBILE_HEAD_REV')
PR_TITLE = os.environ.get('GITHUB_PULL_TITLE', '')
SHORT_HEAD_BRANCH = os.environ.get('SHORT_HEAD_BRANCH')

# If we see this text inside a pull request title then we will not execute any tasks for this PR.
SKIP_TASKS_TRIGGER = '[ci skip]'


BUILDER = TaskBuilder(
    task_id=os.environ.get('TASK_ID'),
    repo_url=REPO_URL,
    git_ref=os.environ.get('MOBILE_HEAD_BRANCH'),
    short_head_branch=SHORT_HEAD_BRANCH,
    commit=COMMIT,
    owner="fenix-eng-notifications@mozilla.com",
    source='{}/raw/{}/.taskcluster.yml'.format(REPO_URL, COMMIT),
    scheduler_id=os.environ.get('SCHEDULER_ID', 'taskcluster-github'),
    tasks_priority=os.environ.get('TASKS_PRIORITY'),
    date_string=os.environ.get('BUILD_DATE'),
    trust_level=int(os.environ.get('TRUST_LEVEL')),
)


def pr():
    if SKIP_TASKS_TRIGGER in PR_TITLE:
        print("Pull request title contains", SKIP_TASKS_TRIGGER)
        print("Exit")
        return {}

    build_tasks = {}
    signing_tasks = {}
    other_tasks = {}

    variant = get_variant('debug')
    assemble_task_id = taskcluster.slugId()
    build_tasks[assemble_task_id] = BUILDER.craft_assemble_task(variant)
    build_tasks[taskcluster.slugId()] = BUILDER.craft_test_task(variant)

    for craft_function in (
        BUILDER.craft_detekt_task,
        BUILDER.craft_ktlint_task,
        BUILDER.craft_lint_task,
        BUILDER.craft_compare_locales_task,
    ):
        other_tasks[taskcluster.slugId()] = craft_function()

    return (build_tasks, signing_tasks, other_tasks)


def push():
    all_tasks = pr()
    other_tasks = all_tasks[-1]
    other_tasks[taskcluster.slugId()] = BUILDER.craft_ui_tests_task()
    return all_tasks 


def raptor(is_staging):
    build_tasks = {}
    signing_tasks = {}
    other_tasks = {}

    mozharness_task_id = fetch_mozharness_task_id()
    gecko_revision = taskcluster.Queue().task(mozharness_task_id)['payload']['env']['GECKO_HEAD_REV']

    variant = get_variant('raptor')
    assemble_task_id = taskcluster.slugId()
    build_tasks[assemble_task_id] = BUILDER.craft_assemble_task(variant)
    signing_task_id = taskcluster.slugId()
    signing_tasks[signing_task_id] = BUILDER.craft_raptor_signing_task(
        assemble_task_id, variant, is_staging)

    all_raptor_craft_functions = [
        BUILDER.craft_raptor_tp6m_task(for_suite=i)
        for i in range(1, 11)
    ] + [
        BUILDER.craft_raptor_speedometer_task,
        BUILDER.craft_raptor_speedometer_power_task,
    ]

    for craft_function in all_raptor_craft_functions:
        args = (signing_task_id, mozharness_task_id, variant, gecko_revision)
        other_tasks[taskcluster.slugId()] = craft_function('armeabi-v7a', *args)
        other_tasks[taskcluster.slugId()] = craft_function('arm64-v8a', *args)
        other_tasks[taskcluster.slugId()] = craft_function('armeabi-v7a', *args, force_run_on_64_bit_device=True)

    return (build_tasks, signing_tasks, other_tasks)


def nightly(is_staging):
    build_tasks = {}
    signing_tasks = {}
    push_tasks = {}
    other_tasks = {}

    formatted_date = datetime.datetime.now().strftime('%y%V')
    version_name = '1.0.{}'.format(formatted_date)
    assemble_task_id = taskcluster.slugId()
    variant = get_variant('nightly')
    build_tasks[assemble_task_id] = BUILDER.craft_assemble_nightly_task(variant, version_name, is_staging)

    signing_task_id = taskcluster.slugId()
    signing_tasks[signing_task_id] = BUILDER.craft_nightly_signing_task(
        assemble_task_id, variant, is_staging=is_staging
    )

    push_task_id = taskcluster.slugId()
    push_tasks[push_task_id] = BUILDER.craft_push_task(signing_task_id, variant, is_staging)

    other_tasks[taskcluster.slugId()] = BUILDER.craft_upload_apk_nimbledroid_task(assemble_task_id)

    return (build_tasks, signing_tasks, push_tasks, other_tasks)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description='Creates and submit a graph of tasks on Taskcluster.'
    )

    subparsers = parser.add_subparsers(dest='command')

    subparsers.add_parser('pull-request')
    subparsers.add_parser('push')

    raptor_parser = subparsers.add_parser('raptor')
    raptor_parser.add_argument('--staging', action='store_true')

    nightly_parser = subparsers.add_parser('nightly')
    nightly_parser.add_argument('--staging', action='store_true')

    result = parser.parse_args()

    command = result.command

    if command in ('pull-request'):
        ordered_groups_of_tasks = pr()
    elif command in ('push'):
        ordered_groups_of_tasks = push()
    elif command == 'raptor':
        ordered_groups_of_tasks = raptor(result.staging)
    elif command == 'nightly':
        ordered_groups_of_tasks = nightly(result.staging)
    else:
        raise Exception('Unsupported command "{}"'.format(command))

    full_task_graph = schedule_task_graph(ordered_groups_of_tasks)

    populate_chain_of_trust_task_graph(full_task_graph)
    populate_chain_of_trust_required_but_unused_files()
