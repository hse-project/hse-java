#!/bin/sh

# SPDX-License-Identifier: Apache-2.0
#
# Copyright (C) 2021-2022 Micron Technology, Inc. All rights reserved.

if ! type shellcheck > /dev/null 2>&1; then
    >&2 echo "shellcheck not found"
    exit 1
fi

source_root=$(realpath "$(dirname "$(dirname "$(dirname "$0")")")")

files=$(find "$source_root" \( \( -name "*.sh" -o -name "*.subr" \) -type f -not -path "$source_root/subprojects/*" -print0 \) | xargs --null)

# shellcheck disable=SC2086 # Need word splitting for $files
shellcheck --external-sources --enable all $files
