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
  <groupId>com.micron</groupId>
  <artifactId>hse</groupeId>
  <version>${hse.version}</version>
<dependency>
```

## From Build

The default install directory is `/opt/hse`[^1]. This can be overridden by
configuring the build with either `-Dprefix=$prefix` or `--prefix=$prefix`.

```shell
meson install -C build
```

If you need to place the JAR into a repository local/remote repository, use the
`deploy` run target.

```shell
ninja -C build deploy
```

The `deploy` run target is keyed off of an environment variable
`HSE_JAVA_DEPLOY_URL`. The default is the user's default local Maven repository,
`$HOME/.m2/repository`. In the event you need to tell another project about your
non-default Maven repository, you can do the following:

On the command line:

```shell
mvn -Dmaven.repo.local=path/to/local/repo
```

Through a Maven `settings.xml` file:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
    http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <localRepository>path/to/local/repo</localRepository>
</settings>
```

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
