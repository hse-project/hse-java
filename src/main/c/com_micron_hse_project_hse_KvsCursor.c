/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

#include <assert.h>
#include <stdint.h>
#include <string.h>
#include <sys/param.h>

#include <hse/hse.h>
#include <jni.h>

#include "hsejni.h"
#include "com_micron_hse_project_hse_KvsCursor.h"

jlong
Java_com_micron_hse_1project_hse_KvsCursor_create__J_3BIIJ(
    JNIEnv    *env,
    jclass     cursor_cls,
    jlong      kvs_handle,
    jbyteArray filter,
    jint       filter_len,
    jint       flags,
    jlong      txn_handle)
{
    (void)cursor_cls;

    struct hse_kvs_cursor *cursor = NULL;
    struct hse_kvs        *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_txn   *txn = (struct hse_txn *)txn_handle;

    jbyte *filter_data = filter ? (*env)->GetByteArrayElements(env, filter, NULL) : NULL;

    const hse_err_t err = hse_kvs_cursor_create(kvs, flags, txn, filter_data, filter_len, &cursor);

    if (filter)
        (*env)->ReleaseByteArrayElements(env, filter, filter_data, JNI_ABORT);

    if (err)
        throw_new_hse_exception(env, err);

    return (jlong)cursor;
}

jlong
Java_com_micron_hse_1project_hse_KvsCursor_create__JLjava_lang_String_2IJ(
    JNIEnv *env,
    jclass  cursor_cls,
    jlong   kvs_handle,
    jstring filter,
    jint    flags,
    jlong   txn_handle)
{
    (void)cursor_cls;

    struct hse_kvs_cursor *cursor = NULL;
    struct hse_kvs        *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_txn   *txn = (struct hse_txn *)txn_handle;

    const char *filter_data = NULL;
    jsize       filter_len = 0;
    if (filter) {
        filter_data = (*env)->GetStringUTFChars(env, filter, NULL);
        filter_len = (*env)->GetStringUTFLength(env, filter);
    }

    const hse_err_t err = hse_kvs_cursor_create(kvs, flags, txn, filter_data, filter_len, &cursor);

    if (filter)
        (*env)->ReleaseStringUTFChars(env, filter, filter_data);

    if (err)
        throw_new_hse_exception(env, err);

    return (jlong)cursor;
}

jlong
Java_com_micron_hse_1project_hse_KvsCursor_create__JLjava_nio_ByteBuffer_2IIIJ(
    JNIEnv *env,
    jclass  cursor_cls,
    jlong   kvs_handle,
    jobject filter,
    jint    filter_len,
    jint    filter_pos,
    jint    flags,
    jlong   txn_handle)
{
    (void)cursor_cls;

    struct hse_kvs_cursor *cursor;
    struct hse_kvs        *kvs = (struct hse_kvs *)kvs_handle;
    struct hse_txn   *txn = (struct hse_txn *)txn_handle;

    const void *filter_data = NULL;
    if (filter) {
        filter_data = (*env)->GetDirectBufferAddress(env, filter);

        // Move the start address based on the position
        filter_data = (uint8_t *)filter_data + filter_pos;
    }

    const hse_err_t err = hse_kvs_cursor_create(kvs, flags, txn, filter_data, filter_len, &cursor);

    if (err)
        throw_new_hse_exception(env, err);

    return (jlong)cursor;
}

void
Java_com_micron_hse_1project_hse_KvsCursor_destroy(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const hse_err_t err = hse_kvs_cursor_destroy(cursor);

    if (err)
        throw_new_hse_exception(env, err);
}

jobject
Java_com_micron_hse_1project_hse_KvsCursor_read__JI(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle,
    jint    flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const void     *key;
    size_t          key_len;
    const void     *value;
    size_t          value_len;
    bool            eof;
    const hse_err_t err =
        hse_kvs_cursor_read(cursor, flags, &key, &key_len, &value, &value_len, &eof);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (eof) {
        (*env)->ThrowNew(env, globals.java.io.EOFException.class, "End of cursor reached");
        return NULL;
    }

    jbyteArray key_array = (*env)->NewByteArray(env, key_len);
    if (!key_array) {
        (*env)->ThrowNew(
            env,
            globals.java.lang.OutOfMemoryError.class,
            "Failed to allocate memory for key array");
        return NULL;
    }

    jbyteArray value_array = (*env)->NewByteArray(env, value_len);
    if (!value_array) {
        (*env)->ThrowNew(
            env,
            globals.java.lang.OutOfMemoryError.class,
            "Failed to allocate memory for value array");
        return NULL;
    }

    (*env)->SetByteArrayRegion(env, key_array, 0, key_len, key);
    (*env)->SetByteArrayRegion(env, value_array, 0, value_len, value);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte arrays
     * are constructed with known lengths, so the previous operations are
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return (*env)->NewObject(
        env,
        globals.java.util.AbstractMap.SimpleImmutableEntry.class,
        globals.java.util.AbstractMap.SimpleImmutableEntry.init,
        key_array,
        value_array);
}

jobject
Java_com_micron_hse_1project_hse_KvsCursor_read__J_3BI_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jbyteArray key_buf,
    jint       key_buf_sz,
    jbyteArray value_buf,
    jint       value_buf_sz,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    jbyte *key_buf_data = key_buf ? (*env)->GetByteArrayElements(env, key_buf, NULL) : NULL;
    jbyte *value_buf_data = value_buf ? (*env)->GetByteArrayElements(env, value_buf, NULL) : NULL;

    size_t          key_len;
    size_t          value_len;
    bool            eof;
    const hse_err_t err = hse_kvs_cursor_read_copy(
        cursor,
        flags,
        key_buf_data,
        key_buf_sz,
        &key_len,
        value_buf_data,
        value_buf_sz,
        &value_len,
        &eof);

    if (key_buf)
        (*env)->ReleaseByteArrayElements(env, key_buf, key_buf_data, (eof || err) ? JNI_ABORT : 0);
    if (value_buf)
        (*env)->ReleaseByteArrayElements(
            env, value_buf, value_buf_data, (eof || err) ? JNI_ABORT : 0);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (eof) {
        (*env)->ThrowNew(env, globals.java.io.EOFException.class, "End of cursor reached");
        return NULL;
    }

    const jobject key_len_obj = (*env)->NewObject(
        env, globals.java.lang.Integer.class, globals.java.lang.Integer.init, key_len);
    const jobject value_len_obj = (*env)->NewObject(
        env, globals.java.lang.Integer.class, globals.java.lang.Integer.init, value_len);

    return (*env)->NewObject(
        env,
        globals.java.util.AbstractMap.SimpleImmutableEntry.class,
        globals.java.util.AbstractMap.SimpleImmutableEntry.init,
        key_len_obj,
        value_len_obj);
}

jobject
Java_com_micron_hse_1project_hse_KvsCursor_read__J_3BILjava_nio_ByteBuffer_2III(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jbyteArray key_buf,
    jint       key_buf_sz,
    jobject    value_buf,
    jint       value_buf_sz,
    jint       value_buf_pos,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    jbyte *key_buf_data = key_buf ? (*env)->GetByteArrayElements(env, key_buf, NULL) : NULL;
    void  *value_buf_data = NULL;
    if (value_buf) {
        value_buf_data = (*env)->GetDirectBufferAddress(env, value_buf);

        // Move the start address based on the position
        value_buf_data = (uint8_t *)value_buf_data + value_buf_pos;
    }

    size_t          key_len;
    size_t          value_len;
    bool            eof;
    const hse_err_t err = hse_kvs_cursor_read_copy(
        cursor,
        flags,
        key_buf_data,
        key_buf_sz,
        &key_len,
        value_buf_data,
        value_buf_sz,
        &value_len,
        &eof);

    if (key_buf)
        (*env)->ReleaseByteArrayElements(env, key_buf, key_buf_data, (eof || err) ? JNI_ABORT : 0);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (eof) {
        (*env)->ThrowNew(env, globals.java.io.EOFException.class, "End of cursor reached");
        return NULL;
    }

    const jobject key_len_obj = (*env)->NewObject(
        env, globals.java.lang.Integer.class, globals.java.lang.Integer.init, key_len);
    const jobject value_len_obj = (*env)->NewObject(
        env, globals.java.lang.Integer.class, globals.java.lang.Integer.init, value_len);

    return (*env)->NewObject(
        env,
        globals.java.util.AbstractMap.SimpleImmutableEntry.class,
        globals.java.util.AbstractMap.SimpleImmutableEntry.init,
        key_len_obj,
        value_len_obj);
}

jobject
Java_com_micron_hse_1project_hse_KvsCursor_read__JLjava_nio_ByteBuffer_2II_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jobject    key_buf,
    jint       key_buf_sz,
    jint       key_buf_pos,
    jbyteArray value_buf,
    jint       value_buf_sz,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    void *key_buf_data = NULL;
    if (key_buf) {
        key_buf_data = (*env)->GetDirectBufferAddress(env, key_buf);

        // Move the start address based on the position
        key_buf_data = (uint8_t *)key_buf_data + key_buf_pos;
    }

    jbyte *value_buf_data = value_buf ? (*env)->GetByteArrayElements(env, value_buf, NULL) : NULL;

    size_t          key_len;
    size_t          value_len;
    bool            eof;
    const hse_err_t err = hse_kvs_cursor_read_copy(
        cursor,
        flags,
        key_buf_data,
        key_buf_sz,
        &key_len,
        value_buf_data,
        value_buf_sz,
        &value_len,
        &eof);

    if (value_buf)
        (*env)->ReleaseByteArrayElements(
            env, value_buf, value_buf_data, (eof || err) ? JNI_ABORT : 0);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (eof) {
        (*env)->ThrowNew(env, globals.java.io.EOFException.class, "End of cursor reached");
        return NULL;
    }

    const jobject key_len_obj = (*env)->NewObject(
        env, globals.java.lang.Integer.class, globals.java.lang.Integer.init, key_len);
    const jobject value_len_obj = (*env)->NewObject(
        env, globals.java.lang.Integer.class, globals.java.lang.Integer.init, value_len);

    return (*env)->NewObject(
        env,
        globals.java.util.AbstractMap.SimpleImmutableEntry.class,
        globals.java.util.AbstractMap.SimpleImmutableEntry.init,
        key_len_obj,
        value_len_obj);
}

jobject
Java_com_micron_hse_1project_hse_KvsCursor_read__JLjava_nio_ByteBuffer_2IILjava_nio_ByteBuffer_2III(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle,
    jobject key_buf,
    jint    key_buf_sz,
    jint    key_buf_pos,
    jobject value_buf,
    jint    value_buf_sz,
    jint    value_buf_pos,
    jint    flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    void *key_buf_data = NULL;
    if (key_buf) {
        key_buf_data = (*env)->GetDirectBufferAddress(env, key_buf);

        // Move the start address based on the position
        key_buf_data = (uint8_t *)key_buf_data + key_buf_pos;
    }

    void *value_buf_data = NULL;
    if (value_buf) {
        value_buf_data = (*env)->GetDirectBufferAddress(env, value_buf);

        // Move the start address based on the position
        value_buf_data = (uint8_t *)value_buf_data + value_buf_pos;
    }

    size_t          key_len;
    size_t          value_len;
    bool            eof;
    const hse_err_t err = hse_kvs_cursor_read_copy(
        cursor,
        flags,
        key_buf_data,
        key_buf_sz,
        &key_len,
        value_buf_data,
        value_buf_sz,
        &value_len,
        &eof);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (eof) {
        (*env)->ThrowNew(env, globals.java.io.EOFException.class, "End of cursor reached");
        return NULL;
    }

    const jobject key_len_obj = (*env)->NewObject(
        env, globals.java.lang.Integer.class, globals.java.lang.Integer.init, key_len);
    const jobject value_len_obj = (*env)->NewObject(
        env, globals.java.lang.Integer.class, globals.java.lang.Integer.init, value_len);

    return (*env)->NewObject(
        env,
        globals.java.util.AbstractMap.SimpleImmutableEntry.class,
        globals.java.util.AbstractMap.SimpleImmutableEntry.init,
        key_len_obj,
        value_len_obj);
}

jbyteArray
Java_com_micron_hse_1project_hse_KvsCursor_seek__J_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jbyteArray key,
    jint       key_len,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    jbyte *key_data = key ? (*env)->GetByteArrayElements(env, key, NULL) : NULL;

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek(cursor, flags, key_data, key_len, &found, &found_len);

    if (key)
        (*env)->ReleaseByteArrayElements(env, key, key_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (!found)
        return NULL;

    jbyteArray found_key = (*env)->NewByteArray(env, found_len);
    (*env)->SetByteArrayRegion(env, found_key, 0, found_len, found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_key;
}

jbyteArray
Java_com_micron_hse_1project_hse_KvsCursor_seek__JLjava_lang_String_2I(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle,
    jstring key,
    jint    flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const char *key_data = NULL;
    jsize       key_len = 0;
    if (key) {
        key_data = (*env)->GetStringUTFChars(env, key, NULL);
        key_len = (*env)->GetStringUTFLength(env, key);
    }

    const void     *found = NULL;
    size_t          found_len = 0;
    const hse_err_t err = hse_kvs_cursor_seek(cursor, flags, key_data, key_len, &found, &found_len);

    if (key)
        (*env)->ReleaseStringUTFChars(env, key, key_data);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (!found)
        return NULL;

    jbyteArray found_key = (*env)->NewByteArray(env, found_len);
    (*env)->SetByteArrayRegion(env, found_key, 0, found_len, found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_key;
}

jbyteArray
Java_com_micron_hse_1project_hse_KvsCursor_seek__JLjava_nio_ByteBuffer_2III(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle,
    jobject key,
    jint    key_len,
    jint    key_pos,
    jint    flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const void *key_data = NULL;
    if (key) {
        key_data = (*env)->GetDirectBufferAddress(env, key);

        // Move the start address based on the position
        key_data = (uint8_t *)key_data + key_pos;
    }

    const void     *found = NULL;
    size_t          found_len = 0;
    const hse_err_t err = hse_kvs_cursor_seek(cursor, flags, key_data, key_len, &found, &found_len);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (!found)
        return NULL;

    jbyteArray found_key = (*env)->NewByteArray(env, found_len);
    (*env)->SetByteArrayRegion(env, found_key, 0, found_len, found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_key;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seek__J_3BI_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jbyteArray key,
    jint       key_len,
    jbyteArray found_buf,
    jint       found_buf_sz,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    jbyte *key_data = key ? (*env)->GetByteArrayElements(env, key, NULL) : NULL;

    const void     *found = NULL;
    size_t          found_len = 0;
    const hse_err_t err = hse_kvs_cursor_seek(cursor, flags, key_data, key_len, &found, &found_len);

    if (key)
        (*env)->ReleaseByteArrayElements(env, key, key_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    if (found_buf) {
        const size_t copy_len = MIN((size_t)found_buf_sz, found_len);
        (*env)->SetByteArrayRegion(env, found_buf, 0, copy_len, found);
    }

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * set with the MIN(found_buf_sz, found_len), so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seek__J_3BILjava_nio_ByteBuffer_2III(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jbyteArray key,
    jint       key_len,
    jobject    found_buf,
    jint       found_buf_sz,
    jint       found_buf_pos,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    jbyte *key_data = key ? (*env)->GetByteArrayElements(env, key, NULL) : NULL;

    const void     *found = NULL;
    size_t          found_len = 0;
    const hse_err_t err = hse_kvs_cursor_seek(cursor, flags, key_data, key_len, &found, &found_len);

    if (key)
        (*env)->ReleaseByteArrayElements(env, key, key_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    if (found_buf) {
        void *found_buf_data = (*env)->GetDirectBufferAddress(env, found_buf);

        found_buf_data = (uint8_t *)found_buf_data + found_buf_pos;
        const size_t copy_len = MIN((size_t)found_buf_sz, found_len);

        memcpy(found_buf_data, found, copy_len);
    }

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seek__JLjava_lang_String_2_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jstring    key,
    jbyteArray found_buf,
    jint       found_buf_sz,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const char *key_data = NULL;
    jsize       key_len = 0;
    if (key) {
        key_data = (*env)->GetStringUTFChars(env, key, NULL);
        key_len = (*env)->GetStringUTFLength(env, key);
    }

    const void     *found = NULL;
    size_t          found_len = 0;
    const hse_err_t err = hse_kvs_cursor_seek(cursor, flags, key_data, key_len, &found, &found_len);

    if (key)
        (*env)->ReleaseStringUTFChars(env, key, key_data);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    if (found_buf) {
        const size_t copy_len = MIN((size_t)found_buf_sz, found_len);
        (*env)->SetByteArrayRegion(env, found_buf, 0, copy_len, found);
    }

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seek__JLjava_lang_String_2Ljava_nio_ByteBuffer_2III(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle,
    jstring key,
    jobject found_buf,
    jint    found_buf_sz,
    jint    found_buf_pos,
    jint    flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const char *key_data = NULL;
    jsize       key_len = 0;
    if (key) {
        key_data = (*env)->GetStringUTFChars(env, key, NULL);
        key_len = (*env)->GetStringUTFLength(env, key);
    }

    const void     *found = NULL;
    size_t          found_len = 0;
    const hse_err_t err = hse_kvs_cursor_seek(cursor, flags, key_data, key_len, &found, &found_len);

    if (key)
        (*env)->ReleaseStringUTFChars(env, key, key_data);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    if (found_buf) {
        void *found_buf_data = (*env)->GetDirectBufferAddress(env, found_buf);

        found_buf_data = (uint8_t *)found_buf_data + found_buf_pos;
        const size_t copy_len = MIN((size_t)found_buf_sz, found_len);

        memcpy(found_buf_data, found, copy_len);
    }

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seek__JLjava_nio_ByteBuffer_2II_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jobject    key,
    jint       key_len,
    jint       key_pos,
    jbyteArray found_buf,
    jint       found_buf_sz,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const void *key_data = NULL;
    if (key) {
        key_data = (*env)->GetDirectBufferAddress(env, key);

        // Move the start address based on the position
        key_data = (uint8_t *)key_data + key_pos;
    }

    const void     *found = NULL;
    size_t          found_len = 0;
    const hse_err_t err = hse_kvs_cursor_seek(cursor, flags, key_data, key_len, &found, &found_len);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    if (found_buf) {
        const size_t copy_len = MIN((size_t)found_buf_sz, found_len);
        (*env)->SetByteArrayRegion(env, found_buf, 0, copy_len, found);
    }

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * set with the MIN(found_buf_sz, found_len), so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seek__JLjava_nio_ByteBuffer_2IILjava_nio_ByteBuffer_2III(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle,
    jobject key,
    jint    key_len,
    jint    key_pos,
    jobject found_buf,
    jint    found_buf_sz,
    jint    found_buf_pos,
    jint    flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const void *key_data = NULL;
    if (key) {
        key_data = (*env)->GetDirectBufferAddress(env, key);

        // Move the start address based on the position
        key_data = (uint8_t *)key_data + key_pos;
    }

    const void     *found = NULL;
    size_t          found_len = 0;
    const hse_err_t err = hse_kvs_cursor_seek(cursor, flags, key_data, key_len, &found, &found_len);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    if (found_buf) {
        void *found_buf_data = (*env)->GetDirectBufferAddress(env, found_buf);

        found_buf_data = (uint8_t *)found_buf_data + found_buf_pos;
        const size_t copy_len = MIN((size_t)found_buf_sz, found_len);

        memcpy(found_buf_data, found, copy_len);
    }

    return found_len;
}

jbyteArray
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__J_3BI_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jbyteArray filter_min,
    jint       filter_min_len,
    jbyteArray filter_max,
    jint       filter_max_len,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    jbyte *filter_min_data =
        filter_min ? (*env)->GetByteArrayElements(env, filter_min, NULL) : NULL;
    jbyte *filter_max_data =
        filter_max ? (*env)->GetByteArrayElements(env, filter_max, NULL) : NULL;

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseByteArrayElements(env, filter_min, filter_min_data, JNI_ABORT);
    if (filter_max)
        (*env)->ReleaseByteArrayElements(env, filter_max, filter_max_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (!found)
        return NULL;

    jbyteArray found_key = (*env)->NewByteArray(env, found_len);
    (*env)->SetByteArrayRegion(env, found_key, 0, found_len, found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_key;
}

jbyteArray
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__J_3BILjava_lang_String_2I(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jbyteArray filter_min,
    jint       filter_min_len,
    jstring    filter_max,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    jbyte *filter_min_data =
        filter_min ? (*env)->GetByteArrayElements(env, filter_min, NULL) : NULL;

    const char *filter_max_data = NULL;
    jsize       filter_max_len = 0;
    if (filter_max) {
        filter_max_data = (*env)->GetStringUTFChars(env, filter_max, NULL);
        filter_max_len = 0;
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (!found)
        return NULL;

    jbyteArray found_key = (*env)->NewByteArray(env, found_len);
    (*env)->SetByteArrayRegion(env, found_key, 0, found_len, found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_key;
}

jbyteArray
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__J_3BILjava_nio_ByteBuffer_2III(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jbyteArray filter_min,
    jint       filter_min_len,
    jobject    filter_max,
    jint       filter_max_len,
    jint       filter_max_pos,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    jbyte *filter_min_data =
        filter_min ? (*env)->GetByteArrayElements(env, filter_min, NULL) : NULL;
    const void *filter_max_data = NULL;
    if (filter_max) {
        filter_max_data = (*env)->GetDirectBufferAddress(env, filter_max);

        // Move the start address based on the position
        filter_max_data = (uint8_t *)filter_max_data + filter_max_pos;
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseByteArrayElements(env, filter_min, filter_min_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (!found)
        return NULL;

    jbyteArray found_key = (*env)->NewByteArray(env, found_len);
    (*env)->SetByteArrayRegion(env, found_key, 0, found_len, found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_key;
}

jbyteArray
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_lang_String_2_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jstring    filter_min,
    jbyteArray filter_max,
    jint       filter_max_len,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const char *filter_min_data = NULL;
    jsize       filter_min_len = 0;
    if (filter_min) {
        filter_min_data = (*env)->GetStringUTFChars(env, filter_min, NULL);
        filter_min_len = (*env)->GetStringUTFLength(env, filter_min);
    }

    jbyte *filter_max_data =
        filter_max ? (*env)->GetByteArrayElements(env, filter_max, NULL) : NULL;

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseStringUTFChars(env, filter_min, filter_min_data);
    if (filter_max)
        (*env)->ReleaseByteArrayElements(env, filter_max, filter_max_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (!found)
        return NULL;

    jbyteArray found_key = (*env)->NewByteArray(env, found_len);
    (*env)->SetByteArrayRegion(env, found_key, 0, found_len, found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_key;
}

jbyteArray
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_lang_String_2Ljava_lang_String_2I(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle,
    jstring filter_min,
    jstring filter_max,
    jint    flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const char *filter_min_data = NULL;
    jsize       filter_min_len = 0;
    if (filter_min) {
        filter_min_data = (*env)->GetStringUTFChars(env, filter_min, NULL);
        filter_min_len = (*env)->GetStringUTFLength(env, filter_min);
    }

    const char *filter_max_data = NULL;
    jsize       filter_max_len = 0;
    if (filter_max) {
        filter_max_data = (*env)->GetStringUTFChars(env, filter_max, NULL);
        filter_max_len = (*env)->GetStringUTFLength(env, filter_max);
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseStringUTFChars(env, filter_min, filter_min_data);
    if (filter_max)
        (*env)->ReleaseStringUTFChars(env, filter_max, filter_max_data);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (!found)
        return NULL;

    jbyteArray found_key = (*env)->NewByteArray(env, found_len);
    (*env)->SetByteArrayRegion(env, found_key, 0, found_len, found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_key;
}

jbyteArray
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_lang_String_2Ljava_nio_ByteBuffer_2III(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle,
    jstring filter_min,
    jobject filter_max,
    jint    filter_max_len,
    jint    filter_max_pos,
    jint    flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const char *filter_min_data = NULL;
    jsize       filter_min_len = 0;
    if (filter_min) {
        filter_min_data = (*env)->GetStringUTFChars(env, filter_min, NULL);
        filter_min_len = (*env)->GetStringUTFLength(env, filter_min);
    }

    const void *filter_max_data = NULL;
    if (filter_max) {
        filter_max_data = (*env)->GetDirectBufferAddress(env, filter_max);
        filter_max_data = (uint8_t *)filter_max_data + filter_max_pos;
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (!found)
        return NULL;

    jbyteArray found_key = (*env)->NewByteArray(env, found_len);
    (*env)->SetByteArrayRegion(env, found_key, 0, found_len, found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_key;
}

jbyteArray
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_nio_ByteBuffer_2II_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jobject    filter_min,
    jint       filter_min_len,
    jint       filter_min_pos,
    jbyteArray filter_max,
    jint       filter_max_len,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const void *filter_min_data = NULL;
    if (filter_min) {
        filter_min_data = (*env)->GetDirectBufferAddress(env, filter_min);

        // Move the start address based on the position
        filter_min_data = (uint8_t *)filter_min_data + filter_min_pos;
    }

    jbyte *filter_max_data =
        filter_max ? (*env)->GetByteArrayElements(env, filter_max, NULL) : NULL;

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_max)
        (*env)->ReleaseByteArrayElements(env, filter_max, filter_max_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (!found)
        return NULL;

    jbyteArray found_key = (*env)->NewByteArray(env, found_len);
    (*env)->SetByteArrayRegion(env, found_key, 0, found_len, found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_key;
}

jbyteArray
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_nio_ByteBuffer_2IILjava_lang_String_2I(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle,
    jobject filter_min,
    jint    filter_min_len,
    jint    filter_min_pos,
    jstring filter_max,
    jint    flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const void *filter_min_data = NULL;
    if (filter_min) {
        filter_min_data = (*env)->GetDirectBufferAddress(env, filter_min);

        // Move the start address based on the position
        filter_min_data = (uint8_t *)filter_min_data + filter_min_pos;
    }

    const void *filter_max_data = NULL;
    jsize       filter_max_len = 0;
    if (filter_max) {
        filter_max_data = (*env)->GetStringUTFChars(env, filter_max, NULL);
        filter_max_len = (*env)->GetStringUTFLength(env, filter_max);
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (!found)
        return NULL;

    jbyteArray found_key = (*env)->NewByteArray(env, found_len);
    (*env)->SetByteArrayRegion(env, found_key, 0, found_len, found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_key;
}

jbyteArray
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_nio_ByteBuffer_2IILjava_nio_ByteBuffer_2III(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle,
    jobject filter_min,
    jint    filter_min_len,
    jint    filter_min_pos,
    jobject filter_max,
    jint    filter_max_len,
    jint    filter_max_pos,
    jint    flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const void *filter_min_data = NULL;
    if (filter_min) {
        filter_min_data = (*env)->GetDirectBufferAddress(env, filter_min);

        // Move the start address based on the position
        filter_min_data = (uint8_t *)filter_min_data + filter_min_pos;
    }

    const void *filter_max_data = NULL;
    if (filter_max) {
        filter_max_data = (*env)->GetDirectBufferAddress(env, filter_max);

        // Move the start address based on the position
        filter_max_data = (uint8_t *)filter_max_data + filter_max_pos;
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (err) {
        throw_new_hse_exception(env, err);
        return NULL;
    }

    if (!found)
        return NULL;

    jbyteArray found_key = (*env)->NewByteArray(env, found_len);
    (*env)->SetByteArrayRegion(env, found_key, 0, found_len, found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_key;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__J_3BI_3BI_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jbyteArray filter_min,
    jint       filter_min_len,
    jbyteArray filter_max,
    jint       filter_max_len,
    jbyteArray found_buf,
    jint       found_buf_sz,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    jbyte *filter_min_data =
        filter_min ? (*env)->GetByteArrayElements(env, filter_min, NULL) : NULL;
    jbyte *filter_max_data =
        filter_min ? (*env)->GetByteArrayElements(env, filter_max, NULL) : NULL;

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseByteArrayElements(env, filter_min, filter_min_data, JNI_ABORT);
    if (filter_max)
        (*env)->ReleaseByteArrayElements(env, filter_max, filter_max_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    (*env)->SetByteArrayRegion(env, found_buf, 0, MIN(found_buf_sz, (jint)found_len), found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__J_3BI_3BILjava_nio_ByteBuffer_2III(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jbyteArray filter_min,
    jint       filter_min_len,
    jbyteArray filter_max,
    jint       filter_max_len,
    jobject    found_buf,
    jint       found_buf_sz,
    jint       found_buf_pos,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    jbyte *filter_min_data =
        filter_min ? (*env)->GetByteArrayElements(env, filter_min, NULL) : NULL;
    jbyte *filter_max_data =
        filter_min ? (*env)->GetByteArrayElements(env, filter_max, NULL) : NULL;

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseByteArrayElements(env, filter_min, filter_min_data, JNI_ABORT);
    if (filter_max)
        (*env)->ReleaseByteArrayElements(env, filter_max, filter_max_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    if (found_buf) {
        void *found_buf_data = (*env)->GetDirectBufferAddress(env, found_buf);

        found_buf_data = (uint8_t *)found_buf_data + found_buf_pos;
        const size_t copy_len = MIN(found_buf_sz, (jint)found_len);

        memcpy(found_buf_data, found, copy_len);
    }

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__J_3BILjava_lang_String_2_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jbyteArray filter_min,
    jint       filter_min_len,
    jstring    filter_max,
    jbyteArray found_buf,
    jint       found_buf_sz,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    jbyte *filter_min_data =
        filter_min ? (*env)->GetByteArrayElements(env, filter_min, NULL) : NULL;
    const char *filter_max_data = NULL;
    jsize       filter_max_len = 0;
    if (filter_max) {
        filter_max_data = (*env)->GetStringUTFChars(env, filter_max, NULL);
        filter_max_len = (*env)->GetStringUTFLength(env, filter_max);
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseByteArrayElements(env, filter_min, filter_min_data, JNI_ABORT);
    if (filter_max)
        (*env)->ReleaseStringUTFChars(env, filter_max, filter_max_data);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    (*env)->SetByteArrayRegion(env, found_buf, 0, MIN(found_buf_sz, (jint)found_len), found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__J_3BILjava_lang_String_2Ljava_nio_ByteBuffer_2III(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jbyteArray filter_min,
    jint       filter_min_len,
    jstring    filter_max,
    jobject    found_buf,
    jint       found_buf_sz,
    jint       found_buf_pos,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    jbyte *filter_min_data =
        filter_min ? (*env)->GetByteArrayElements(env, filter_min, NULL) : NULL;
    const char *filter_max_data = NULL;
    jsize       filter_max_len = 0;
    if (filter_max) {
        filter_max_data = (*env)->GetStringUTFChars(env, filter_max, NULL);
        filter_max_len = (*env)->GetStringUTFLength(env, filter_max);
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseByteArrayElements(env, filter_min, filter_min_data, JNI_ABORT);
    if (filter_max)
        (*env)->ReleaseStringUTFChars(env, filter_max, filter_max_data);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    if (found_buf) {
        void *found_buf_data = (*env)->GetDirectBufferAddress(env, found_buf);

        found_buf_data = (uint8_t *)found_buf_data + found_buf_pos;
        const size_t copy_len = MIN(found_buf_sz, (jint)found_len);

        memcpy(found_buf_data, found, copy_len);
    }

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__J_3BILjava_nio_ByteBuffer_2II_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jbyteArray filter_min,
    jint       filter_min_len,
    jobject    filter_max,
    jint       filter_max_len,
    jint       filter_max_pos,
    jbyteArray found_buf,
    jint       found_buf_sz,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    jbyte *filter_min_data =
        filter_min ? (*env)->GetByteArrayElements(env, filter_min, NULL) : NULL;

    const void *filter_max_data = NULL;
    if (filter_max) {
        filter_max_data = (*env)->GetDirectBufferAddress(env, filter_max);

        // Move the start address based on the position
        filter_max_data = (uint8_t *)filter_max_data + filter_max_pos;
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseByteArrayElements(env, filter_min, filter_min_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    (*env)->SetByteArrayRegion(env, found_buf, 0, MIN(found_buf_sz, (jint)found_len), found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__J_3BILjava_nio_ByteBuffer_2IILjava_nio_ByteBuffer_2III(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jbyteArray filter_min,
    jint       filter_min_len,
    jobject    filter_max,
    jint       filter_max_len,
    jint       filter_max_pos,
    jobject    found_buf,
    jint       found_buf_sz,
    jint       found_buf_pos,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    jbyte *filter_min_data =
        filter_min ? (*env)->GetByteArrayElements(env, filter_min, NULL) : NULL;

    const void *filter_max_data = NULL;
    if (filter_max) {
        filter_max_data = (*env)->GetDirectBufferAddress(env, filter_max);

        // Move the start address based on the position
        filter_max_data = (uint8_t *)filter_max_data + filter_max_pos;
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseByteArrayElements(env, filter_min, filter_min_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    if (found_buf) {
        void *found_buf_data = (*env)->GetDirectBufferAddress(env, found_buf);

        found_buf_data = (uint8_t *)found_buf_data + found_buf_pos;
        const size_t copy_len = MIN((size_t)found_buf_sz, found_len);

        memcpy(found_buf_data, found, copy_len);
    }

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_lang_String_2_3BI_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jstring    filter_min,
    jbyteArray filter_max,
    jint       filter_max_len,
    jbyteArray found_buf,
    jint       found_buf_sz,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const char *filter_min_data = NULL;
    jsize       filter_min_len = 0;
    if (filter_min) {
        filter_min_data = (*env)->GetStringUTFChars(env, filter_min, NULL);
        filter_min_len = (*env)->GetStringUTFLength(env, filter_min);
    }

    jbyte *filter_max_data =
        filter_max ? (*env)->GetByteArrayElements(env, filter_max, NULL) : NULL;

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseStringUTFChars(env, filter_min, NULL);
    if (filter_max)
        (*env)->ReleaseByteArrayElements(env, filter_max, filter_max_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    (*env)->SetByteArrayRegion(env, found_buf, 0, MIN(found_buf_sz, (jint)found_len), found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_lang_String_2_3BILjava_nio_ByteBuffer_2III(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jstring    filter_min,
    jbyteArray filter_max,
    jint       filter_max_len,
    jobject    found_buf,
    jint       found_buf_sz,
    jint       found_buf_pos,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const char *filter_min_data = NULL;
    jsize       filter_min_len = 0;
    if (filter_min) {
        filter_min_data = (*env)->GetStringUTFChars(env, filter_min, NULL);
        filter_min_len = (*env)->GetStringUTFLength(env, filter_min);
    }

    jbyte *filter_max_data =
        filter_max ? (*env)->GetByteArrayElements(env, filter_max, NULL) : NULL;

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseStringUTFChars(env, filter_min, NULL);
    if (filter_max)
        (*env)->ReleaseByteArrayElements(env, filter_max, filter_max_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    if (found_buf) {
        void *found_buf_data = (*env)->GetDirectBufferAddress(env, found_buf);

        found_buf_data = (uint8_t *)found_buf_data + found_buf_pos;
        const size_t copy_len = MIN((size_t)found_buf_sz, found_len);

        memcpy(found_buf_data, found, copy_len);
    }

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_lang_String_2Ljava_lang_String_2_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jstring    filter_min,
    jstring    filter_max,
    jbyteArray found_buf,
    jint       found_buf_sz,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const char *filter_min_data = NULL;
    jsize       filter_min_len = 0;
    if (filter_min) {
        filter_min_data = (*env)->GetStringUTFChars(env, filter_min, NULL);
        filter_min_len = (*env)->GetStringUTFLength(env, filter_min);
    }

    const char *filter_max_data = NULL;
    jsize       filter_max_len = 0;
    if (filter_max) {
        filter_max_data = (*env)->GetStringUTFChars(env, filter_max, NULL);
        filter_max_len = (*env)->GetStringUTFLength(env, filter_max);
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseStringUTFChars(env, filter_min, NULL);
    if (filter_max)
        (*env)->ReleaseStringUTFChars(env, filter_max, NULL);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    (*env)->SetByteArrayRegion(env, found_buf, 0, MIN(found_buf_sz, (jint)found_len), found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_lang_String_2Ljava_lang_String_2Ljava_nio_ByteBuffer_2III(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle,
    jstring filter_min,
    jstring filter_max,
    jobject found_buf,
    jint    found_buf_sz,
    jint    found_buf_pos,
    jint    flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const char *filter_min_data = NULL;
    jsize       filter_min_len = 0;
    if (filter_min) {
        filter_min_data = (*env)->GetStringUTFChars(env, filter_min, NULL);
        filter_min_len = (*env)->GetStringUTFLength(env, filter_min);
    }

    const char *filter_max_data = NULL;
    jsize       filter_max_len = 0;
    if (filter_max) {
        filter_max_data = (*env)->GetStringUTFChars(env, filter_max, NULL);
        filter_max_len = (*env)->GetStringUTFLength(env, filter_max);
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseStringUTFChars(env, filter_min, NULL);
    if (filter_max)
        (*env)->ReleaseStringUTFChars(env, filter_max, NULL);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    if (found_buf) {
        void *found_buf_data = (*env)->GetDirectBufferAddress(env, found_buf);

        found_buf_data = (uint8_t *)found_buf_data + found_buf_pos;
        const size_t copy_len = MIN((size_t)found_buf_sz, found_len);

        memcpy(found_buf_data, found, copy_len);
    }

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_lang_String_2Ljava_nio_ByteBuffer_2II_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jstring    filter_min,
    jobject    filter_max,
    jint       filter_max_len,
    jint       filter_max_pos,
    jbyteArray found_buf,
    jint       found_buf_sz,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const char *filter_min_data = NULL;
    jsize       filter_min_len = 0;
    if (filter_min) {
        filter_min_data = (*env)->GetStringUTFChars(env, filter_min, NULL);
        filter_min_len = (*env)->GetStringUTFLength(env, filter_min);
    }

    const void *filter_max_data = NULL;
    if (filter_max) {
        filter_max_data = (*env)->GetDirectBufferAddress(env, filter_max);

        // Move the start address based on the position
        filter_max_data = (uint8_t *)filter_max_data + filter_max_pos;
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseStringUTFChars(env, filter_min, NULL);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    (*env)->SetByteArrayRegion(env, found_buf, 0, MIN(found_buf_sz, (jint)found_len), found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_lang_String_2Ljava_nio_ByteBuffer_2IILjava_nio_ByteBuffer_2III(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle,
    jstring filter_min,
    jobject filter_max,
    jint    filter_max_len,
    jint    filter_max_pos,
    jobject found_buf,
    jint    found_buf_sz,
    jint    found_buf_pos,
    jint    flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const char *filter_min_data = NULL;
    jsize       filter_min_len = 0;
    if (filter_min) {
        filter_min_data = (*env)->GetStringUTFChars(env, filter_min, NULL);
        filter_min_len = (*env)->GetStringUTFLength(env, filter_min);
    }

    const void *filter_max_data = NULL;
    if (filter_max) {
        filter_max_data = (*env)->GetDirectBufferAddress(env, filter_max);

        // Move the start address based on the position
        filter_max_data = (uint8_t *)filter_max_data + filter_max_pos;
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_min)
        (*env)->ReleaseStringUTFChars(env, filter_min, NULL);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    if (found_buf) {
        void *found_buf_data = (*env)->GetDirectBufferAddress(env, found_buf);

        found_buf_data = (uint8_t *)found_buf_data + found_buf_pos;
        const size_t copy_len = MIN((size_t)found_buf_sz, found_len);

        memcpy(found_buf_data, found, copy_len);
    }

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_nio_ByteBuffer_2II_3BI_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jobject    filter_min,
    jint       filter_min_len,
    jint       filter_min_pos,
    jbyteArray filter_max,
    jint       filter_max_len,
    jbyteArray found_buf,
    jint       found_buf_sz,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const void *filter_min_data = NULL;
    if (filter_min) {
        filter_min_data = (*env)->GetDirectBufferAddress(env, filter_min);

        // Move the start address based on the position
        filter_min_data = (uint8_t *)filter_min_data + filter_min_pos;
    }

    jbyte *filter_max_data =
        filter_min ? (*env)->GetByteArrayElements(env, filter_max, NULL) : NULL;

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_max)
        (*env)->ReleaseByteArrayElements(env, filter_max, filter_max_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    (*env)->SetByteArrayRegion(env, found_buf, 0, MIN(found_buf_sz, (jint)found_len), found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_nio_ByteBuffer_2II_3BILjava_nio_ByteBuffer_2III(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jobject    filter_min,
    jint       filter_min_len,
    jint       filter_min_pos,
    jbyteArray filter_max,
    jint       filter_max_len,
    jobject    found_buf,
    jint       found_buf_sz,
    jint       found_buf_pos,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const void *filter_min_data = NULL;
    if (filter_min) {
        filter_min_data = (*env)->GetDirectBufferAddress(env, filter_min);

        // Move the start address based on the position
        filter_min_data = (uint8_t *)filter_min_data + filter_min_pos;
    }

    jbyte *filter_max_data =
        filter_min ? (*env)->GetByteArrayElements(env, filter_max, NULL) : NULL;

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_max)
        (*env)->ReleaseByteArrayElements(env, filter_max, filter_max_data, JNI_ABORT);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    if (found_buf) {
        void *found_buf_data = (*env)->GetDirectBufferAddress(env, found_buf);

        found_buf_data = (uint8_t *)found_buf_data + found_buf_pos;
        const size_t copy_len = MIN((size_t)found_buf_sz, found_len);

        memcpy(found_buf_data, found, copy_len);
    }

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_nio_ByteBuffer_2IILjava_lang_String_2_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jobject    filter_min,
    jint       filter_min_len,
    jint       filter_min_pos,
    jstring    filter_max,
    jbyteArray found_buf,
    jint       found_buf_sz,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const void *filter_min_data = NULL;
    if (filter_min) {
        filter_min_data = (*env)->GetDirectBufferAddress(env, filter_min);

        // Move the start address based on the position
        filter_min_data = (uint8_t *)filter_min_data + filter_min_pos;
    }

    const char *filter_max_data = NULL;
    jsize       filter_max_len = 0;
    if (filter_max) {
        filter_max_data = (*env)->GetStringUTFChars(env, filter_max, NULL);
        filter_max_len = (*env)->GetStringUTFLength(env, filter_max);
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_max)
        (*env)->ReleaseStringUTFChars(env, filter_max, filter_max_data);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    (*env)->SetByteArrayRegion(env, found_buf, 0, MIN(found_buf_sz, (jint)found_len), found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_nio_ByteBuffer_2IILjava_lang_String_2Ljava_nio_ByteBuffer_2III(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle,
    jobject filter_min,
    jint    filter_min_len,
    jint    filter_min_pos,
    jstring filter_max,
    jobject found_buf,
    jint    found_buf_sz,
    jint    found_buf_pos,
    jint    flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const void *filter_min_data = NULL;
    if (filter_min) {
        filter_min_data = (*env)->GetDirectBufferAddress(env, filter_min);

        // Move the start address based on the position
        filter_min_data = (uint8_t *)filter_min_data + filter_min_pos;
    }

    const char *filter_max_data = NULL;
    jsize       filter_max_len = 0;
    if (filter_max) {
        filter_max_data = (*env)->GetStringUTFChars(env, filter_max, NULL);
        filter_max_len = (*env)->GetStringUTFLength(env, filter_max);
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (filter_max)
        (*env)->ReleaseStringUTFChars(env, filter_max, filter_max_data);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    if (found_buf) {
        void *found_buf_data = (*env)->GetDirectBufferAddress(env, found_buf);

        found_buf_data = (uint8_t *)found_buf_data + found_buf_pos;
        const size_t copy_len = MIN((size_t)found_buf_sz, found_len);

        memcpy(found_buf_data, found, copy_len);
    }

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_nio_ByteBuffer_2IILjava_nio_ByteBuffer_2II_3BII(
    JNIEnv    *env,
    jobject    cursor_obj,
    jlong      cursor_handle,
    jobject    filter_min,
    jint       filter_min_len,
    jint       filter_min_pos,
    jobject    filter_max,
    jint       filter_max_len,
    jint       filter_max_pos,
    jbyteArray found_buf,
    jint       found_buf_sz,
    jint       flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const void *filter_min_data = NULL;
    if (filter_min) {
        filter_min_data = (*env)->GetDirectBufferAddress(env, filter_min);

        // Move the start address based on the position
        filter_min_data = (uint8_t *)filter_min_data + filter_min_pos;
    }

    const void *filter_max_data = NULL;
    if (filter_max) {
        filter_max_data = (*env)->GetDirectBufferAddress(env, filter_max);

        // Move the start address based on the position
        filter_max_data = (uint8_t *)filter_max_data + filter_max_pos;
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    (*env)->SetByteArrayRegion(env, found_buf, 0, MIN(found_buf_sz, (jint)found_len), found);

    /* Assert that no ArrayIndexOutOfBoundsException occurred. The byte array is
     * constructed with a size of found_len, so the previous operation is
     * infallible in theory.
     */
    assert(!(*env)->ExceptionCheck(env));

    return found_len;
}

jint
Java_com_micron_hse_1project_hse_KvsCursor_seekRange__JLjava_nio_ByteBuffer_2IILjava_nio_ByteBuffer_2IILjava_nio_ByteBuffer_2III(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle,
    jobject filter_min,
    jint    filter_min_len,
    jint    filter_min_pos,
    jobject filter_max,
    jint    filter_max_pos,
    jint    filter_max_len,
    jobject found_buf,
    jint    found_buf_sz,
    jint    found_buf_pos,
    jint    flags)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const void *filter_min_data = NULL;
    if (filter_min) {
        filter_min_data = (*env)->GetDirectBufferAddress(env, filter_min);

        // Move the start address based on the position
        filter_min_data = (uint8_t *)filter_min_data + filter_min_pos;
    }

    const void *filter_max_data = NULL;
    if (filter_max) {
        filter_max_data = (*env)->GetDirectBufferAddress(env, filter_max);

        // Move the start address based on the position
        filter_max_data = (uint8_t *)filter_max_data + filter_max_pos;
    }

    const void     *found;
    size_t          found_len;
    const hse_err_t err = hse_kvs_cursor_seek_range(
        cursor,
        flags,
        filter_min_data,
        filter_min_len,
        filter_max_data,
        filter_max_len,
        &found,
        &found_len);

    if (err) {
        throw_new_hse_exception(env, err);
        return 0;
    }

    if (!found)
        return 0;

    if (found_buf) {
        void *found_buf_data = (*env)->GetDirectBufferAddress(env, found_buf);

        found_buf_data = (uint8_t *)found_buf_data + found_buf_pos;
        const size_t copy_len = MIN((size_t)found_buf_sz, found_len);

        memcpy(found_buf_data, found, copy_len);
    }

    return found_len;
}

void
Java_com_micron_hse_1project_hse_KvsCursor_updateView(
    JNIEnv *env,
    jobject cursor_obj,
    jlong   cursor_handle)
{
    (void)cursor_obj;

    struct hse_kvs_cursor *cursor = (struct hse_kvs_cursor *)cursor_handle;

    const hse_err_t err = hse_kvs_cursor_update_view(cursor, 0);

    if (err)
        throw_new_hse_exception(env, err);
}
