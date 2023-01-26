/* SPDX-License-Identifier: Apache-2.0 OR MIT
 *
 * SPDX-FileCopyrightText: Copyright 2021 Micron Technology, Inc.
 */

package io.github.hse_project.hse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class LimitsTest {
    @Test
    public void limits() {
        assertEquals(256, Limits.KVS_COUNT_MAX);
        assertEquals(1344, Limits.KVS_KEY_LEN_MAX);
        assertEquals(32, Limits.KVS_PFX_LEN_MAX);
        assertEquals(32, Limits.KVS_NAME_LEN_MAX);
        assertEquals(1024 * 1024, Limits.KVS_VALUE_LEN_MAX);
    }
}
