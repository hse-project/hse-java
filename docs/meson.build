javadoc = find_program('javadoc', required: get_option('docs'), disabler: true)

javadocs = custom_target(
    'javadoc',
    build_by_default: true,
    input: java_sources,
    output: 'output',
    command: [
        javadoc,
        '-d',
        meson.current_build_dir() / 'output',
        '-classpath',
        meson.project_source_root() / 'src/main/java',
        '-windowtitle',
        'HSE Java Bindings',
        '-link',
        'https://docs.oracle.com/javase/8/docs/api/',
        '-source',
        '8',
        '-quiet',
        '-encoding',
        'utf-8',
        '-charset',
        'utf-8',
        '-docencoding',
        'utf-8',
        '-keywords',
        javac.version().version_compare('>1.8') ? [
            '--show-types',
            'public',
            '--show-members',
            'public',
            '--show-packages',
            'exported',
            '--show-module-contents',
            'api',
        ] : [],
        package,
        '@INPUT@',
    ],
    depend_files: files(
        meson.project_source_root() / 'src/main/java/com/micron/hse/package-info.java'
    )
)

if python.found()
    run_target(
        'javadocs-serve',
        command: [
            find_program('sh', required: false, disabler: true),
            '-c',
            ' '.join([
                python.full_path(),
                '-m',
                'http.server',
                '--directory',
                meson.current_build_dir() / 'output',
                '"${HSE_JAVA_JAVADOC_SERVE_PORT:-0}"',
            ])
        ],
        depends: javadocs
    )
endif
