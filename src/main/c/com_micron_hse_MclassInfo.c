/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

#include <hse/hse.h>
#include <jni.h>

#include "hsejni.h"
#include "com_micron_hse_MclassInfo.h"

void
Java_com_micron_hse_MclassInfo_get(
    JNIEnv *env,
    jobject mclass_info_obj,
    jlong   kvdb_handle,
    jint    mclass)
{
    struct hse_mclass_info info;
    struct hse_kvdb       *kvdb = (struct hse_kvdb *)kvdb_handle;

    const hse_err_t err = hse_kvdb_mclass_info_get(kvdb, mclass, &info);
    if (err) {
        throw_new_hse_exception(env, err);
        return;
    }

    const jstring path_str = (*env)->NewStringUTF(env, info.mi_path);
    if ((*env)->ExceptionCheck(env))
        return;
    const jobjectArray var_args =
        (*env)->NewObjectArray(env, 0, globals.java.lang.String.class, NULL);
    if ((*env)->ExceptionCheck(env))
        return;
    const jobject path_obj = (*env)->CallStaticObjectMethod(
        env,
        globals.java.nio.file.Paths.class,
        globals.java.nio.file.Paths.get,
        path_str,
        var_args);
    if ((*env)->ExceptionCheck(env))
        return;

    (*env)->SetIntField(
        env,
        mclass_info_obj,
        globals.com.micron.hse.MclassInfo.allocatedBytes,
        info.mi_allocated_bytes);
    if ((*env)->ExceptionCheck(env))
        return;
    (*env)->SetIntField(
        env, mclass_info_obj, globals.com.micron.hse.MclassInfo.usedBytes, info.mi_used_bytes);
    if ((*env)->ExceptionCheck(env))
        return;
    (*env)->SetObjectField(env, mclass_info_obj, globals.com.micron.hse.MclassInfo.path, path_obj);
    if ((*env)->ExceptionCheck(env))
        return;
}
