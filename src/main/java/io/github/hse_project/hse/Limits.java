/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

package io.github.hse_project.hse;

/** Various numeric limits presrcibed by HSE. */
public final class Limits {
    /** Maximum number of KVSs contained within one KVDB. */
    public static final int KVS_COUNT_MAX = 256;

    /**
     * Maximum key length.
     *
     * <p>
     * A common requirement clients have for key length is 1024. Combined with a
     * discriminant and (potentially) a chunk key, this pushes us to 1030 bytes
     * keys. Looking at the packing for the on-media format for data, we can
     * have at most 3 keys of such large size in a 4k page. Lopping off 64-bytes
     * for other data, and we can have 3 keys of 1344 bytes.
     * </p>
     *
     * <p>Keys need not be NULL-terminated.</p>
     */
    public static final int KVS_KEY_LEN_MAX = 1344;

    /**
     * Maximum length of a KVS name.
     *
     * <p>
     * KVS names are NULL-terminated strings. The string plus the NULL-terminator
     * must fit into a {@link #KVS_NAME_LEN_MAX} byte buffer.
     * </p>
     */
    public static final int KVS_NAME_LEN_MAX = 32;

    /** Maximum key prefix length. */
    public static final int KVS_PFX_LEN_MAX = 32;

    /**
     * Maximum value length.
     *
     * <p>Values need not be NULL-terminated.</p>
     */
    public static final int KVS_VALUE_LEN_MAX = 1024 * 1024;

    private Limits() {}
}
