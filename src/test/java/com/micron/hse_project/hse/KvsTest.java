/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

package com.micron.hse_project.hse;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class KvsTest {
    private static final int NUM_ENTRIES = 5;
    private static Kvdb kvdb;
    private static Kvs kvs;
    private static Kvs txnKvs;

    @BeforeAll
    public static void setupSuite() throws HseException {
        Hse.init("rest.enabled=false");
        kvdb = TestUtils.setupKvdb();
    }

    @AfterAll
    public static void tearDownSuite() throws HseException {
        TestUtils.tearDownKvdb(kvdb);
        Hse.fini();
    }

    public static void addData(final Kvs store, final Transaction txn) throws HseException {
        for (int i = 0; i < NUM_ENTRIES; i++) {
            final String key = String.format("key%d", i);
            final String value = String.format("value%d", i);

            store.put(key, value, txn);
        }
    }

    @BeforeEach
    public void setupTest() throws HseException {
        final String[] cparams = new String[]{"prefix.length=3"};
        final String[] rparams = new String[]{"transactions.enabled=true"};

        kvs = TestUtils.setupKvs(kvdb, "kvs", cparams, null);
        txnKvs = TestUtils.setupKvs(kvdb, "txnKvs", cparams, rparams);

        addData(kvs, null);
        try (Transaction txn = kvdb.transaction()) {
            txn.begin();

            addData(txnKvs, txn);
        }
    }

    @AfterEach
    public void tearDownTest() throws HseException {
        TestUtils.tearDownKvs(kvdb, kvs);
        TestUtils.tearDownKvs(kvdb, txnKvs);
    }

    @Test
    public void delete_NullKey() throws HseException {
        assertThrows(HseException.class, () -> kvs.delete((byte[]) null));
        assertThrows(HseException.class, () -> kvs.delete((String) null));
        assertThrows(HseException.class, () -> kvs.delete((ByteBuffer) null));
    }

    @Test
    public void delete_NonDirectByteBuffer() {
        assertThrows(AssertionError.class, () -> kvs.delete(ByteBuffer.allocate(5)));
    }

    @Test
    public void delete() throws HseException {
        kvs.delete("key0");
        assertFalse(kvs.get("key0").isPresent());

        kvs.delete("key1".getBytes(StandardCharsets.UTF_8));
        assertFalse(kvs.get("key1").isPresent());

        final ByteBuffer keyBuffer = ByteBuffer.allocateDirect(4)
            .put("key2".getBytes(StandardCharsets.UTF_8));
        keyBuffer.position(0);
        kvs.delete(keyBuffer);
        assertFalse(kvs.get("key2").isPresent());
    }

    @Test
    public void delete_Transactional() throws HseException {
        try (Transaction txn = kvdb.transaction()) {
            txn.begin();

            final String key = String.format("key%d", NUM_ENTRIES);
            final String value = String.format("value%d", NUM_ENTRIES);

            txnKvs.put(key, value, txn);
            txnKvs.delete(key, txn);
            assertFalse(txnKvs.get(key, txn).isPresent());

            txnKvs.put(key, value, txn);
            txnKvs.delete(key.getBytes(StandardCharsets.UTF_8), txn);
            assertFalse(txnKvs.get(key, txn).isPresent());

            txnKvs.put(key, value, txn);
            final ByteBuffer keyBuffer = ByteBuffer.allocateDirect(4)
                .put(key.getBytes(StandardCharsets.UTF_8));
            keyBuffer.position(0);
            txnKvs.delete(keyBuffer, txn);
            assertFalse(txnKvs.get(key, txn).isPresent());

            txn.abort();
        }
    }

    @Test
    public void get_NullKey() throws HseException {
        assertThrows(HseException.class, () -> kvs.get((byte[]) null));
        assertThrows(HseException.class, () -> kvs.get((String) null));
        assertThrows(HseException.class, () -> kvs.get((ByteBuffer) null));
        assertThrows(HseException.class, () -> kvs.get((byte[]) null, (byte[]) null));
        assertThrows(HseException.class, () -> kvs.get((String) null, (byte[]) null));
        assertThrows(HseException.class, () -> kvs.get((byte[]) null, (ByteBuffer) null));
        assertThrows(HseException.class, () -> kvs.get((String) null, (ByteBuffer) null));
        assertThrows(HseException.class, () -> kvs.get((ByteBuffer) null, (ByteBuffer) null));
    }

    @Test
    public void get_NullValueBuf() throws HseException {
        final String key = "key0";
        final byte[] keyData = key.getBytes(StandardCharsets.UTF_8);
        Optional<Integer> valueLen;

        valueLen = kvs.get(keyData, (byte[]) null);
        assertEquals(6, valueLen.get());

        valueLen = kvs.get("key0", (byte[]) null);
        assertEquals(6, valueLen.get());

        final ByteBuffer buf = ByteBuffer.allocateDirect(keyData.length);
        buf.put(keyData);
        buf.position(0);
        valueLen = kvs.get(buf, (ByteBuffer) null);
        assertEquals(6, valueLen.get());
    }

    @Test
    public void get_NonDirectByteBuffer() {
        final ByteBuffer direct = ByteBuffer.allocateDirect(5);
        final ByteBuffer nonDirect = ByteBuffer.allocate(5);

        assertThrows(AssertionError.class, () -> kvs.get(nonDirect));
        assertThrows(AssertionError.class, () -> kvs.get((byte[]) null, nonDirect));
        assertThrows(AssertionError.class, () -> kvs.get((String) null, nonDirect));
        assertThrows(AssertionError.class, () -> kvs.get(nonDirect, (byte[]) null));
        assertThrows(AssertionError.class, () -> kvs.get(direct, nonDirect));
        assertThrows(AssertionError.class, () -> kvs.get(nonDirect, direct));
    }

    @Test
    public void get() throws HseException {
        final String key = "key0";
        final byte[] value = "value0".getBytes(StandardCharsets.UTF_8);
        final byte[] keyData = key.getBytes(StandardCharsets.UTF_8);
        final ByteBuffer keyBuffer = ByteBuffer.allocateDirect(keyData.length);
        final byte[] valueBufArray = new byte[value.length];
        final ByteBuffer valueBufBuffer = ByteBuffer.allocateDirect(value.length);
        Optional<Integer> valueLen;

        assertArrayEquals(value, kvs.get(key).get());
        assertArrayEquals(value, kvs.get(keyData).get());
        keyBuffer.put(keyData);
        keyBuffer.position(0);
        assertArrayEquals(value, kvs.get(keyBuffer).get());
        keyBuffer.position(0);

        valueLen = kvs.get(keyData, valueBufArray);
        assertEquals(6, valueLen.get());
        assertArrayEquals(value, valueBufArray);
        Arrays.fill(valueBufArray, (byte) 0);

        valueLen = kvs.get(keyData, valueBufBuffer);
        assertEquals(6, valueLen.get());
        valueBufBuffer.get(valueBufArray);
        assertArrayEquals(value, valueBufArray);
        Arrays.fill(valueBufArray, (byte) 0);
        valueBufBuffer.position(0);
        valueBufBuffer.clear();

        valueLen = kvs.get(key, valueBufArray);
        assertEquals(6, valueLen.get());
        assertArrayEquals(value, valueBufArray);
        Arrays.fill(valueBufArray, (byte) 0);

        valueLen = kvs.get(key, valueBufBuffer);
        assertEquals(6, valueLen.get());
        valueBufBuffer.get(valueBufArray);
        assertArrayEquals(value, valueBufArray);
        Arrays.fill(valueBufArray, (byte) 0);
        valueBufBuffer.position(0);
        valueBufBuffer.clear();

        valueLen = kvs.get(keyBuffer, valueBufArray);
        assertEquals(6, valueLen.get());
        assertArrayEquals(value, valueBufArray);
        Arrays.fill(valueBufArray, (byte) 0);
        keyBuffer.position(0);

        valueLen = kvs.get(keyBuffer, valueBufBuffer);
        assertEquals(6, valueLen.get());
        valueBufBuffer.get(valueBufArray);
        assertArrayEquals(value, valueBufArray);
        Arrays.fill(valueBufArray, (byte) 0);
    }

    @Test
    public void get_Transactional() throws HseException {
        try (Transaction txn = kvdb.transaction()) {
            txn.begin();

            final String key = String.format("key%d", NUM_ENTRIES);
            final byte[] keyData = key.getBytes(StandardCharsets.UTF_8);
            final byte[] value = String.format("value%d", NUM_ENTRIES)
                .getBytes(StandardCharsets.UTF_8);
            final ByteBuffer keyBuffer = ByteBuffer.allocateDirect(keyData.length);
            final byte[] valueBufArray = new byte[value.length];
            final ByteBuffer valueBufBuffer = ByteBuffer.allocateDirect(value.length);
            Optional<Integer> valueLen;

            txnKvs.put(key, value, txn);

            assertArrayEquals(value, txnKvs.get(key, txn).get());
            assertArrayEquals(value, txnKvs.get(keyData, txn).get());
            keyBuffer.put(keyData);
            keyBuffer.position(0);
            assertArrayEquals(value, txnKvs.get(keyBuffer, txn).get());
            keyBuffer.position(0);

            valueLen = txnKvs.get(keyData, valueBufArray, txn);
            assertEquals(6, valueLen.get());
            assertArrayEquals(value, valueBufArray);
            Arrays.fill(valueBufArray, (byte) 0);

            valueLen = txnKvs.get(keyData, valueBufBuffer, txn);
            assertEquals(6, valueLen.get());
            valueBufBuffer.get(valueBufArray);
            assertArrayEquals(value, valueBufArray);
            Arrays.fill(valueBufArray, (byte) 0);
            valueBufBuffer.position(0);
            valueBufBuffer.clear();

            valueLen = txnKvs.get(key, valueBufArray, txn);
            assertEquals(6, valueLen.get());
            assertArrayEquals(value, valueBufArray);
            Arrays.fill(valueBufArray, (byte) 0);

            valueLen = txnKvs.get(key, valueBufBuffer, txn);
            assertEquals(6, valueLen.get());
            valueBufBuffer.get(valueBufArray);
            assertArrayEquals(value, valueBufArray);
            Arrays.fill(valueBufArray, (byte) 0);
            valueBufBuffer.position(0);
            valueBufBuffer.clear();

            valueLen = txnKvs.get(keyBuffer, valueBufArray, txn);
            assertEquals(6, valueLen.get());
            assertArrayEquals(value, valueBufArray);
            Arrays.fill(valueBufArray, (byte) 0);
            keyBuffer.position(0);

            valueLen = txnKvs.get(keyBuffer, valueBufBuffer, txn);
            assertEquals(6, valueLen.get());
            valueBufBuffer.get(valueBufArray);
            assertArrayEquals(value, valueBufArray);

            txn.abort();
        }
    }

    @Test
    public void getName() {
        assertEquals("kvs", kvs.getName());
    }

    @Test
    public void getParam_NullParam() {
        assertThrows(HseException.class, () -> kvs.getParam(null));
    }

    @Test
    public void getParam() throws HseException {
        final String value = kvs.getParam("transactions.enabled");

        assertEquals("false", value);
    }

    @Test
    public void prefixDelete_NullPrefix() {
        assertThrows(HseException.class, () -> kvs.prefixDelete((byte[]) null));
        assertThrows(HseException.class, () -> kvs.prefixDelete((String) null));
        assertThrows(HseException.class, () -> kvs.prefixDelete((ByteBuffer) null));
    }

    @Test
    public void prefixDelete_NonDirectByteBuffer() {
        assertThrows(AssertionError.class, () -> kvs.prefixDelete(ByteBuffer.allocate(3)));
    }

    @Test
    public void prefixDelete() throws HseException {
        final String pfx = "key";
        final byte[] pfxData = pfx.getBytes(StandardCharsets.UTF_8);
        final ByteBuffer pfxBuffer = ByteBuffer.allocateDirect(pfxData.length);

        pfxBuffer.put(pfxData);
        pfxBuffer.position(0);

        kvs.prefixDelete(pfxData);
        assertFalse(kvs.get("key0").isPresent());
        addData(kvs, null);

        kvs.prefixDelete(pfx);
        assertFalse(kvs.get("key0").isPresent());
        addData(kvs, null);

        kvs.prefixDelete(pfxBuffer);
        assertFalse(kvs.get("key0").isPresent());
    }

    @Test
    public void put_NullKey() {
        assertThrows(HseException.class, () -> kvs.put((byte[]) null, (byte[]) null));
        assertThrows(HseException.class, () -> kvs.put((String) null, (byte[]) null));
        assertThrows(HseException.class, () -> kvs.put((ByteBuffer) null, (byte[]) null));
    }

    @Test
    public void put_NonDirectByteBuffer() {
        final String key = "key0";
        final String value = "value0";
        final byte[] keyData = key.getBytes(StandardCharsets.UTF_8);
        final byte[] valueData = value.getBytes(StandardCharsets.UTF_8);
        final ByteBuffer keyBuffer = ByteBuffer.allocateDirect(keyData.length).put(keyData);
        final ByteBuffer valueBuffer = ByteBuffer.allocateDirect(valueData.length).put(valueData);
        final ByteBuffer nonDirect = ByteBuffer.allocate(keyData.length);

        valueBuffer.position(0);
        keyBuffer.position(0);

        assertThrows(AssertionError.class, () -> kvs.put(keyData, nonDirect));
        assertThrows(AssertionError.class, () -> kvs.put(key, nonDirect));
        assertThrows(AssertionError.class, () -> kvs.put(keyBuffer, nonDirect));
        assertThrows(AssertionError.class, () -> kvs.put(nonDirect, valueData));
        assertThrows(AssertionError.class, () -> kvs.put(nonDirect, value));
        assertThrows(AssertionError.class, () -> kvs.put(nonDirect, valueBuffer));
    }

    @Test
    public void put() throws HseException {
        final String key = String.format("key%d", NUM_ENTRIES);
        final String value = String.format("value%d", NUM_ENTRIES);
        final byte[] keyData = key.getBytes(StandardCharsets.UTF_8);
        final byte[] valueData = value.getBytes(StandardCharsets.UTF_8);
        final ByteBuffer keyBuffer = ByteBuffer.allocateDirect(keyData.length).put(keyData);
        final ByteBuffer valueBuffer = ByteBuffer.allocateDirect(valueData.length).put(valueData);

        keyBuffer.position(0);
        valueBuffer.position(0);

        assertFalse(kvs.get(key).isPresent());

        kvs.put(keyData, valueData);
        assertArrayEquals(valueData, kvs.get(key).get());
        kvs.delete(key);
        assertFalse(kvs.get(key).isPresent());

        kvs.put(keyData, value);
        assertArrayEquals(valueData, kvs.get(key).get());
        kvs.delete(key);
        assertFalse(kvs.get(key).isPresent());

        kvs.put(keyData, valueBuffer);
        assertArrayEquals(valueData, kvs.get(key).get());
        kvs.delete(key);
        assertFalse(kvs.get(key).isPresent());
        valueBuffer.position(0);

        kvs.put(key, valueData);
        assertArrayEquals(valueData, kvs.get(key).get());
        kvs.delete(key);
        assertFalse(kvs.get(key).isPresent());

        kvs.put(key, value);
        assertArrayEquals(valueData, kvs.get(key).get());
        kvs.delete(key);
        assertFalse(kvs.get(key).isPresent());

        kvs.put(key, valueBuffer);
        assertArrayEquals(valueData, kvs.get(key).get());
        kvs.delete(key);
        assertFalse(kvs.get(key).isPresent());
        valueBuffer.position(0);

        kvs.put(keyBuffer, valueData);
        assertArrayEquals(valueData, kvs.get(key).get());
        kvs.delete(key);
        assertFalse(kvs.get(key).isPresent());
        keyBuffer.position(0);

        kvs.put(keyBuffer, value);
        assertArrayEquals(valueData, kvs.get(key).get());
        kvs.delete(key);
        assertFalse(kvs.get(key).isPresent());
        keyBuffer.position(0);

        kvs.put(keyBuffer, valueBuffer);
        assertArrayEquals(valueData, kvs.get(key).get());
        kvs.delete(key);
        assertFalse(kvs.get(key).isPresent());
        keyBuffer.position(0);
        valueBuffer.position(0);
    }

    @Test
    public void put_Transactional() throws HseException {
        final String key = String.format("key%d", NUM_ENTRIES);
        final String value = String.format("value%d", NUM_ENTRIES);
        final byte[] keyData = key.getBytes(StandardCharsets.UTF_8);
        final byte[] valueData = value.getBytes(StandardCharsets.UTF_8);
        final ByteBuffer keyBuffer = ByteBuffer.allocateDirect(keyData.length).put(keyData);
        final ByteBuffer valueBuffer = ByteBuffer.allocateDirect(valueData.length).put(valueData);

        keyBuffer.position(0);
        valueBuffer.position(0);

        try (Transaction txn = kvdb.transaction()) {
            txn.begin();

            assertFalse(txnKvs.get(key).isPresent());

            txnKvs.put(keyData, valueData, txn);
            assertArrayEquals(valueData, txnKvs.get(key, txn).get());
            txnKvs.delete(key, txn);
            assertFalse(txnKvs.get(key, txn).isPresent());

            txnKvs.put(keyData, value, txn);
            assertArrayEquals(valueData, txnKvs.get(key, txn).get());
            txnKvs.delete(key, txn);
            assertFalse(txnKvs.get(key, txn).isPresent());

            txnKvs.put(keyData, valueBuffer, txn);
            assertArrayEquals(valueData, txnKvs.get(key, txn).get());
            txnKvs.delete(key, txn);
            assertFalse(txnKvs.get(key, txn).isPresent());
            valueBuffer.position(0);

            txnKvs.put(key, valueData, txn);
            assertArrayEquals(valueData, txnKvs.get(key, txn).get());
            txnKvs.delete(key, txn);
            assertFalse(txnKvs.get(key, txn).isPresent());

            txnKvs.put(key, value, txn);
            assertArrayEquals(valueData, txnKvs.get(key, txn).get());
            txnKvs.delete(key, txn);
            assertFalse(txnKvs.get(key, txn).isPresent());

            txnKvs.put(key, valueBuffer, txn);
            assertArrayEquals(valueData, txnKvs.get(key, txn).get());
            txnKvs.delete(key, txn);
            assertFalse(txnKvs.get(key).isPresent());
            valueBuffer.position(0);

            txnKvs.put(keyBuffer, valueData, txn);
            assertArrayEquals(valueData, txnKvs.get(key, txn).get());
            txnKvs.delete(key, txn);
            assertFalse(txnKvs.get(key, txn).isPresent());
            keyBuffer.position(0);

            txnKvs.put(keyBuffer, value, txn);
            assertArrayEquals(valueData, txnKvs.get(key, txn).get());
            txnKvs.delete(key, txn);
            assertFalse(txnKvs.get(key, txn).isPresent());
            keyBuffer.position(0);

            txnKvs.put(keyBuffer, valueBuffer, txn);
            assertArrayEquals(valueData, txnKvs.get(key, txn).get());
            txnKvs.delete(key, txn);
            assertFalse(txnKvs.get(key, txn).isPresent());
            keyBuffer.position(0);
            valueBuffer.position(0);

            txn.abort();
        }
    }

    @Test
    public void putFlags() {
        for (final Kvs.PutFlags flag : EnumSet.allOf(Kvs.PutFlags.class)) {
            switch (flag) {
                case PRIO:
                    assertEquals(0, flag.ordinal());
                    break;
                case VCOMP_OFF:
                    assertEquals(1, flag.ordinal());
                    break;
                case VCOMP_ON:
                    assertEquals(2, flag.ordinal());
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown PUT flag: " + flag.toString());
            }
        }
    }
}
