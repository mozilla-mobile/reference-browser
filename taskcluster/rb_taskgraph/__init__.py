# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

from __future__ import absolute_import, print_function, unicode_literals

import os

from importlib import import_module
from six import text_type
from taskgraph.parameters import extend_parameters_schema
from voluptuous import Required


def register(graph_config):
    """
    Import all modules that are siblings of this one, triggering decorators in
    the process.
    """
    _import_modules(["job", "worker_types", "routes", "target_tasks"])


def _import_modules(modules):
    for module in modules:
        import_module(".{}".format(module), package=__name__)
