/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

package com.micron.hse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/** Key-value Database (KVDB). */
public final class Kvdb extends NativeObject implements AutoCloseable {
    /** KVDB home. */
    private final Path home;

    private Kvdb(final Path kvdbHome, final String... params) throws HseException {
        this.handle = open(kvdbHome.toString(), params);
        this.home = Paths.get(getHome(this.handle));
    }

    private static native void addStorage(String kvdbHome, String[] params) throws HseException;
    private static native void create(String kvdbHome, String[] params) throws HseException;
    private static native void drop(String kvdbHome) throws HseException;
    private static native long open(String kvdbHome, String[] params) throws HseException;
    private native void close(long kvdbHandle) throws HseException;
    private native void compact(long kvdbHandle, int flags) throws HseException;
    private native String getHome(long kvdbHandle);
    private native String[] getKvsNames(long kvdbHandle);
    private native String getParam(long kvdbHandle, String param) throws HseException;
    private native boolean isMclassConfigured(long kvdbHandle, int mclass);
    private native void sync(long kvdbHandle, int flags) throws HseException;

    /**
     * Add new media class storage to an existing offline KVDB.
     *
     * @param kvdbHome KVDB home directory.
     * @param params List of KVDB create-time parameters in key=value format.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public static void addStorage(final Path kvdbHome, final String... params) throws HseException {
        addStorage(kvdbHome.toString(), params);
    }

    /**
     * Create a KVDB.
     *
     * <p>This function is not thread safe.</p>
     *
     * @param kvdbHome KVDB home directory.
     * @param params List of parameters in key=value format.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public static void create(final Path kvdbHome, final String... params) throws HseException {
        create(kvdbHome.toString(), params);
    }

    /**
     * Drop a KVDB.
     *
     * <p>This function is not thread safe.</p>
     *
     * @param kvdbHome KVDB home directory.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public static void drop(final Path kvdbHome) throws HseException {
        drop(kvdbHome.toString());
    }

    /**
     * Open a KVDB.
     *
     * <p>This function is not thread safe.</p>
     *
     * @param kvdbHome KVDB home directory.
     * @param params List of parameters in key=value format.
     * @return KVDB.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public static Kvdb open(final Path kvdbHome, final String... params) throws HseException {
        return new Kvdb(kvdbHome, params);
    }

    /**
     * Close a KVDB.
     *
     * <p>
     * After invoking this function, calling any other KVDB functions will
     * result in undefined behavior unless the KVDB is re-opened.
     * </p>
     *
     * <p>This function is not thread safe.</p>
     *
     * @throws HseException Underlying C function returned a non-zero value.
     */
    @Override
    public void close() throws HseException {
        if (this.handle != 0) {
            close(this.handle);
            this.handle = 0;
        }
    }

    /**
     * Refer to {@link #compact(EnumSet)}.
     *
     * <p>{@code flags} defaults to {@code null}.</p>
     *
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #compact(EnumSet)
     */
    public void compact() throws HseException {
        compact(null);
    }

    /**
     * Request a data compaction operation.
     *
     * <p>
     * In managing the data within an HSE KVDB, there are maintenance activities
     * that occur as background processing. The application may be aware that it
     * is advantageous to do enough maintenance now for the database to be as
     * compact as it ever would be in normal operation.
     * </p>
     *
     * <p>This function is thread safe.</p>
     *
     * @param flags Compaction flags.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #getCompactStatus()
     */
    public void compact(EnumSet<CompactFlags> flags) throws HseException {
        final int flagsValue = flags == null ? 0 : flags.stream()
            .mapToInt(flag -> 1 << flag.ordinal())
            .sum();

        compact(this.handle, flagsValue);
    }

    /**
     * Get status of an ongoing compaction activity.
     *
     * <p>
     * The caller can examine the fields of the {@link CompactStatus} object to
     * determine the current state of maintenance compaction.
     * </p>
     *
     * <p>This function is not thread safe.</p>
     *
     * @return Status of compaction request.
     * @throws HseException Underlying C function returned a non-zero value.
     * @throws UnsupportedOperationException Experimental API is not enabled.
     * @see #compact(EnumSet)
     */
    public CompactStatus getCompactStatus() throws HseException {
        return new CompactStatus(this);
    }

    /**
     * Get the KVDB home.
     *
     * <p>This function is thread safe.</p>
     *
     * @return KVDB home.
     * @see #home
     */
    public Path getHome() {
        return this.home;
    }

    /**
     * Get the names of the KVSs within the given KVDB.
     *
     * <p>This function is thread safe.</p>
     *
     * @return List of KVS names.
     */
    public List<String> getKvsNames() {
        return Collections.unmodifiableList(Arrays.asList(getKvsNames(this.handle)));
    }

    /**
     * Get media class information from a KVDB.
     *
     * <p>This function is thread safe.</p>
     *
     * @param mclass Media class to query for.
     * @return Media class information.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public MclassInfo getMclassInfo(final Mclass mclass) throws HseException {
        return new MclassInfo(this, mclass);
    }

    /**
     * Get a KVDB parameter.
     *
     * <p>This function is thread safe.</p>
     *
     * @param param Parameter name.
     * @return Stringified version of the parameter value.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public String getParam(final String param) throws HseException {
        return getParam(this.handle, param);
    }

    /**
     * Check if a media class is configured for a KVDB.
     *
     * <p>This function is thread safe.</p>
     *
     * @param mclass Media class to query for.
     * @return Whether or not {@code mclass} is configured.
     */
    public boolean isMclassConfigured(final Mclass mclass) {
        return isMclassConfigured(this.handle, mclass.ordinal());
    }

    /**
     * Create a KVS within the referenced KVDB.
     *
     * <p>This function is not thread safe.</p>
     *
     * @param kvsName KVS name.
     * @param params List of parameters in key=value format.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public void kvsCreate(final String kvsName, final String... params) throws HseException {
        Kvs.create(this, kvsName, params);
    }

    /**
     * Drop a KVS from the referenced KVDB.
     *
     * <p>It is an error to call this function on a KVS that is open.</p>
     *
     * <p>This function is not thread safe.</p>
     *
     * @param kvsName KVS name.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public void kvsDrop(final String kvsName) throws HseException {
        Kvs.drop(this, kvsName);
    }

    /**
     * Open a KVS in the referenced KVDB.
     *
     * <p>This function is not thread safe.</p>
     *
     * @param kvsName KVS name.
     * @param params List of parameter in key=value format.
     * @return KVS.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Kvs kvsOpen(final String kvsName, final String... params) throws HseException {
        return new Kvs(this, kvsName, params);
    }

    /**
     * {@code flags} defaults to {@code null}.
     *
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #sync(EnumSet)
     */
    public void sync() throws HseException {
        sync(null);
    }

    /**
     * Sync data in all of the referenced KVDB's KVSs to stable media.
     *
     * <p>This function is thread safe.</p>
     *
     * @param flags Flags for operation specialization.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public void sync(EnumSet<SyncFlags> flags) throws HseException {
        final int flagsValue = flags == null ? 0 : flags.stream()
            .mapToInt(flag -> 1 << flag.ordinal())
            .sum();

        sync(this.handle, flagsValue);
    }

    /**
     * Allocate transaction.
     *
     * <p>This function is thread safe.</p>
     *
     * @return Transaction.
     */
    public KvdbTransaction transaction() {
        return new KvdbTransaction(this);
    }

    /** {@link Kvdb#compact(EnumSet)} flags. */
    public enum CompactFlags {
        /** Cancel an ongoing compaction request. */
        CANCEL,
        /** Compact to the space amp low watermark. */
        SAMP_LWM,
    }

    /** {@link Kvdb#sync(EnumSet)} flags. */
    public enum SyncFlags {
        /**
         * Return immediately after initiating operation instead of waiting for
         * completion.
         */
        ASYNC,
    }

    /** Status of a compaction request. */
    public static final class CompactStatus {
        /** Space amp low water mark (%). */
        private int sampLwm;
        /** Space amp high water mark (%). */
        private int sampHwm;
        /** Current space amp (%). */
        private int sampCurr;
        /** Is an externally requested compaction underway. */
        private boolean active;
        /** Was an externally requested compaction canceled. */
        private boolean canceled;

        private CompactStatus(Kvdb kvdb) throws HseException {
            get(kvdb.handle);
        }

        private native void get(long kvdbHandle) throws HseException;

        /**
         * Get the space amp low water mark percentage.
         *
         * @return Space amp low water mark percentage.
         */
        public int getSampLwm() {
            return sampLwm;
        }

        /**
         * Get the space amp high water mark percentage.
         *
         * @return Space amp high water mark percentage.
         */
        public int getSampHwm() {
            return sampHwm;
        }

        /**
         * Get the current space amp percentage.
         *
         * @return Current space amp percentage.
         */
        public int getSampCurr() {
            return sampCurr;
        }

        /**
         * Get whether an externally requested compaction is underway.
         *
         * @return Whether an externally requested compaction is underway.
         */
        public boolean getActive() {
            return active;
        }

        /**
         * Get whether an externally requested compaction was canceled.
         *
         * @return Whether an externally requested compaction was canceled.
         */
        public boolean getCanceled() {
            return canceled;
        }
    }
}
