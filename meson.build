# SPDX-License-Identifier: Apache-2.0 OR MIT
#
# SPDX-FileCopyrightText: Copyright 2021 Micron Technology, Inc.

project(
    'hse-java',
    'c',
    'java',
    version: run_command(
        'mvn',
        '-f',
        meson.project_source_root() / 'pom.xml',
        'help:evaluate',
        '-Dexpression=project.version',
        '--quiet',
        '-DforceStdout',
        check: true
    ).stdout().strip(),
    license: 'Apache-2.0 OR MIT',
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

version_components = meson.project_version().split('.')

hse_java_major_version = version_components[0].to_int()
hse_java_minor_version = version_components[1].to_int()
hse_java_patch_version = version_components[2].to_int()

fs = import('fs')
javamod = import('java')

cc = meson.get_compiler('c')
javac = meson.get_compiler('java')

mvn = find_program('mvn')
sh = find_program('sh', required: get_option('tests'))
python = find_program('python3', required: get_option('tests'))

pom_file = meson.project_source_root() / 'pom.xml'
group_id = run_command(
    'mvn',
    '-f',
    pom_file,
    'help:evaluate',
    '-Dexpression=project.groupId',
    '--quiet',
    '-DforceStdout',
    check: true
).stdout().strip()
artifact_id = run_command(
    'mvn',
    '-f',
    pom_file,
    'help:evaluate',
    '-Dexpression=project.artifactId',
    '--quiet',
    '-DforceStdout',
    check: true
).stdout().strip()
package = '@0@.@1@'.format(group_id, artifact_id)
jar_filename = '@0@-@1@.jar'.format(artifact_id, meson.project_version())

jni_dep = dependency('jni', version: '>=1.8.0')
hse_dep = dependency(
    'hse-@0@'.format(hse_java_major_version),
    version: [
        '>=@0@.0.0'.format(hse_java_major_version),
        '<@0@.0.0'.format(hse_java_major_version + 1),
    ],
    default_options: [
        'default_library=static',
        'warning_level=0',
        'werror=false',
        'cli=false',
        'docs=disabled',
        'tests=false',
        'tools=disabled',
        'samples=false',
        'bindings=none',
    ]
)

add_project_arguments(cc.get_supported_arguments(
        '-Wdeclaration-after-statement'
    ),
    language: 'c'
)

subdir('src')
if get_option('docs')
    subdir('docs')
endif

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
    '-f',
    pom_file,
    'deploy:deploy-file',
    '-Dmaven.test.skip=true',
    '-Durl=' + repo,
    '-DpomFile=' + pom_file,
    '-Dfile=' + hse_jar.full_path()
)

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
        pom_file,
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
