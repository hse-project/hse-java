javadocs = custom_target(
    'javadocs',
    build_by_default: true,
    input: [
        java_sources,
        pom_file,
    ],
    output: [
        'apidocs',
    ],
    command: [
        mvn,
        '-f',
        pom_file,
        '-P',
        'meson',
        'javadoc:javadoc-no-fork',
        '--quiet',
        '-DforceStdout',
        '-Dmeson.build_root=@0@'.format(meson.project_build_root()),
        '-Dmeson.current_build_dir=@0@'.format(meson.current_build_dir()),
    ],
    install: true,
    install_dir: get_option('datadir') / 'javadoc' / 'hse-@0@'.format(hse_java_major_version)
)

if python.found()
    run_target(
        'javadocs-serve',
        command: [
            sh,
            '-c',
            ' '.join([
                python.full_path(),
                '-m',
                'http.server',
                '--directory',
                meson.current_build_dir() / 'apidocs',
                '"${HSE_JAVA_JAVADOC_SERVE_PORT:-0}"',
            ])
        ],
        depends: javadocs
    )
endif
