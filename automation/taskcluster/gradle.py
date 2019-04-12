import json
import subprocess

from variant import Variant


def load_variants():
    variants_json = _run_gradle_process('printBuildVariants', 'variants: ')
    variants = json.loads(variants_json)
    return [Variant.from_gradle_variant_string(raw_variant) for raw_variant in variants]


def load_geckoview_nightly_version():
    nightly_version = _run_gradle_process('printGeckoviewVersions', 'nightly: ')
    nightly_version = nightly_version.strip('"')
    return nightly_version


def _run_gradle_process(gradle_command, output_prefix):
    process = subprocess.Popen(["./gradlew", "--no-daemon", "--quiet", gradle_command],
                               stdout=subprocess.PIPE)
    output, err = process.communicate()
    exit_code = process.wait()

    if exit_code is not 0:
        print("Gradle command returned error: {}".format(exit_code))

    output = output.decode('utf-8')
    output_line = [line for line in output.split('\n') if line.startswith(output_prefix)][0]
    return output_line.split(' ', 1)[1]
