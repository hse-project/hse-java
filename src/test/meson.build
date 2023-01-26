# SPDX-License-Identifier: Apache-2.0 OR MIT
#
# SPDX-FileCopyrightText: Copyright 2021 Micron Technology, Inc.

if get_option('b_sanitize') != 'none'
    subdir_done()
endif

add_test_setup(
    'default',
    is_default: not meson.is_subproject()
)

add_test_setup('all')

add_test_setup('ci')

subdir('java')
