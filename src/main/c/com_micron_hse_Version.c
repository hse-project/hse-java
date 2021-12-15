/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

#include <hse/version.h>
#include <jni.h>

#include "com_micron_hse_Version.h"

jint
Java_com_micron_hse_Version_major(JNIEnv *env, jclass version_cls)
{
    (void)env;
    (void)version_cls;

    return HSE_VERSION_MAJOR;
}

jint
Java_com_micron_hse_Version_minor(JNIEnv *env, jclass version_cls)
{
    (void)env;
    (void)version_cls;

    return HSE_VERSION_MINOR;
}

jint
Java_com_micron_hse_Version_patch(JNIEnv *env, jclass version_cls)
{
    (void)env;
    (void)version_cls;

    return HSE_VERSION_PATCH;
}

jstring
Java_com_micron_hse_Version_string(JNIEnv *env, jclass version_cls)
{
    (void)env;
    (void)version_cls;

    return (*env)->NewStringUTF(env, HSE_VERSION_STRING);
}
