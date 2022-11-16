/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

#include <stddef.h>
#include <stdlib.h>

#include <hse/hse.h>
#include <jni.h>

#include "io_github_hse_project_hse_Hse.h"
#include "hsejni.h"

jstring
Java_io_github_hse_1project_hse_Hse_cgetParam(JNIEnv *env, jclass hse_cls, jstring param)
{
    char *buf = NULL;
    hse_err_t err = 0;
    size_t needed_sz;
    jstring value = NULL;
    const char *param_chars = NULL;;

    (void)env;
    (void)hse_cls;

    if (param)
        param_chars = (*env)->GetStringUTFChars(env, param, NULL);

    err = hse_param_get(param_chars, NULL, 0, &needed_sz);
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
    if (param)
        (*env)->ReleaseStringUTFChars(env, param, param_chars);

    // Don't allocate a new string if an exception has been thrown.
    if (!(*env)->ExceptionCheck(env))
        value = (*env)->NewStringUTF(env, buf);

    free(buf);

    return value;
}

void
Java_io_github_hse_1project_hse_Hse_cinit(
    JNIEnv *env,
    jclass hse_cls,
    jstring config,
    jobjectArray params)
{
    jsize paramc;
    hse_err_t err;
    const char **paramv;
    const char *config_chars = NULL;

    (void)hse_cls;

    if (config)
        config_chars = (*env)->GetStringUTFChars(env, config, NULL);

    to_paramv(env, params, &paramc, &paramv);
    if ((*env)->ExceptionCheck(env))
        return;

    err = hse_init(config_chars, paramc, paramv);

    if (config)
        (*env)->ReleaseStringUTFChars(env, config, config_chars);

    free_paramv(env, params, paramc, paramv);

    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_io_github_hse_1project_hse_Hse_cfini(JNIEnv *env, jclass hse_cls)
{
    (void)env;
    (void)hse_cls;

    hse_fini();
}
