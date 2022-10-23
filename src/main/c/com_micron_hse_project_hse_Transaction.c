/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

#include <assert.h>

#include <hse/hse.h>
#include <jni.h>

#include "hsejni.h"
#include "com_micron_hse_project_hse_Transaction.h"

jlong
Java_com_micron_hse_1project_hse_Transaction_alloc(
    JNIEnv *env,
    jclass  txn_cls,
    jlong   kvdb_handle)
{
    (void)env;
    (void)txn_cls;

    struct hse_kvdb *kvdb = (struct hse_kvdb *)kvdb_handle;

    struct hse_txn *txn = hse_kvdb_txn_alloc(kvdb);

    return (jlong)txn;
}

void
Java_com_micron_hse_1project_hse_Transaction_free(
    JNIEnv *env,
    jobject txn_obj,
    jlong   kvdb_handle,
    jlong   txn_handle)
{
    (void)env;
    (void)txn_obj;

    struct hse_kvdb     *kvdb = (struct hse_kvdb *)kvdb_handle;
    struct hse_txn *txn = (struct hse_txn *)txn_handle;

    hse_kvdb_txn_free(kvdb, txn);
}

void
Java_com_micron_hse_1project_hse_Transaction_abort(
    JNIEnv *env,
    jobject txn_obj,
    jlong   kvdb_handle,
    jlong   txn_handle)
{
    (void)txn_obj;

    struct hse_kvdb     *kvdb = (struct hse_kvdb *)kvdb_handle;
    struct hse_txn *txn = (struct hse_txn *)txn_handle;

    const hse_err_t err = hse_txn_abort(kvdb, txn);
    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_com_micron_hse_1project_hse_Transaction_begin(
    JNIEnv *env,
    jobject txn_obj,
    jlong   kvdb_handle,
    jlong   txn_handle)
{
    (void)txn_obj;

    struct hse_kvdb     *kvdb = (struct hse_kvdb *)kvdb_handle;
    struct hse_txn *txn = (struct hse_txn *)txn_handle;

    const hse_err_t err = hse_txn_begin(kvdb, txn);
    if (err)
        throw_new_hse_exception(env, err);
}

void
Java_com_micron_hse_1project_hse_Transaction_commit(
    JNIEnv *env,
    jobject txn_obj,
    jlong   kvdb_handle,
    jlong   txn_handle)
{
    (void)txn_obj;

    struct hse_kvdb     *kvdb = (struct hse_kvdb *)kvdb_handle;
    struct hse_txn *txn = (struct hse_txn *)txn_handle;

    const hse_err_t err = hse_txn_commit(kvdb, txn);
    if (err)
        throw_new_hse_exception(env, err);
}

jobject
Java_com_micron_hse_1project_hse_Transaction_getState(
    JNIEnv *env,
    jobject txn_obj,
    jlong   kvdb_handle,
    jlong   txn_handle)
{
    (void)env;
    (void)txn_obj;

    struct hse_kvdb     *kvdb = (struct hse_kvdb *)kvdb_handle;
    struct hse_txn *txn = (struct hse_txn *)txn_handle;

    const enum hse_txn_state state = hse_txn_state_get(kvdb, txn);

    switch (state) {
        case HSE_TXN_ABORTED:
            return globals.com.micron.hse_project.hse.Transaction.State.ABORTED;
        case HSE_TXN_ACTIVE:
            return globals.com.micron.hse_project.hse.Transaction.State.ACTIVE;
        case HSE_TXN_COMMITTED:
            return globals.com.micron.hse_project.hse.Transaction.State.COMMITTED;
        case HSE_TXN_INVALID:
            return globals.com.micron.hse_project.hse.Transaction.State.INVALID;
    }

    abort();
}
