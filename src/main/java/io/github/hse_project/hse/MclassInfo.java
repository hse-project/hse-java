/* SPDX-License-Identifier: Apache-2.0 OR MIT
 *
 * SPDX-FileCopyrightText: Copyright 2021 Micron Technology, Inc.
 */

package io.github.hse_project.hse;

import java.nio.file.Path;

/** Media class information. */
public final class MclassInfo {
    /** Allocated storage space for a media class. */
    private long allocatedBytes;
    /** Used storage space for a media class. */
    private long usedBytes;
    /** Path to a media class. */
    private Path path;

    MclassInfo(final Kvdb kvdb, final Mclass mclass) throws HseException {
        get(kvdb.handle, mclass.ordinal());
    }

    private native void get(long kvdbHandle, int mclass) throws HseException;

    /**
     * Get the number of allocated bytes.
     *
     * @return Number of allocated bytes.
     */
    public long getAllocatedBytes() {
        return allocatedBytes;
    }

    /**
     * Get the path.
     *
     * @return Path.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Get the amount of used bytes.
     *
     * @return Number of used bytes.
     */
    public long getUsedBytes() {
        return usedBytes;
    }
}
