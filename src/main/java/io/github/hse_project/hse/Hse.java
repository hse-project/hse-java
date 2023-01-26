/* SPDX-License-Identifier: Apache-2.0 OR MIT
 *
 * SPDX-FileCopyrightText: Copyright 2021 Micron Technology, Inc.
 */

package io.github.hse_project.hse;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Encompasses HSE global functions and loads the HSE JNI library.
 */
public final class Hse {
    /** Whether the library has already previously been loaded. */
    private static final AtomicBoolean LIBRARY_LOADED = new AtomicBoolean(false);

    private Hse() {}

    private static native String cgetParam(String param) throws HseException;
    private static native void cinit(String config, String[] params)
            throws HseException;
    private static native void cfini();

    /**
     * Get an HSE global parameter.
     *
     * <p>This function is thread safe.</p>
     *
     * @param param Parameter name.
     * @return Stringified version of the parameter value.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public static String getParam(final String param) throws HseException {
        return cgetParam(param);
    }

    /**
     * Refer to {@code #init(Path, String...)}.
     *
     * <p>{@code config} defaults to {@code null}.</p>
     *
     * @param params List of parameters in key=value format.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public static void init(final String... params) throws HseException {
        init(null, params);
    }

    /**
     * Initialize the HSE subsystem.
     *
     * <p>
     * This function initializes a range of different internal HSE structures.
     * It must be called before any other HSE functions are used.
     * </p>
     *
     * <p>
     * This function will load the native library if it hasn't already been
     * loaded.
     * </p>
     *
     * <p>This function is not thread safe and is idempotent.</p>
     *
     * @param config Path to a global configuration file.
     * @param params List of parameters in key=value format.
     * @throws HseException Underlying C function returned a non-zero value.
     */
    public static void init(final Path config, final String... params) throws HseException {
        loadLibrary();

        cinit(config == null ? null : config.toString(), params);
    }

    /**
     * Shutdown the HSE subsystem.
     *
     * <p>
     * This function cleanly finalizes a range of different internal HSE
     * structures. It should be called prior to application exit.
     * </p>
     *
     * <p>
     * After invoking this function, calling any other HSE functions will
     * result in undefined behavior unless HSE is re-initialized.
     * </p>
     *
     * <p>This function is not thread safe.</p>
     */
    public static void fini() {
        cfini();
    }

    /**
     * Load the system library which implements the HSE JNI interface.
     *
     * @see System#loadLibrary(String)
     */
    public static void loadLibrary() {
        if (LIBRARY_LOADED.get()) {
            return;
        }

        System.loadLibrary("hsejni-3");

        LIBRARY_LOADED.set(true);
    }

    /**
     * Load the specified library which implements the HSE JNI interface.
     *
     * @param library Path to library to load.
     * @see System#load
     */
    public static void loadLibrary(final Path library) {
        if (LIBRARY_LOADED.get()) {
            return;
        }

        System.load(library.toString());

        LIBRARY_LOADED.set(true);
    }
}
