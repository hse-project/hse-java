/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

#include <assert.h>

#include <hse/hse.h>
#include <jni.h>

#ifdef HSE_JAVA_EXPERIMENTAL
#include <hse/experimental.h>
#endif

#include "io_github_hse_project_hse_Kvdb.h"
#include "hsejni.h"

void
Java_io_github_hse_1project_hse_Kvdb_addStorage(
    JNIEnv *env,
    jclass kvdb_cls,
    jstring kvdb_home,
    jobjectArray params)
{
    jsize paramc;
    hse_err_t err;
    const char **paramv;
    const char *kvdb_home_chars = NULL;

    (void)kvdb_cls;

    if (kvdb_home)
        kvdb_home_chars = (*env)->GetStringUTFChars(env, kvdb_home, NULL);

    to_paramv(env, params, &paramc, &paramv);
    if ((*env)->ExceptionCheck(env))
        return;

    err = hse_kvdb_storage_add(kvdb_home_chars, paramc, paramv);

    (*env)->ReleaseStringUTFChars(env, kvdb_home, kvdb_home_chars);
    free_paramv(env, params, paramc, paramv);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvdb_create(
    JNIEnv *env,
    jclass kvdb_cls,
    jstring kvdb_home,
    jobjectArray params)
{
    jsize paramc;
    hse_err_t err;
    const char **paramv;
    const char *kvdb_home_chars = NULL;

    (void)kvdb_cls;

    if (kvdb_home)
        kvdb_home_chars = (*env)->GetStringUTFChars(env, kvdb_home, NULL);

    to_paramv(env, params, &paramc, &paramv);
    if ((*env)->ExceptionCheck(env))
        return;

    err = hse_kvdb_create(kvdb_home_chars, paramc, paramv);

    (*env)->ReleaseStringUTFChars(env, kvdb_home, kvdb_home_chars);
    free_paramv(env, params, paramc, paramv);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvdb_drop(JNIEnv *env, jclass kvdb_cls, jstring kvdb_home)
{
    hse_err_t err;
    const char *kvdb_home_chars = NULL;

    (void)kvdb_cls;

    if (kvdb_home)
        kvdb_home_chars = (*env)->GetStringUTFChars(env, kvdb_home, NULL);

    err = hse_kvdb_drop(kvdb_home_chars);

    (*env)->ReleaseStringUTFChars(env, kvdb_home, kvdb_home_chars);

    if (err)
        throw_new_hse_exception(env, err);
}

jlong
Java_io_github_hse_1project_hse_Kvdb_open(
    JNIEnv *env,
    jclass kvdb_cls,
    jstring kvdb_home,
    jobjectArray params)
{
    hse_err_t err;
    jsize paramc;
    const char **paramv;
    struct hse_kvdb *kvdb;
    const char *kvdb_home_chars = NULL;

    (void)kvdb_cls;

    if (kvdb_home)
        kvdb_home_chars = (*env)->GetStringUTFChars(env, kvdb_home, NULL);

    to_paramv(env, params, &paramc, &paramv);
    if ((*env)->ExceptionCheck(env))
        return 0;

    err = hse_kvdb_open(kvdb_home_chars, paramc, paramv, &kvdb);

    (*env)->ReleaseStringUTFChars(env, kvdb_home, kvdb_home_chars);
    free_paramv(env, params, paramc, paramv);

    if (err)
        throw_new_hse_exception(env, err);

    return (jlong)kvdb;
}

void
Java_io_github_hse_1project_hse_Kvdb_close(JNIEnv *env, jobject kvdb_obj, jlong kvdb_handle)
{
    hse_err_t err;
    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    (void)kvdb_obj;

    err = hse_kvdb_close(kvdb);
    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvdb_compact(
    JNIEnv *env,
    jobject kvdb_obj,
    jlong kvdb_handle,
    jint flags)
{
#ifdef HSE_JAVA_EXPERIMENTAL
    hse_err_t err;
    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    (void)kvdb_obj;

    err = hse_kvdb_compact(kvdb, flags);
    if (err)
        throw_new_hse_exception(env, err);
#else
    (void)kvdb_obj;
    (void)flags;

    (*env)->ThrowNew(
        env,
        globals.java.lang.UnsupportedOperationException.class,
        "Experimental support is not enabled");
#endif
}

jstring
Java_io_github_hse_1project_hse_Kvdb_getHome(JNIEnv *env, jobject kvdb_obj, jlong kvdb_handle)
{
    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    (void)kvdb_obj;

    return (*env)->NewStringUTF(env, hse_kvdb_home_get(kvdb));
}

jarray
Java_io_github_hse_1project_hse_Kvdb_getKvsNames(JNIEnv *env, jobject kvdb_obj, jlong kvdb_handle)
{
    char **namev;
    size_t namec;
    jarray names;
    hse_err_t err;
    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    (void)kvdb_obj;

    err = hse_kvdb_kvs_names_get(kvdb, &namec, &namev);
    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    names = (*env)->NewObjectArray(env, namec, globals.java.lang.String.class, NULL);
    for (unsigned int i = 0; i < namec; i++) {
        jstring name = (*env)->NewStringUTF(env, namev[i]);
        if ((*env)->ExceptionCheck(env))
            return NULL;

        (*env)->SetObjectArrayElement(env, names, i, name);
        if ((*env)->ExceptionCheck(env))
            return NULL;
    }

    hse_kvdb_kvs_names_free(kvdb, namev);

    return names;
}

jstring
Java_io_github_hse_1project_hse_Kvdb_getParam(
    JNIEnv *env,
    jobject kvdb_obj,
    jlong kvdb_handle,
    jstring param)
{
    hse_err_t err;
    char *buf = NULL;
    size_t needed_sz;
    jstring value = NULL;
    const char *param_chars = NULL;
    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    (void)env;
    (void)kvdb_obj;

    if (param)
        param_chars = (*env)->GetStringUTFChars(env, param, NULL);

    err = hse_kvdb_param_get(kvdb, param_chars, NULL, 0, &needed_sz);
    if (err) {
        throw_new_hse_exception(env, err);
        goto out;
    }

    buf = malloc((needed_sz + 1) * sizeof(*buf));
    if (!buf) {
        (*env)->ThrowNew(
            env,
            globals.java.lang.OutOfMemoryError.class,
            "Failed to allocate memory for parameter buffer");
        goto out;
    }

    err = hse_param_get(param_chars, buf, needed_sz + 1, NULL);
    if (err) {
        throw_new_hse_exception(env, err);
        goto out;
    }

out:
    (*env)->ReleaseStringUTFChars(env, param, param_chars);

    // Don't allocate a new string if an exception has been thrown.
    if (!(*env)->ExceptionCheck(env))
        value = (*env)->NewStringUTF(env, buf);

    free(buf);

    return value;
}

jboolean
Java_io_github_hse_1project_hse_Kvdb_isMclassConfigured(
    JNIEnv *env,
    jobject kvdb_obj,
    jlong kvdb_handle,
    jint mclass)
{
    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    (void)env;
    (void)kvdb_obj;

    return hse_kvdb_mclass_is_configured(kvdb, mclass);
}

void
Java_io_github_hse_1project_hse_Kvdb_sync(
    JNIEnv *env,
    jobject kvdb_obj,
    jlong kvdb_handle,
    jint flags)
{
    hse_err_t err;
    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    (void)kvdb_obj;

    err = hse_kvdb_sync(kvdb, flags);
    if (err)
        throw_new_hse_exception(env, err);
}
