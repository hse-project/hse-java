# Contributing to the HSE Project

We welcome your contributions to the HSE project.

## General Information on Contributing

Please review the general information on contributing to the HSE project found
in the [`CONTRIBUTING.md`](https://github.com/hse-project/hse/blob/master/CONTRIBUTING.md)
file in the `hse` repo. It contains important information on contributing to any
repo in the HSE project.

## Information on Contributing to this Repo

### Cloning

You can clone HSE through both HTTPS and SSH protocols.

```sh
# HTTPS
git clone https://github.com/hse-project/hse-java.git
# SSH
git clone git@github.com:hse-project/hse-java.git
```

### Working in the HSE Repository

If you are working on a feature or bug that requires work in both `hse` and
`hse-python`, then make `subprojects/hse` a symlink to the `hse` repository on
your system or vice-versa. Then in the event you edit `hse`, `hse-python` will
see the changes as well and re-build/re-link appropriately.

### Building

Refer to the [README.md](./README.md#building) to get started.

#### JNI Development

`hse-java` is built using the [Java Native Interface (JNI)](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/).
Another userful link for JNI development is within the Android NDK
[development guide](https://developer.android.com/training/articles/perf-jni)

Review the previous links for JNI APIs and development tips.

### Testing

After building `hse-java` successfully, tests can be ran using the following
command:

```shell
meson test -C build
```

The `hse-java` test suite is intentionally small because all that needs to be
tested is that the `hse-java` calls get translated successfully. If you want
to add functional tests for HSE, please send your contributions there.

### Creating a New Class

In the event a new class must be added to `hse-java`, there is one thing to keep
in mind. If the class requires a reference to a C object, extend `NativeObject`
and store the address in the `handle` member.

### Translating Flags

Translating flags is not necessarily very straightforward. `hse-java` creates a
higher level API requires an `Enum` to be created. Depending on what
object/operation the flag modifies, place it in that file as an inner class.
`hse-java` APIs receive a `java.util.EnumSet` of a flags `Enum`. In order to
convert an `EnumSet` to the numeric version, use the following pattern:

```java
import java.util.EnumSet;

public operation(final EnumSet<Flags> flags) {
    final int flagsValue = flags.stream().mapToInt(flag -> 1 << flag.ordinal()).sum();
    // ...
}
```

### Exceptions

`hse-java` makes liberal use of [checked exceptions](https://docs.oracle.com/javase/tutorial/essential/exceptions/runtime.html).
If you wrap an API which returns an `hse_err_t`, mark the native function as
`throws HseException`.

If something about the C API, can be mapped to a built-in Java exception, use
it. For instance, `hse_kvs_cursor_read()` returns whether the cursor is at
`EOF`. In this case, on the read which `eof` becomes true, throw an
`EOFException`.

In rare cases, it may be advisable to swallow the `HseException`. One instance
of this is `CursorIterator` implementing `Iterator`. `Iterator::next()` doesn't
support marking `throws HseException`, so instead we construct a
`NoSuchElementException` and pass it the value of `HseException::toString()`.
`NoSuchElementException` extends `RuntimeException` which allows us to push the
cause of the `HseException` up the stack. It is an unfortunate situation, but it
is the best solution at the time of writing in our opinion.
