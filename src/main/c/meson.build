c_sources = files(
    'com_micron_hse_Hse.c',
    'com_micron_hse_Kvdb.c',
    'com_micron_hse_Kvdb_CompactStatus.c',
    'com_micron_hse_KvdbTransaction.c',
    'com_micron_hse_Kvs.c',
    'com_micron_hse_KvsCursor.c',
    'com_micron_hse_MclassInfo.c',
    'com_micron_hse_Version.c',
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
    [c_sources, native_headers],
    c_args: c_args,
    include_directories: include_directories('.'),
    dependencies: [
        hse_dep,
        jni_dep,
    ],
    gnu_symbol_visibility: 'hidden',
    install: true
)
