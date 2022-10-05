/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

package com.micron.hse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public final class HseTest {
    @BeforeAll
    public static void setupSuite() throws HseException {
        Hse.init("rest.enabled=false");
    }

    @AfterAll
    public static void tearDownSuite() {
        Hse.fini();
    }

    @Test
    public void getParamNullParam() {
        assertThrows(HseException.class, () -> Hse.getParam(null));
    }

    @Test
    public void getParam() throws HseException {
        final String value = Hse.getParam("logging.enabled");

        assertEquals("true", value);
    }
}
