/* SPDX-License-Identifier: Apache-2.0 OR MIT
 *
 * SPDX-FileCopyrightText: Copyright 2021 Micron Technology, Inc.
 */

#include <assert.h>
#include <stdbool.h>
#include <stdint.h>

#include <hse/hse.h>
#include <jni.h>

#include "io_github_hse_project_hse_Kvs.h"
#include "hsejni.h"

void
Java_io_github_hse_1project_hse_Kvs_create(
    JNIEnv *env,
    jclass kvs_cls,
    jlong kvdb_handle,
    jstring kvs_name,
    jobjectArray params)
{
    jsize paramc;
    hse_err_t err;
    const char **paramv;
    const char *kvs_name_chars = NULL;
    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    (void)kvs_cls;

    if (kvs_name)
        kvs_name_chars = (*env)->GetStringUTFChars(env, kvs_name, NULL);

    to_paramv(env, params, &paramc, &paramv);
    if ((*env)->ExceptionCheck(env))
        return;

    err = hse_kvdb_kvs_create(kvdb, kvs_name_chars, paramc, paramv);

    if (kvs_name)
        (*env)->ReleaseStringUTFChars(env, kvs_name, kvs_name_chars);

    free_paramv(env, params, paramc, paramv);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvs_drop(
    JNIEnv *env,
    jclass kvs_cls,
    jlong kvdb_handle,
    jstring kvs_name)
{
    hse_err_t err;
    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;
    const char *kvs_name_chars = NULL;

    (void)kvs_cls;

    if (kvs_name)
        kvs_name_chars = (*env)->GetStringUTFChars(env, kvs_name, NULL);

    err = hse_kvdb_kvs_drop(kvdb, kvs_name_chars);

    if (kvs_name)
        (*env)->ReleaseStringUTFChars(env, kvs_name, kvs_name_chars);

    if (err)
        throw_new_hse_exception(env, err);
}

jlong
Java_io_github_hse_1project_hse_Kvs_open(
    JNIEnv *env,
    jclass kvs_cls,
    jlong kvdb_handle,
    jstring kvs_name,
    jobjectArray params)
{
    jsize paramc;
    hse_err_t err;
    const char **paramv;
    struct hse_kvs *kvs;
    const char *kvs_name_chars = NULL;
    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    (void)kvs_cls;

    if (kvs_name)
        kvs_name_chars = (*env)->GetStringUTFChars(env, kvs_name, NULL);

    to_paramv(env, params, &paramc, &paramv);
    if ((*env)->ExceptionCheck(env))
        return 0;

    err = hse_kvdb_kvs_open(kvdb, kvs_name_chars, paramc, paramv, &kvs);

    if (kvs_name)
        (*env)->ReleaseStringUTFChars(env, kvs_name, kvs_name_chars);

    free_paramv(env, params, paramc, paramv);

    if (err)
        throw_new_hse_exception(env, err);

    return (jlong)kvs;
}

void
Java_io_github_hse_1project_hse_Kvs_close(JNIEnv *env, jobject kvs_obj, jlong kvs_handle)
{
    hse_err_t err;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;

    (void)kvs_obj;

    err = hse_kvdb_kvs_close(kvs);
    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvs_delete__J_3BIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jbyteArray key,
    jint key_len,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    jbyte *key_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key)
        key_data = (*env)->GetByteArrayElements(env, key, NULL);

    err = hse_kvs_delete(kvs, flags, txn, key_data, key_len);

    if (key)
        (*env)->ReleaseByteArrayElements(env, key, key_data, JNI_ABORT);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvs_delete__JLjava_lang_String_2IJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jstring key,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    jsize key_len = 0;
    const char *key_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key) {
        key_data = (*env)->GetStringUTFChars(env, key, NULL);
        key_len = (*env)->GetStringUTFLength(env, key);
    }

    err = hse_kvs_delete(kvs, flags, txn, key_data, key_len);

    if (key)
        (*env)->ReleaseStringUTFChars(env, key, key_data);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvs_delete__JLjava_nio_ByteBuffer_2IIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jobject key,
    jint key_len,
    jint key_pos,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    const void *key_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key) {
        key_data = (*env)->GetDirectBufferAddress(env, key);

        // Move the start address based on the position
        key_data = (uint8_t *)key_data + key_pos;
    }

    err = hse_kvs_delete(kvs, flags, txn, key_data, key_len);

    if (err)
        throw_new_hse_exception(env, err);
}

jbyteArray
Java_io_github_hse_1project_hse_Kvs_get__J_3BIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jbyteArray key,
    jint key_len,
    jint flags,
    jlong txn_handle)
{
    bool found;
    hse_err_t err;
    size_t value_len;
    jbyte *value_data;
    jbyte *key_data = NULL;
    jbyteArray value = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key)
        key_data = (*env)->GetByteArrayElements(env, key, NULL);

    value_data = malloc(HSE_KVS_VALUE_LEN_MAX * sizeof(*value_data));
    if (!value_data) {
        (*env)->ThrowNew(
            env,
            globals.java.lang.OutOfMemoryError.class,
            "Failed to allocate memory for value buffer");
        return NULL;
    }

    err = hse_kvs_get(
        kvs, flags, txn, key_data, key_len, &found, value_data, HSE_KVS_VALUE_LEN_MAX, &value_len);

    if (key)
        (*env)->ReleaseByteArrayElements(env, key, key_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        goto out;
    }

    if (!found)
        goto out;

    value = (*env)->NewByteArray(env, value_len);
    if (!value) {
        (*env)->ThrowNew(
            env,
            globals.java.lang.OutOfMemoryError.class,
            "Failed to allocate memory for value byte array");
        goto out;
    }

    (*env)->SetByteArrayRegion(env, value, 0, value_len, value_data);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of value_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

out:
    free(value_data);

    return value;
}

jbyteArray
Java_io_github_hse_1project_hse_Kvs_get__JLjava_lang_String_2IJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jstring key,
    jint flags,
    jlong txn_handle)
{
    bool found;
    hse_err_t err;
    size_t value_len;
    jsize key_len = 0;
    jbyte *value_data;
    jbyteArray value = NULL;
    const char *key_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key) {
        key_data = (*env)->GetStringUTFChars(env, key, NULL);
        key_len = (*env)->GetStringUTFLength(env, key);
    }

    value_data = malloc(HSE_KVS_VALUE_LEN_MAX * sizeof(*value_data));
    if (!value_data) {
        (*env)->ThrowNew(
            env,
            globals.java.lang.OutOfMemoryError.class,
            "Failed to allocate memory for value buffer");
        return NULL;
    }

    err = hse_kvs_get(
        kvs, flags, txn, key_data, key_len, &found, value_data, HSE_KVS_VALUE_LEN_MAX, &value_len);

    if (key)
        (*env)->ReleaseStringUTFChars(env, key, key_data);

    if (err) {
        throw_new_hse_exception(env, err);
        goto out;
    }

    if (!found)
        goto out;

    value = (*env)->NewByteArray(env, value_len);
    if (!value) {
        (*env)->ThrowNew(
            env,
            globals.java.lang.OutOfMemoryError.class,
            "Failed to allocate memory for value byte array");
        goto out;
    }

    (*env)->SetByteArrayRegion(env, value, 0, value_len, value_data);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of value_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

out:
    free(value_data);

    return value;
}

jbyteArray
Java_io_github_hse_1project_hse_Kvs_get__JLjava_nio_ByteBuffer_2IIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jobject key,
    jint key_len,
    jint key_pos,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    size_t value_len;
    jbyte *value_data;
    bool found = false;
    jbyteArray value = NULL;
    const void *key_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key) {
        key_data = (*env)->GetDirectBufferAddress(env, key);

        // Move the start address based on the position
        key_data = (uint8_t *)key_data + key_pos;
    }

    value_data = malloc(HSE_KVS_VALUE_LEN_MAX * sizeof(*value_data));
    if (!value_data) {
        (*env)->ThrowNew(
            env,
            globals.java.lang.OutOfMemoryError.class,
            "Failed to allocate memory for value buffer");
        return NULL;
    }

    err = hse_kvs_get(
        kvs, flags, txn, key_data, key_len, &found, value_data, HSE_KVS_VALUE_LEN_MAX, &value_len);
    if (err) {
        throw_new_hse_exception(env, err);
        goto out;
    }

    if (!found)
        goto out;

    value = (*env)->NewByteArray(env, value_len);
    if (!value) {
        (*env)->ThrowNew(
            env,
            globals.java.lang.OutOfMemoryError.class,
            "Failed to allocate memory for value byte array");
        goto out;
    }

    (*env)->SetByteArrayRegion(env, value, 0, value_len, value_data);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of value_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

out:
    free(value_data);

    return value;
}

jint
Java_io_github_hse_1project_hse_Kvs_get__J_3BI_3BIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jbyteArray key,
    jint key_len,
    jbyteArray value_buf,
    jint value_buf_sz,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    size_t value_len;
    bool found = false;
    jbyte *key_data = NULL;
    jbyte *value_buf_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key)
        key_data = (*env)->GetByteArrayElements(env, key, NULL);
    if (value_buf)
        value_buf_data = (*env)->GetByteArrayElements(env, value_buf, NULL);

    err = hse_kvs_get(
        kvs, flags, txn, key_data, key_len, &found, value_buf_data, value_buf_sz, &value_len);

    if (key)
        (*env)->ReleaseByteArrayElements(env, key, key_data, JNI_ABORT);
    if (value_buf) {
        /* In the case the key isn't found OR error, save a copy operation and
         * ABORT.
         */
        (*env)->ReleaseByteArrayElements(
            env, value_buf, value_buf_data, (!found || err) ? JNI_ABORT : 0);
    }

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    /* Use the least-most bit to represent whether the key was found or not
     * because 0-length values are different from non-existent keys.
     *
     * To get information out of the packed integer, do the following:
     *
     * int packedValueLen = get(...);
     * boolean found = (packedValueLen & 0b1) == 1;
     * int valueLen = packedValueLen >> 1;
     */
    return (value_len << 1 | 0x1);
}

jint
Java_io_github_hse_1project_hse_Kvs_get__J_3BILjava_nio_ByteBuffer_2IIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jbyteArray key,
    jint key_len,
    jobject value_buf,
    jint value_buf_sz,
    jint value_buf_pos,
    jint flags,
    jlong txn_handle)
{
    bool found;
    hse_err_t err;
    size_t value_len;
    jbyte *key_data = NULL;
    void *value_buf_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key)
        key_data = (*env)->GetByteArrayElements(env, key, NULL);

    if (value_buf) {
        value_buf_data = (*env)->GetDirectBufferAddress(env, value_buf);

        // Move the start address based on the position
        value_buf_data = (uint8_t *)value_buf_data + value_buf_pos;
    }

    err = hse_kvs_get(
        kvs, flags, txn, key_data, key_len, &found, value_buf_data, value_buf_sz, &value_len);

    if (key)
        (*env)->ReleaseByteArrayElements(env, key, key_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    /* Use the least-most bit to represent whether the key was found or not
     * because 0-length values are different from non-existent keys.
     *
     * To get information out of the packed integer, do the following:
     *
     * int packedValueLen = get(...);
     * boolean found = (packedValueLen & 0b1) == 1;
     * int valueLen = packedValueLen >> 1;
     */
    return (value_len << 1 | 0x1);
}

jint
Java_io_github_hse_1project_hse_Kvs_get__JLjava_lang_String_2_3BIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jstring key,
    jbyteArray value_buf,
    jint value_buf_sz,
    jint flags,
    jlong txn_handle)
{
    bool found;
    hse_err_t err;
    size_t value_len;
    jsize key_len = 0;
    const char *key_data = NULL;
    jbyte *value_buf_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key) {
        key_data = (*env)->GetStringUTFChars(env, key, NULL);
        key_len = (*env)->GetStringUTFLength(env, key);
    }

    if (value_buf)
        value_buf_data = (*env)->GetByteArrayElements(env, value_buf, NULL);

    err = hse_kvs_get(
        kvs, flags, txn, key_data, key_len, &found, value_buf_data, value_buf_sz, &value_len);

    if (value_buf) {
        /* In the case the key isn't found OR error, save a copy operation and
         * ABORT.
         */
        (*env)->ReleaseByteArrayElements(
            env, value_buf, value_buf_data, (!found || err) ? JNI_ABORT : 0);
    }

    if (key)
        (*env)->ReleaseStringUTFChars(env, key, key_data);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    /* Use the least-most bit to represent whether the key was found or not
     * because 0-length values are different from non-existent keys.
     *
     * To get information out of the packed integer, do the following:
     *
     * int packedValueLen = get(...);
     * boolean found = (packedValueLen & 0b1) == 1;
     * int valueLen = packedValueLen >> 1;
     */
    return (value_len << 1 | 0x1);
}

jint
Java_io_github_hse_1project_hse_Kvs_get__JLjava_lang_String_2Ljava_nio_ByteBuffer_2IIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jstring key,
    jobject value_buf,
    jint value_buf_sz,
    jint value_buf_pos,
    jint flags,
    jlong txn_handle)
{
    bool found;
    hse_err_t err;
    size_t value_len;
    jsize key_len = 0;
    const char *key_data = NULL;
    void *value_buf_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key) {
        key_data = (*env)->GetStringUTFChars(env, key, NULL);
        key_len = (*env)->GetStringUTFLength(env, key);
    }

    if (value_buf) {
        value_buf_data = (*env)->GetDirectBufferAddress(env, value_buf);

        // Move the start address based on the position
        value_buf_data = (uint8_t *)value_buf_data + value_buf_pos;
    }

    err = hse_kvs_get(
        kvs, flags, txn, key_data, key_len, &found, value_buf_data, value_buf_sz, &value_len);

    if (key)
        (*env)->ReleaseStringUTFChars(env, key, key_data);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    /* Use the least-most bit to represent whether the key was found or not
     * because 0-length values are different from non-existent keys.
     *
     * To get information out of the packed integer, do the following:
     *
     * int packedValueLen = get(...);
     * boolean found = (packedValueLen & 0b1) == 1;
     * int valueLen = packedValueLen >> 1;
     */
    return (value_len << 1 | 0x1);
}

jint
Java_io_github_hse_1project_hse_Kvs_get__JLjava_nio_ByteBuffer_2II_3BIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jobject key,
    jint key_len,
    jint key_pos,
    jbyteArray value_buf,
    jint value_buf_sz,
    jint flags,
    jlong txn_handle)
{
    bool found;
    hse_err_t err;
    size_t value_len;
    const void *key_data = NULL;
    jbyte *value_buf_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key) {
        key_data = (*env)->GetDirectBufferAddress(env, key);

        // Move the start address based on the position
        key_data = (uint8_t *)key_data + key_pos;
    }

    if (value_buf)
        value_buf_data = (*env)->GetByteArrayElements(env, value_buf, NULL);

    err = hse_kvs_get(
        kvs, flags, txn, key_data, key_len, &found, value_buf_data, value_buf_sz, &value_len);

    if (value_buf) {
        /* In the case the key isn't found OR error, save a copy operation and
         * ABORT.
         */
        (*env)->ReleaseByteArrayElements(
            env, value_buf, value_buf_data, (!found || err) ? JNI_ABORT : 0);
    }

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    /* Use the least-most bit to represent whether the key was found or not
     * because 0-length values are different from non-existent keys.
     *
     * To get information out of the packed integer, do the following:
     *
     * int packedValueLen = get(...);
     * boolean found = (packedValueLen & 0b1) == 1;
     * int valueLen = packedValueLen >> 1;
     */
    return (value_len << 1 | 0x1);
}

jint
Java_io_github_hse_1project_hse_Kvs_get__JLjava_nio_ByteBuffer_2IILjava_nio_ByteBuffer_2IIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jobject key,
    jint key_len,
    jint key_pos,
    jobject value_buf,
    jint value_buf_sz,
    jint value_buf_pos,
    jint flags,
    jlong txn_handle)
{
    bool found;
    hse_err_t err;
    size_t value_len;
    void *value_buf_data = NULL;
    const void *key_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key) {
        key_data = (*env)->GetDirectBufferAddress(env, key);

        // Move the start address based on the position
        key_data = (uint8_t *)key_data + key_pos;
    }

    if (value_buf) {
        value_buf_data = (*env)->GetDirectBufferAddress(env, value_buf);

        // Move the start address based on the position
        value_buf_data = (uint8_t *)value_buf_data + value_buf_pos;
    }

    err = hse_kvs_get(
        kvs, flags, txn, key_data, key_len, &found, value_buf_data, value_buf_sz, &value_len);
    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    /* Use the least-most bit to represent whether the key was found or not
     * because 0-length values are different from non-existent keys.
     *
     * To get information out of the packed integer, do the following:
     *
     * int packedValueLen = get(...);
     * boolean found = (packedValueLen & 0b1) == 1;
     * int valueLen = packedValueLen >> 1;
     */
    return (value_len << 1 | 0x1);
}

jstring
Java_io_github_hse_1project_hse_Kvs_getName(JNIEnv *env, jobject kvs_obj, jlong kvs_handle)
{
    const char *name;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;

    (void)kvs_obj;

    name = hse_kvs_name_get(kvs);

    return (*env)->NewStringUTF(env, name);
}

jstring
Java_io_github_hse_1project_hse_Kvs_getParam(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jstring param)
{
    char *buf = NULL;
    size_t needed_sz;
    hse_err_t err = 0;
    jstring value = NULL;
    const char *param_chars = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;

    (void)env;
    (void)kvs_obj;

    if (param)
        param_chars = (*env)->GetStringUTFChars(env, param, NULL);

    err = hse_kvs_param_get(kvs, param_chars, NULL, 0, &needed_sz);
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

    err = hse_kvs_param_get(kvs, param_chars, buf, needed_sz + 1, NULL);
    if (err) {
        throw_new_hse_exception(env, err);
        goto out;
    }

out:
    if (param)
        (*env)->ReleaseStringUTFChars(env, param, param_chars);

    // Don't allocate a new string if an exception has been thrown.
    if (!(*env)->ExceptionCheck(env))
        value = (*env)->NewStringUTF(env, buf);

    free(buf);

    return value;
}

void
Java_io_github_hse_1project_hse_Kvs_prefixDelete__J_3BIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jbyteArray pfx,
    jint pfx_len,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    jbyte *pfx_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (pfx)
        pfx_data = (*env)->GetByteArrayElements(env, pfx, NULL);

    err = hse_kvs_prefix_delete(kvs, flags, txn, pfx_data, pfx_len);

    if (pfx)
        (*env)->ReleaseByteArrayElements(env, pfx, pfx_data, JNI_ABORT);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvs_prefixDelete__JLjava_lang_String_2IJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jstring pfx,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    jsize pfx_len = 0;
    const char *pfx_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (pfx) {
        pfx_data = (*env)->GetStringUTFChars(env, pfx, NULL);
        pfx_len = (*env)->GetStringUTFLength(env, pfx);
    }

    err = hse_kvs_prefix_delete(kvs, flags, txn, pfx_data, pfx_len);

    if (pfx)
        (*env)->ReleaseStringUTFChars(env, pfx, pfx_data);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvs_prefixDelete__JLjava_nio_ByteBuffer_2IIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jobject pfx,
    jint pfx_len,
    jint pfx_pos,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    const void *pfx_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (pfx) {
        pfx_data = (*env)->GetDirectBufferAddress(env, pfx);

        // Move the start address based on the position
        pfx_data = (uint8_t *)pfx_data + pfx_pos;
    }

    err = hse_kvs_prefix_delete(kvs, flags, txn, pfx_data, pfx_len);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvs_put__J_3BI_3BIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jbyteArray key,
    jint key_len,
    jbyteArray value,
    jint value_len,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;
    jbyte *key_data = NULL;
    jbyte *value_data = NULL;

    (void)kvs_obj;

    if (key)
        key_data = (*env)->GetByteArrayElements(env, key, NULL);
    if (value)
        value_data = (*env)->GetByteArrayElements(env, value, NULL);

    err = hse_kvs_put(kvs, flags, txn, key_data, key_len, value_data, value_len);

    if (key)
        (*env)->ReleaseByteArrayElements(env, key, key_data, JNI_ABORT);
    if (value)
        (*env)->ReleaseByteArrayElements(env, value, value_data, JNI_ABORT);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvs_put__J_3BILjava_lang_String_2IJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jbyteArray key,
    jint key_len,
    jstring value,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    jsize value_len = 0;
    jbyte *key_data = NULL;
    const char *value_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key)
        key_data = (*env)->GetByteArrayElements(env, key, NULL);

    if (value) {
        value_data = (*env)->GetStringUTFChars(env, value, NULL);
        value_len = (*env)->GetStringUTFLength(env, value);
    }

    err = hse_kvs_put(kvs, flags, txn, key_data, key_len, value_data, value_len);

    if (key)
        (*env)->ReleaseByteArrayElements(env, key, key_data, JNI_ABORT);
    if (value)
        (*env)->ReleaseStringUTFChars(env, value, value_data);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvs_put__J_3BILjava_nio_ByteBuffer_2IIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jbyteArray key,
    jint key_len,
    jobject value,
    jint value_len,
    jint value_pos,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    jbyte *key_data = NULL;
    const void *value_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key)
        key_data = (*env)->GetByteArrayElements(env, key, NULL);

    if (value) {
        value_data = (*env)->GetDirectBufferAddress(env, value);

        // Move the start address based on the position
        value_data = (uint8_t *)value_data + value_pos;
    }

    err = hse_kvs_put(kvs, flags, txn, key_data, key_len, value_data, value_len);

    if (key)
        (*env)->ReleaseByteArrayElements(env, key, key_data, JNI_ABORT);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvs_put__JLjava_lang_String_2_3BIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jstring key,
    jbyteArray value,
    jint value_len,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    jsize key_len = 0;
    const char *key_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;
    jbyte *value_data = NULL;

    (void)kvs_obj;

    if (key) {
        key_data = (*env)->GetStringUTFChars(env, key, NULL);
        key_len = (*env)->GetStringUTFLength(env, key);
    }

    if (value)
        value_data = (*env)->GetByteArrayElements(env, value, NULL);

    err = hse_kvs_put(kvs, flags, txn, key_data, key_len, value_data, value_len);

    if (key)
        (*env)->ReleaseStringUTFChars(env, key, key_data);
    if (value)
        (*env)->ReleaseByteArrayElements(env, value, value_data, JNI_ABORT);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvs_put__JLjava_lang_String_2Ljava_lang_String_2IJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jstring key,
    jstring value,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    jsize key_len = 0;
    jsize value_len = 0;
    const char *key_data = NULL;
    const char *value_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key) {
        key_data = (*env)->GetStringUTFChars(env, key, NULL);
        key_len = (*env)->GetStringUTFLength(env, key);
    }

    if (value) {
        value_data = (*env)->GetStringUTFChars(env, value, NULL);
        value_len = (*env)->GetStringUTFLength(env, value);
    }

    err = hse_kvs_put(kvs, flags, txn, key_data, key_len, value_data, value_len);

    if (key)
        (*env)->ReleaseStringUTFChars(env, key, key_data);
    if (value)
        (*env)->ReleaseStringUTFChars(env, value, value_data);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvs_put__JLjava_lang_String_2Ljava_nio_ByteBuffer_2IIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jstring key,
    jobject value,
    jint value_len,
    jint value_pos,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    jsize key_len = 0;
    const char *key_data = NULL;
    const void *value_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key) {
        key_data = (*env)->GetStringUTFChars(env, key, NULL);
        key_len = (*env)->GetStringUTFLength(env, key);
    }

    if (value) {
        value_data = (*env)->GetDirectBufferAddress(env, value);

        // Move the start address based on the position
        value_data = (uint8_t *)value_data + value_pos;
    }

    err = hse_kvs_put(kvs, flags, txn, key_data, key_len, value_data, value_len);

    if (key)
        (*env)->ReleaseStringUTFChars(env, key, key_data);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvs_put__JLjava_nio_ByteBuffer_2II_3BIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jobject key,
    jint key_len,
    jint key_pos,
    jbyteArray value,
    jint value_len,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    jbyte *value_data = NULL;
    const void *key_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key) {
        key_data = (*env)->GetDirectBufferAddress(env, key);

        // Move the start address based on the position
        key_data = (uint8_t *)key_data + key_pos;
    }

    if (value)
        value_data = (*env)->GetByteArrayElements(env, value, NULL);

    err = hse_kvs_put(kvs, flags, txn, key_data, key_len, value_data, value_len);

    if (value)
        (*env)->ReleaseByteArrayElements(env, value, value_data, JNI_ABORT);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvs_put__JLjava_nio_ByteBuffer_2IILjava_lang_String_2IJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jobject key,
    jint key_len,
    jint key_pos,
    jstring value,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    jsize value_len = 0;
    const void *key_data = NULL;
    const char *value_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key) {
        key_data = (*env)->GetDirectBufferAddress(env, key);

        // Move the start address based on the position
        key_data = (uint8_t *)key_data + key_pos;
    }

    if (value) {
        value_data = (*env)->GetStringUTFChars(env, value, NULL);
        value_len = (*env)->GetStringUTFLength(env, value);
    }

    err = hse_kvs_put(kvs, flags, txn, key_data, key_len, value_data, value_len);
    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Kvs_put__JLjava_nio_ByteBuffer_2IILjava_nio_ByteBuffer_2IIIJ(
    JNIEnv *env,
    jobject kvs_obj,
    jlong kvs_handle,
    jobject key,
    jint key_len,
    jint key_pos,
    jobject value,
    jint value_len,
    jint value_pos,
    jint flags,
    jlong txn_handle)
{
    hse_err_t err;
    const void *key_data = NULL;
    const void *value_data = NULL;
    struct hse_kvs *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_kvdb_txn *txn = (struct hse_kvdb_txn *)txn_handle;

    (void)kvs_obj;

    if (key) {
        key_data = (*env)->GetDirectBufferAddress(env, key);

        // Move the start address based on the position
        key_data = (uint8_t *)key_data + key_pos;
    }

    if (value) {
        value_data = (*env)->GetDirectBufferAddress(env, value);

        // Move the start address based on the position
        value_data = (uint8_t *)value_data + value_pos;
    }

    err = hse_kvs_put(kvs, flags, txn, key_data, key_len, value_data, value_len);
    if (err)
        throw_new_hse_exception(env, err);
}
