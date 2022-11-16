preprocessed_group_id = group_id.replace('.', '_').replace('-', '_')

c_sources = files(
    '@0@_@1@_Hse.c'.format(preprocessed_group_id, artifact_id),
    '@0@_@1@_Kvdb.c'.format(preprocessed_group_id, artifact_id),
    '@0@_@1@_Kvdb_CompactStatus.c'.format(preprocessed_group_id, artifact_id),
    '@0@_@1@_KvdbTransaction.c'.format(preprocessed_group_id, artifact_id),
    '@0@_@1@_Kvs.c'.format(preprocessed_group_id, artifact_id),
    '@0@_@1@_KvsCursor.c'.format(preprocessed_group_id, artifact_id),
    '@0@_@1@_MclassInfo.c'.format(preprocessed_group_id, artifact_id),
    '@0@_@1@_Version.c'.format(preprocessed_group_id, artifact_id),
    'hsejni.c'
)

native_headers = javamod.generate_native_headers(
    java_sources,
    package: package,
    classes: [
        'Hse',
        'Kvdb',
        'Kvdb.CompactStatus',
        'KvdbTransaction',
        'Kvs',
        'KvsCursor',
        'MclassInfo',
        'Version',
    ]
)

c_args = []
if get_option('experimental')
    c_args += '-DHSE_JAVA_EXPERIMENTAL'
endif

hsejni = shared_module(
    'hsejni-@0@'.format(hse_java_major_version),
    c_sources,
    native_headers,
    c_args: c_args,
    include_directories: include_directories('.'),
    dependencies: [
        hse_dep,
        jni_dep,
    ],
    gnu_symbol_visibility: 'hidden',
    install: true
)
