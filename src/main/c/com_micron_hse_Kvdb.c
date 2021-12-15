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

#include "hsejni.h"
#include "com_micron_hse_Kvdb.h"

void
Java_com_micron_hse_Kvdb_addStorage(
    JNIEnv      *env,
    jclass       kvdb_cls,
    jstring      kvdb_home,
    jobjectArray params)
{
    (void)kvdb_cls;

    const char *kvdb_home_chars = (*env)->GetStringUTFChars(env, kvdb_home, NULL);

    jsize        paramc;
    const char **paramv;
    to_paramv(env, params, &paramc, &paramv);
    if ((*env)->ExceptionCheck(env))
        return;

    const hse_err_t err = hse_kvdb_storage_add(kvdb_home_chars, paramc, paramv);

    (*env)->ReleaseStringUTFChars(env, kvdb_home, kvdb_home_chars);
    free_paramv(env, params, paramc, paramv);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_com_micron_hse_Kvdb_create(
    JNIEnv      *env,
    jclass       kvdb_cls,
    jstring      kvdb_home,
    jobjectArray params)
{
    (void)kvdb_cls;

    const char *kvdb_home_chars = (*env)->GetStringUTFChars(env, kvdb_home, NULL);

    jsize        paramc;
    const char **paramv;
    to_paramv(env, params, &paramc, &paramv);
    if ((*env)->ExceptionCheck(env))
        return;

    const hse_err_t err = hse_kvdb_create(kvdb_home_chars, paramc, paramv);

    (*env)->ReleaseStringUTFChars(env, kvdb_home, kvdb_home_chars);
    free_paramv(env, params, paramc, paramv);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_com_micron_hse_Kvdb_drop(JNIEnv *env, jclass kvdb_cls, jstring kvdb_home)
{
    (void)kvdb_cls;

    const char *kvdb_home_chars = (*env)->GetStringUTFChars(env, kvdb_home, NULL);

    const hse_err_t err = hse_kvdb_drop(kvdb_home_chars);

    (*env)->ReleaseStringUTFChars(env, kvdb_home, kvdb_home_chars);

    if (err)
        throw_new_hse_exception(env, err);
}

jlong
Java_com_micron_hse_Kvdb_open(JNIEnv *env, jclass kvdb_cls, jstring kvdb_home, jobjectArray params)
{
    (void)kvdb_cls;

    const char *kvdb_home_chars = (*env)->GetStringUTFChars(env, kvdb_home, NULL);

    jsize        paramc;
    const char **paramv;
    to_paramv(env, params, &paramc, &paramv);
    if ((*env)->ExceptionCheck(env))
        return 0;

    struct hse_kvdb *kvdb = NULL;
    const hse_err_t  err = hse_kvdb_open(kvdb_home_chars, paramc, paramv, &kvdb);

    (*env)->ReleaseStringUTFChars(env, kvdb_home, kvdb_home_chars);
    free_paramv(env, params, paramc, paramv);

    if (err)
        throw_new_hse_exception(env, err);

    return (jlong)kvdb;
}

void
Java_com_micron_hse_Kvdb_close(JNIEnv *env, jobject kvdb_obj, jlong kvdb_handle)
{
    (void)kvdb_obj;

    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    const hse_err_t err = hse_kvdb_close(kvdb);
    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_com_micron_hse_Kvdb_compact(JNIEnv *env, jobject kvdb_obj, jlong kvdb_handle, jint flags)
{
    (void)kvdb_obj;

#ifdef HSE_JAVA_EXPERIMENTAL
    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    const hse_err_t err = hse_kvdb_compact(kvdb, flags);
    if (err)
        throw_new_hse_exception(env, err);
#else
    (void)flags;

    (*env)->ThrowNew(
        env,
        globals.java.lang.UnsupportedOperationException.class,
        "Experimental support is not enabled");
#endif
}

jstring
Java_com_micron_hse_Kvdb_getHome(JNIEnv *env, jobject kvdb_obj, jlong kvdb_handle)
{
    (void)kvdb_obj;

    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    return (*env)->NewStringUTF(env, hse_kvdb_home_get(kvdb));
}

jarray
Java_com_micron_hse_Kvdb_getKvsNames(JNIEnv *env, jobject kvdb_obj, jlong kvdb_handle)
{
    (void)kvdb_obj;

    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    char          **namev;
    size_t          namec;
    const hse_err_t err = hse_kvdb_kvs_names_get(kvdb, &namec, &namev);
    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    jarray names = (*env)->NewObjectArray(env, namec, globals.java.lang.String.class, NULL);
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
Java_com_micron_hse_Kvdb_getParam(JNIEnv *env, jobject kvdb_obj, jlong kvdb_handle, jstring param)
{
    (void)env;
    (void)kvdb_obj;

    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    hse_err_t err = 0;
    jstring   value = NULL;
    char     *buf = NULL;

    const char *param_chars = (*env)->GetStringUTFChars(env, param, NULL);

    size_t needed_sz;
    err = hse_kvdb_param_get(kvdb, param_chars, NULL, 0, &needed_sz);
    if (err) {
        throw_new_hse_exception(env, err);
        goto out;
    }

    buf = malloc(needed_sz + 1);
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
Java_com_micron_hse_Kvdb_isMclassConfigured(
    JNIEnv *env,
    jobject kvdb_obj,
    jlong   kvdb_handle,
    jint    mclass)
{
    (void)env;
    (void)kvdb_obj;

    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    return hse_kvdb_mclass_is_configured(kvdb, mclass);
}

void
Java_com_micron_hse_Kvdb_sync(JNIEnv *env, jobject kvdb_obj, jlong kvdb_handle, jint flags)
{
    (void)kvdb_obj;

    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    const hse_err_t err = hse_kvdb_sync(kvdb, flags);
    if (err)
        throw_new_hse_exception(env, err);
}
