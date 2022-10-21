/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

#include <assert.h>
#include <stdlib.h>

#include <hse/hse.h>
#include <jni.h>

#include "hsejni.h"

#define HSE_JNI_VERSION JNI_VERSION_1_8

struct globals globals;

void
to_paramv(JNIEnv *env, jobjectArray params, jsize *paramc, const char ***paramv)
{
    jsize        tmp_pc;
    const char **tmp_pv = NULL;

    assert(env);
    assert(paramc);
    assert(paramv);

    *paramc = 0;
    *paramv = NULL;

    if (!params)
        return;

    tmp_pc = (*env)->GetArrayLength(env, params);

    if (tmp_pc == 0)
        return;

    tmp_pv = malloc(tmp_pc * sizeof(char *));
    if (!tmp_pv) {
        (*env)->ThrowNew(
            env,
            globals.java.lang.OutOfMemoryError.class,
            "Failed to allocate memory for C-string paramater array");
        return;
    }

    for (jsize i = 0; i < tmp_pc; i++) {
        const jobject param = (*env)->GetObjectArrayElement(env, params, i);

        tmp_pv[i] = (*env)->GetStringUTFChars(env, param, NULL);
    }

    *paramc = tmp_pc;
    *paramv = tmp_pv;
}

void
free_paramv(JNIEnv *env, jobjectArray params, jsize paramc, const char **paramv)
{
    assert(env);

    for (jsize i = 0; i < paramc; i++) {
        const jobject param = (*env)->GetObjectArrayElement(env, params, i);

        (*env)->ReleaseStringUTFChars(env, param, paramv[i]);
    }

    free(paramv);
}

jint
throw_new_hse_exception(JNIEnv *env, hse_err_t err)
{
    assert(env);
    assert(err);

    const size_t needed_sz = hse_strerror(err, NULL, 0);
    char        *buf = malloc(needed_sz + 1);
    if (!buf)
        return (*env)->ThrowNew(
            env,
            globals.java.lang.OutOfMemoryError.class,
            "Failed to allocate memory for error buffer");

    hse_strerror(err, buf, needed_sz + 1);

    const jstring message = (*env)->NewStringUTF(env, buf);
    free(buf);
    if ((*env)->ExceptionCheck(env))
        return JNI_ERR;

    const jint             rc = hse_err_to_errno(err);
    const enum hse_err_ctx ctx = hse_err_to_ctx(err);

    jobject context_obj = NULL;
    switch (ctx) {
        case HSE_ERR_CTX_NONE:
            context_obj = globals.com.micron.hse_project.hse.HseException.Context.NONE;
            break;
        case HSE_ERR_CTX_TXN_EXPIRED:
            context_obj = globals.com.micron.hse_project.hse.HseException.Context.TXN_EXPIRED;
    }

    assert(context_obj);

    const jobject hse_exception_obj = (*env)->NewObject(
        env,
        globals.com.micron.hse_project.hse.HseException.class,
        globals.com.micron.hse_project.hse.HseException.init,
        message,
        rc,
        context_obj);
    if ((*env)->ExceptionCheck(env))
        return JNI_ERR;

    return (*env)->Throw(env, (jthrowable)hse_exception_obj);
}

/* If any exceptions are generated in the JNI_OnLoad() function, it is
 * programmer error.
 */
#define ASSERT_NO_EXCEPTION() (assert(!(*env)->ExceptionCheck(env)))

/* In the event, the ref is NULL, the system is out of memory. Grecefully push
 * the error back up to the JVM. If we run out of memory part of the way through
 * JNI_OnLoad(), just leak it. By calling JNIEnv::NewGlobalRef(), we are keeping
 * the various references alive for the entire process thus saving multiple
 * lookups. Note that the JVM most likely caches the class references in some
 * fashion, but let's save the function call overhead at the expense of a little
 * memory for allocating the global reference.
 *
 * https://en.wikipedia.org/wiki/Ostrich_algorithm
 */
#define ERROR_IF_REF_IS_NULL() \
    do {                       \
        if (!local)            \
            return JNI_ERR;    \
    } while (0)

jint
JNI_OnLoad(JavaVM *vm, void *reserved)
{
    (void)reserved;

    jint    rc;
    JNIEnv *env;
    void   *local;

    rc = (*vm)->GetEnv(vm, (void **)&env, HSE_JNI_VERSION);
    if (rc)
        return rc;

    local = (*env)->FindClass(env, "com/micron/hse_project/hse/HseException");
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.HseException.class = (*env)->NewGlobalRef(env, local);
    ERROR_IF_REF_IS_NULL();
    globals.com.micron.hse_project.hse.HseException.init = (*env)->GetMethodID(
        env,
        globals.com.micron.hse_project.hse.HseException.class,
        "<init>",
        "(Ljava/lang/String;ILcom/micron/hse_project/hse/HseException$Context;)V");
    ASSERT_NO_EXCEPTION();

    local = (*env)->FindClass(env, "com/micron/hse_project/hse/HseException$Context");
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.HseException.Context.class =
        (*env)->NewGlobalRef(env, local);
    ERROR_IF_REF_IS_NULL();

    const jfieldID none_field = (*env)->GetStaticFieldID(
        env,
        globals.com.micron.hse_project.hse.HseException.Context.class,
        "NONE",
        "Lcom/micron/hse_project/hse/HseException$Context;");
    ASSERT_NO_EXCEPTION();
    local = (*env)->GetStaticObjectField(
        env, globals.com.micron.hse_project.hse.HseException.Context.class, none_field);
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.HseException.Context.NONE = (*env)->NewGlobalRef(env, local);
    ERROR_IF_REF_IS_NULL();

    const jfieldID txn_expired_field = (*env)->GetStaticFieldID(
        env,
        globals.com.micron.hse_project.hse.HseException.Context.class,
        "TXN_EXPIRED",
        "Lcom/micron/hse_project/hse/HseException$Context;");
    ASSERT_NO_EXCEPTION();
    local = (*env)->GetStaticObjectField(
        env, globals.com.micron.hse_project.hse.HseException.Context.class, txn_expired_field);
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.HseException.Context.TXN_EXPIRED =
        (*env)->NewGlobalRef(env, local);
    ERROR_IF_REF_IS_NULL();

    local = (*env)->FindClass(env, "com/micron/hse_project/hse/Kvdb$CompactStatus");
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.Kvdb.CompactStatus.class = (*env)->NewGlobalRef(env, local);
    ERROR_IF_REF_IS_NULL();
    globals.com.micron.hse_project.hse.Kvdb.CompactStatus.sampLwm = (*env)->GetFieldID(
        env, globals.com.micron.hse_project.hse.Kvdb.CompactStatus.class, "sampLwm", "I");
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.Kvdb.CompactStatus.sampHwm = (*env)->GetFieldID(
        env, globals.com.micron.hse_project.hse.Kvdb.CompactStatus.class, "sampHwm", "I");
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.Kvdb.CompactStatus.sampCurr = (*env)->GetFieldID(
        env, globals.com.micron.hse_project.hse.Kvdb.CompactStatus.class, "sampCurr", "I");
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.Kvdb.CompactStatus.active = (*env)->GetFieldID(
        env, globals.com.micron.hse_project.hse.Kvdb.CompactStatus.class, "active", "Z");
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.Kvdb.CompactStatus.canceled = (*env)->GetFieldID(
        env, globals.com.micron.hse_project.hse.Kvdb.CompactStatus.class, "canceled", "Z");
    ASSERT_NO_EXCEPTION();

    local = (*env)->FindClass(env, "com/micron/hse_project/hse/KvdbTransaction$State");
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.KvdbTransaction.State.class =
        (*env)->NewGlobalRef(env, local);
    ERROR_IF_REF_IS_NULL();

    const jfieldID aborted_field = (*env)->GetStaticFieldID(
        env,
        globals.com.micron.hse_project.hse.KvdbTransaction.State.class,
        "ABORTED",
        "Lcom/micron/hse_project/hse/KvdbTransaction$State;");
    ASSERT_NO_EXCEPTION();
    local = (*env)->GetStaticObjectField(
        env, globals.com.micron.hse_project.hse.KvdbTransaction.State.class, aborted_field);
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.KvdbTransaction.State.ABORTED =
        (*env)->NewGlobalRef(env, local);
    ;
    ERROR_IF_REF_IS_NULL();

    const jfieldID active_field = (*env)->GetStaticFieldID(
        env,
        globals.com.micron.hse_project.hse.KvdbTransaction.State.class,
        "ACTIVE",
        "Lcom/micron/hse_project/hse/KvdbTransaction$State;");
    ASSERT_NO_EXCEPTION();
    local = (*env)->GetStaticObjectField(
        env, globals.com.micron.hse_project.hse.KvdbTransaction.State.class, active_field);
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.KvdbTransaction.State.ACTIVE =
        (*env)->NewGlobalRef(env, local);
    ;
    ERROR_IF_REF_IS_NULL();

    const jfieldID committed_field = (*env)->GetStaticFieldID(
        env,
        globals.com.micron.hse_project.hse.KvdbTransaction.State.class,
        "COMMITTED",
        "Lcom/micron/hse_project/hse/KvdbTransaction$State;");
    ASSERT_NO_EXCEPTION();
    local = (*env)->GetStaticObjectField(
        env, globals.com.micron.hse_project.hse.KvdbTransaction.State.class, committed_field);
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.KvdbTransaction.State.COMMITTED =
        (*env)->NewGlobalRef(env, local);
    ;
    ERROR_IF_REF_IS_NULL();

    const jfieldID invalid_field = (*env)->GetStaticFieldID(
        env,
        globals.com.micron.hse_project.hse.KvdbTransaction.State.class,
        "INVALID",
        "Lcom/micron/hse_project/hse/KvdbTransaction$State;");
    ASSERT_NO_EXCEPTION();
    local = (*env)->GetStaticObjectField(
        env, globals.com.micron.hse_project.hse.KvdbTransaction.State.class, invalid_field);
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.KvdbTransaction.State.INVALID =
        (*env)->NewGlobalRef(env, local);
    ;
    ERROR_IF_REF_IS_NULL();

    local = (*env)->FindClass(env, "com/micron/hse_project/hse/MclassInfo");
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.MclassInfo.class = (*env)->NewGlobalRef(env, local);
    ERROR_IF_REF_IS_NULL();
    globals.com.micron.hse_project.hse.MclassInfo.allocatedBytes = (*env)->GetFieldID(
        env, globals.com.micron.hse_project.hse.MclassInfo.class, "allocatedBytes", "J");
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.MclassInfo.usedBytes = (*env)->GetFieldID(
        env, globals.com.micron.hse_project.hse.MclassInfo.class, "usedBytes", "J");
    ASSERT_NO_EXCEPTION();
    globals.com.micron.hse_project.hse.MclassInfo.path = (*env)->GetFieldID(
        env, globals.com.micron.hse_project.hse.MclassInfo.class, "path", "Ljava/nio/file/Path;");
    ASSERT_NO_EXCEPTION();

    local = (*env)->FindClass(env, "java/io/EOFException");
    ASSERT_NO_EXCEPTION();
    globals.java.io.EOFException.class = (*env)->NewGlobalRef(env, local);
    ERROR_IF_REF_IS_NULL();

    local = (*env)->FindClass(env, "java/lang/Integer");
    ASSERT_NO_EXCEPTION();
    globals.java.lang.Integer.class = (*env)->NewGlobalRef(env, local);
    ERROR_IF_REF_IS_NULL();
    globals.java.lang.Integer.init =
        (*env)->GetMethodID(env, globals.java.lang.Integer.class, "<init>", "(I)V");
    ASSERT_NO_EXCEPTION();

    local = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
    ASSERT_NO_EXCEPTION();
    globals.java.lang.OutOfMemoryError.class = (*env)->NewGlobalRef(env, local);
    ERROR_IF_REF_IS_NULL();

    local = (*env)->FindClass(env, "java/lang/String");
    ASSERT_NO_EXCEPTION();
    globals.java.lang.String.class = (*env)->NewGlobalRef(env, local);
    ERROR_IF_REF_IS_NULL();

    local = (*env)->FindClass(env, "java/lang/UnsupportedOperationException");
    ASSERT_NO_EXCEPTION();
    globals.java.lang.UnsupportedOperationException.class = (*env)->NewGlobalRef(env, local);
    ERROR_IF_REF_IS_NULL();

    local = (*env)->FindClass(env, "java/nio/file/Paths");
    ASSERT_NO_EXCEPTION();
    globals.java.nio.file.Paths.class = (*env)->NewGlobalRef(env, local);
    ERROR_IF_REF_IS_NULL();
    globals.java.nio.file.Paths.get = (*env)->GetStaticMethodID(
        env,
        globals.java.nio.file.Paths.class,
        "get",
        "(Ljava/lang/String;[Ljava/lang/String;)Ljava/nio/file/Path;");
    ASSERT_NO_EXCEPTION();

    local = (*env)->FindClass(env, "java/util/AbstractMap$SimpleImmutableEntry");
    ASSERT_NO_EXCEPTION();
    globals.java.util.AbstractMap.SimpleImmutableEntry.class = (*env)->NewGlobalRef(env, local);
    ERROR_IF_REF_IS_NULL();
    globals.java.util.AbstractMap.SimpleImmutableEntry.init = (*env)->GetMethodID(
        env,
        globals.java.util.AbstractMap.SimpleImmutableEntry.class,
        "<init>",
        "(Ljava/lang/Object;Ljava/lang/Object;)V");
    ASSERT_NO_EXCEPTION();

    local = (*env)->FindClass(env, "java/util/Optional");
    ASSERT_NO_EXCEPTION();
    globals.java.util.Optional.class = (*env)->NewGlobalRef(env, local);
    ERROR_IF_REF_IS_NULL();
    globals.java.util.Optional.empty = (*env)->GetStaticMethodID(
        env, globals.java.util.Optional.class, "empty", "()Ljava/util/Optional;");
    ASSERT_NO_EXCEPTION();
    globals.java.util.Optional.of = (*env)->GetStaticMethodID(
        env, globals.java.util.Optional.class, "of", "(Ljava/lang/Object;)Ljava/util/Optional;");
    ASSERT_NO_EXCEPTION();
    globals.java.util.Optional.ofNullable = (*env)->GetStaticMethodID(
        env,
        globals.java.util.Optional.class,
        "ofNullable",
        "(Ljava/lang/Object;)Ljava/util/Optional;");
    ASSERT_NO_EXCEPTION();

    return HSE_JNI_VERSION;
}

void
JNI_OnUnload(JavaVM *vm, void *reserved)
{
    (void)reserved;

    jint    rc;
    JNIEnv *env;

    rc = (*vm)->GetEnv(vm, (void **)&env, HSE_JNI_VERSION);
    if (rc)
        return;

    (*env)->DeleteGlobalRef(env, globals.com.micron.hse_project.hse.HseException.class);
    (*env)->DeleteGlobalRef(env, globals.com.micron.hse_project.hse.HseException.Context.class);
    (*env)->DeleteGlobalRef(env, globals.com.micron.hse_project.hse.HseException.Context.NONE);
    (*env)->DeleteGlobalRef(
        env, globals.com.micron.hse_project.hse.HseException.Context.TXN_EXPIRED);
    (*env)->DeleteGlobalRef(env, globals.com.micron.hse_project.hse.Kvdb.CompactStatus.class);
    (*env)->DeleteGlobalRef(env, globals.com.micron.hse_project.hse.KvdbTransaction.State.class);
    (*env)->DeleteGlobalRef(env, globals.com.micron.hse_project.hse.KvdbTransaction.State.ABORTED);
    (*env)->DeleteGlobalRef(env, globals.com.micron.hse_project.hse.KvdbTransaction.State.ACTIVE);
    (*env)->DeleteGlobalRef(
        env, globals.com.micron.hse_project.hse.KvdbTransaction.State.COMMITTED);
    (*env)->DeleteGlobalRef(env, globals.com.micron.hse_project.hse.KvdbTransaction.State.INVALID);
    (*env)->DeleteGlobalRef(env, globals.com.micron.hse_project.hse.MclassInfo.class);
    (*env)->DeleteGlobalRef(env, globals.java.io.EOFException.class);
    (*env)->DeleteGlobalRef(env, globals.java.lang.Integer.class);
    (*env)->DeleteGlobalRef(env, globals.java.lang.UnsupportedOperationException.class);
    (*env)->DeleteGlobalRef(env, globals.java.lang.String.class);
    (*env)->DeleteGlobalRef(env, globals.java.lang.OutOfMemoryError.class);
    (*env)->DeleteGlobalRef(env, globals.java.nio.file.Paths.class);
    (*env)->DeleteGlobalRef(env, globals.java.util.AbstractMap.SimpleImmutableEntry.class);
    (*env)->DeleteGlobalRef(env, globals.java.util.Optional.class);
}
