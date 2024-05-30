# Common-Events

Cross-platform Minecraft event library

This library is based on [QSL]'s event system, but makes use of class scanning like forge uses, to allow for easy
registration of event listeners.

[QSL]: https://github.com/QuiltMC/quilt-standard-libraries

## Getting Common-Events

You can get Common-Events by adding the following to your `build.gradle` file:
```groovy
repositories {
    maven {
        name = "Kneelawk"
        url = "https://maven.kneelawk.com/releases/"
    }
}

dependencies {
    // If using loom:
    modImplementation "com.kneelawk.common-events:common-events-<platform>:<version>"
    include "com.kneelawk.common-events:common-events-<platform>:<version>"
    
    // If using userdev:
    implementation "com.kneelawk.common-events:common-events-<platform>:<version>"
    jarJar "com.kneelawk.common-events:common-events-<platform>:<version>"
}
```

## How to Register Listeners

Like in Fabric and Quilt's event systems, callback listeners can be registered directly on event objects. However,
callback listeners can also be registered via annotations, ensuring that callbacks are called without having to worry
about manual registration.

* First, create a `common-events.json` file in your mod's root directory with the following content:
    ```json
    {
        "scan": true
    }
    ```
* Then annotate a class within your mod with `@Scan`. This will allow that class to be searched for listening
  methods.
* Finally, annotate a `public static` method with `@Listen(<callback-interface>.class)`. The listening method *must*
  have the same signature as the callback interface's single method.

### Class-Loading and Static Initializers

Common-Events will scan all mods at the earliest possible occasion. However, this will not cause class-loading
of `@Scan`-annotated classes. These classes will only be loaded and statically initialized when an event is created
with a callback-interface type that one of the class's methods listens for, unless the classes are loaded by something
else sooner. This allows `@Scan`-annotated classes to safely listen for client-sided events without having to worry
about accidentally getting loaded on dedicated servers.

### Only Scanning Specific Classes

The `scan` field of the `common-events.json` file can instead contain an array of classes to scan for event listeners,
like so:

```json
{
    "scan": [
        "com.kneelawk.example.ExampleListener",
        "com.kneelawk.example.client.ExampleClientListener"
    ]
}
```

Classes referenced here also have the same kind of delayed initialization as classes annotated with `@Listener`.

## Kotlin Adapter

The kotlin adapter can be used by adding a dependency on the following:

```groovy
modImplementation "com.kneelawk.common-events:common-events-kotlin-<platform>:<version>"
```

The kotlin adapter allows you to mark `object`s with the `@Scan` annotation without having to mark your `@Listen`
-annotated methods with `@JvmStatic`. This also allows `@BusEvent`-annotated kotlin fields to be discovered, which is
impossible with the java adapter.
