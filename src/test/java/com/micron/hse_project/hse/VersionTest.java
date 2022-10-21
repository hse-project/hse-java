/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

package com.micron.hse_project.hse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class VersionTest {
    @BeforeAll
    public static void setupSuite() throws HseException {
        Hse.loadLibrary();
    }

    @Test
    public void version() {
        assertTrue(Version.getMajor() > 0);
        assertTrue(Version.getMinor() >= 0);
        assertTrue(Version.getPatch() >= 0);
        assertNotNull(Version.getString());
    }
}
