project(
    'hse-java',
    ['c', 'java'],
    version: files('VERSION'),
    license: 'Apache-2.0',
    default_options: [
        'prefix=/opt/hse',
        'b_ndebug=if-release',
        'c_std=gnu11',
        'buildtype=debugoptimized',
        'warning_level=3',
        # Meson for whatever reason chooses not to respect HSE's default
        # options :).
        'force_fallback_for=lz4,cjson',
    ],
    meson_version: '>= 0.62.0'
)

package = 'com.micron.hse'

version_components = meson.project_version().split('.')

hse_java_major_version = version_components[0]
hse_java_minor_version = version_components[1]
hse_java_patch_version = version_components[2]

jar_filename = 'hse-@0@.jar'.format(meson.project_version())

fs = import('fs')
javamod = import('java')

cc = meson.get_compiler('c')
javac = meson.get_compiler('java')

mvn = find_program('mvn', required: get_option('tests'), disabler: true)

python = find_program('python3', required: get_option('tests'))

jni_dep = dependency('jni', version: '>=1.8.0')
hse_dep = dependency(
    'hse-3',
    version: ['>=3.0.0', '<4.0.0'],
    default_options: [
        'warning_level=0',
        'default_library=static',
        'cli=false',
        'docs=disabled',
        'tests=false',
        'tools=disabled',
        'samples=false',
        'bindings=none',
        'db_bench=false',
    ]
)

subdir('src')
subdir('docs')

if mvn.found()
    if get_option('repo') == ''
        repo = 'file://' + run_command(
            mvn,
            'help:evaluate',
            '-Dexpression=settings.localRepository',
            '--quiet',
            '-DforceStdout',
            check: true
        ).stdout().strip()
    else
        repo = get_option('repo')
    endif

    meson.add_install_script(
            mvn,
            'deploy:deploy-file',
            '-Durl=' + repo,
            '-Dmaven.test.skip=true',
            '-DpomFile=' + meson.project_source_root() / 'pom.xml',
            '-Dfile=' + hse_jar.full_path()
    )
endif

run_target(
    'checkoss',
    command: [
        find_program('scripts/dev/checkoss.sh'),
        '@BUILD_ROOT@',
        '@SOURCE_ROOT@',
    ]
)

run_target(
    'jshell',
    command: [
        find_program('jshell', required: false, disabler: true),
    ],
    depends: [
        hsejni,
        hse_jar,
    ],
    env: environment({
        'LD_LIBRARY_PATH': fs.parent(hsejni.full_path()),
        'CLASSPATH': hse_jar.full_path(),
    })
)

run_target(
    'checkstyle',
    command: [
        mvn,
        '-f',
        meson.project_source_root() / 'pom.xml',
        'checkstyle:check'
    ]
)

shellcheck = find_program('shellcheck', required: false)
if shellcheck.found()
    run_target(
        'shellcheck',
        command: [
            find_program('scripts/dev/shellcheck.sh'),
        ]
    )
endif
