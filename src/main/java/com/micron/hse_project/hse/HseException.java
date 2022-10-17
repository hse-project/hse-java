/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

package com.micron.hse_project.hse;

/**
 * Thrown when HSE encounters an error. Wrapper around {@code hse_err_t}.
 */
public final class HseException extends Exception {
    private static final long serialVersionUID = 8995408998818557762L;

    /** Errno value returned by HSE. */
    private final int errno;
    /** Error context. */
    private final Context ctx;

    HseException(final String message, final int errno, final Context ctx) {
        super(message);

        this.errno = errno;
        this.ctx = ctx;
    }

    /**
     * Get the error's context.
     *
     * @return Error's context.
     */
    public Context getContext() {
        return this.ctx;
    }

    /**
     * Get the error's errno representation.
     *
     * @return Error's errno equivalent.
     */
    public int getErrno() {
        return this.errno;
    }

    /** Error context values. */
    public enum Context {
        /** No context. */
        NONE,
        /** Transaction timed out. */
        TXN_EXPIRED,
    }
}
