ABIS = ('aarch64', 'arm', 'x86')


class Variant:
    def __init__(self, raw: str, flavor: str, engine: str, abi: str, build_type: str):
        self.raw = raw
        self.engine = engine
        self.abi = abi
        self.build_type = build_type
        self.for_gradle_command = raw[:1].upper() + raw[1:]
        self._signed_by_default = build_type == 'debug'
        self._flavor = flavor

    def platform(self):
        return 'android-{}-{}'.format(self.abi, self.build_type)

    def gradle_apk_path(self):
        return '{flavor}/{build_type}/app-{engine}-{abi}-{build_type}{unsigned}.apk'.format(
            flavor=self._flavor,
            build_type=self.build_type,
            engine=self.engine,
            abi=self.abi,
            unsigned='' if self._signed_by_default else '-unsigned',
        )

    @staticmethod
    def from_gradle_variant_string(raw_variant: str):
        # The variant string is composed of three pieces (engine, abi, and build type)
        # but it doesn't delimit them. So, we need to keep track of all the possible prefixes
        # (engines) and middle bits (abis), and solve for the build type. Awesome.

        if not raw_variant.startswith('geckoNightly'):
            raise ValueError('This variant ("{}") does not start with the only supported '
                             'engine of "geckoNightly"'.format(raw_variant))
        engine = 'geckoNightly'

        for supported_abi in ABIS:
            if raw_variant[len(engine):].startswith(supported_abi.capitalize()):
                abi = supported_abi
                break
        else:
            raise ValueError('This variant ("{}") does not match any of our supported '
                             'abis ({})'.format(raw_variant, ABIS))

        build_type = raw_variant[len(engine + abi):]
        # first letter was artificially uppercase'd for the full variant
        build_type = build_type[:1].lower() + build_type[1:]
        return Variant(raw_variant, engine + abi, engine, abi, build_type)
