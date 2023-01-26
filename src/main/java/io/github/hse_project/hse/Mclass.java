/* SPDX-License-Identifier: Apache-2.0 OR MIT
 *
 * SPDX-FileCopyrightText: Copyright 2021 Micron Technology, Inc.
 */

package io.github.hse_project.hse;

/** Media classes. */
public enum Mclass {
    /** Capacity media class. */
    CAPACITY,
    /** Staging media class. */
    STAGING,
    /** PMEM media class. */
    PMEM;

    /**
     * {@inheritDoc}
     */
    public String toString() {
        /* This is essentially an implementation detail based on the fact the
         * names of the media classes in C are the lower-case equivalents of
         * name. This allows us to not have to reach into C for getting the
         * name.
         */
        return this.name().toLowerCase();
    }
}
