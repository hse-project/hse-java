/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

package com.micron.hse_project.hse;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Optional;
import java.util.AbstractMap.SimpleImmutableEntry;

/**
 * See the concepts and best practices sections on
 * <a href="https://hse-project.github.io.">https://hse-project.github.io</a>.
 */
public final class KvsCursor extends NativeObject implements AutoCloseable {
    KvsCursor(final Kvs kvs, byte[] filter, EnumSet<CreateFlags> flags, final Transaction txn)
            throws HseException {
        final int filterLen = filter == null ? 0 : filter.length;
        final long txnHandle = txn == null ? 0 : txn.handle;
        final int flagsValue = flags == null ? 0 : flags.stream()
            .mapToInt(flag -> 1 << flag.ordinal())
            .sum();

        this.handle = create(kvs.handle, filter, filterLen, flagsValue, txnHandle);
    }

    KvsCursor(final Kvs kvs, final String filter, EnumSet<CreateFlags> flags,
            final Transaction txn) throws HseException {
        final long txnHandle = txn == null ? 0 : txn.handle;
        final int flagsValue = flags == null ? 0 : flags.stream()
            .mapToInt(flag -> 1 << flag.ordinal())
            .sum();

        this.handle = create(kvs.handle, filter, flagsValue, txnHandle);
    }

    KvsCursor(final Kvs kvs, final ByteBuffer filter, EnumSet<CreateFlags> flags,
            final Transaction txn) throws HseException {
        int filterLen = 0;
        int filterPos = 0;
        if (filter != null) {
            assert filter.isDirect();

            filterLen = filter.remaining();
            filterPos = filter.position();

            filter.position(filter.limit());
        }

        final long txnHandle = txn == null ? 0 : txn.handle;
        final int flagsValue = flags == null ? 0 : flags.stream()
            .mapToInt(flag -> 1 << flag.ordinal())
            .sum();

        this.handle = create(kvs.handle, filter, filterLen, filterPos, flagsValue, txnHandle);
    }

    private static native long create(long kvsHandle, byte[] filter, int filterLen, int flags,
        long txnHandle) throws HseException;
    private static native long create(long kvsHandle, String filter, int flags, long txnHandle)
            throws HseException;
    private static native long create(long kvsHandle, ByteBuffer filter, int filterLen,
        int filterPos, int flags, long txnHandle) throws HseException;
    private native void destroy(long cursorHandle) throws HseException;
    private native SimpleImmutableEntry<byte[], byte[]> read(long cursorHandle, int flags)
            throws EOFException, HseException;
    private native SimpleImmutableEntry<Integer, Integer> read(long cursorHandle, byte[] keyBuf,
        int keyBufSz, byte[] valueBuf, int valueBufSz, int flags) throws EOFException, HseException;
    private native SimpleImmutableEntry<Integer, Integer> read(long cursorHandle, byte[] keyBuf,
        int keyBufSz, ByteBuffer valueBuf, int valueBufSz, int valueBufPos, int flags)
            throws EOFException, HseException;
    private native SimpleImmutableEntry<Integer, Integer> read(long cursorHandle, ByteBuffer keyBuf,
        int keyBufSz, int keyBufPos, byte[] valueBuf, int valueBufSz, int flags)
            throws EOFException, HseException;
    private native SimpleImmutableEntry<Integer, Integer> read(long cursorHandle, ByteBuffer keyBuf,
        int keyBufSz, int keyBufPos, ByteBuffer valueBuf, int valueBufSz, int valueBufPos,
        int flags) throws HseException;
    private native byte[] seek(long cursorHandle, byte[] key, int keyLen, int flags)
            throws HseException;
    private native byte[] seek(long cursorHandle, String key, int flags) throws HseException;
    private native byte[] seek(long cursorHandle, ByteBuffer key, int keyLen, int keyPos, int flags)
            throws HseException;
    private native int seek(long cursorHandle, byte[] key, int keyLen, byte[] foundBuf,
        int foundBufSz, int flags) throws HseException;
    private native int seek(long cursorHandle, byte[] key, int keyLen, ByteBuffer foundBuf,
        int foundBufSz, int foundBufPos, int flags) throws HseException;
    private native int seek(long cursorHandle, String key, byte[] foundBuf, int foundBufSz,
        int flags) throws HseException;
    private native int seek(long cursorHandle, String key, ByteBuffer foundBuf, int foundBufSz,
        int foundBufPos, int flags) throws HseException;
    private native int seek(long cursorHandle, ByteBuffer key, int keyLen, int keyPos,
        byte[] foundBuf, int foundBufSz, int flags) throws HseException;
    private native int seek(long cursorHandle, ByteBuffer key, int keyLen, int keyPos,
        ByteBuffer foundBuf, int foundBufSz, int foundBufPos, int flags)
            throws HseException;
    private native byte[] seekRange(long cursorHandle, byte[] filterMin, int filterMinLen,
        byte[] filterMax, int filterMaxLen, int flags) throws HseException;
    private native byte[] seekRange(long cursorHandle, byte[] filterMin, int filterMinLen,
        String filterMax, int flags) throws HseException;
    private native byte[] seekRange(long cursorHandle, byte[] filterMin, int filterMinLen,
        ByteBuffer filterMax, int filterMaxLen, int filterMaxPos, int flags) throws HseException;
    private native byte[] seekRange(long cursorHandle, String filterMin, byte[] filterMax,
        int filterMaxLen, int flags) throws HseException;
    private native byte[] seekRange(long cursorHandle, String filterMin, String filterMax,
        int flags) throws HseException;
    private native byte[] seekRange(long cursorHandle, String filterMin, ByteBuffer filterMax,
        int filterMaxLen, int filterMaxPos, int flags) throws HseException;
    private native byte[] seekRange(long cursorHandle, ByteBuffer filterMin, int filterMinLen,
        int filterMinPos, byte[] filterMax, int filterMaxLen, int flags) throws HseException;
    private native byte[] seekRange(long cursorHandle, ByteBuffer filterMin, int filterMinLen,
        int filterMinPos, String filterMax, int flags) throws HseException;
    private native byte[] seekRange(long cursorHandle, ByteBuffer filterMin, int filterMinLen,
        int filterMinPos, ByteBuffer filterMax, int filterMaxLen, int filterMaxPos, int flags)
            throws HseException;
    private native int seekRange(long cursorHandle, byte[] filterMin, int filterMinLen,
        byte[] filterMax, int filterMaxLen, byte[] foundBuf, int foundBufSz, int flags)
            throws HseException;
    private native int seekRange(long cursorHandle, byte[] filterMin, int filterMinLen,
        byte[] filterMax, int filterMaxLen, ByteBuffer foundBuf, int foundBufSz, int foundBufPos,
        int flags) throws HseException;
    private native int seekRange(long cursorHandle, byte[] filterMin, int filterMinLen,
        String filterMax, byte[] foundBuf, int foundBufSz, int flags) throws HseException;
    private native int seekRange(long cursorHandle, byte[] filterMin, int filterMinLen,
        String filterMax, ByteBuffer foundBuf, int foundBufSz, int foundBufPos, int flags)
            throws HseException;
    private native int seekRange(long cursorHandle, byte[] filterMin, int filterMinLen,
        ByteBuffer filterMax, int filterMaxLen, int filterMaxPos, byte[] foundBuf, int foundBufSz,
        int flags) throws HseException;
    private native int seekRange(long cursorHandle, byte[] filterMin, int filterMinLen,
        ByteBuffer filterMax, int filterMaxLen, int filterMaxPos, ByteBuffer foundBuf,
        int foundBufSz, int foundBufPos, int flags) throws HseException;
    private native int seekRange(long cursorHandle, String filterMin, byte[] filterMax,
        int filterMaxLen, byte[] foundBuf, int foundBufSz, int flags) throws HseException;
    private native int seekRange(long cursorHandle, String filterMin, byte[] filterMax,
        int filterMaxLen, ByteBuffer foundBuf, int foundBufSz, int foundBufPos, int flags)
            throws HseException;
    private native int seekRange(long cursorHandle, String filterMin, String filterMax,
        byte[] foundBuf, int foundBufSz, int flags) throws HseException;
    private native int seekRange(long cursorHandle, String filterMin, String filterMax,
        ByteBuffer foundBuf, int foundBufSz, int foundBufPos, int flags) throws HseException;
    private native int seekRange(long cursorHandle, String filterMin, ByteBuffer filterMax,
        int filterMaxLen, int filterMaxPos, byte[] foundBuf, int foundBufSz, int flags)
            throws HseException;
    private native int seekRange(long cursorHandle, String filterMin, ByteBuffer filterMax,
        int filterMaxLen, int filterMaxPos, ByteBuffer foundBuf, int foundBufSz, int foundBufPos,
        int flags) throws HseException;
    private native int seekRange(long cursorHandle, ByteBuffer filterMin, int filterMinLen,
        int filterMinPos, byte[] filterMax, int filterMaxLen, byte[] foundBuf, int foundBufSz,
        int flags) throws HseException;
    private native int seekRange(long cursorHandle, ByteBuffer filterMin, int filterMinLen,
        int filterMinPos, byte[] filterMax, int filterMaxLen, ByteBuffer foundBuf, int foundBufSz,
        int foundBufPos, int flags) throws HseException;
    private native int seekRange(long cursorHandle, ByteBuffer filterMin, int filterMinLen,
        int filterMinPos, String filterMax, byte[] foundBuf, int foundBufSz, int flags)
            throws HseException;
    private native int seekRange(long cursorHandle, ByteBuffer filterMin, int filterMinLen,
        int filterMinPos, String filterMax, ByteBuffer foundBuf, int foundBufSz, int foundBufPos,
        int flags) throws HseException;
    private native int seekRange(long cursorHandle, ByteBuffer filterMin, int filterMinLen,
        int filterMinPos, ByteBuffer filterMax, int filterMaxLen, int filterMaxPos, byte[] foundBuf,
        int foundBufSz, int flags) throws HseException;
    private native int seekRange(long cursorHandle, ByteBuffer filterMin, int filterMinLen,
        int filterMinPos, ByteBuffer filterMax, int filterMaxLen, int filterMaxPos,
        ByteBuffer foundBuf, int foundBufSz, int foundBufPos, int flags) throws HseException;
    private native void updateView(long cursorHandle) throws HseException;

    /**
     * Refer to {@link #read(byte[], byte[])}.
     *
     * @return Key-value pair.
     * @throws EOFException Cursor has no more elements to read.
     * @throws HseException Underlying C function returned a non-zero value
     */
    public SimpleImmutableEntry<byte[], byte[]> read()
            throws EOFException, HseException {
        return read(this.handle, 0);
    }

    /**
     * Iteratively access the elements pointed to by the cursor.
     *
     * <p>
     * Read a key-value pair from the cursor, advancing the cursor past its
     * current location.
     * </p>
     *
     * <p>This function is thread safe.</p>
     *
     * @param keyBuf Buffer into which the next key will be copied.
     * @param valueBuf Buffer into which the next value will be copied.
     * @return Key and value lengths.
     * @throws EOFException Cursor has no more elements to read.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public SimpleImmutableEntry<Integer, Integer> read(final byte[] keyBuf,
            final byte[] valueBuf) throws EOFException, HseException {
        final int keyBufSz = keyBuf == null ? 0 : keyBuf.length;
        final int valueBufSz = valueBuf == null ? 0 : valueBuf.length;

        return read(this.handle, keyBuf, keyBufSz, valueBuf, valueBufSz, 0);
    }

    /**
     * Refer to {@link #read(byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param keyBuf Buffer into which the next key will be copied.
     * @param valueBuf Buffer into which the next value will be copied.
     *      {@link ByteBuffer#limit(int)} will be called with the known size of
     *      the value if it is smaller than the original limit.
     * @return Key and value lengths.
     * @throws EOFException Cursor has no more elements to read.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public SimpleImmutableEntry<Integer, Integer> read(final byte[] keyBuf,
            final ByteBuffer valueBuf) throws EOFException, HseException {
        final int keyBufSz = keyBuf == null ? 0 : keyBuf.length;

        int valueBufSz = 0;
        int valueBufPos = 0;
        if (valueBuf != null) {
            assert valueBuf.isDirect();

            valueBufSz = valueBuf.remaining();
            valueBufPos = valueBuf.position();
        }

        final SimpleImmutableEntry<Integer, Integer> entry = read(this.handle, keyBuf, keyBufSz,
            valueBuf, valueBufSz, valueBufPos, 0);

        if (valueBuf != null) {
            valueBuf.limit(Math.min(valueBuf.limit(), valueBufPos + entry.getValue()));
        }

        return entry;
    }

    /**
     * Refer to {@link #read(byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param keyBuf Buffer into which the next key will be copied.
     *      {@link ByteBuffer#limit(int)} will be called with the known size of
     *      the value if it is smaller than the original limit.
     * @param valueBuf Buffer into which the next value will be copied.
     * @return Key and value lengths.
     * @throws EOFException Cursor has no more elements to read.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public SimpleImmutableEntry<Integer, Integer> read(final ByteBuffer keyBuf,
            final byte[] valueBuf) throws EOFException, HseException {
        int keyBufSz = 0;
        int keyBufPos = 0;
        if (keyBuf != null) {
            assert keyBuf.isDirect();

            keyBufSz = keyBuf.remaining();
            keyBufPos = keyBuf.position();
        }

        final int valueBufSz = valueBuf == null ? 0 : valueBuf.length;

        final SimpleImmutableEntry<Integer, Integer> entry = read(this.handle, keyBuf, keyBufSz,
            keyBufPos, valueBuf, valueBufSz, 0);

        if (keyBuf != null) {
            keyBuf.limit(Math.min(keyBuf.limit(), keyBufPos + entry.getKey()));
        }

        return entry;
    }

    /**
     * Refer to {@link #read(byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param keyBuf Buffer into which the next key will be copied.
     *      {@link ByteBuffer#limit(int)} will be called with the known size of
     *      the value if it is smaller than the original limit.
     * @param valueBuf Buffer into which the next value will be copied.
     *      {@link ByteBuffer#limit(int)} will be called with the known size of
     *      the value if it is smaller than the original limit.
     * @return Key and value lengths.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws EOFException Cursor has no more elements to read.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public SimpleImmutableEntry<Integer, Integer> read(final ByteBuffer keyBuf,
            final ByteBuffer valueBuf) throws EOFException, HseException {
        int keyBufSz = 0;
        int keyBufPos = 0;
        if (keyBuf != null) {
            assert keyBuf.isDirect();

            keyBufSz = keyBuf.remaining();
            keyBufPos = keyBuf.position();
        }

        int valueBufSz = 0;
        int valueBufPos = 0;
        if (valueBuf != null) {
            assert valueBuf.isDirect();

            valueBufSz = valueBuf.remaining();
            valueBufPos = valueBuf.position();
        }

        final SimpleImmutableEntry<Integer, Integer> entry = read(this.handle,
            keyBuf, keyBufSz, keyBufPos, valueBuf, valueBufSz, valueBufPos, 0);

        if (keyBuf != null) {
            keyBuf.limit(Math.min(keyBuf.limit(), keyBufPos + entry.getKey()));
        }

        if (valueBuf != null) {
            valueBuf.limit(Math.min(valueBuf.limit(), valueBufPos + entry.getValue()));
        }

        return entry;
    }

    /**
     * Refer to {@link #seek(byte[], byte[])}.
     *
     * @param key Key to find.
     * @return Next key in sequence.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<byte[]> seek(final byte[] key) throws HseException {
        final int keyLen = key == null ? 0 : key.length;

        return Optional.ofNullable(seek(this.handle, key, keyLen, 0));
    }

    /**
     * Refer to {@link #seek(byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * @param key Key to find.
     * @return Next key in sequence.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<byte[]> seek(final String key) throws HseException {
        return Optional.ofNullable(seek(this.handle, key, 0));
    }

    /**
     * Refer to {@link #seek(byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param key Key to find.
     * @return Next key in sequence.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<byte[]> seek(final ByteBuffer key)
            throws HseException {
        int keyPos = 0;
        int keyLen = 0;
        if (key != null) {
            assert key.isDirect();

            keyLen = key.remaining();
            keyPos = key.position();

            key.position(key.limit());
        }

        return Optional.ofNullable(seek(this.handle, key, keyLen, keyPos, 0));
    }

    /**
     * Move the cursor to point at the key-value pair at or closest to
     * {@code key}.
     *
     * <p>The next read will start at this point.</p>
     *
     * <p>This function is thread safe.</p>
     *
     * @param key Key to find.
     * @param foundBuf Next key in sequence.
     * @return Length of the found key.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<Integer> seek(final byte[] key, final byte[] foundBuf) throws HseException {
        final int keyLen = key == null ? 0 : key.length;
        final int foundBufSz = foundBuf == null ? 0 : foundBuf.length;

        final int foundLen = seek(this.handle, key, keyLen, foundBuf, foundBufSz, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seek(byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param key Key to find.
     * @param foundBuf Next key in sequence. {@link ByteBuffer#limit(int)} will
     *      be called with the known size of the value if it is smaller than the
     *      original limit.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<Integer> seek(final byte[] key, final ByteBuffer foundBuf)
            throws HseException {
        final int keyLen = key == null ? 0 : key.length;

        int foundBufSz = 0;
        int foundBufPos = 0;
        if (foundBuf != null) {
            assert foundBuf.isDirect();

            foundBufSz = foundBuf.remaining();
            foundBufPos = foundBuf.position();
        }

        final int foundLen = seek(this.handle, key, keyLen, foundBuf, foundBufSz, foundBufPos, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        if (foundBuf != null) {
            foundBuf.limit(Math.min(foundBuf.limit(), foundBufPos + foundLen));
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seek(byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * @param key Key to find.
     * @param foundBuf Next key in sequence.
     * @return Length of the found key.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<Integer> seek(final String key, final byte[] foundBuf) throws HseException {
        final int foundBufSz = foundBuf == null ? 0 : foundBuf.length;

        final int foundLen = seek(this.handle, key, foundBuf, foundBufSz, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seek(byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param key Key to find.
     * @param foundBuf Next key in sequence. {@link ByteBuffer#limit(int)} will
     *      be called with the known size of the value if it is smaller than the
     *      original limit.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<Integer> seek(final String key, final ByteBuffer foundBuf)
            throws HseException {
        int foundBufSz = 0;
        int foundBufPos = 0;
        if (foundBuf != null) {
            assert foundBuf.isDirect();

            foundBufSz = foundBuf.remaining();
            foundBufPos = foundBuf.position();
        }

        final int foundLen = seek(this.handle, key, foundBuf, foundBufSz, foundBufPos, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        if (foundBuf != null) {
            foundBuf.limit(Math.min(foundBuf.limit(), foundBufPos + foundLen));
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seek(byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param key Key to find.
     * @param foundBuf Next key in sequence.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<Integer> seek(final ByteBuffer key, final byte[] foundBuf)
            throws HseException {
        int keyLen = 0;
        int keyPos = 0;
        if (key != null) {
            assert key.isDirect();

            keyLen = key.remaining();
            keyPos = key.position();

            key.position(key.limit());
        }

        final int foundBufSz = foundBuf == null ? 0 : foundBuf.length;

        final int foundLen = seek(this.handle, key, keyLen, keyPos, foundBuf, foundBufSz, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seek(byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param key Key to find.
     * @param foundBuf Next key in sequence. {@link ByteBuffer#limit(int)} will
     *      be called with the known size of the value if it is smaller than the
     *      original limit.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<Integer> seek(final ByteBuffer key, final ByteBuffer foundBuf)
            throws HseException {
        int keyLen = 0;
        int keyPos = 0;
        if (key != null) {
            assert key.isDirect();

            keyLen = key.remaining();
            keyPos = key.position();

            key.position(key.limit());
        }

        int foundBufSz = 0;
        int foundBufPos = 0;
        if (foundBuf != null) {
            assert foundBuf.isDirect();

            foundBufSz = foundBuf.remaining();
            foundBufPos = foundBuf.position();
        }

        final int foundLen = seek(this.handle, key, keyLen, keyPos, foundBuf, foundBufSz,
            foundBufPos, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        if (foundBuf != null) {
            foundBuf.limit(Math.min(foundBuf.limit(), foundBufPos + foundLen));
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @return Next key in sequence.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<byte[]> seekRange(final byte[] filterMin, final byte[] filterMax)
            throws HseException {
        final int filterMinLen = filterMin == null ? 0 : filterMin.length;
        final int filterMaxLen = filterMax == null ? 0 : filterMax.length;

        return Optional.ofNullable(seekRange(this.handle, filterMin, filterMinLen, filterMax,
            filterMaxLen, 0));
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @return Next key in sequence.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<byte[]> seekRange(final byte[] filterMin, final String filterMax)
            throws HseException {
        final int filterMinLen = filterMin == null ? 0 : filterMin.length;

        return Optional.ofNullable(seekRange(this.handle, filterMin, filterMinLen, filterMax, 0));
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @return Next key in sequence.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<byte[]> seekRange(final byte[] filterMin, final ByteBuffer filterMax)
            throws HseException {
        final int filterMinLen = filterMin == null ? 0 : filterMin.length;

        int filterMaxLen = 0;
        int filterMaxPos = 0;
        if (filterMax != null) {
            assert filterMax.isDirect();

            filterMaxLen = filterMax.remaining();
            filterMaxPos = filterMax.position();

            filterMax.position(filterMax.limit());
        }

        return Optional.ofNullable(seekRange(this.handle, filterMin, filterMinLen, filterMax,
            filterMaxLen, filterMaxPos, 0));
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @return Next key in sequence.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<byte[]> seekRange(final String filterMin, final byte[] filterMax)
            throws HseException {
        final int filterMaxLen = filterMax == null ? 0 : filterMax.length;

        return Optional.ofNullable(seekRange(this.handle, filterMin, filterMax, filterMaxLen, 0));
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @return Next key in sequence.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<byte[]> seekRange(final String filterMin, final String filterMax)
            throws HseException {
        return Optional.ofNullable(seekRange(this.handle, filterMin, filterMax, 0));
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @return Next key in sequence.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<byte[]> seekRange(final String filterMin, final ByteBuffer filterMax)
            throws HseException {
        int filterMaxLen = 0;
        int filterMaxPos = 0;
        if (filterMax != null) {
            assert filterMax.isDirect();

            filterMaxLen = filterMax.remaining();
            filterMaxPos = filterMax.position();

            filterMax.position(filterMax.limit());
        }

        return Optional.ofNullable(seekRange(this.handle, filterMin, filterMax, filterMaxLen,
            filterMaxPos, 0));
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @return Next key in sequence.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<byte[]> seekRange(final ByteBuffer filterMin, final byte[] filterMax)
            throws HseException {
        int filterMinLen = 0;
        int filterMinPos = 0;
        if (filterMin != null) {
            assert filterMin.isDirect();

            filterMinLen = filterMin.remaining();
            filterMinPos = filterMin.position();

            filterMin.position(filterMin.limit());
        }

        final int filterMaxLen = filterMax == null ? 0 : filterMax.length;

        return Optional.ofNullable(seekRange(this.handle, filterMin, filterMinLen, filterMinPos,
            filterMax, filterMaxLen, 0));
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @return Next key in sequence.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<byte[]> seekRange(final ByteBuffer filterMin, final String filterMax)
            throws HseException {
        int filterMinLen = 0;
        int filterMinPos = 0;
        if (filterMin != null) {
            assert filterMin.isDirect();

            filterMinLen = filterMin.remaining();
            filterMinPos = filterMin.position();

            filterMin.position(filterMin.limit());
        }

        return Optional.ofNullable(seekRange(this.handle, filterMin, filterMinLen, filterMinPos,
            filterMax, 0));
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @return Next key in sequence.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<byte[]> seekRange(final ByteBuffer filterMin, final ByteBuffer filterMax)
            throws HseException {
        int filterMinLen = 0;
        int filterMinPos = 0;
        if (filterMin != null) {
            assert filterMin.isDirect();

            filterMinLen = filterMin.remaining();
            filterMinPos = filterMin.position();

            filterMin.position(filterMin.limit());
        }

        int filterMaxLen = 0;
        int filterMaxPos = 0;
        if (filterMax != null) {
            assert filterMax.isDirect();

            filterMaxLen = filterMax.remaining();
            filterMaxPos = filterMax.position();

            filterMax.position(filterMax.limit());
        }

        return Optional.ofNullable(seekRange(this.handle, filterMin, filterMinLen, filterMinPos,
            filterMax, filterMaxLen, filterMaxPos, 0));
    }

    /**
     * Move the cursor to the closest match to key, gated by the given
     * filter.
     *
     * <p>
     * Keys read from this cursor will belong to the closed interval defined by
     * the given filter: [{@code filterMin}, {@code filterMax}]. For KVSs
     * storing segmented keys, performance will be enhanced when
     * {@code filterMin} length and {@code filterMax} length are greater than or
     * equal to the KVS key prefix length.
     * </p>
     *
     * <p>This function is only supported for forward cursors.</p>
     * <p>This function is thread safe.</p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence.
     * @return Next key in sequence.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<Integer> seekRange(final byte[] filterMin, final byte[] filterMax,
            final byte[] foundBuf) throws HseException {
        final int filterMinLen = filterMin == null ? 0 : filterMin.length;
        final int filterMaxLen = filterMax == null ? 0 : filterMax.length;
        final int foundBufSz = foundBuf == null ? 0 : foundBuf.length;

        final int foundLen = seekRange(this.handle, filterMin, filterMinLen, filterMax,
            filterMaxLen, foundBuf, foundBufSz, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence. {@link ByteBuffer#limit(int)} will
     *      be called with the known size of the value if it is smaller than the
     *      original limit.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<Integer> seekRange(final byte[] filterMin, final byte[] filterMax,
            final ByteBuffer foundBuf) throws HseException {
        final int filterMinLen = filterMin == null ? 0 : filterMin.length;
        final int filterMaxLen = filterMax == null ? 0 : filterMax.length;

        int foundBufSz = 0;
        int foundBufPos = 0;
        if (foundBuf != null) {
            assert foundBuf.isDirect();

            foundBufSz = foundBuf.remaining();
            foundBufPos = foundBuf.position();
        }

        final int foundLen = seekRange(this.handle, filterMin, filterMinLen, filterMax,
            filterMaxLen, foundBuf, foundBufSz, foundBufPos, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        if (foundBuf != null) {
            foundBuf.limit(Math.min(foundBuf.limit(), foundBufPos + foundLen));
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence.
     * @return Length of the found key.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<Integer> seekRange(final byte[] filterMin, final String filterMax,
            final byte[] foundBuf) throws HseException {
        final int filterMinLen = filterMin == null ? 0 : filterMin.length;
        final int foundBufSz = foundBuf == null ? 0 : foundBuf.length;

        final int foundLen = seekRange(this.handle, filterMin, filterMinLen, filterMax, foundBuf,
            foundBufSz, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence. {@link ByteBuffer#limit(int)} will
     *      be called with the known size of the value if it is smaller than the
     *      original limit.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<Integer> seekRange(final byte[] filterMin, final String filterMax,
            final ByteBuffer foundBuf) throws HseException {
        final int filterMinLen = filterMin == null ? 0 : filterMin.length;

        int foundBufSz = 0;
        int foundBufPos = 0;
        if (foundBuf != null) {
            assert foundBuf.isDirect();

            foundBufSz = foundBuf.remaining();
            foundBufPos = foundBuf.position();
        }

        final int foundLen = seekRange(this.handle, filterMin, filterMinLen, filterMax,
            foundBuf, foundBufSz, foundBufPos, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        if (foundBuf != null) {
            foundBuf.limit(Math.min(foundBuf.limit(), foundBufPos + foundLen));
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<Integer> seekRange(final byte[] filterMin, final ByteBuffer filterMax,
            final byte[] foundBuf) throws HseException {
        final int filterMinLen = filterMin == null ? 0 : filterMin.length;

        int filterMaxLen = 0;
        int filterMaxPos = 0;
        if (filterMax != null) {
            assert filterMax.isDirect();

            filterMaxLen = filterMax.remaining();
            filterMaxPos = filterMax.position();

            filterMax.position(filterMax.limit());
        }

        final int foundBufSz = foundBuf == null ? 0 : foundBuf.length;

        final int foundLen = seekRange(this.handle, filterMin, filterMinLen, filterMax,
            filterMaxLen, filterMaxPos, foundBuf, foundBufSz, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence. {@link ByteBuffer#limit(int)} will
     *      be called with the known size of the value if it is smaller than the
     *      original limit.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<Integer> seekRange(final byte[] filterMin, final ByteBuffer filterMax,
            final ByteBuffer foundBuf) throws HseException {
        final int filterMinLen = filterMin == null ? 0 : filterMin.length;

        int filterMaxLen = 0;
        int filterMaxPos = 0;
        if (filterMax != null) {
            assert filterMax.isDirect();

            filterMaxLen = filterMax.remaining();
            filterMaxPos = filterMax.position();

            filterMax.position(filterMax.limit());
        }

        int foundBufSz = 0;
        int foundBufPos = 0;
        if (foundBuf != null) {
            assert foundBuf.isDirect();

            foundBufSz = foundBuf.remaining();
            foundBufPos = foundBuf.position();
        }

        final int foundLen = seekRange(this.handle, filterMin, filterMinLen, filterMax,
            filterMaxLen, filterMaxPos, foundBuf, foundBufSz, foundBufPos, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        if (foundBuf != null) {
            foundBuf.limit(Math.min(foundBuf.limit(), foundBufPos + foundLen));
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence.
     * @return Length of the found key.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<Integer> seekRange(final String filterMin, final byte[] filterMax,
            final byte[] foundBuf) throws HseException {
        final int filterMaxLen = filterMax == null ? 0 : filterMax.length;
        final int foundBufSz = foundBuf == null ? 0 : foundBuf.length;

        final int foundLen = seekRange(this.handle, filterMin, filterMax, filterMaxLen,
            foundBuf, foundBufSz, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence. {@link ByteBuffer#limit(int)} will
     *      be called with the known size of the value if it is smaller than the
     *      original limit.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<Integer> seekRange(final String filterMin, final byte[] filterMax,
            final ByteBuffer foundBuf) throws HseException {
        final int filterMaxLen = filterMax == null ? 0 : filterMax.length;

        int foundBufSz = 0;
        int foundBufPos = 0;
        if (foundBuf != null) {
            assert foundBuf.isDirect();

            foundBufSz = foundBuf.remaining();
            foundBufPos = foundBuf.position();
        }

        final int foundLen = seekRange(this.handle, filterMin, filterMax, filterMaxLen,
            foundBuf, foundBufSz, foundBufPos, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        if (foundBuf != null) {
            foundBuf.limit(Math.min(foundBuf.limit(), foundBufPos + foundLen));
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence.
     * @return Length of the found key.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<Integer> seekRange(final String filterMin, final String filterMax,
            final byte[] foundBuf) throws HseException {
        final int foundBufSz = foundBuf == null ? 0 : foundBuf.length;

        final int foundLen = seekRange(this.handle, filterMin, filterMax, foundBuf, foundBufSz, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence. {@link ByteBuffer#limit(int)} will
     *      be called with the known size of the value if it is smaller than the
     *      original limit.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<Integer> seekRange(final String filterMin, final String filterMax,
            final ByteBuffer foundBuf) throws HseException {
        int foundBufSz = 0;
        int foundBufPos = 0;
        if (foundBuf != null) {
            assert foundBuf.isDirect();

            foundBufSz = foundBuf.remaining();
            foundBufPos = foundBuf.position();
        }

        final int foundLen = seekRange(this.handle, filterMin, filterMax, foundBuf,
            foundBufSz, foundBufPos, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        if (foundBuf != null) {
            foundBuf.limit(Math.min(foundBuf.limit(), foundBufPos + foundLen));
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<Integer> seekRange(final String filterMin, final ByteBuffer filterMax,
            final byte[] foundBuf) throws HseException {
        int filterMaxLen = 0;
        int filterMaxPos = 0;
        if (filterMax != null) {
            assert filterMax.isDirect();

            filterMaxLen = filterMax.remaining();
            filterMaxPos = filterMax.position();

            filterMax.position(filterMax.limit());
        }

        final int foundBufSz = foundBuf == null ? 0 : foundBuf.length;

        final int foundLen = seekRange(this.handle, filterMin, filterMax, filterMaxLen,
            filterMaxPos, foundBuf, foundBufSz, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence. {@link ByteBuffer#limit(int)} will
     *      be called with the known size of the value if it is smaller than the
     *      original limit.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<Integer> seekRange(final String filterMin, final ByteBuffer filterMax,
            final ByteBuffer foundBuf) throws HseException {
        int filterMaxLen = 0;
        int filterMaxPos = 0;
        if (filterMax != null) {
            assert filterMax.isDirect();

            filterMaxLen = filterMax.remaining();
            filterMaxPos = filterMax.position();

            filterMax.position(filterMax.limit());
        }

        int foundBufSz = 0;
        int foundBufPos = 0;
        if (foundBuf != null) {
            assert foundBuf.isDirect();

            foundBufSz = foundBuf.remaining();
            foundBufPos = foundBuf.position();
        }

        final int foundLen = seekRange(this.handle, filterMin, filterMax, filterMaxLen,
            filterMaxPos, foundBuf, foundBufSz, foundBufPos, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        if (foundBuf != null) {
            foundBuf.limit(Math.min(foundBuf.limit(), foundBufPos + foundLen));
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<Integer> seekRange(final ByteBuffer filterMin, final byte[] filterMax,
            final byte[] foundBuf) throws HseException {
        int filterMinLen = 0;
        int filterMinPos = 0;
        if (filterMin != null) {
            assert filterMin.isDirect();

            filterMinPos = filterMin.position();
            filterMinLen = filterMin.remaining();

            filterMin.position(filterMin.limit());
        }

        final int filterMaxLen = filterMax == null ? 0 : filterMax.length;
        final int foundBufSz = foundBuf == null ? 0 : foundBuf.length;

        final int foundLen = seekRange(this.handle, filterMin, filterMinLen, filterMinPos,
            filterMax, filterMaxLen, foundBuf, foundBufSz, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence. {@link ByteBuffer#limit(int)} will
     *      be called with the known size of the value if it is smaller than the
     *      original limit.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<Integer> seekRange(final ByteBuffer filterMin, final byte[] filterMax,
            final ByteBuffer foundBuf) throws HseException {
        int filterMinLen = 0;
        int filterMinPos = 0;
        if (filterMin != null) {
            assert filterMin.isDirect();

            filterMinLen = filterMin.remaining();
            filterMinPos = filterMin.position();

            filterMin.position(filterMin.limit());
        }

        final int filterMaxLen = filterMax == null ? 0 : filterMax.length;

        int foundBufSz = 0;
        int foundBufPos = 0;
        if (foundBuf != null) {
            assert foundBuf.isDirect();

            foundBufSz = foundBuf.remaining();
            foundBufPos = foundBuf.position();
        }

        final int foundLen = seekRange(this.handle, filterMin, filterMinLen, filterMinPos,
            filterMax, filterMaxLen, foundBuf, foundBufSz, foundBufPos, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        if (foundBuf != null) {
            foundBuf.limit(Math.min(foundBuf.limit(), foundBufPos + foundLen));
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<Integer> seekRange(final ByteBuffer filterMin, final String filterMax,
            final byte[] foundBuf) throws HseException {
        int filterMinLen = 0;
        int filterMinPos = 0;
        if (filterMin != null) {
            assert filterMin.isDirect();

            filterMinLen = filterMin.remaining();
            filterMinPos = filterMin.position();

            filterMin.position(filterMin.limit());
        }

        final int foundBufSz = foundBuf == null ? 0 : foundBuf.length;

        final int foundLen = seekRange(this.handle, filterMin, filterMinLen, filterMinPos,
            filterMax, foundBuf, foundBufSz, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link String} arguments are converted to modified UTF-8.</p>
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence. {@link ByteBuffer#limit(int)} will
     *      be called with the known size of the value if it is smaller than the
     *      original limit.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#modified-utf-8">Modified UTF-8</a>
     */
    public Optional<Integer> seekRange(final ByteBuffer filterMin, final String filterMax,
            final ByteBuffer foundBuf) throws HseException {
        int filterMinLen = 0;
        int filterMinPos = 0;
        if (filterMin != null) {
            assert filterMin.isDirect();

            filterMinLen = filterMin.remaining();
            filterMinPos = filterMin.position();

            filterMin.position(filterMin.limit());
        }

        int foundBufSz = 0;
        int foundBufPos = 0;
        if (foundBuf != null) {
            assert foundBuf.isDirect();

            foundBufSz = foundBuf.remaining();
            foundBufPos = foundBuf.position();
        }

        final int foundLen = seekRange(this.handle, filterMin, filterMinLen, filterMinPos,
            filterMax, foundBuf, foundBufSz, foundBufPos, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        if (foundBuf != null) {
            foundBuf.limit(Math.min(foundBuf.limit(), foundBufPos + foundLen));
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<Integer> seekRange(final ByteBuffer filterMin, final ByteBuffer filterMax,
            final byte[] foundBuf) throws HseException {
        int filterMinLen = 0;
        int filterMinPos = 0;
        if (filterMin != null) {
            assert filterMin.isDirect();

            filterMinLen = filterMin.remaining();
            filterMinPos = filterMin.position();

            filterMin.position(filterMin.limit());
        }

        int filterMaxLen = 0;
        int filterMaxPos = 0;
        if (filterMax != null) {
            assert filterMax.isDirect();

            filterMaxLen = filterMax.remaining();
            filterMaxPos = filterMax.position();

            filterMax.position(filterMax.limit());
        }

        final int foundBufSz = foundBuf == null ? 0 : foundBuf.length;

        final int foundLen = seekRange(this.handle, filterMin, filterMinLen, filterMinPos,
            filterMax, filterMaxLen, filterMaxPos, foundBuf, foundBufSz, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        return Optional.of(foundLen);
    }

    /**
     * Refer to {@link #seekRange(byte[], byte[], byte[])}.
     *
     * <p>Any {@link ByteBuffer} arguments must be direct.</p>
     *
     * <p>
     * Note that the length of any byte buffer given to HSE is
     * {@link ByteBuffer#remaining}.
     * </p>
     *
     * @param filterMin Filter minimum.
     * @param filterMax Filter maximum.
     * @param foundBuf Next key in sequence. {@link ByteBuffer#limit(int)} will
     *      be called with the known size of the value if it is smaller than the
     *      original limit.
     * @return Length of the found key.
     * @throws AssertionError All {@link ByteBuffer} parameters must be direct.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public Optional<Integer> seekRange(final ByteBuffer filterMin, final ByteBuffer filterMax,
            final ByteBuffer foundBuf) throws HseException {
        int filterMinLen = 0;
        int filterMinPos = 0;
        if (filterMin != null) {
            assert filterMin.isDirect();

            filterMinLen = filterMin.remaining();
            filterMinPos = filterMin.position();

            filterMin.position(filterMin.limit());
        }

        int filterMaxLen = 0;
        int filterMaxPos = 0;
        if (filterMax != null) {
            assert filterMax.isDirect();

            filterMaxLen = filterMax.remaining();
            filterMaxPos = filterMax.position();

            filterMax.position(filterMax.limit());
        }

        int foundBufSz = 0;
        int foundBufPos = 0;
        if (foundBuf != null) {
            assert foundBuf.isDirect();

            foundBufSz = foundBuf.remaining();
            foundBufPos = foundBuf.position();
        }

        final int foundLen = seekRange(this.handle, filterMin, filterMinLen, filterMinPos,
            filterMax, filterMaxLen, filterMaxPos, foundBuf, foundBufSz, foundBufPos, 0);

        if (foundLen == 0) {
            return Optional.empty();
        }

        if (foundBuf != null) {
            foundBuf.limit(Math.min(foundBuf.limit(), foundBufPos + foundLen));
        }

        return Optional.of(foundLen);
    }

    /**
     * Update the cursor view.
     *
     * <p>
     * This operation updates the snapshot view of a non-transaction cursor. It
     * is a no-op on transaction cursors.
     * </p>
     *
     * <p>This function is thread safe.</p>
     *
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public void updateView() throws HseException {
        updateView(this.handle);
    }

    /**
     * Destroy cursor.
     *
     * <p>
     * After invoking this function, calling any other cursor functions with
     * this handle will result in undefined behavior.
     * </p>
     *
     * @throws HseException Underlying C function returned a non-zero value.
     */
    @Override
    public void close() throws HseException {
        if (this.handle != 0) {
            destroy(this.handle);
            this.handle = 0;
        }
    }

    /**
     * {@link Kvs#cursor(byte[], EnumSet, Transaction)} (et al.) flags.
     */
    public enum CreateFlags {
        /** Iterate in reverse lexicographical order. */
        REV,
    }
}
