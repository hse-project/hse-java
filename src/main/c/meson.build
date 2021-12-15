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

java_classes = [
    'Hse',
    'Kvdb',
    'Kvdb_CompactStatus',
    'KvdbTransaction',
    'Kvs',
    'KvsCursor',
    'MclassInfo',
    'Version',
]

native_headers_file_names = []

foreach jc : java_classes
    native_headers_file_names += '@0@_@1@.h'.format(
        package.replace('.', '_'),
        jc.replace('.', '_')
    )
endforeach

native_headers = custom_target(
    'native-headers',
    input: java_sources,
    output: native_headers_file_names,
    command: [
        javac.cmd_array(),
        '-d',
        # Java 1.8.0 won't create the directory for us
        javac.version().version_compare('> 1.8.0') ? '@PRIVATE_DIR@' : meson.current_build_dir(),
        '-h',
        meson.current_build_dir(),
        java_sources,
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
        jdk_dep,
    ],
    gnu_symbol_visibility: 'hidden',
    install: true
)
