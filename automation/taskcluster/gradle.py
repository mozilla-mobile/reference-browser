import json
import subprocess

from variant import Variant


def load_variants(build_type: str):
    variants_json = _run_gradle_process('printBuildVariants', 'variants: ')
    variants = json.loads(variants_json)
    return [Variant(variant_dict['name'], variant_dict['abi'], variant_dict['isSigned'], variant_dict['buildType'])
            for variant_dict in variants
            if variant_dict['buildType'] == build_type]


def load_geckoview_nightly_version():
    nightly_version = _run_gradle_process('printGeckoviewVersions', 'nightly: ')
    nightly_version = nightly_version.strip('"')
    return nightly_version


def _run_gradle_process(gradle_command, output_prefix):
    process = subprocess.Popen(["./gradlew", "--quiet", gradle_command],
                               stdout=subprocess.PIPE)
    print('Running gradle command: "{}"...'.format(gradle_command))
    output, err = process.communicate()
    exit_code = process.wait()

    if exit_code is not 0:
        print("Gradle command returned error: {}".format(exit_code))

    output = output.decode('utf-8')
    output_line = [line for line in output.split('\n') if line.startswith(output_prefix)][0]
    return output_line.split(' ', 1)[1]
