# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

"""Check the packaged Gradle dependency cache against the inventory that
`--write-verification-metadata` leaves behind, so that an incomplete artifact
fails here rather than in a downstream task (bug 1953671).

Some of what the inventory lists comes from repositories that aren't proxied
through Nexus and so is expected to be absent from the packaged tree; --unproxied
says which paths those are. Anything else absent, or any component packaged with
one of its files missing, is a real fault.
"""

import argparse
import hashlib
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def parse_inventory(path):
    """Yield (relative_directory, {filename: sha256}) for each component."""
    root = ET.parse(path).getroot()
    for component in root.iter():
        if not component.tag.endswith("component"):
            continue
        group = component.get("group")
        name = component.get("name")
        version = component.get("version")
        if not (group and name and version):
            continue

        artifacts = {}
        for artifact in component:
            if not artifact.tag.endswith("artifact"):
                continue
            checksum = next(
                (
                    child.get("value")
                    for child in artifact
                    if child.tag.endswith("sha256")
                ),
                None,
            )
            artifacts[artifact.get("name")] = checksum

        if artifacts:
            yield Path(*group.split("."), name, version), artifacts


def sha256(path):
    digest = hashlib.sha256()
    with path.open("rb") as fh:
        for block in iter(lambda: fh.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def verify(inventory, repositories, unproxied):
    problems = []
    unpackaged = []
    components = checked = artifacts = 0

    for relative_dir, expected in inventory:
        components += 1

        # Downstream tasks are given every one of these as a repository, so an
        # artifact only has to be in one of them.
        directories = [
            repository / relative_dir
            for repository in repositories
            if (repository / relative_dir).is_dir()
        ]
        if not directories:
            if str(relative_dir).startswith(unproxied):
                unpackaged.append(str(relative_dir))
            else:
                problems.append(f"absent: {relative_dir}")
            continue

        checked += 1
        checksums = None
        for filename, checksum in sorted(expected.items()):
            found = next(
                (d / filename for d in directories if (d / filename).is_file()), None
            )
            if found:
                if checksum and sha256(found) != checksum:
                    problems.append(f"corrupt: {found}")
                else:
                    artifacts += 1
                continue

            # The inventory records the file name from the module metadata,
            # which for Kotlin Multiplatform is not the name the repository
            # publishes it under. Fall back to matching the contents.
            if checksums is None:
                checksums = {
                    sha256(path)
                    for directory in directories
                    for path in directory.iterdir()
                    if path.is_file()
                }
            if checksum and checksum in checksums:
                artifacts += 1
            else:
                problems.append(f"missing: {relative_dir}/{filename}")

    return components, checked, artifacts, unpackaged, problems


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--inventory",
        required=True,
        type=Path,
        help="verification-metadata.dryrun.xml written by the enumeration pass",
    )
    parser.add_argument(
        "--unproxied",
        action="append",
        default=[],
        metavar="PREFIX",
        help="path prefix served by a repository that isn't proxied through "
        "Nexus, so is expected to be absent from the packaged tree; repeatable",
    )
    parser.add_argument(
        "repository",
        nargs="+",
        type=Path,
        help="packaged repository directories to check, e.g. central google",
    )
    args = parser.parse_args()

    if not args.inventory.is_file():
        print(
            f"FATAL ERROR: no inventory at {args.inventory}. Did the "
            "enumeration pass run?"
        )
        return 1

    components, checked, artifacts, unpackaged, problems = verify(
        parse_inventory(args.inventory), args.repository, tuple(args.unproxied)
    )
    print(
        f"verified {artifacts} artifacts across {checked} of {components} "
        f"components; {len(unpackaged)} are not packaged, coming from a "
        "repository we don't proxy"
    )
    for path in unpackaged:
        print(f"  not packaged: {path}")

    if not components:
        print(f"FATAL ERROR: {args.inventory} lists no components at all.")
        return 1

    if problems:
        print(f"FATAL ERROR: {len(problems)} absent, missing or corrupt:")
        for problem in problems:
            print(f"  {problem}")
        print("The dependency cache is incomplete. Try re-running this task.")
        return 1

    return 0


if __name__ == "__main__":
    sys.exit(main())
