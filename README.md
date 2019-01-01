### URL Handler Bug: Maven Plugins

This project demonstrates an issue with the Classworlds class loader framework that is used by Maven.

Specifically, the code in this repo shows an issue with resolution of custom URL stream handlers.

#### Introduction

The demo defines a custom stream handler named `MemoryURLStreamHandler` for URLs of the form: `memory://...`. This handler uses 2 methods to allow `URL#getURLStreamHandler` to dereference URLs with protocol `memory`:

1. `SPI`: The class `org.safris.demo.classworlds.memory.Handler$Provider` extends `URLStreamHandlerProvider`, and a file exists to register this service provider in: `META-INF/services/java.net.spi.URLStreamHandlerProvider`.

2. `java.protocol.handler.pkgs` property: On clinit of `MemoryURLStreamHandler`, the `java.protocol.handler.pkgs` property is set to specify the package path for: `org.safris.demo.classworlds.memory.Handler`.

#### Success case

The demo implements a `MemoryURLStreamHandlerTest` class to demonstrate that the `MemoryURLStreamHandler` is being loaded correctly.

#### Failure case

This Maven project is packaged as a `maven-plugin`, and implements a `DemoMojo` class that has 1 line of code:

```java
final URL memUrl = MemoryURLStreamHandler.createURL("hello".getBytes());
```

If you run `mvn clean install` on the command line, the build will use the `maven-invoker-plugin` to invoke the plugin being built, and the execution will fail with:

```java
java.net.MalformedURLException: unknown protocol: memory
```

#### What is the issue?

In short, the issue caused by the fact that the class loader in which the plugin classes (and dependencies) are loaded is not the system class loader, but rather is one of the Classworlds class loaders.

By debugging through the `URL#getURLStreamHandler` class, you can see what is happening:

##### Loading via SPI

1. The `URL` class first attempts to find the stream handler via SPI (on line 1388). On line 1257, the `URL` class makes the actual call to `ServiceLoader#load` in the `ClassLoader.getSystemClassLoader()`. This method call does not find the `META-INF/services/java.net.spi.URLStreamHandlerProvider` file, because the system class loadeer does not have a reference to the resource path of the package of this maven plugin.

##### Loading via `java.protocol.handler.pkgs`

1. The `URL` class then attempts to find the stream handler via `java.protocol.handler.pkgs` (on line 1392). It is succseefully able to dereference the registered package name on line 1226.
2. The `URL` class thereafter attempts to load the class name on line 1234 via `Class.forName(clsName)`. Note that the `URL` class belongs to the bootstrap class loader, so the call to `Class.forName(clsName)` will not be able to resolve the `org.safris.demo.classworlds.memory.Handler`.
2. The `URL` class thereafter attempts to load the class name on line 1238 via `ClassLoader.getSystemClassLoader().loadClass(clsName)`. Normally, this should work, and `MemoryURLStreamHandlerTest` can show this working. However, the system class loader does not reference the classes or resource paths of the project -- instead, the classes are loaded in the Classworlds class loader model.