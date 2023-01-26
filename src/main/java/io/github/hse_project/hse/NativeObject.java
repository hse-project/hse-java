/* SPDX-License-Identifier: Apache-2.0 OR MIT
 *
 * SPDX-FileCopyrightText: Copyright 2021 Micron Technology, Inc.
 */

package io.github.hse_project.hse;

/** Object which has a C counterpart. */
abstract class NativeObject {
    /** Address of the C object. */
    protected long handle;
}
