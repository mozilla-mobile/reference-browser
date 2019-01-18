#  Reference Browser

[![Task Status](https://github.taskcluster.net/v1/repository/mozilla-mobile/reference-browser/master/badge.svg)](https://github.taskcluster.net/v1/repository/mozilla-mobile/reference-browser/master/latest)

A full-featured browser reference implementation using [Mozilla Android Components](https://github.com/mozilla-mobile/android-components).

# Download Nightly builds

Signed Nightly builds can be downloaded from:

* [⬇️ ARM64/Aarch64 devices (64 bit; Android 5+)](https://index.taskcluster.net/v1/task/project.mobile.reference-browser.signed-nightly.nightly.latest/artifacts/public/app-geckoNightly-aarch64-release-unsigned.apk)
* [⬇️ ARM devices (32 bit; Android 5+)](https://index.taskcluster.net/v1/task/project.mobile.reference-browser.signed-nightly.nightly.latest/artifacts/public/app-geckoNightly-arm-release-unsigned.apk)
* [⬇️ x86  devices (32 bit; Android 5+)](https://index.taskcluster.net/v1/task/project.mobile.reference-browser.signed-nightly.nightly.latest/artifacts/public/app-geckoNightly-x86-release-unsigned.apk)

Note that all builds are signed with a non-production / throw-away key. The latest Nightly build task can be found [here](https://tools.taskcluster.net/index/project.mobile.reference-browser.signed-nightly.nightly/latest).

# Getting Involved

We encourage you to participate in this open source project. We love pull requests, bug reports, ideas, (security) code reviews or any kind of positive contribution.

Before you attempt to make a contribution please read the [Community Participation Guidelines](https://www.mozilla.org/en-US/about/governance/policies/participation/).

* [View current Issues](https://github.com/mozilla-mobile/reference-browser/issues) or [View current Pull Requests](https://github.com/mozilla-mobile/reference-browser/pulls).

* [List of good first issues](https://github.com/mozilla-mobile/reference-browser/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) (**New contributors start here!**) and [List of "help wanted" issues](https://github.com/mozilla-mobile/reference-browser/issues?q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22).

* IRC: [#android-components (irc.mozilla.org)](https://wiki.mozilla.org/IRC) | [view logs](https://mozilla.logbot.info/android-components/)

* Subscribe to our mailing list [android-components@](https://lists.mozilla.org/listinfo/android-components) to keep up to date ([Archives](https://lists.mozilla.org/pipermail/android-components/)).

# Local Development

## Dependency substitutions

You might be interested in building this project against local versions of some of the dependencies.
This could be done either by using a [local maven repository](https://mozilla-mobile.github.io/android-components/contributing/testing-components-inside-app) (quite cumbersome), or via Gradle's [dependency substitutions](https://docs.gradle.org/current/userguide/customizing_dependency_resolution_behavior.html) (not at all cumbersome!).

Currently, the substitution flow is streamlined for some of the core dependencies via configuration flags in `local.properties`. You can build against a local checkout of the following dependencies by specifying their local paths:
- [application-services](https://github.com/mozilla/application-services), specifying its path via `substitutions.application-services.dir=../application-services`
  - This assumes that you have an `application-services` project at the same level in the directory hierarchy as the `reference-browser`.

Do not forget to run a Gradle sync in Android Studio after changing `local.properties`. If you specified any substitutions, they will be reflected in the modules list, and you'll be able to modify them from a single Android Studio window.

# License

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/
