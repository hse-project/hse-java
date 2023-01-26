/* SPDX-License-Identifier: Apache-2.0 OR MIT
 *
 * SPDX-FileCopyrightText: Copyright 2021 Micron Technology, Inc.
 */

package io.github.hse_project.hse;

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
