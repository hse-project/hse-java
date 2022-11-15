/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

package io.github.hse_project.hse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.hse_project.hse.KvdbTransaction.State;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public final class TransactionTest {
    private static Kvdb kvdb;
    private static Kvs kvs;

    @BeforeAll
    public static void setupSuite() throws HseException {
        TestUtils.registerShutdownHook();
        Hse.init("rest.enabled=false");
        kvdb = TestUtils.setupKvdb();
        kvs = TestUtils.setupKvs(kvdb, "kvs", null, new String[]{"transactions.enabled=true"});
    }

    @AfterAll
    public static void tearDownSuite() throws HseException {
        TestUtils.tearDownKvs(kvdb, kvs);
        TestUtils.tearDownKvdb(kvdb);
        Hse.fini();
    }

    @Test
    public void stateTransitions() throws HseException {
        final KvdbTransaction txn = kvdb.transaction();

        assertEquals(txn.getState(), State.INVALID);
        txn.begin();
        assertEquals(txn.getState(), State.ACTIVE);
        txn.abort();
        assertEquals(txn.getState(), State.ABORTED);
        txn.begin();
        txn.commit();
        assertEquals(txn.getState(), State.COMMITTED);

        txn.close();
    }

    @Test
    public void autoClose() throws HseException {
        assertFalse(kvs.get("hello").isPresent());

        try (KvdbTransaction txn = kvdb.transaction()) {
            txn.begin();

            kvs.put("hello", "world", txn);
        }

        assertTrue(kvs.get("hello").isPresent());
        assertFalse(kvs.get("world").isPresent());

        try (KvdbTransaction txn = kvdb.transaction()) {
            txn.begin();

            kvs.put("world", "hello", txn);

            txn.abort();
        }

        assertFalse(kvs.get("world").isPresent());
    }
}
