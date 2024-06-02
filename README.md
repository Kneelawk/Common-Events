# Common-Events

[![Github Release Status]][Github Release] [![Maven Status]][Maven] [![Javadoc Badge]][Javadoc] [![Discord Badge]][Discord] [![Ko-fi Badge]][Ko-fi]

[Github Release Status]: https://img.shields.io/github/v/release/Kneelawk/Common-Events?include_prereleases&sort=semver&style=flat-square&logo=github

[Github Release]: https://github.com/Kneelawk/Common-Events/releases/latest

[Maven Status]: https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.kneelawk.com%2Freleases%2Fcom%2Fkneelawk%2Fcommon-events%2Fcommon-events-xplat-intermediary%2Fmaven-metadata.xml&style=flat-square&logo=apachemaven&logoColor=blue

[Maven]: https://maven.kneelawk.com/#/releases/com/kneelawk/common-events

[Javadoc Badge]: https://img.shields.io/badge/-javadoc-green?style=flat-square

[Javadoc]: https://maven.kneelawk.com/javadoc/releases/com/kneelawk/common-events/common-events-xplat-intermediary/latest

[Discord Badge]: https://img.shields.io/discord/988299232731607110?style=flat-square&logo=discord

[Discord]: https://discord.gg/6vgpHcKmxg

[Ko-fi Badge]: https://img.shields.io/badge/ko--fi-donate-blue?style=flat-square&logo=kofi

[Ko-fi]: https://ko-fi.com/kneelawk

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

### Example

Imagine a callback interface like so:

```java
@FunctionalInterface
public interface MyCallback {
    void doThing(String str);
}
```

You can create an event for this interface like so:

```java
public static final Event<MyCallback> MY_CALLBACK_EVENT = Event.create(MyCallback.class, callbacks -> str -> {
    for (MyCallback callback : callbacks) {
        callback.doThing(str);
    }
});
```

Then, potentially in a different mod entirely, you could add a listener for this callback:

```java
@Scan
public class MyListener {
    @Listen(MyCallback.class)
    public static void onDoThing(String str) {
        System.out.println("Doing thing: " + str + "!");
    }
}
```

This will insert the `onDoThing` method into the `MY_CALLBACK_EVENT` event as a lambda method reference.

You can then invoke the event by getting the event's invoker and calling your callback method on it:

```java
public static void invokeMyCallback() {
    MY_CALLBACK_EVENT.invoker().doThing("test");
}
```

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

Classes referenced here also have the same kind of delayed initialization as classes annotated with `@Scan`. Classes
here can additionally be annotated with `@Scan` in order to limit them to being loaded only on the client or the server.

## Kotlin Adapter

The kotlin adapter can be used by adding a dependency on the following:

```groovy
modImplementation "com.kneelawk.common-events:common-events-kotlin-<platform>:<version>"
include "com.kneelawk.common-events:common-events-kotlin-<platform>:<version>"
```

And by adding an `adapter` statement to your `common-events.json` like so:

```json
{
    "adapter": "kotlin",
    "scan": true
}
```

The kotlin adapter allows you to mark `object`s with the `@Scan` annotation without having to mark your `@Listen`
-annotated methods with `@JvmStatic`. This also allows `@BusEvent`-annotated kotlin fields to be discovered, which is
impossible with the java adapter.
