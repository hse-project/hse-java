/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

package com.micron.hse;

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
