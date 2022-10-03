/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

package com.micron.hse;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public final class KvdbTest {
    private static Kvdb kvdb;

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

    @Test
    public void compactStatus() throws HseException {
        try {
            kvdb.getCompactStatus();
        } catch (final UnsupportedOperationException e) {
        } catch (final HseException e) {
            fail(e);
        }
    }

    @Test
    public void home() {
        assertEquals(TestUtils.getKvdbHome(), kvdb.getHome());
    }

    @Test
    public void kvsCreate() {
        assertThrows(HseException.class, () -> kvdb.kvsCreate(null));
    }


    @Test
    public void kvsDrop() {
        assertThrows(HseException.class, () -> kvdb.kvsDrop(null));
    }

    @Test
    public void kvsNames() throws HseException {
        assertTrue(kvdb.getKvsNames().size() == 0);

        final Kvs kvs = TestUtils.setupKvs(kvdb, "kvs");

        assertArrayEquals(new String[]{"kvs"}, kvdb.getKvsNames().toArray());

        TestUtils.tearDownKvs(kvdb, kvs);
    }

    @Test
    public void kvsOpen() {
        assertThrows(HseException.class, () -> kvdb.kvsCreate(null));
    }

    @Test
    public void mclassInfo() throws HseException {
        final MclassInfo info = kvdb.getMclassInfo(Mclass.CAPACITY);

        assertNotEquals(0, info.getAllocatedBytes());
        assertNotEquals(0, info.getUsedBytes());
        assertEquals(
            Paths.get(TestUtils.getKvdbHome().toString(), Mclass.CAPACITY.toString()),
            info.getPath());

        assertThrows(HseException.class, () -> kvdb.getMclassInfo(Mclass.STAGING));
    }

    @Test
    public void sync() throws HseException {
        kvdb.sync();
    }
}
