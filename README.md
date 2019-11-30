#  Reference Browser

[![Task Status](https://github.taskcluster.net/v1/repository/mozilla-mobile/reference-browser/master/badge.svg)](https://github.taskcluster.net/v1/repository/mozilla-mobile/reference-browser/master/latest)

A web browser reference implementation using [Mozilla Android Components](https://github.com/mozilla-mobile/android-components).

*The Reference Browser is not a product intended to ship to end users. Instead it is a Technology Preview for many new mobile components that multiple teams at Mozilla are currently working on*

It includes the Mozilla Web Platform via GeckoView, a new modern Firefox Accounts and Cloud Sync implementation and the new "Glean" telemetry library. All these components will be foundational for Mozilla's existing and upcoming Android products.

The Reference Browser can also be a starting point for your own new browser-like applications. It depends heavily on the [Android Components](https://github.com/mozilla-mobile/android-components) project where most of the actual implementation lives. That project also includes many smaller sample applications.

# Getting Involved

We encourage you to participate in this open source project. We love pull requests, bug reports, ideas, (security) code reviews or any kind of positive contribution.

Before you attempt to make a contribution please read the [Community Participation Guidelines](https://www.mozilla.org/en-US/about/governance/policies/participation/).

* [View current Issues](https://github.com/mozilla-mobile/reference-browser/issues) or [View current Pull Requests](https://github.com/mozilla-mobile/reference-browser/pulls).

* IRC: [#android-components (irc.mozilla.org)](https://wiki.mozilla.org/IRC) | [view logs](https://mozilla.logbot.info/android-components/)

* Subscribe to our mailing list [android-components@](https://lists.mozilla.org/listinfo/android-components) to keep up to date ([Archives](https://lists.mozilla.org/pipermail/android-components/)).


# Test Channel on Google Play Store

To get the Reference Browser on your device, follow these two steps:

1) Visit https://groups.google.com/forum/#!forum/mozilla-reference-browser and join the Google Group
2) Visit https://play.google.com/apps/testing/org.mozilla.reference.browser on your device to join the test program and to install the app

Make sure you use the same Google Account for both steps.

# Download Nightly Builds Directly

Signed Nightly builds can be downloaded from:

* [⬇️ ARM64/Aarch64 devices (64 bit; Android 5+)](https://firefox-ci-tc.services.mozilla.com/api/index/v1/task/project.mobile.reference-browser.v3.nightly.latest/artifacts/public/target.arm64-v8a.apk)
* [⬇️ ARM devices (32 bit; Android 5+)](https://firefox-ci-tc.services.mozilla.com/api/index/v1/task/project.mobile.reference-browser.v3.nightly.latest/artifacts/public/target.armeabi-v7a.apk)
* [⬇️ x86_64  devices (64 bit; Android 5+)](https://firefox-ci-tc.services.mozilla.com/api/index/v1/task/project.mobile.reference-browser.v3.nightly.latest/artifacts/public/target.x86_64.apk)
* [⬇️ x86  devices (32 bit; Android 5+)](https://firefox-ci-tc.services.mozilla.com/api/index/v1/task/project.mobile.reference-browser.v3.nightly.latest/artifacts/public/target.x86.apk)

> Please note that these builds do not auto-update, you will have to keep up to date manually.

The latest Nightly build task can be found [here](https://firefox-ci-tc.services.mozilla.com/tasks/index/project.mobile.reference-browser.v3.nightly/latest).

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
- [GeckoView](https://hg.mozilla.org/mozilla-central), specifying its path via `dependencySubstitutions.geckoviewTopsrcdir=/path/to/mozilla-central` (and, optionally, `dependencySubstitutions.geckoviewTopobjdir=/path/to/topobjdir`). See [Bug 1533465](https://bugzilla.mozilla.org/show_bug.cgi?id=1533465).
  - This assumes that you have built, packaged, and published your local GeckoView -- but don't worry, the dependency substitution script has the latest instructions for doing that.

Do not forget to run a Gradle sync in Android Studio after changing `local.properties`. If you specified any substitutions, they will be reflected in the modules list, and you'll be able to modify them from a single Android Studio window.

# License

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/
