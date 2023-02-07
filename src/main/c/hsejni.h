/* SPDX-License-Identifier: Apache-2.0 OR MIT
 *
 * SPDX-FileCopyrightText: Copyright 2021 Micron Technology, Inc.
 */

#ifndef HSE_JAVA_COMMON_H
#define HSE_JAVA_COMMON_H

#include <jni.h>

#include <hse/types.h>

/* This object is populated during the JNI_OnLoad() function. It saves various
 * class IDs, method IDs, and field IDs for caching purposes.
 */
struct globals {
    struct {
        struct {
            struct {
                struct {
                    struct {
                        jclass class;
                        jmethodID init;
                        struct {
                            jclass class;
                            jobject NONE;
                            jobject TXN_EXPIRED;
                        } Context;
                    } HseException;
                    struct {
                        struct {
                            jclass class;
                            jfieldID sampLwm;
                            jfieldID sampHwm;
                            jfieldID sampCurr;
                            jfieldID active;
                            jfieldID canceled;
                        } CompactStatus;
                    } Kvdb;
                    struct {
                        struct {
                            jclass class;
                            jobject ABORTED;
                            jobject ACTIVE;
                            jobject COMMITTED;
                            jobject INVALID;
                        } State;
                    } KvdbTransaction;
                    struct {
                        jclass class;
                        jfieldID allocatedBytes;
                        jfieldID usedBytes;
                        jfieldID path;
                    } MclassInfo;
                } hse;
            } hse_project;
        } github;
    } io;
    struct {
        struct {
            struct {
                jclass class;
            } EOFException;
        } io;
        struct {
            struct {
                jclass class;
                jmethodID init;
            } Integer;
            struct {
                jclass class;
            } OutOfMemoryError;
            struct {
                jclass class;
            } String;
            struct {
                jclass class;
            } UnsupportedOperationException;
        } lang;
        struct {
            struct {
                struct {
                    jclass class;
                    jmethodID get;
                } Paths;
            } file;
        } nio;
        struct {
            struct {
                struct {
                    jclass class;
                    jmethodID init;
                } SimpleImmutableEntry;
            } AbstractMap;
            struct {
                jclass class;
                jmethodID empty;
                jmethodID of;
                jmethodID ofNullable;
            } Optional;
        } util;
    } java;
};

extern struct globals globals;

void
to_paramv(JNIEnv *env, jobjectArray params, jsize *paramc, const char ***paramv);

void
free_paramv(JNIEnv *env, jobjectArray params, jsize paramc, const char **paramv);

jint
throw_new_hse_exception(JNIEnv *env, hse_err_t err);

#endif
