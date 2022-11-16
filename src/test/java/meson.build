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
            pom_file,
            '-P',
            'meson',
            'test',
            '-Dtest=@0@.@1@.@2@'.format(group_id.replace('-', '_'), artifact_id, t),
            '-Dmeson.build_root=@0@'.format(meson.project_build_root()),
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
