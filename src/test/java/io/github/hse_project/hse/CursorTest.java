/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

package io.github.hse_project.hse;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.AbstractMap.SimpleImmutableEntry;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class CursorTest {
    private static final int NUM_ENTRIES = 5;
    private static Kvdb kvdb;
    private static Kvs kvs;
    private static Kvs txnKvs;

    @BeforeAll
    public static void setupSuite() throws HseException {
        TestUtils.registerShutdownHook();
        Hse.init("rest.enabled=false");
        kvdb = TestUtils.setupKvdb();
    }

    @AfterAll
    public static void tearDownSuite() throws HseException {
        TestUtils.tearDownKvdb(kvdb);
        Hse.fini();
    }

    @BeforeEach
    public void setupTest() throws HseException {
        final String[] cparams = new String[]{"prefix.length=3"};
        final String[] rparams = new String[]{"transactions.enabled=true"};

        kvs = TestUtils.setupKvs(kvdb, "kvs", cparams, null);
        txnKvs = TestUtils.setupKvs(kvdb, "txnKvs", cparams, rparams);

        addData(kvs, null);
    }

    @AfterEach
    public void tearDownTest() throws HseException {
        TestUtils.tearDownKvs(kvdb, kvs);
        TestUtils.tearDownKvs(kvdb, txnKvs);
    }

    public static void addData(final Kvs store, final KvdbTransaction txn) throws HseException {
        for (int i = 0; i < NUM_ENTRIES; i++) {
            final String key = String.format("key%d", i);
            final String value = String.format("value%d", i);

            store.put(key, value, txn);
        }
    }

    @Test
    public void create() throws HseException {
        final String pfx = "key";
        final byte[] pfxData = pfx.getBytes(StandardCharsets.UTF_8);
        final ByteBuffer pfxBuffer = ByteBuffer.allocateDirect(pfxData.length).put(pfxData);

        pfxBuffer.position(0);

        int i = 0;

        try (KvsCursor cursor = kvs.cursor()) {
            for (i = 0;; i++) {
                final byte[] key = String.format("key%d", i).getBytes(StandardCharsets.UTF_8);
                final byte[] value = String.format("value%d", i).getBytes(StandardCharsets.UTF_8);

                final SimpleImmutableEntry<byte[], byte[]> entry = cursor.read();
                assertArrayEquals(key, entry.getKey());
                assertArrayEquals(value, entry.getValue());
            }
        } catch (final EOFException e) {
            assertEquals(NUM_ENTRIES, i);
        }

        try (KvsCursor cursor = kvs.cursor(pfxData)) {
            for (i = 0;; i++) {
                final byte[] key = String.format("key%d", i).getBytes(StandardCharsets.UTF_8);
                final byte[] value = String.format("value%d", i).getBytes(StandardCharsets.UTF_8);

                final SimpleImmutableEntry<byte[], byte[]> entry = cursor.read();
                assertArrayEquals(key, entry.getKey());
                assertArrayEquals(value, entry.getValue());
            }
        } catch (final EOFException e) {
            assertEquals(NUM_ENTRIES, i);
        }

        try (KvsCursor cursor = kvs.cursor(pfx)) {
            for (i = 0;; i++) {
                final byte[] key = String.format("key%d", i).getBytes(StandardCharsets.UTF_8);
                final byte[] value = String.format("value%d", i).getBytes(StandardCharsets.UTF_8);

                final SimpleImmutableEntry<byte[], byte[]> entry = cursor.read();
                assertArrayEquals(key, entry.getKey());
                assertArrayEquals(value, entry.getValue());
            }
        } catch (final EOFException e) {
            assertEquals(NUM_ENTRIES, i);
        }

        try (KvsCursor cursor = kvs.cursor(pfxBuffer)) {
            for (i = 0;; i++) {
                final byte[] key = String.format("key%d", i).getBytes(StandardCharsets.UTF_8);
                final byte[] value = String.format("value%d", i).getBytes(StandardCharsets.UTF_8);

                final SimpleImmutableEntry<byte[], byte[]> entry = cursor.read();
                assertArrayEquals(key, entry.getKey());
                assertArrayEquals(value, entry.getValue());
            }
        } catch (final EOFException e) {
            assertEquals(NUM_ENTRIES, i);
        }
    }

    @Test
    public void create_NullPrefix() throws HseException {
        try (KvsCursor cursor = kvs.cursor((byte[]) null)) { }
        try (KvsCursor cursor = kvs.cursor((String) null)) { }
        try (KvsCursor cursor = kvs.cursor((ByteBuffer) null)) { }
    }

    @Test
    public void create_NonDirectByteBuffer() {
        assertThrows(AssertionError.class, () -> kvs.cursor(ByteBuffer.allocate(4)));
    }

    @Test
    public void create_Reverse() throws HseException {
        final String pfx = "key";
        final byte[] pfxData = pfx.getBytes(StandardCharsets.UTF_8);
        final ByteBuffer pfxBuffer = ByteBuffer.allocateDirect(pfxData.length).put(pfxData);
        final EnumSet<KvsCursor.CreateFlags> flags = EnumSet.of(KvsCursor.CreateFlags.REV);

        pfxBuffer.position(0);

        int i = 0;

        try (KvsCursor cursor = kvs.cursor(flags)) {
            for (i = 0;; i++) {
                final byte[] key = String.format("key%d", NUM_ENTRIES - i - 1).getBytes(
                    StandardCharsets.UTF_8);
                final byte[] value = String.format("value%d", NUM_ENTRIES - i - 1).getBytes(
                    StandardCharsets.UTF_8);

                final SimpleImmutableEntry<byte[], byte[]> entry = cursor.read();
                assertArrayEquals(key, entry.getKey());
                assertArrayEquals(value, entry.getValue());
            }
        } catch (final EOFException e) {
            assertEquals(NUM_ENTRIES, i);
        }

        try (KvsCursor cursor = kvs.cursor(pfxData, flags)) {
            for (i = 0;; i++) {
                final byte[] key = String.format("key%d", NUM_ENTRIES - i - 1).getBytes(
                    StandardCharsets.UTF_8);
                final byte[] value = String.format("value%d", NUM_ENTRIES - i - 1).getBytes(
                    StandardCharsets.UTF_8);

                final SimpleImmutableEntry<byte[], byte[]> entry = cursor.read();
                assertArrayEquals(key, entry.getKey());
                assertArrayEquals(value, entry.getValue());
            }
        } catch (final EOFException e) {
            assertEquals(NUM_ENTRIES, i);
        }

        try (KvsCursor cursor = kvs.cursor(pfx, flags)) {
            for (i = 0;; i++) {
                final byte[] key = String.format("key%d", NUM_ENTRIES - i - 1).getBytes(
                    StandardCharsets.UTF_8);
                final byte[] value = String.format("value%d", NUM_ENTRIES - i - 1).getBytes(
                    StandardCharsets.UTF_8);

                final SimpleImmutableEntry<byte[], byte[]> entry = cursor.read();
                assertArrayEquals(key, entry.getKey());
                assertArrayEquals(value, entry.getValue());
            }
        } catch (final EOFException e) {
            assertEquals(NUM_ENTRIES, i);
        }

        try (KvsCursor cursor = kvs.cursor(pfxBuffer, flags)) {
            for (i = 0;; i++) {
                final byte[] key = String.format("key%d", NUM_ENTRIES - i - 1).getBytes(
                    StandardCharsets.UTF_8);
                final byte[] value = String.format("value%d", NUM_ENTRIES - i - 1).getBytes(
                    StandardCharsets.UTF_8);

                final SimpleImmutableEntry<byte[], byte[]> entry = cursor.read();
                assertArrayEquals(key, entry.getKey());
                assertArrayEquals(value, entry.getValue());
            }
        } catch (final EOFException e) {
            assertEquals(NUM_ENTRIES, i);
        }
    }

    @Test
    public void create_Transactional() throws HseException {
        final int entries = 3;
        final String pfx = "abc";
        final byte[] pfxData = pfx.getBytes(StandardCharsets.UTF_8);
        final ByteBuffer pfxBuffer = ByteBuffer.allocateDirect(pfxData.length).put(pfxData);

        pfxBuffer.position(0);

        try (KvdbTransaction txn = kvdb.transaction()) {
            txn.begin();

            for (int i = 0; i < entries; i++) {
                txnKvs.put(String.format("%s%d", pfx, i), String.format("%s%d", pfx, i), txn);
            }

            int i = 0;

            try (KvsCursor cursor = txnKvs.cursor(txn)) {
                for (i = 0;; i++) {
                    final byte[] key = String.format("%s%d", pfx, i).getBytes(
                        StandardCharsets.UTF_8);
                    final byte[] value = String.format("%s%d", pfx, i).getBytes(
                        StandardCharsets.UTF_8);

                    final SimpleImmutableEntry<byte[], byte[]> entry = cursor.read();
                    assertArrayEquals(key, entry.getKey());
                    assertArrayEquals(value, entry.getValue());
                }
            } catch (final EOFException e) {
                assertEquals(entries, i);
            }

            try (KvsCursor cursor = txnKvs.cursor(pfxData, txn)) {
                for (i = 0;; i++) {
                    final byte[] key = String.format("%s%d", pfx, i).getBytes(
                        StandardCharsets.UTF_8);
                    final byte[] value = String.format("%s%d", pfx, i).getBytes(
                        StandardCharsets.UTF_8);

                    final SimpleImmutableEntry<byte[], byte[]> entry = cursor.read();
                    assertArrayEquals(key, entry.getKey());
                    assertArrayEquals(value, entry.getValue());
                }
            } catch (final EOFException e) {
                assertEquals(entries, i);
            }

            try (KvsCursor cursor = txnKvs.cursor(pfx, txn)) {
                for (i = 0;; i++) {
                    final byte[] key = String.format("%s%d", pfx, i).getBytes(
                        StandardCharsets.UTF_8);
                    final byte[] value = String.format("%s%d", pfx, i).getBytes(
                        StandardCharsets.UTF_8);

                    final SimpleImmutableEntry<byte[], byte[]> entry = cursor.read();
                    assertArrayEquals(key, entry.getKey());
                    assertArrayEquals(value, entry.getValue());
                }
            } catch (final EOFException e) {
                assertEquals(entries, i);
            }

            try (KvsCursor cursor = txnKvs.cursor(pfxBuffer, txn)) {
                for (i = 0;; i++) {
                    final byte[] key = String.format("%s%d", pfx, i).getBytes(
                        StandardCharsets.UTF_8);
                    final byte[] value = String.format("%s%d", pfx, i).getBytes(
                        StandardCharsets.UTF_8);

                    final SimpleImmutableEntry<byte[], byte[]> entry = cursor.read();
                    assertArrayEquals(key, entry.getKey());
                    assertArrayEquals(value, entry.getValue());
                }
            } catch (final EOFException e) {
                assertEquals(entries, i);
            }

            txn.abort();
        }
    }

    @Test
    public void read() throws HseException {
        final byte[] keyArrayBuf = new byte[10];
        final byte[] valueArrayBuf = new byte[10];
        final ByteBuffer keyBufferBuf = ByteBuffer.allocateDirect(10);
        final ByteBuffer valueBufferBuf = ByteBuffer.allocateDirect(10);

        try (KvsCursor cursor = kvs.cursor()) {
            final SimpleImmutableEntry<byte[], byte[]> entry = cursor.read();
            assertArrayEquals("key0".getBytes(StandardCharsets.UTF_8), entry.getKey());
            assertArrayEquals("value0".getBytes(StandardCharsets.UTF_8), entry.getValue());
        } catch (final EOFException e) {
            fail(e);
        }

        try (KvsCursor cursor = kvs.cursor()) {
            final SimpleImmutableEntry<Integer, Integer> entry =
                cursor.read(keyArrayBuf, valueArrayBuf);
            assertArrayEquals("key0".getBytes(StandardCharsets.UTF_8), Arrays.copyOf(keyArrayBuf,
                entry.getKey()));
            assertArrayEquals("value0".getBytes(StandardCharsets.UTF_8), Arrays.copyOf(
                valueArrayBuf, entry.getValue()));
        } catch (final EOFException e) {
            fail(e);
        }

        Arrays.fill(keyArrayBuf, (byte) 0);
        Arrays.fill(valueArrayBuf, (byte) 0);

        try (KvsCursor cursor = kvs.cursor()) {
            final SimpleImmutableEntry<Integer, Integer> entry = cursor.read(keyArrayBuf,
                valueBufferBuf);
            valueBufferBuf.get(valueArrayBuf, 0, entry.getValue());
            assertArrayEquals("key0".getBytes(StandardCharsets.UTF_8), Arrays.copyOf(keyArrayBuf,
                entry.getKey()));
            assertArrayEquals("value0".getBytes(StandardCharsets.UTF_8), Arrays.copyOf(
                valueArrayBuf, entry.getValue()));
        } catch (final EOFException e) {
            fail(e);
        }

        Arrays.fill(keyArrayBuf, (byte) 0);
        Arrays.fill(valueArrayBuf, (byte) 0);
        valueBufferBuf.position(0);
        valueBufferBuf.clear();

        try (KvsCursor cursor = kvs.cursor()) {
            final SimpleImmutableEntry<Integer, Integer> entry = cursor.read(keyBufferBuf,
                valueArrayBuf);
            keyBufferBuf.get(keyArrayBuf, 0, entry.getKey());
            assertArrayEquals("key0".getBytes(StandardCharsets.UTF_8), Arrays.copyOf(keyArrayBuf,
                entry.getKey()));
            assertArrayEquals("value0".getBytes(StandardCharsets.UTF_8), Arrays.copyOf(
                valueArrayBuf, entry.getValue()));
        } catch (final EOFException e) {
            fail(e);
        }

        Arrays.fill(keyArrayBuf, (byte) 0);
        Arrays.fill(valueArrayBuf, (byte) 0);
        keyBufferBuf.clear();
        keyBufferBuf.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final SimpleImmutableEntry<Integer, Integer> entry = cursor.read(keyBufferBuf,
                valueBufferBuf);
            keyBufferBuf.get(keyArrayBuf, 0, entry.getKey());
            valueBufferBuf.get(valueArrayBuf, 0, entry.getValue());
            assertArrayEquals("key0".getBytes(StandardCharsets.UTF_8), Arrays.copyOf(keyArrayBuf,
                entry.getKey()));
            assertArrayEquals("value0".getBytes(StandardCharsets.UTF_8), Arrays.copyOf(
                valueArrayBuf, entry.getValue()));
        } catch (final EOFException e) {
            fail(e);
        }
    }

    @Test
    public void read_NullBuffers() throws HseException {
        final byte[] keyArrayBuf = new byte[4];
        final ByteBuffer keyBufferBuf = ByteBuffer.allocateDirect(4);

        try (KvsCursor cursor = kvs.cursor()) {
            assertThrows(HseException.class, () -> cursor.read((byte[]) null, (byte[]) null));
            assertThrows(HseException.class, () -> cursor.read((byte[]) null, (ByteBuffer) null));
            assertThrows(HseException.class, () -> cursor.read((ByteBuffer) null, (byte[]) null));
            assertThrows(HseException.class, () -> {
                cursor.read((ByteBuffer) null, (ByteBuffer) null);
            });
        }

        try (KvsCursor cursor = kvs.cursor()) {
            final SimpleImmutableEntry<Integer, Integer> entry = cursor.read(keyArrayBuf,
                (byte[]) null);

            assertEquals(4, entry.getKey());
            assertEquals(6, entry.getValue());
        } catch (final EOFException e) {
            fail(e);
        }

        try (KvsCursor cursor = kvs.cursor()) {
            final SimpleImmutableEntry<Integer, Integer> entry = cursor.read(keyArrayBuf,
                (ByteBuffer) null);

            assertEquals(4, entry.getKey());
            assertEquals(6, entry.getValue());
        } catch (final EOFException e) {
            fail(e);
        }

        try (KvsCursor cursor = kvs.cursor()) {
            final SimpleImmutableEntry<Integer, Integer> entry = cursor.read(keyBufferBuf,
                (byte[]) null);

            assertEquals(4, entry.getKey());
            assertEquals(6, entry.getValue());
        } catch (final EOFException e) {
            fail(e);
        }

        keyBufferBuf.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final SimpleImmutableEntry<Integer, Integer> entry = cursor.read(keyBufferBuf,
                (ByteBuffer) null);

            assertEquals(4, entry.getKey());
            assertEquals(6, entry.getValue());
        } catch (final EOFException e) {
            fail(e);
        }
    }

    @Test
    public void read_NonDirectByteBuffers() {
        final byte[] array = new byte[4];
        final ByteBuffer direct = ByteBuffer.allocateDirect(4);
        final ByteBuffer nonDirect = ByteBuffer.allocate(4);

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.read(array, nonDirect);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.read(nonDirect, direct);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.read(direct, nonDirect);
            }
        });
    }

    @Test
    public void seek() throws HseException {
        final String key = "key3";
        final byte[] keyData = key.getBytes(StandardCharsets.UTF_8);
        final ByteBuffer keyBuffer = ByteBuffer.allocateDirect(keyData.length).put(keyData);

        keyBuffer.position(0);

        final byte[] foundArrayBuf = new byte[4];
        final ByteBuffer foundBufferBuf = ByteBuffer.allocateDirect(4);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<byte[]> found = cursor.seek(keyData);
            assertArrayEquals(keyData, found.get());
        }

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<byte[]> found = cursor.seek(key);
            assertArrayEquals(keyData, found.get());
        }

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<byte[]> found = cursor.seek(keyBuffer);
            assertArrayEquals(keyData, found.get());
        }

        keyBuffer.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seek(keyData, foundArrayBuf);
            assertEquals(4, found.get());
            assertArrayEquals(keyData, Arrays.copyOf(foundArrayBuf, found.get()));
        }

        Arrays.fill(foundArrayBuf, (byte) 0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seek(keyData, foundBufferBuf);
            assertEquals(4, found.get());
            foundBufferBuf.get(foundArrayBuf);
            assertArrayEquals(keyData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        foundBufferBuf.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seek(key, foundArrayBuf);
            assertEquals(4, found.get());
            assertArrayEquals(keyData, Arrays.copyOf(foundArrayBuf, found.get()));
        }

        Arrays.fill(foundArrayBuf, (byte) 0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seek(key, foundBufferBuf);
            assertEquals(4, found.get());
            foundBufferBuf.get(foundArrayBuf);
            assertArrayEquals(keyData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        foundBufferBuf.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seek(keyBuffer, foundArrayBuf);
            assertEquals(4, found.get());
            assertArrayEquals(keyData, Arrays.copyOf(foundArrayBuf, found.get()));
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        keyBuffer.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seek(keyBuffer, foundBufferBuf);
            assertEquals(4, found.get());
            foundBufferBuf.get(foundArrayBuf);
            assertArrayEquals(keyData, foundArrayBuf);
        }
    }

    @Test
    public void seek_NonDirectByteBuffer() {
        final ByteBuffer nonDirect = ByteBuffer.allocate(6);

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seek(nonDirect);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seek((byte[]) null, nonDirect);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seek((String) null, nonDirect);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seek((ByteBuffer) null, nonDirect);
            }
        });
    }

    @Test
    public void seekRange() throws HseException {
        final String filterMin = "key0";
        final String filterMax = "key4";
        final byte[] filterMinData = filterMin.getBytes(StandardCharsets.UTF_8);
        final byte[] filterMaxData = filterMax.getBytes(StandardCharsets.UTF_8);
        final ByteBuffer filterMinBuffer = ByteBuffer.allocateDirect(filterMaxData.length)
            .put(filterMinData);
        final ByteBuffer filterMaxBuffer = ByteBuffer.allocateDirect(filterMaxData.length)
            .put(filterMaxData);

        filterMinBuffer.position(0);
        filterMaxBuffer.position(0);

        final byte[] foundArrayBuf = new byte[4];
        final ByteBuffer foundBufferBuf = ByteBuffer.allocateDirect(4);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<byte[]> found = cursor.seekRange(filterMinData, filterMaxData);
            assertArrayEquals(filterMinData, found.get());
        }

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<byte[]> found = cursor.seekRange(filterMinData, filterMax);
            assertArrayEquals(filterMinData, found.get());
        }

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<byte[]> found = cursor.seekRange(filterMinData, filterMaxBuffer);
            assertArrayEquals(filterMinData, found.get());
        }

        filterMaxBuffer.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<byte[]> found = cursor.seekRange(filterMin, filterMaxData);
            assertArrayEquals(filterMinData, found.get());
        }

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<byte[]> found = cursor.seekRange(filterMin, filterMax);
            assertArrayEquals(filterMinData, found.get());
        }

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<byte[]> found = cursor.seekRange(filterMin, filterMaxBuffer);
            assertArrayEquals(filterMinData, found.get());
        }

        filterMaxBuffer.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<byte[]> found = cursor.seekRange(filterMinBuffer, filterMaxData);
            assertArrayEquals(filterMinData, found.get());
        }

        filterMinBuffer.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<byte[]> found = cursor.seekRange(filterMinBuffer, filterMax);
            assertArrayEquals(filterMinData, found.get());
        }

        filterMinBuffer.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<byte[]> found = cursor.seekRange(filterMinBuffer, filterMaxBuffer);
            assertArrayEquals(filterMinData, found.get());
        }

        filterMinBuffer.position(0);
        filterMaxBuffer.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMinData, filterMaxData,
                foundArrayBuf);
            assertEquals(4, found.get());
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMinData, filterMaxData,
                foundBufferBuf);
            assertEquals(4, found.get());
            foundBufferBuf.get(foundArrayBuf);
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        foundBufferBuf.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMinData, filterMax,
                foundArrayBuf);
            assertEquals(4, found.get());
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMinData, filterMax,
                foundBufferBuf);
            assertEquals(4, found.get());
            foundBufferBuf.get(foundArrayBuf);
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        foundBufferBuf.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMinData, filterMaxBuffer,
                foundArrayBuf);
            assertEquals(4, found.get());
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        filterMaxBuffer.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMinData, filterMaxBuffer,
                foundBufferBuf);
            assertEquals(4, found.get());
            foundBufferBuf.get(foundArrayBuf);
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        filterMaxBuffer.position(0);
        foundBufferBuf.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMin, filterMaxData,
                foundArrayBuf);
            assertEquals(4, found.get());
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMin, filterMaxData,
                foundBufferBuf);
            assertEquals(4, found.get());
            foundBufferBuf.get(foundArrayBuf);
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        foundBufferBuf.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMin, filterMax, foundArrayBuf);
            assertEquals(4, found.get());
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMin, filterMax, foundBufferBuf);
            assertEquals(4, found.get());
            foundBufferBuf.get(foundArrayBuf);
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        foundBufferBuf.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMin, filterMaxBuffer,
                foundArrayBuf);
            assertEquals(4, found.get());
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        filterMaxBuffer.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMin, filterMaxBuffer,
                foundBufferBuf);
            assertEquals(4, found.get());
            foundBufferBuf.get(foundArrayBuf);
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        filterMaxBuffer.position(0);
        foundBufferBuf.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMinBuffer, filterMaxData,
                foundArrayBuf);
            assertEquals(4, found.get());
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        filterMinBuffer.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMinBuffer, filterMaxData,
                foundBufferBuf);
            assertEquals(4, found.get());
            foundBufferBuf.get(foundArrayBuf);
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        filterMinBuffer.position(0);
        foundBufferBuf.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMinBuffer, filterMax,
                foundArrayBuf);
            assertEquals(4, found.get());
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        filterMinBuffer.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMinBuffer, filterMax,
                foundBufferBuf);
            assertEquals(4, found.get());
            foundBufferBuf.get(foundArrayBuf);
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        foundBufferBuf.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMinBuffer, filterMaxBuffer,
                foundArrayBuf);
            assertEquals(4, found.get());
            assertArrayEquals(filterMinData, foundArrayBuf);
        }

        Arrays.fill(foundArrayBuf, (byte) 0);
        filterMinBuffer.position(0);
        filterMaxBuffer.position(0);

        try (KvsCursor cursor = kvs.cursor()) {
            final Optional<Integer> found = cursor.seekRange(filterMinBuffer, filterMaxBuffer,
                foundBufferBuf);
            assertEquals(4, found.get());
            foundBufferBuf.get(foundArrayBuf);
            assertArrayEquals(filterMinData, foundArrayBuf);
        }
    }

    @Test
    public void seekRange_NonDirectByteBuffers() throws HseException {
        final ByteBuffer nonDirect = ByteBuffer.allocate(4);

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seekRange((byte[]) null, nonDirect);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seekRange((String) null, nonDirect);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seekRange((ByteBuffer) null, nonDirect);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seekRange(nonDirect, (ByteBuffer) null);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seekRange((byte[]) null, (byte[]) null, nonDirect);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seekRange((byte[]) null, (String) null, nonDirect);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seekRange((byte[]) null, (ByteBuffer) null, nonDirect);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seekRange((String) null, (byte[]) null, nonDirect);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seekRange((String) null, (String) null, nonDirect);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seekRange((String) null, (ByteBuffer) null, nonDirect);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seekRange((ByteBuffer) null, (byte[]) null, nonDirect);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seekRange((ByteBuffer) null, (String) null, nonDirect);
            }
        });

        assertThrows(AssertionError.class, () -> {
            try (KvsCursor cursor = kvs.cursor()) {
                cursor.seekRange((ByteBuffer) null, (ByteBuffer) null, nonDirect);
            }
        });
    }

    @Test
    public void createFlags() {
        for (final KvsCursor.CreateFlags flag : EnumSet.allOf(KvsCursor.CreateFlags.class)) {
            switch (flag) {
                case REV:
                    assertEquals(0, flag.ordinal());
                    break;
                default:
                    throw new UnsupportedOperationException(
                        "Unknown cursor create flag: " + flag.toString());
            }
        }
    }
}
