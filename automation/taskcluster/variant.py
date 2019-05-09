class Variant:
    def __init__(self, raw: str, abi: str, is_signed: bool, build_type: str):
        self.raw = raw
        self.abi = abi
        self.build_type = build_type
        self._is_signed = is_signed
        self.for_gradle_command = raw[:1].upper() + raw[1:]
        self.platform = 'android-{}-{}'.format(self.abi, self.build_type)

    def gradle_apk_path(self):
        return '{abi}/{build_type}/app-{abi}-{build_type}{unsigned}.apk'.format(
            build_type=self.build_type,
            abi=self.abi,
            unsigned='' if self._is_signed else '-unsigned',
        )

    @staticmethod
    def from_values(abi: str, is_signed: bool, build_type: str):
        raw = abi + build_type[:1].upper() + build_type[1:]
        return Variant(raw, abi, is_signed, build_type)
