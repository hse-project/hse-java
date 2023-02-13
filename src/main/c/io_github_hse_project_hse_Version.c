/* SPDX-License-Identifier: Apache-2.0 OR MIT
 *
 * SPDX-FileCopyrightText: Copyright 2021 Micron Technology, Inc.
 */

#include <jni.h>

#include <hse/version.h>

#include "io_github_hse_project_hse_Version.h"

jint
Java_io_github_hse_1project_hse_Version_major(JNIEnv *env, jclass version_cls)
{
    (void)env;
    (void)version_cls;

    return HSE_VERSION_MAJOR;
}

jint
Java_io_github_hse_1project_hse_Version_minor(JNIEnv *env, jclass version_cls)
{
    (void)env;
    (void)version_cls;

    return HSE_VERSION_MINOR;
}

jint
Java_io_github_hse_1project_hse_Version_patch(JNIEnv *env, jclass version_cls)
{
    (void)env;
    (void)version_cls;

    return HSE_VERSION_PATCH;
}

jstring
Java_io_github_hse_1project_hse_Version_string(JNIEnv *env, jclass version_cls)
{
    (void)env;
    (void)version_cls;

    return (*env)->NewStringUTF(env, HSE_VERSION_STRING);
}
