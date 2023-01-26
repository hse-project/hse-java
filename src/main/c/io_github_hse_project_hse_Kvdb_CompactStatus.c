/* SPDX-License-Identifier: Apache-2.0 OR MIT
 *
 * SPDX-FileCopyrightText: Copyright 2021 Micron Technology, Inc.
 */

#include <hse/hse.h>
#include <jni.h>

#ifdef HSE_JAVA_EXPERIMENTAL
#include <hse/experimental.h>
#endif

#include "io_github_hse_project_hse_Kvdb_CompactStatus.h"
#include "hsejni.h"

void
Java_io_github_hse_1project_hse_Kvdb_00024CompactStatus_get(
    JNIEnv *env,
    jobject compact_status_obj,
    jlong kvdb_handle)
{
#ifdef HSE_JAVA_EXPERIMENTAL
    hse_err_t err;
    struct hse_kvdb_compact_status compact_status;
    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    err = hse_kvdb_compact_status_get(kvdb, &compact_status);
    if (err) {
        throw_new_hse_exception(env, err);
        return;
    }

    (*env)->SetIntField(
        env,
        compact_status_obj,
        globals.io.github.hse_project.hse.Kvdb.CompactStatus.sampLwm,
        compact_status.kvcs_samp_lwm);
    if ((*env)->ExceptionCheck(env))
        return;
    (*env)->SetIntField(
        env,
        compact_status_obj,
        globals.io.github.hse_project.hse.Kvdb.CompactStatus.sampHwm,
        compact_status.kvcs_samp_hwm);
    if ((*env)->ExceptionCheck(env))
        return;
    (*env)->SetIntField(
        env,
        compact_status_obj,
        globals.io.github.hse_project.hse.Kvdb.CompactStatus.sampCurr,
        compact_status.kvcs_samp_curr);
    if ((*env)->ExceptionCheck(env))
        return;
    (*env)->SetBooleanField(
        env,
        compact_status_obj,
        globals.io.github.hse_project.hse.Kvdb.CompactStatus.active,
        compact_status.kvcs_active);
    if ((*env)->ExceptionCheck(env))
        return;
    (*env)->SetBooleanField(
        env,
        compact_status_obj,
        globals.io.github.hse_project.hse.Kvdb.CompactStatus.canceled,
        compact_status.kvcs_canceled);
    if ((*env)->ExceptionCheck(env))
        return;
#else
    (void)compact_status_obj;
    (void)kvdb_handle;

    (*env)->ThrowNew(
        env,
        globals.java.lang.UnsupportedOperationException.class,
        "Experimental support is not enabled");
#endif
}
