# HSE Java Bindings

[Heterogeneous-Memory Storage Engine](https://github.com/hse-project/hse-java)
bindings for Java.

## Building

### Dependencies

- Java Development Kit `>= 1.8`

`hse-java` targets JDK 1.8 in order to be compatible with most Java-based
software projects.

---

`hse-java` is built using the [Meson build system](https://mesonbuild.com/).
In the event HSE is not visible to the `hse-java` build system, HSE will be
fetched and built alongside `hse-java`.

The minimum version of Meson required to build `hse-java` can be found in the
root [meson.build](./meson.build). In there, you will find a `meson_version`
keyword argument to the `project()` function at the beginnging of the file.

If your system doesn't supply a Meson version new enough to build HSE, refer to
the Meson
[installation instructions](https://mesonbuild.com/Getting-meson.html).

```shell
meson setup build
meson compile -C build
```

Check the output of `meson configure build` or
[`meson_options.txt`](./meson_options.txt) for various build options.

## Installation

### From Maven Central

Add something like the following to your `pom.xml` or equivalent file:

```xml
<dependency>
  <groupId>io.github.hse-project</groupId>
  <artifactId>hse</artifactId>
  <version>${hse.version}</version>
<dependency>
```

### From Build

The default install directory is `/opt/hse`[^1]. This can be overridden by
configuring the build with either `-Dprefix=$prefix` or `--prefix=$prefix`.

```shell
meson install -C build
```

Note that this will also install the JAR file to the output of
`mvn help:evaluate -Dexpression=settings.localRepository` by default. If it must
be deployed to another repository, set `-Drepo` using a URI syntax,
`file://...`.

## Recommendations

### Errno Values

`HseException.getErrno()` returns a scalar. Should you need to take that scalar
and get an `errno` constant from it, we recommend using the Java Native Runtime
Constants
[package](https://mvnrepository.com/artifact/com.github.jnr/jnr-constants).

```java
try {
    // ...
} catch (final HseException e) {
    final Errno errno = Errno.valueOf(e.getErrno());

    // ...
}
```

[^1]: You may have to configure your class path or your Java build tooling to
allow the Java compiler to see the `hse-java` jar.
