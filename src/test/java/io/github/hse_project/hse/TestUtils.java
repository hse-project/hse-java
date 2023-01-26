/* SPDX-License-Identifier: Apache-2.0 OR MIT
 *
 * SPDX-FileCopyrightText: Copyright 2021 Micron Technology, Inc.
 */

package io.github.hse_project.hse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import jnr.constants.platform.Errno;

public final class TestUtils {
    public static Path kvdbHome = Optional.ofNullable(System.getProperty("home"))
        .map(Paths::get)
        .orElse(defaultHome());

    private TestUtils() {}

    private static Path defaultHome() {
        String directory;

        directory = System.getenv("HSE_TEST_RUNNER_DIR");
        if (directory == null) {
            directory = System.getenv("MESON_BUILD_ROOT");
            if (directory == null) {
                directory = System.getProperty("java.io.tmpdir");
            }
        }

        try {
            return Files.createTempDirectory(Paths.get(directory),
                String.format("mtest-%s-", System.getProperty("test")));
        } catch (final IOException e) {
            System.err.println(e.toString());
            System.exit(1);
            return null;
        }
    }

    public static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Files.delete(TestUtils.kvdbHome);
                } catch (final IOException e) { }
            }
        });
    }

    public static Kvdb setupKvdb() throws HseException {
        return setupKvdb(null, null);
    }

    public static Kvdb setupKvdb(final String[] cparams, final String[] rparams)
            throws HseException {
        try {
            Kvdb.create(kvdbHome, cparams);
            return Kvdb.open(kvdbHome, rparams);
        } catch (final HseException e) {
            if (Errno.valueOf(e.getErrno()) == Errno.EEXIST) {
                return Kvdb.open(kvdbHome, rparams);
            } else {
                throw e;
            }
        }
    }

    public static void tearDownKvdb(final Kvdb kvdb) throws HseException {
        kvdb.close();
        Kvdb.drop(kvdbHome);
    }

    public static Kvs setupKvs(final Kvdb kvdb, final String kvsName) throws HseException {
        return setupKvs(kvdb, kvsName, null, null);
    }

    public static Kvs setupKvs(final Kvdb kvdb, final String kvsName, final String[] cparams,
            final String[] rparams) throws HseException {
        try {
            kvdb.kvsCreate(kvsName, cparams);
            return kvdb.kvsOpen(kvsName, rparams);
        } catch (final HseException e) {
            if (Errno.valueOf(e.getErrno()) == Errno.EEXIST) {
                return kvdb.kvsOpen(kvsName, rparams);
            } else {
                throw e;
            }
        }
    }

    public static void tearDownKvs(final Kvdb kvdb, final Kvs kvs) throws HseException {
        final String kvsName = kvs.getName();

        kvs.close();
        kvdb.kvsDrop(kvsName);
    }

    public static Path getKvdbHome() {
        return kvdbHome;
    }
}
