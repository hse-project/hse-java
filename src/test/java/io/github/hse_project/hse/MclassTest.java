/* SPDX-License-Identifier: Apache-2.0 OR MIT
 *
 * SPDX-FileCopyrightText: Copyright 2021 Micron Technology, Inc.
 */

package io.github.hse_project.hse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

public class MclassTest {
    @Test
    public void string() {
        for (final Mclass mclass : EnumSet.allOf(Mclass.class)) {
            switch (mclass) {
                case CAPACITY:
                    assertEquals("capacity", mclass.toString());
                    break;
                case STAGING:
                    assertEquals("staging", mclass.toString());
                    break;
                case PMEM:
                    assertEquals("pmem", mclass.toString());
                    break;
                default:
                    throw new UnsupportedOperationException(
                        "Unknown media class: " + mclass.toString());
            }
        }
    }

    @Test
    public void ordinal() {
        for (final Mclass mclass : EnumSet.allOf(Mclass.class)) {
            switch (mclass) {
                case CAPACITY:
                    assertEquals(0, mclass.ordinal());
                    break;
                case STAGING:
                    assertEquals(1, mclass.ordinal());
                    break;
                case PMEM:
                    assertEquals(2, mclass.ordinal());
                    break;
                default:
                    throw new UnsupportedOperationException(
                        "Unknown media class: " + mclass.toString());
            }
        }
    }
}
