/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.
 */

package com.micron.hse_project.hse;

/** Object which has a C counterpart. */
abstract class NativeObject {
    /** Address of the C object. */
    protected long handle;
}
