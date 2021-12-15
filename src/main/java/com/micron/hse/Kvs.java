/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

package com.micron.hse;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Optional;

import com.micron.hse.KvsCursor.CreateFlags;

/** Key-Value Store (KVS). */
public final class Kvs extends NativeObject implements AutoCloseable {
    /** Name of the KVS. */
    private final String name;

    Kvs(final Kvdb kvdb, final String kvsName, final String... params) throws HseException {
        this.handle = open(kvdb.handle, kvsName, params);
        this.name = getName(this.handle);
    }

    private static native void create(long kvdbHandle, String kvsName, String[] params)
            throws HseException;
    private static native void drop(long kvdbHandle, String kvsName)
            throws HseException;
    private static native long open(long kvdbHandle, String kvsName, String[] params)
            throws HseException;
    private native void close(long kvsHandle) throws HseException;
    private native void delete(long kvsHandle, byte[] key, int keyLen, int flags,
        long txnHandle) throws HseException;
    private native void delete(long kvsHandle, ByteBuffer key, int keyLen, int keyPos,
        int flags, long txnHandle) throws HseException;
    private native byte[] get(long kvsHandle, byte[] key, int keyLen, int flags, long txnHandle)
            throws HseException;
    private native byte[] get(long kvsHandle, ByteBuffer key, int keyLen, int keyPos, int flags,
        long txnHandle) throws HseException;
    private native int get(long kvsHandle, byte[] key, int keyLen, byte[] valueBuf,
        int valueBufSz, int flags, long txnHandle) throws HseException;
    private native int get(long kvsHandle, byte[] key, int keyLen, ByteBuffer valueBuf,
        int valueBufSz, int valueBufPos, int flags, long txnHandle) throws HseException;
    private native int get(long kvsHandle, ByteBuffer key, int keyLen, int keyPos,
        byte[] valueBuf, int valueBufSz, int flags, long txnHandle) throws HseException;
    private native int get(long kvsHandle, ByteBuffer key, int keyLen,
        int keyPos, ByteBuffer valueBuf, int valueBufSz, int valueBufPos, int flags,
        long txnHandle) throws HseException;
    private native String getName(long kvsHandle);
    private native String getParam(long kvdbHandle, String param) throws HseException;
    private native void prefixDelete(long kvsHandle, byte[] pfx, int pfxLen, int flags,
        long txnHandle) throws HseException;
    private native void prefixDelete(long kvsHandle, ByteBuffer pfx, int pfxLen, int pfxPos,
        int flags, long txnHandle) throws HseException;
    private native void put(long kvsHandle, byte[] key, int keyLen, byte[] value, int valueLen,
        int flags, long txnHandle) throws HseException;
    private native void put(long kvsHandle, byte[] key, int keyLen, ByteBuffer value, int valueLen,
        int valuePos, int flags, long txnHandle) throws HseException;
    private native void put(long kvsHandle, ByteBuffer key, int keyLen, int keyPos,
        ByteBuffer value, int valueLen, int valuePos, int flags, long txnHandle)
            throws HseException;
    private native void put(long kvsHandle, ByteBuffer key, int keyLen, int keyPos, byte[] value,
        int valueLen, int flags, long txnHandle) throws HseException;

    /**
     * Create a KVS within the referenced KVDB.
     *
     * <p>This function is not thread safe.</p>
     *
     * @param kvdb KVDB.
     * @param kvsName KVS name.
     * @param params List of parameters in key=value format.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    static void create(final Kvdb kvdb, final String kvsName, final String... params)
            throws HseException {
        create(kvdb.handle, kvsName, params);
    }

    /**
     * Drop a KVS from the referenced KVDB.
     *
     * <p>It is an error to call this function on a KVS that is open.</p>
     *
     * <p>This function is not thread safe.</p>
     *
     * @param kvdb KVDB.
     * @param kvsName KVS name.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    static void drop(final Kvdb kvdb, final String kvsName) throws HseException {
        drop(kvdb.handle, kvsName);
    }

    /**
     * Close an open KVS.
     *
     * <p>
     * After invoking this function, calling any other KVS functions will result
     * in undefined behavior unless the KVS is re-opened.
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
     * Refer to {@link #cursor(byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>
     * {@code filter}, {@code flags}, and {@code txn} default to {@code null}.
     * </p>
     *
     * @return Cursor.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #cursor(byte[], EnumSet, KvdbTransaction)
     */
    public KvsCursor cursor() throws HseException {
        return cursor((byte[]) null, null, null);
    }

    /**
     * Refer to {@link #cursor(byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} and {@code txn} default to {@code null}.</p>
     *
     * @param filter Iteration limited to keys matching this prefix filter.
     * @return Cursor.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #cursor(byte[], EnumSet, KvdbTransaction)
     */
    public KvsCursor cursor(final byte[] filter) throws HseException {
        return cursor(filter, null, null);
    }

    /**
     * Refer to {@link #cursor(byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} and {@code txn} default to {@code null}.</p>
     *
     * @param filter Iteration limited to keys matching this prefix filter.
     * @return Cursor.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #cursor(String, EnumSet, KvdbTransaction)
     */
    public KvsCursor cursor(final String filter) throws HseException {
        return cursor(filter, null, null);
    }

    /**
     * Refer to {@link #cursor(byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} and {@code txn} default to {@code null}.</p>
     *
     * @param filter Iteration limited to keys matching this prefix filter.
     * @return Cursor.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #cursor(ByteBuffer, EnumSet, KvdbTransaction)
     */
    public KvsCursor cursor(final ByteBuffer filter) throws HseException {
        return cursor(filter, null, null);
    }

    /**
     * Refer to {@link #cursor(byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code filter} and {@code txn} default to {@code null}.</p>
     *
     * @param flags Flags for operation specialization.
     * @return Cursor.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #cursor(byte[], EnumSet, KvdbTransaction)
     */
    public KvsCursor cursor(final EnumSet<CreateFlags> flags) throws HseException {
        return cursor((byte[]) null, flags, null);
    }

    /**
     * Refer to {@link #cursor(byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code filter} and {@code flags} default to {@code null}.</p>
     *
     * @param txn Transaction context.
     * @return Cursor.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #cursor(byte[], EnumSet, KvdbTransaction)
     */
    public KvsCursor cursor(final KvdbTransaction txn) throws HseException {
        return cursor((byte[]) null, null, txn);
    }

    /**
     * Refer to {@link #cursor(byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param filter Iteration limited to keys matching this prefix filter.
     * @param flags Flags for operation specialization.
     * @return Cursor.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #cursor(byte[], EnumSet, KvdbTransaction)
     */
    public KvsCursor cursor(final byte[] filter, final EnumSet<CreateFlags> flags)
            throws HseException {
        return cursor(filter, flags, null);
    }

    /**
     * Refer to {@link #cursor(byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param filter Iteration limited to keys matching this prefix filter.
     * @param flags Flags for operation specialization.
     * @return Cursor.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #cursor(String, EnumSet, KvdbTransaction)
     */
    public KvsCursor cursor(final String filter, final EnumSet<CreateFlags> flags)
            throws HseException {
        return cursor(filter, flags, null);
    }

    /**
     * Refer to {@link #cursor(byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param filter Iteration limited to keys matching this prefix filter.
     * @param flags Flags for operation specialization.
     * @return Cursor.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #cursor(ByteBuffer, EnumSet, KvdbTransaction)
     */
    public KvsCursor cursor(final ByteBuffer filter, final EnumSet<CreateFlags> flags)
            throws HseException {
        return cursor(filter, flags, null);
    }

    /**
     * Refer to {@link #cursor(byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} defaults to {@code null}.</p>
     *
     * @param filter Iteration limited to keys matching this prefix filter.
     * @param txn Transaction context.
     * @return Cursor.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #cursor(byte[], EnumSet, KvdbTransaction)
     */
    public KvsCursor cursor(final byte[] filter, final KvdbTransaction txn) throws HseException {
        return cursor(filter, null, txn);
    }

    /**
     * Refer to {@link #cursor(byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} defaults to {@code null}.</p>
     *
     * @param filter Iteration limited to keys matching this prefix filter.
     * @param txn Transaction context.
     * @return Cursor.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #cursor(String, EnumSet, KvdbTransaction)
     */
    public KvsCursor cursor(final String filter, final KvdbTransaction txn) throws HseException {
        return cursor(filter, null, txn);
    }

    /**
     * Refer to {@link #cursor(byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} defaults to {@code null}.</p>
     *
     * @param filter Iteration limited to keys matching this prefix filter.
     * @param txn Transaction context.
     * @return Cursor.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #cursor(ByteBuffer, EnumSet, KvdbTransaction)
     */
    public KvsCursor cursor(final ByteBuffer filter, final KvdbTransaction txn)
            throws HseException {
        return cursor(filter, null, txn);
    }

    /**
     * Refer to {@link #cursor(byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} defaults to {@code null}.</p>
     *
     * @param flags Flags for operation specialization.
     * @param txn Transaction context.
     * @return Cursor.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #cursor(byte[], EnumSet, KvdbTransaction)
     */
    public KvsCursor cursor(final EnumSet<CreateFlags> flags, final KvdbTransaction txn)
            throws HseException {
        return cursor((byte[]) null, flags, txn);
    }

    /**
     * Creates a cursor used to iterate over key-value pairs in a KVS.
     *
     * <p><b>Non-transactional cursors:</b></p>
     *
     * <p>
     * If {@code txn} is {@code null}, a non-transactional cursor is created.
     * Non-transactional cursors have an ephemeral snapshot view of the KVS at
     * the time it the cursor is created. The snapshot view is maintained for
     * the life of the cursor. Writes to the KVS (put, deletes, etc.) made after
     * the cursor is created will not be visible to the cursor unless
     * {@link KvsCursor#updateView()} is used to obtain a more recent snapshot
     * view. Non-transactional cursors can be used on transactional and
     * non-transactional KVSs.
     * </p>
     *
     * <p><b>Transactional cursors:</b></p>
     *
     * <p>
     * If {@code txn} is not {@code null}, it must be a valid transaction handle
     * or undefined behavior will result. If it is a valid handle to a
     * transaction in the ACTIVE state, a transactional cursor is created. A
     * transaction cursor's view includes the transaction's writes overlaid on
     * the transaction's ephemeral snapshot view of the KVS. If the transaction
     * is committed or aborted before the cursor is destroyed, the cursor's view
     * reverts to same snaphsot view the transaction had when first became
     * active. The cursor will no longer be able to see the transaction's
     * writes. Calling {@link KvsCursor#updateView()} on a transactional cursor
     * is a no-op and has no effect on the cursor's view. Transactional cursors
     * can only be used on transactional KVSs.
     * </p>
     *
     * <p><b>Prefix vs non-prefix cursors:</b></p>
     *
     * <p>
     * Parameters {@code filter} and its length can be used to iterate over the
     * subset of keys in the KVS whose first {@code filter} length bytes match
     * the {@code filter} length bytes pointed to by {@code filter}.
     * </p>
     *
     * <p>
     * A prefix cursor is created when the following two conditions are met:
     * </p>
     * <ul>
     *   <li>
     *     KVS was created with a prefix length &gt; 0 (i.e., it is a prefix
     *     KVS)
     *   </li>
     *   <li>
     *     {@code filter} != {@code null} and {@code filter} length &gt;=
     *     {@code prefix.length}.
     *   </li>
     * </ul>
     *
     * <p>Otherwise, a non-prefix cursor is created.</p>
     *
     * <p>
     * Applications should arrange their key-value data to avoid the need for
     * non-prefix cursors as they are significantly slower and more
     * resource-intensive than prefix cursors. Note that simply using a filter
     * doesn't create a prefix cursor -- it must meet the two conditions listed
     * above.
     * </p>
     *
     * <p>This function is thread safe.</p>
     *
     * @param filter Iteration limited to keys matching this prefix filter.
     * @param flags Flags for operation specialization.
     * @param txn Transaction context.
     * @return Cursor.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public KvsCursor cursor(final byte[] filter, final EnumSet<CreateFlags> flags,
            final KvdbTransaction txn) throws HseException {
        return new KvsCursor(this, filter, flags, txn);
    }

    /**
     * Refer to {@link #cursor(byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>Any {@link String} arguments are converted to UTF-8 byte arrays.</p>
     *
     * @param filter Iteration limited to keys matching this prefix filter.
     * @param flags Flags for operation specialization.
     * @param txn Transaction context.
     * @return Cursor.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #cursor(byte[], EnumSet, KvdbTransaction)
     */
    public KvsCursor cursor(final String filter, final EnumSet<CreateFlags> flags,
            final KvdbTransaction txn) throws HseException {
        return new KvsCursor(this, filter, flags, txn);
    }

    /**
     * Refer to {@link #cursor(byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#limit()} - {@link ByteBuffer#position()}.
     * </p>
     *
     * @param filter Iteration limited to keys matching this prefix filter.
     * @param flags Flags for operation specialization.
     * @param txn Transaction context.
     * @return Cursor.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public KvsCursor cursor(final ByteBuffer filter, final EnumSet<CreateFlags> flags,
            final KvdbTransaction txn) throws HseException {
        return new KvsCursor(this, filter, flags, txn);
    }

    /**
     * Refer to {@link #delete(byte[], KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to delete.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #delete(byte[], KvdbTransaction)
     */
    public void delete(final byte[] key) throws HseException {
        delete(key, null);
    }

    /**
     * Refer to {@link #delete(byte[], KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to delete.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #delete(String, KvdbTransaction)
     */
    public void delete(final String key) throws HseException {
        final byte[] keyData = key == null ? null : key.getBytes(StandardCharsets.UTF_8);

        delete(keyData, null);
    }

    /**
     * Refer to {@link #delete(byte[], KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to delete.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #delete(ByteBuffer, KvdbTransaction)
     */
    public void delete(final ByteBuffer key) throws HseException {
        delete(key, null);
    }

    /**
     * Delete the key and its associated value from the KVS.
     *
     * <p>It is not an error if the key does not exist within the KVS.</p>
     *
     * <p>This function is thread safe.</p>
     *
     * @param key Key to delete.
     * @param txn Transaction context.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see KvdbTransaction For information on how deletes within transactions
     *      are handled.
     */
    public void delete(final byte[] key, final KvdbTransaction txn) throws HseException {
        final int keyLen = key == null ? 0 : key.length;
        final long txnHandle = txn == null ? 0 : txn.handle;

        delete(this.handle, key, keyLen, 0, txnHandle);
    }

    /**
     * Refer to {@link #delete(byte[], KvdbTransaction)}.
     *
     * <p>Any {@link String} arguments are converted to UTF-8 byte arrays.</p>
     *
     * @param key Transaction context.
     * @param txn Key to delete.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #delete(byte[], KvdbTransaction)
     */
    public void delete(final String key, final KvdbTransaction txn) throws HseException {
        final byte[] keyData = key == null ? null : key.getBytes(StandardCharsets.UTF_8);

        delete(keyData, txn);
    }

    /**
     * Refer to {@link #delete(byte[], KvdbTransaction)}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#limit()} - {@link ByteBuffer#position()}.
     * </p>
     *
     * @param key Key to delete.
     * @param txn Transaction context.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #delete(byte[], KvdbTransaction)
     */
    public void delete(final ByteBuffer key, final KvdbTransaction txn) throws HseException {
        int keyLen = 0;
        int keyPos = 0;
        if (key != null) {
            assert key.isDirect();

            keyLen = key.remaining();
            keyPos = key.position();

            key.position(key.limit());
        }

        final long txnHandle = txn == null ? 0 : txn.handle;

        delete(this.handle, key, keyLen, keyPos, 0, txnHandle);
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to get.
     * @return Buffer into which the value associated with {@code key} will be
     *      copied.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(byte[], KvdbTransaction) For full description.
     */
    public Optional<byte[]> get(final byte[] key) throws HseException {
        return get(key, (KvdbTransaction) null);
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to get.
     * @return Buffer into which the value associated with {@code key} will be
     *      copied.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(String, KvdbTransaction)
     */
    public Optional<byte[]> get(final String key) throws HseException {
        final byte[] keyData = key == null ? null : key.getBytes(StandardCharsets.UTF_8);

        return get(keyData);
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to get.
     * @return Buffer into which the value associated with {@code key} will be
     *      copied.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(ByteBuffer, KvdbTransaction)
     */
    public Optional<byte[]> get(final ByteBuffer key)
            throws HseException {
        return get(key, (KvdbTransaction) null);
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * @param key Key to get.
     * @param txn Transaction context.
     * @return Buffer into which the value associated with {@code key} will be
     *      copied.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(byte[], byte[], KvdbTransaction)
     */
    public Optional<byte[]> get(final byte[] key, final KvdbTransaction txn) throws HseException {
        final int keyLen = key == null ? 0 : key.length;
        final long txnHandle = txn == null ? 0 : txn.handle;

        return Optional.ofNullable(get(this.handle, key, keyLen, 0, txnHandle));
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>Any {@link String} arguments are converted to UTF-8 byte arrays.</p>
     *
     * @param key Key to get.
     * @param txn Transaction context.
     * @return Buffer into which the value associated with {@code key} will be
     *      copied.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(byte[], KvdbTransaction)
     */
    public Optional<byte[]> get(final String key, final KvdbTransaction txn) throws HseException {
        final byte[] keyData = key == null ? null : key.getBytes(StandardCharsets.UTF_8);

        return get(keyData, txn);
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#limit()} - {@link ByteBuffer#position()}.
     * </p>
     *
     * @param key Key to get.
     * @param txn Transaction context.
     * @return Buffer into which the value associated with {@code key} will be
     *      copied.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(byte[], KvdbTransaction)
     */
    public Optional<byte[]> get(final ByteBuffer key, final KvdbTransaction txn)
            throws HseException {
        int keyLen = 0;
        int keyPos = 0;
        if (key != null) {
            assert key.isDirect();

            keyLen = key.remaining();
            keyPos = key.position();

            key.position(key.limit());
        }

        final long txnHandle = txn == null ? 0 : txn.handle;

        final Optional<byte[]> value = Optional.ofNullable(get(this.handle, key,
            keyLen, keyPos, 0, txnHandle));

        return value;
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to get.
     * @param valueBuf Buffer into which the value associated with {@code key}
     *      will be copied.
     * @return Actual length of the value if {@code key} was found.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(byte[], byte[], KvdbTransaction)
     */
    public Optional<Integer> get(final byte[] key, final byte[] valueBuf) throws HseException {
        return get(key, valueBuf, null);
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to get.
     * @param valueBuf Buffer into which the value associated with {@code key}
     *      will be copied.
     * @return Actual length of the value if {@code key} was found.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(String, byte[], KvdbTransaction)
     */
    public Optional<Integer> get(final String key, final byte[] valueBuf) throws HseException {
        return get(key, valueBuf, null);
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to get.
     * @param valueBuf Buffer into which the value associated with {@code key}
     *      will be copied.
     * @return Actual length of the value if {@code key} was found.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(ByteBuffer, byte[], KvdbTransaction)
     */
    public Optional<Integer> get(final ByteBuffer key, final byte[] valueBuf) throws HseException {
        return get(key, valueBuf, null);
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to get.
     * @param valueBuf Buffer into which the value associated with {@code key}
     *      will be copied. {@link ByteBuffer#limit(int)} will be called with
     *      the known size of the value if it is smaller than the original limit.
     * @return Actual length of the value if {@code key} was found.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(byte[], ByteBuffer, KvdbTransaction)
     */
    public Optional<Integer> get(final byte[] key, final ByteBuffer valueBuf) throws HseException {
        return get(key, valueBuf, null);
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to get.
     * @param valueBuf Buffer into which the value associated with {@code key}
     *      will be copied. {@link ByteBuffer#limit(int)} will be called with
     *      the known size of the value if it is smaller than the original limit.
     * @return Actual length of the value if {@code key} was found.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(String, ByteBuffer, KvdbTransaction)
     */
    public Optional<Integer> get(final String key, final ByteBuffer valueBuf) throws HseException {
        return get(key, valueBuf, null);
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to get.
     * @param valueBuf Buffer into which the value associated with {@code key}
     *      will be copied. {@link ByteBuffer#limit(int)} will be called with
     *      the known size of the value if it is smaller than the original limit.
     * @return Actual length of the value if {@code key} was found.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(ByteBuffer, ByteBuffer, KvdbTransaction)
     */
    public Optional<Integer> get(final ByteBuffer key, final ByteBuffer valueBuf)
            throws HseException {
        return get(key, valueBuf, null);
    }

    /**
     * Retrieve the value for a given key from the referenced KVS.
     *
     * <p>This function is thread safe.</p>
     *
     * @param key Key to get.
     * @param valueBuf Buffer into which the value associated with {@code key}
     *      will be copied.
     * @param txn Transaction context.
     * @return Actual length of the value if {@code key} was found.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<Integer> get(final byte[] key, byte[] valueBuf, final KvdbTransaction txn)
            throws HseException {
        final int keyLen = key == null ? 0 : key.length;
        final int valueBufSz = valueBuf == null ? 0 : valueBuf.length;
        final long txnHandle = txn == null ? 0 : txn.handle;

        final int packedValueLen = get(this.handle, key, keyLen, valueBuf, valueBufSz, 0,
            txnHandle);
        final boolean found = (packedValueLen & 0b1) == 1;
        final int valueLen = packedValueLen >> 1;

        return found ? Optional.of(valueLen) : Optional.empty();
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>Any {@link String} arguments are converted to UTF-8 byte arrays.</p>
     *
     * @param key Key to get.
     * @param valueBuf Buffer into which the value associated with {@code key}
     *      will be copied.
     * @param txn Transaction context.
     * @return Actual length of the value if {@code key} was found.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(byte[], byte[], KvdbTransaction)
     */
    public Optional<Integer> get(final String key, final byte[] valueBuf, final KvdbTransaction txn)
            throws HseException {
        final byte[] keyData = key == null ? null : key.getBytes(StandardCharsets.UTF_8);

        return get(keyData, valueBuf, txn);
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#limit()} - {@link ByteBuffer#position()}.
     * </p>
     *
     * @param key Key to get.
     * @param valueBuf Buffer into which the value associated with {@code key}
     *      will be copied.
     * @param txn Transaction context.
     * @return Actual length of the value if {@code key} was found.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(byte[], byte[], KvdbTransaction)
     */
    public Optional<Integer> get(final ByteBuffer key, final byte[] valueBuf,
            final KvdbTransaction txn) throws HseException {
        int keyLen = 0;
        int keyPos = 0;
        if (key != null) {
            assert key.isDirect();

            keyLen = key.remaining();
            keyPos = key.position();

            key.position(key.limit());
        }

        final int valueBufSz = valueBuf == null ? 0 : valueBuf.length;

        final long txnHandle = txn == null ? 0 : txn.handle;

        final int packedValueLen = get(this.handle, key, keyLen, keyPos, valueBuf, valueBufSz, 0,
            txnHandle);
        final boolean found = (packedValueLen & 0b1) == 1;

        if (!found) {
            return Optional.empty();
        }

        return Optional.of(packedValueLen >> 1);
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#limit()} - {@link ByteBuffer#position()}.
     * </p>
     *
     * @param key Key to get.
     * @param valueBuf Buffer into which the value associated with {@code key}
     *      will be copied. {@link ByteBuffer#limit(int)} will be called with
     *      the known size of the value if it is smaller than the original limit.
     * @param txn Transaction context.
     * @return Actual length of the value if {@code key} was found.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(byte[], byte[], KvdbTransaction)
     */

    public Optional<Integer> get(final byte[] key, final ByteBuffer valueBuf,
            final KvdbTransaction txn) throws HseException {
        final int keyLen = key == null ? 0 : key.length;

        int valueBufSz = 0;
        int valueBufPos = 0;
        if (valueBuf != null) {
            assert valueBuf.isDirect();

            valueBufSz = valueBuf.remaining();
            valueBufPos = valueBuf.position();
        }

        final long txnHandle = txn == null ? 0 : txn.handle;

        final int packedValueLen = get(this.handle, key, keyLen, valueBuf, valueBufSz, valueBufPos,
            0, txnHandle);
        final boolean found = (packedValueLen & 0b1) == 1;

        if (!found) {
            return Optional.empty();
        }

        final int valueLen = packedValueLen >> 1;

        if (valueBuf != null) {
            valueBuf.limit(Math.min(valueBuf.limit(), valueLen + valueBufPos));
        }

        return Optional.of(valueLen);
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>Any {@link String} arguments are converted to UTF-8 byte arrays.</p>
     *
     * @param key Key to get.
     * @param valueBuf Buffer into which the value associated with {@code key}
     *      will be copied. {@link ByteBuffer#limit(int)} will be called with
     *      the known size of the value if it is smaller than the original limit.
     * @param txn Transaction context.
     * @return Actual length of the value if {@code key} was found.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(byte[], byte[], KvdbTransaction)
     */
    public Optional<Integer> get(final String key, final ByteBuffer valueBuf,
            final KvdbTransaction txn) throws HseException {
        final byte[] keyData = key == null ? null : key.getBytes(StandardCharsets.UTF_8);

        return get(keyData, valueBuf, txn);
    }

    /**
     * Refer to {@link #get(byte[], byte[], KvdbTransaction)}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#limit()} - {@link ByteBuffer#position()}.
     * </p>
     *
     * @param key Key to get.
     * @param valueBuf Buffer into which the value associated with {@code key}
     *      will be copied. {@link ByteBuffer#limit(int)} will be called with
     *      the known size of the value if it is smaller than the original limit.
     * @param txn Transaction context.
     * @return Actual length of the value if {@code key} was found.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #get(byte[], byte[], KvdbTransaction)
     */
    public Optional<Integer> get(final ByteBuffer key, final ByteBuffer valueBuf,
            final KvdbTransaction txn) throws HseException {
        int keyLen = 0;
        int keyPos = 0;
        if (key != null) {
            assert key.isDirect();

            keyLen = key.remaining();
            keyPos = key.position();

            key.position(key.limit());
        }

        int valueBufSz = 0;
        int valueBufPos = 0;
        if (valueBuf != null) {
            assert valueBuf.isDirect();

            valueBufSz = valueBuf.remaining();
            valueBufPos = valueBuf.position();
        }

        final long txnHandle = txn == null ? 0 : txn.handle;

        final int packedValueLen = get(this.handle, key, keyLen, keyPos, valueBuf, valueBufSz,
            valueBufPos, 0, txnHandle);
        final boolean found = (packedValueLen & 0b1) == 1;

        if (!found) {
            return Optional.empty();
        }

        final int valueLen = packedValueLen >> 1;

        if (valueBuf != null) {
            valueBuf.limit(Math.min(valueBuf.limit(), valueLen + valueBufPos));
        }

        return Optional.of(valueLen);
    }

    /**
     * Get the KVS name.
     *
     * <p>This function is thread safe.</p>
     *
     * @return Name.
     * @see #name
     */
    public String getName() {
        return this.name;
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
     * Refer to {@link #prefixDelete(byte[], KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param pfx Prefix of keys to delete.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public void prefixDelete(final byte[] pfx) throws HseException {
        prefixDelete(pfx, null);
    }

    /**
     * Refer to {@link #prefixDelete(byte[], KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param pfx Prefix of keys to delete.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #prefixDelete(String, KvdbTransaction)
     */
    public void prefixDelete(final String pfx) throws HseException {
        prefixDelete(pfx, null);
    }

    /**
     * Refer to {@link #prefixDelete(byte[], KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param pfx Prefix of keys to delete.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #prefixDelete(ByteBuffer, KvdbTransaction)
     */
    public void prefixDelete(final ByteBuffer pfx) throws HseException {
        prefixDelete(pfx, null);
    }

    /**
     * Delete all key-value pairs matching the key prefix from a KVS storing
     * segmented keys.
     *
     * <p>
     * This interface is used to delete an entire range of segmented keys. To do
     * this the caller passes a filter with a length equal to the KVSs key
     * prefix length. It is not an error if no keys exist matching the filter.
     * If there is a filtered iteration in progress, then that iteration can
     * fail if {@link #prefixDelete(byte[], KvdbTransaction)} is called with a
     * filter matching the iteration.
     * </p>
     *
     * <p>
     * If {@link #prefixDelete(byte[], KvdbTransaction)} is called from a
     * transaction context, it affects no key-value mutations that are part of
     * the same transaction. Stated differently, for KVS commands issued within
     * a transaction, all calls to
     * {@link #prefixDelete(byte[], KvdbTransaction)} are treated as though they
     * were issued serially at the beginning of the transaction regardless of
     * the actual order these commands appeared in.
     * </p>
     *
     * <p>This function is thread safe.</p>
     *
     * @param pfx Prefix of keys to delete.
     * @param txn Transaction context.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public void prefixDelete(final byte[] pfx, final KvdbTransaction txn) throws HseException {
        final int pfxLen = pfx == null ? 0 : pfx.length;
        final long txnHandle = txn == null ? 0 : txn.handle;

        prefixDelete(this.handle, pfx, pfxLen, 0, txnHandle);
    }

    /**
     * Refer to {@link #prefixDelete(byte[], KvdbTransaction)}.
     *
     * <p>Any {@link String} arguments are converted to UTF-8 byte arrays.</p>
     *
     * @param pfx Prefix of keys to delete.
     * @param txn Transaction context.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #prefixDelete(byte[], KvdbTransaction)
     */
    public void prefixDelete(final String pfx, final KvdbTransaction txn) throws HseException {
        final byte[] pfxData = pfx == null ? null : pfx.getBytes(StandardCharsets.UTF_8);

        prefixDelete(pfxData, txn);
    }

    /**
     * Refer to {@link #prefixDelete(byte[], KvdbTransaction)}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#limit()} - {@link ByteBuffer#position()}.
     * </p>
     *
     * @param pfx Prefix of keys to delete.
     * @param txn Transaction context.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #prefixDelete(byte[], KvdbTransaction)
     */
    public void prefixDelete(final ByteBuffer pfx, final KvdbTransaction txn) throws HseException {
        int pfxLen = 0;
        int pfxPos = 0;
        if (pfx != null) {
            assert pfx.isDirect();

            pfxLen = pfx.remaining();
            pfxPos = pfx.position();

            pfx.position(pfx.limit());
        }

        final long txnHandle = txn == null ? 0 : txn.handle;

        prefixDelete(this.handle, pfx, pfxLen, pfxPos, 0, txnHandle);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} and {@code txn} default to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], byte[], EnumSet, KvdbTransaction)
     */
    public void put(final byte[] key, final byte[] value) throws HseException {
        put(key, value, null, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} and {@code txn} default to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], String, EnumSet, KvdbTransaction)
     */
    public void put(final byte[] key, final String value) throws HseException {
        put(key, value, null, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} and {@code txn} default to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], ByteBuffer, EnumSet, KvdbTransaction)
     */
    public void put(final byte[] key, final ByteBuffer value) throws HseException {
        put(key, value, null, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} and {@code txn} default to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(String, byte[], EnumSet, KvdbTransaction)
     */
    public void put(final String key, final byte[] value) throws HseException {
        put(key, value, null, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} and {@code txn} default to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(String, String, EnumSet, KvdbTransaction)
     */
    public void put(final String key, final String value) throws HseException {
        put(key, value, null, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} and {@code txn} default to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(String, ByteBuffer, EnumSet, KvdbTransaction)
     */
    public void put(final String key, final ByteBuffer value) throws HseException {
        put(key, value, null, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} and {@code txn} default to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(ByteBuffer, byte[], EnumSet, KvdbTransaction)
     */
    public void put(final ByteBuffer key, final byte[] value) throws HseException {
        put(key, value, null, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} and {@code txn} default to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(ByteBuffer, String, EnumSet, KvdbTransaction)
     */
    public void put(final ByteBuffer key, final String value) throws HseException {
        put(key, value, null, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} and {@code txn} default to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(ByteBuffer, ByteBuffer, EnumSet, KvdbTransaction)
     */
    public void put(final ByteBuffer key, final ByteBuffer value) throws HseException {
        put(key, value, null, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], byte[], EnumSet, KvdbTransaction)
     */
    public void put(final byte[] key, final byte[] value, final EnumSet<PutFlags> flags)
            throws HseException {
        put(key, value, flags, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], String, EnumSet, KvdbTransaction)
     */
    public void put(final byte[] key, final String value, final EnumSet<PutFlags> flags)
            throws HseException {
        put(key, value, flags, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], ByteBuffer, EnumSet, KvdbTransaction)
     */
    public void put(final byte[] key, final ByteBuffer value, final EnumSet<PutFlags> flags)
            throws HseException {
        put(key, value, flags, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(String, byte[], EnumSet, KvdbTransaction)
     */
    public void put(final String key, final byte[] value, final EnumSet<PutFlags> flags)
            throws HseException {
        put(key, value, flags, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(String, String, EnumSet, KvdbTransaction)
     */
    public void put(final String key, final String value, final EnumSet<PutFlags> flags)
            throws HseException {
        put(key, value, flags, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(String, ByteBuffer, EnumSet, KvdbTransaction)
     */
    public void put(final String key, final ByteBuffer value, final EnumSet<PutFlags> flags)
            throws HseException {
        put(key, value, flags, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(ByteBuffer, byte[], EnumSet, KvdbTransaction)
     */
    public void put(final ByteBuffer key, final byte[] value, final EnumSet<PutFlags> flags)
            throws HseException {
        put(key, value, flags, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(ByteBuffer, String, EnumSet, KvdbTransaction)
     */
    public void put(final ByteBuffer key, final String value, final EnumSet<PutFlags> flags)
            throws HseException {
        put(key, value, flags, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code txn} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(ByteBuffer, ByteBuffer, EnumSet, KvdbTransaction)
     */
    public void put(final ByteBuffer key, final ByteBuffer value,
            final EnumSet<PutFlags> flags) throws HseException {
        put(key, value, flags, null);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param txn Transaction context.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], byte[], EnumSet, KvdbTransaction)
     */
    public void put(final byte[] key, final byte[] value, final KvdbTransaction txn)
            throws HseException {
        put(key, value, null, txn);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param txn Transaction context.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], String, EnumSet, KvdbTransaction)
     */
    public void put(final byte[] key, final String value, final KvdbTransaction txn)
            throws HseException {
        put(key, value, null, txn);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param txn Transaction context.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], ByteBuffer, EnumSet, KvdbTransaction)
     */
    public void put(final byte[] key, final ByteBuffer value, final KvdbTransaction txn)
            throws HseException {
        put(key, value, null, txn);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param txn Transaction context.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(String, byte[], EnumSet, KvdbTransaction)
     */
    public void put(final String key, final byte[] value, final KvdbTransaction txn)
            throws HseException {
        put(key, value, null, txn);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param txn Transaction context.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(String, String, EnumSet, KvdbTransaction)
     */
    public void put(final String key, final String value, final KvdbTransaction txn)
            throws HseException {
        put(key, value, null, txn);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param txn Transaction context.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(String, ByteBuffer, EnumSet, KvdbTransaction)
     */
    public void put(final String key, final ByteBuffer value, final KvdbTransaction txn)
            throws HseException {
        put(key, value, null, txn);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param txn Transaction context.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(ByteBuffer, byte[], EnumSet, KvdbTransaction)
     */
    public void put(final ByteBuffer key, final byte[] value, final KvdbTransaction txn)
            throws HseException {
        put(key, value, null, txn);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param txn Transaction context.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(ByteBuffer, String, EnumSet, KvdbTransaction)
     */
    public void put(final ByteBuffer key, final String value, final KvdbTransaction txn)
            throws HseException {
        put(key, value, null, txn);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>{@code flags} defaults to {@code null}.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param txn Transaction context.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(ByteBuffer, ByteBuffer, EnumSet, KvdbTransaction)
     */
    public void put(final ByteBuffer key, final ByteBuffer value, final KvdbTransaction txn)
            throws HseException {
        put(key, value, null, txn);
    }

    /**
     * Put a key-value pair into the referenced KVS.
     *
     * <p>
     * If the key already exists in the KVS then the value is effectively
     * overwritten.
     * </p>
     *
     * <p>
     * The HSE KVDB attempts to maintain reasonable QoS and for high-throughput
     * clients this results in very short sleep's being inserted into the put
     * path. For some kinds of data (e.g., control metadata) the client may wish
     * to not experience that delay. For relatively low data rate uses, the
     * caller can set the {@link PutFlags#PRIO} flag for an
     * {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}. Care should be
     * taken when doing so to ensure that the system does not become overrun. As
     * a rough approximation, doing 1M priority puts per second marked as PRIO
     * is likely an issue. On the other hand, doing 1K small puts per second
     * marked as PRIO is almost certainly fine.
     * </p>
     *
     * <p>
     * If compression is enabled for the given kvs, then
     * {@link #put(byte[], byte[], EnumSet, KvdbTransaction)} will attempt to
     * compress the value unless the {@link PutFlags#VCOMP_OFF} flag is given.
     * Otherwise, the {@link PutFlags#VCOMP_OFF} flag is ignored.
     * </p>
     *
     * <p>This function is thread safe.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @param txn Transaction context.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see KvdbTransaction For information on how puts within transactions are
     *      handled.
     */
    public void put(final byte[] key, final byte[] value, final EnumSet<PutFlags> flags,
            final KvdbTransaction txn) throws HseException {
        final int keyLen = key == null ? 0 : key.length;
        final int valueLen = value == null ? 0 : value.length;

        final int flagsValue = flags == null ? 0 : flags.stream()
            .mapToInt(flag -> 1 << flag.ordinal())
            .sum();
        final long txnHandle = txn == null ? 0 : txn.handle;

        put(this.handle, key, keyLen, value, valueLen, flagsValue, txnHandle);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>Any {@link String} arguments are converted to UTF-8 byte arrays.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @param txn Transaction context.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], byte[], EnumSet, KvdbTransaction)
     */
    public void put(final byte[] key, final String value, final EnumSet<PutFlags> flags,
            final KvdbTransaction txn) throws HseException {
        final byte[] valueData = value == null ? null : value.getBytes(StandardCharsets.UTF_8);

        put(key, valueData, flags, txn);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#limit()} - {@link ByteBuffer#position()}.
     * </p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @param txn Transaction context.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], byte[], EnumSet, KvdbTransaction)
     */
    public void put(final byte[] key, final ByteBuffer value, final EnumSet<PutFlags> flags,
            final KvdbTransaction txn) throws HseException {
        final int keyLen = key == null ? 0 : key.length;

        int valueLen = 0;
        int valuePos = 0;
        if (value != null) {
            assert value.isDirect();

            valueLen = value.remaining();
            valuePos = value.position();
        }

        final int flagsValue = flags == null ? 0 : flags.stream()
            .mapToInt(flag -> 1 << flag.ordinal())
            .sum();
        final long txnHandle = txn == null ? 0 : txn.handle;

        put(this.handle, key, keyLen, value, valueLen, valuePos, flagsValue,
            txnHandle);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>Any {@link String} arguments are converted to UTF-8 byte arrays.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @param txn Transaction context.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], byte[], EnumSet, KvdbTransaction)
     */
    public void put(final String key, final byte[] value, final EnumSet<PutFlags> flags,
            final KvdbTransaction txn) throws HseException {
        final byte[] keyData = key == null ? null : key.getBytes(StandardCharsets.UTF_8);

        put(keyData, value, flags, txn);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>Any {@link String} arguments are converted to UTF-8 byte arrays.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @param txn Transaction context.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], byte[], EnumSet, KvdbTransaction)
     */
    public void put(final String key, final String value, final EnumSet<PutFlags> flags,
            final KvdbTransaction txn) throws HseException {
        final byte[] keyData = key == null ? null : key.getBytes(StandardCharsets.UTF_8);
        final byte[] valueData = value == null ? null : value.getBytes(StandardCharsets.UTF_8);

        put(keyData, valueData, flags, txn);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>Any {@link String} arguments are converted to UTF-8 byte arrays.</p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @param txn Transaction context.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], ByteBuffer, EnumSet, KvdbTransaction)
     */
    public void put(final String key, final ByteBuffer value, final EnumSet<PutFlags> flags,
            final KvdbTransaction txn) throws HseException {
        final byte[] keyData = key == null ? null : key.getBytes(StandardCharsets.UTF_8);

        put(keyData, value, flags, txn);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#limit()} - {@link ByteBuffer#position()}.
     * </p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @param txn Transaction context.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], byte[], EnumSet, KvdbTransaction)
     */
    public void put(final ByteBuffer key, final byte[] value,
            final EnumSet<PutFlags> flags, final KvdbTransaction txn) throws HseException {
        int keyLen = 0;
        int keyPos = 0;
        if (key != null) {
            assert key.isDirect();

            keyLen = key.remaining();
            keyPos = key.position();

            key.position(key.limit());
        }

        final int valueLen = value == null ? 0 : value.length;
        final int flagsValue = flags == null ? 0 : flags.stream()
            .mapToInt(flag -> 1 << flag.ordinal())
            .sum();
        final long txnHandle = txn == null ? 0 : txn.handle;

        put(this.handle, key, keyLen, keyPos, value, valueLen, flagsValue, txnHandle);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>Any {@link String} arguments are converted to UTF-8 byte arrays.</p>
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#limit()} - {@link ByteBuffer#position()}.
     * </p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @param txn Transaction context.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(ByteBuffer, byte[], EnumSet, KvdbTransaction)
     */
    public void put(final ByteBuffer key, final String value,
            final EnumSet<PutFlags> flags, final KvdbTransaction txn) throws HseException {
        final byte[] valueData = value == null ? null : value.getBytes(StandardCharsets.UTF_8);

        put(key, valueData, flags, txn);
    }

    /**
     * Refer to {@link #put(byte[], byte[], EnumSet, KvdbTransaction)}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#limit()} - {@link ByteBuffer#position()}.
     * </p>
     *
     * @param key Key to put into the KVS.
     * @param value Value associated with {@code key}.
     * @param flags Flags for operation specialization.
     * @param txn Transaction context.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see #put(byte[], byte[], EnumSet, KvdbTransaction)
     */
    public void put(final ByteBuffer key, final ByteBuffer value,
            final EnumSet<PutFlags> flags, final KvdbTransaction txn) throws HseException {
        int keyLen = 0;
        int keyPos = 0;
        if (key != null) {
            assert key.isDirect();

            keyLen = key.remaining();
            keyPos = key.position();

            key.position(key.limit());
        }

        int valueLen = 0;
        int valuePos = 0;
        if (value != null) {
            assert value.isDirect();

            valueLen = value.remaining();
            valuePos = value.position();

            value.position(value.limit());
        }

        final int flagsValue = flags == null ? 0 : flags.stream()
            .mapToInt(flag -> 1 << flag.ordinal())
            .sum();
        final long txnHandle = txn == null ? 0 : txn.handle;

        put(this.handle, key, keyLen, keyPos, value, valueLen, valuePos,
            flagsValue, txnHandle);
    }

    /**
     * {@link Kvs#put(byte[], byte[], EnumSet, KvdbTransaction)} (et al.) flags.
     */
    public enum PutFlags {
        /** Operation will not be throttled. */
        PRIO,
        /** Value will not be compressed. */
        VCOMP_OFF,
    }
}
