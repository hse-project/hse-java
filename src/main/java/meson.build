java_sources = files(
    'com/micron/hse_project/hse/Hse.java',
    'com/micron/hse_project/hse/HseException.java',
    'com/micron/hse_project/hse/Kvdb.java',
    'com/micron/hse_project/hse/Kvs.java',
    'com/micron/hse_project/hse/KvsCursor.java',
    'com/micron/hse_project/hse/Limits.java',
    'com/micron/hse_project/hse/Mclass.java',
    'com/micron/hse_project/hse/MclassInfo.java',
    'com/micron/hse_project/hse/NativeObject.java',
    'com/micron/hse_project/hse/Transaction.java',
    'com/micron/hse_project/hse/Version.java',
)

hse_jar = jar(
    fs.stem(jar_filename),
    java_sources,
    install: true
)
