/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

#include <hse/hse.h>
#include <jni.h>

#ifdef HSE_JAVA_EXPERIMENTAL
#include <hse/experimental.h>
#endif

#include "hsejni.h"
#include "com_micron_hse_Kvdb_CompactStatus.h"

void
Java_com_micron_hse_Kvdb_00024CompactStatus_get(
    JNIEnv *env,
    jobject compact_status_obj,
    jlong   kvdb_handle)
{
#ifdef HSE_JAVA_EXPERIMENTAL
    struct hse_kvdb_compact_status compact_status;
    struct hse_kvdb               *kvdb = (struct hse_kvdb *)kvdb_handle;

    const hse_err_t err = hse_kvdb_compact_status_get(kvdb, &compact_status);
    if (err) {
        throw_new_hse_exception(env, err);
        return;
    }

    (*env)->SetIntField(
        env,
        compact_status_obj,
        globals.com.micron.hse.Kvdb.CompactStatus.sampLwm,
        compact_status.kvcs_samp_lwm);
    if ((*env)->ExceptionCheck(env))
        return;
    (*env)->SetIntField(
        env,
        compact_status_obj,
        globals.com.micron.hse.Kvdb.CompactStatus.sampHwm,
        compact_status.kvcs_samp_hwm);
    if ((*env)->ExceptionCheck(env))
        return;
    (*env)->SetIntField(
        env,
        compact_status_obj,
        globals.com.micron.hse.Kvdb.CompactStatus.sampCurr,
        compact_status.kvcs_samp_curr);
    if ((*env)->ExceptionCheck(env))
        return;
    (*env)->SetBooleanField(
        env,
        compact_status_obj,
        globals.com.micron.hse.Kvdb.CompactStatus.active,
        compact_status.kvcs_active);
    if ((*env)->ExceptionCheck(env))
        return;
    (*env)->SetBooleanField(
        env,
        compact_status_obj,
        globals.com.micron.hse.Kvdb.CompactStatus.canceled,
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
