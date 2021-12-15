java_sources = files(
    'com/micron/hse/Hse.java',
    'com/micron/hse/HseException.java',
    'com/micron/hse/Kvdb.java',
    'com/micron/hse/KvdbTransaction.java',
    'com/micron/hse/Kvs.java',
    'com/micron/hse/KvsCursor.java',
    'com/micron/hse/Limits.java',
    'com/micron/hse/Mclass.java',
    'com/micron/hse/MclassInfo.java',
    'com/micron/hse/NativeObject.java',
    'com/micron/hse/Version.java',
)

hse_jar = jar(
    fs.stem(jar_filename),
    java_sources,
    install: true,
    install_dir: get_option('datadir') / 'java'
)
