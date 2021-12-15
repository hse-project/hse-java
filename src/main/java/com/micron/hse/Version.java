/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

package com.micron.hse;

/**
 * Various version information describing the HSE build.
 */
public final class Version {
    private Version() {}

    private static native int major();
    private static native int minor();
    private static native int patch();
    private static native String string();

    /**
     * Get the major version of HSE.
     *
     * @return HSE major version.
     */
    public static int getMajor() {
        return major();
    }

    /**
     * Get the minor version of HSE.
     *
     * @return HSE minor version.
     */
    public static int getMinor() {
        return minor();
    }

    /**
     * Get the patch version of HSE.
     *
     * @return HSE patch version.
     */
    public static int getPatch() {
        return patch();
    }

    /**
     * Get a string representing the HSE version.
     *
     * @return HSE version string.
     */
    public static String getString() {
        return string();
    }
}
