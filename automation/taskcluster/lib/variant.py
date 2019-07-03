class VariantApk:
    def __init__(self, abi, file_name):
        self.abi = abi
        self._file_name = file_name
        self.taskcluster_path = 'public/target.{}.apk'.format(abi)

    def absolute_path(self, build_type):
        return '/build/reference-browser/app/build/outputs/apk/{build_type}/{file_name}'.format(
            build_type=build_type,
            file_name=self._file_name
        )


class Variant:
    def __init__(self, name, build_type, apks):
        self.name = name
        self.build_type = build_type
        self.apks = apks
        self.taskcluster_apk_paths = ['public/target.{}.apk'.format(apk.abi) for apk in apks]
