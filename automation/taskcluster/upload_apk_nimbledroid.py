import argparse
import os

import requests


def main():
    parser = argparse.ArgumentParser(
        description='Uploads APK to nimbledroid'
    )

    parser.add_argument('apk', type=argparse.FileType('r'))

    apk = parser.parse_args().apk
    api_key = os.environ['API_KEY']

    print('SKIP: Uploads {} to nimbledroid'.format(apk))
    return

    response = requests.post(
        'https://nimbledroid.com/api/v2/apks',
        auth=(api_key, ''),
        headers={'Accept': '*/*'},
        files={'apk': apk},
        data={'auto_scenarios': 'false'}
    )

    if response.status_code != 201:
        raise RuntimeError('Nimbledroid responded with non-201 response: "{}"', response.json())

    print('Successfully uploaded to Nimbledroid', response.json())


if __name__ == '__main__':
    main()
