/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

package com.micron.hse_project.hse;

/**
 * The HSE KVDB provides transactions with operations spanning KVSs within a
 * single KVDB. These transactions have snapshot isolation (a specific form of
 * MVCC) with the normal semantics (see "Concurrency Control and Recovery in
 * Database Systems" by PA Bernstein).
 *
 * <p>
 * One unusual aspect of the API as it relates to transactions is that the data
 * object that is used to hold client-level transaction state is allocated
 * separately from the transaction being initiated. As a result, the same object
 * handle should be reused again and again.
 * </p>
 *
 * <p>
 * In addition, there is very limited coupling between threading and
 * transactions. A single thread may have many transactions in flight
 * simultaneously. Also operations within a transaction can be performed by
 * multiple threads. The latter mode of operation must currently restrict calls
 * so that only one thread is actively performing an operation in the context of
 * a particular transaction at any particular time.
 * </p>
 *
 * <p>
 * The general lifecycle of a transaction is as follows:
 * </p>
 *
 * <pre>
 *                       +----------+
 *                       | INVALID  |
 *                       +----------+
 *                             |
 *                             v
 *                       +----------+
 *     +----------------&gt;|  ACTIVE  |&lt;----------------+
 *     |                 +----------+                 |
 *     |  +-----------+    |      |     +----------+  |
 *     +--| COMMITTED |&lt;---+      +----&gt;| ABORTED  |--+
 *        +-----------+                 +----------+
 * </pre>
 *
 * <p>
 * When a transaction is initially allocated, it starts in the INVALID state.
 * When {@link #begin()} is called with transaction in the INVALID, COMMITTED,
 * or ABORTED states, it moves to the ACTIVE state. It is an error to call the
 * {@link #begin()} function on a transaction in the ACTIVE state. For a
 * transaction in the ACTIVE state, only the functions {@link #commit()},
 * {@link #abort()}, or {@link #close()} may be called (with the last doing an
 * abort prior to the free).
 * </p>
 *
 * <p>
 * When a transaction becomes ACTIVE, it establishes an ephemeral snapshot view
 * of the state of the KVDB. Any data mutations outside of the transaction's
 * context after that point are not visible to the transaction. Similarly, any
 * mutations performed within the context of the transaction are not visible
 * outside of the transaction unless and until it is committed. All such
 * mutations become visible atomically when the transaction commits.
 * </p>
 *
 * <p>
 * Within a transaction whenever a write operation e.g., put, delete, etc.,
 * encounters a write conflict, that operation returns an error code of
 * ECANCELED. The caller is then expected to re-try the operation in a new
 * transaction.
 * </p>
 */
public final class KvdbTransaction extends NativeObject implements AutoCloseable {
    /** KVDB the transaction is associated with. */
    private final Kvdb kvdb;

    KvdbTransaction(final Kvdb kvdb) {
        this.kvdb = kvdb;
        this.handle = alloc(kvdb.handle);
    }

    private static native long alloc(long kvdbHandle);

    private native void abort(long kvdbHandle, long txnHandle) throws HseException;

    private native void begin(long kvdbHandle, long txnHandle) throws HseException;

    private native void commit(long kvdbHandle, long txnHandle) throws HseException;

    private native void free(long kvdbHandle, long txnHandle);

    private native State getState(long kvdbHandle, long txnHandle);

    /**
     * Abort/rollback transaction.
     *
     * <p>
     * The call fails if the referenced transaction is not in the ACTIVE state.
     * </p>
     *
     * <p>This function is thread safe.</p>
     *
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public void abort() throws HseException {
        abort(kvdb.handle, this.handle);
    }

    /**
     * Initiate transaction.
     *
     * <p>
     * The call fails if the transaction handle refers to an ACTIVE transaction.
     * </p>
     *
     * <p>This function is thread safe.</p>
     *
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public void begin() throws HseException {
        begin(kvdb.handle, this.handle);
    }

    /**
     * Close transaction.
     *
     * <p>
     * Commits the transaction if it is in the ACTIVE state. Otherwise, the
     * transaction is aborted.
     * </p>
     *
     * <p>This function is thread safe.</p>
     *
     * @throws HseException Underlying C function returned a non-zero value.
     */
    @Override
    public void close() throws HseException {
        if (this.handle == 0) {
            return;
        }

        final State state = getState();
        if (state == State.ACTIVE) {
            commit();
        } else {
            abort();
        }

        free(kvdb.handle, this.handle);
        this.handle = 0;
    }

    /**
     * Commit all the mutations of the referenced transaction.
     *
     * <p>
     * The call fails if the referenced transaction is not in the ACTIVE state.
     * </p>
     *
     * <p>This function is thread safe.</p>
     *
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public void commit() throws HseException {
        commit(kvdb.handle, this.handle);
    }

    /**
     * Get the state of the transaction.
     *
     * <p>This function is thread safe.</p>
     *
     * @return Transaction's state.
     */
    public State getState() {
        return getState(kvdb.handle, this.handle);
    }

    /** Transaction state. */
    public enum State {
        /** Invalid state. */
        INVALID,
        /** Active state. */
        ACTIVE,
        /** Committed state. */
        COMMITTED,
        /** Aborted state. */
        ABORTED,
    }
}
