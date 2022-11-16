preprocessed_group_id = group_id.replace('.', '/').replace('-', '_')

java_sources = files(
    '@0@/@1@/Hse.java'.format(preprocessed_group_id, artifact_id),
    '@0@/@1@/HseException.java'.format(preprocessed_group_id, artifact_id),
    '@0@/@1@/Kvdb.java'.format(preprocessed_group_id, artifact_id),
    '@0@/@1@/KvdbTransaction.java'.format(preprocessed_group_id, artifact_id),
    '@0@/@1@/Kvs.java'.format(preprocessed_group_id, artifact_id),
    '@0@/@1@/KvsCursor.java'.format(preprocessed_group_id, artifact_id),
    '@0@/@1@/Limits.java'.format(preprocessed_group_id, artifact_id),
    '@0@/@1@/Mclass.java'.format(preprocessed_group_id, artifact_id),
    '@0@/@1@/MclassInfo.java'.format(preprocessed_group_id, artifact_id),
    '@0@/@1@/NativeObject.java'.format(preprocessed_group_id, artifact_id),
    '@0@/@1@/Version.java'.format(preprocessed_group_id, artifact_id),
)

hse_jar = custom_target(
    'jar',
    build_by_default: true,
    input: [
        java_sources,
        pom_file,
    ],
    output: [
        jar_filename,
    ],
    command: [
        'mvn',
        '-f',
        pom_file,
        '-P',
        'meson',
        'package',
        '--quiet',
        '-DforceStdout',
        '-Dmaven.test.skip=true',
        '-Dmeson.build_root=@0@'.format(meson.project_build_root()),
        '-Dmeson.current_build_dir=@0@'.format(meson.current_build_dir()),
    ],
    install: true,
    install_dir: get_option('datadir') / 'java'
)
