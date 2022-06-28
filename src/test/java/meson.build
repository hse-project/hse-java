tests = [
    'CursorTest',
    'HseTest',
    'KvdbTest',
    'KvsTest',
    'LimitsTest',
    'MclassTest',
    'TransactionTest',
    'VersionTest',
]

foreach t : tests
    test(
        t,
        mvn,
        args: [
            '-f',
            meson.project_source_root() / 'pom.xml',
            'test',
            '-Dtest=@0@.@1@'.format(package, t),
            '-Dmeson.build.dir=@0@'.format(meson.project_build_root()),
        ],
        suite: ['unit'],
        timeout: 120,
        is_parallel : false,
        depends: [hsejni],
        env: environment({
            'HSE_TEST_RUNNER_RUNNER': 'java',
        })
    )
endforeach
