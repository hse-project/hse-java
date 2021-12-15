# Contributing to the HSE Project

We welcome your contributions to the HSE project.

The first section below contains general information on contributing to
the HSE project. It is referenced by the `CONTRIBUTING.md` files in all
other HSE project repos.

The second section contains information on contributing to this specific repo.

## General Information on Contributing

### Prior to Starting Work

* Review the [RFC process](https://github.com/hse-project/rfcs) to determine
if the work you are planning requires an RFC.
* Use the `Ideas` category of the HSE
[discussions forum](https://github.com/hse-project/hse/discussions)
to get feedback on minor features or enhancements not requiring an RFC.
* File an issue in the appropriate repo using the predefined templates.

### Submitting a Pull Request

* Submit pull requests (PRs) following the GitHub
[fork and pull model](https://docs.github.com/en/github/collaborating-with-pull-requests/getting-started/about-collaborative-development-models#fork-and-pull-model).
* Commits must be signed-off which indicates that you agree to the
[Developer Certificate of Origin](https://developercertificate.org/).
This is done using the `--signoff` option when committing your changes.
* Initial commits must be rebased.
* Use the predefined PR template and specify which issue the commit
addresses, what the commit does, and provide a concise description of
the change.
* All new code must include unit or functional tests.
* All existing unit and functional tests must pass.
* For any data path changes, run the benchmark suite before and after
your PR to verify there is no regression.

### Coding Style

All the C code within HSE conforms to the pre-defined `clang-format` file. All
Python code you may find in the code base conforms entirely to the `black`
formatter. For Meson files, try to match the style in other files, but most
importantly use 4 spaces for indention rather than tabs.

Make sure all contributions adhere to the aforementioned styles.

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
