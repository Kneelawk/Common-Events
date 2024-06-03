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

## Example

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

This will insert the `onDoThing` method into the `MY_CALLBACK_EVENT` event as a lambda method reference. Note that the
listener method does not have to have the same name as the callback interface method.

You can then invoke the event by getting the event's invoker and calling your callback method on it:

```java
public static void invokeMyCallback() {
    MY_CALLBACK_EVENT.invoker().doThing("test");
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

Classes referenced here also have the same kind of delayed initialization as classes annotated with `@Scan`. Classes
here can additionally be annotated with `@Scan` in order to limit them to being loaded only on the client or the server.

## Creating Events

Not all callback interfaces have to have a single method or even have to be interfaces. However, having your callback
interfaces be functional interfaces does make event implementation easier.

Furthermore, if your event simply needs to invoke all listeners with the given arguments, then you can construct the
event using `createSimple`, without even having to define an implementation, as one is generated for you:

```java
public static final Event<MyCallback> MY_CALLBACK_EVENT = Event.createSimple(MyCallback.class);
```

The simple generated implementation does not support fancy things like event cancellation or return values.

### Callback Argument Recommendation

When creating a new callback interface, it is recommended that you use a custom type as your callback method's single
argument, then have each actual argument you want to pass to listeners be available through methods on that custom type.
This allows you to add new arguments without breaking existing listeners. And if you need to remove an argument,
listeners will get a compile-time error instead of a runtime error.

```java
@FunctionalInterface
public interface MyCallback {
    void onCallback(Context ctx);

    interface Context {
        String actualArg1();

        long actualArg2();
    }
}
```

Then a listener would look like:

```java
@Scan
public class MyListener {
    @Listen(MyCallback.class)
    public static void onMyCallback(MyCallback.Context ctx) {
        // use ctx.actualArg1() and ctx.actualArg2() here
    }
}
```

## Event Buses

`EventBus`es are a convenience collection of `Event`s. They allow you to register things to several events at once.
Event buses have two sets of listener registration methods: `registerListener` and `registerListeners`. Note that the
second set of methods registers multiple listeners at once.

The `registerListener` methods require the callback interface class the listener is being registered for, the listener
implementation itself, and a few optional arguments like qualifier, phase, and key.

The `registerListeners` methods require just a listener object and an optional key. If the listener object is an
instance of a class, then that instance is scanned for public instance methods annotated with `@Listen` and then those
methods are registered as method-references to their associated events. This registers all annotated methods, including
those from super-classes. If the listener object is a `Class` itself, then that class is scanned for public static
methods annotated with `@Listen` and then those methods are registered as method-references to their associated events.
Unlike with instance registration, this does **not** register static methods in super-classes.

An event bus can be created like so:

```java
public static final EventBus MY_BUS =
        EventBus.builder(ResourceLocation.fromNamespaceAndPath("mod_id", "bus_name")).build();
```

### Adding Events

There are a couple ways to add events to event buses. The first is a straightforward call to `addEvent`:

```java
public static final Event<MyCallback> MY_EVENT = Event.createSimple(MyCallback.class);

static {
    MY_BUS.addEvent(MY_EVENT);
}
```

However, this is only useful if we can be certain that the `MY_EVENT` is added to `MY_BUS` before any listeners are
registered to `MY_BUS`. This is because `EventBus`es only register listeners to events that are currently in the bus.
Currently, if an event is added to the bus after a listener is registered to that bus, then that listener will not be
registered to the newly added event. Though this may change in future versions of Common Events.

Event buses will, by default, fire an event when they are created. This can be used to make sure that the bus contains
an event by the time listeners get registed to it:

```java
@Listen(EventBus.Created)
public static void onBusCreated(EventBus bus) {
    if (bus.getName().equals(ResourceLocation.fromNamespaceAndPath("mod_id", "bus_name"))) {
        bus.addEvent(MY_EVENT);
    }
}
```

The other way of adding events to event buses is via the `@BusEvent` annotation. This annotation will automatically
register the annotated event to the listed buses. This annotation can be used to register an event to multiple buses at
once.

```java
@BusEvent({"mod_id:bus_name", "other_mod:other_bus"})
public static final Event<MyCallback> MY_EVENT = Event.createSimple(MyCallback.class);
```

This method of adding an event to an event bus is the most efficient, but it is also the least configurable.

### The Main Bus

Common Events supplies an existing main bus. This main bus can be used by adding a dependency on the following:

```groovy
modImplementation "com.kneelawk.common-events:common-events-main-bus-<platform>:<version>"
include "com.kneelawk.common-events:common-events-main-bus-<platform>:<version>"
```

You can add events to the main bus by annotating them like so:

```java
@BusEvent(CommonEventsMainBus.NAME)
public static final Event<MyCallback> MY_CALLBACK_EVENT = Event.createSimple(MyCallback.class);
```

Using the `CommonEventsMainBus.NAME` constant here will not cause class-loading, so using it in an annotation is fine,
because its value gets baked into the annotation at compile-time.

## Unregistering Listeners

When a listener is registered, it can optionally be registered with a key object. This key object is what is used to
find the listener when you want to unregister the listener. There can only be one of each key object registered in each
event.

```java
public static void registerMyListener() {
    MY_EVENT.registerKeyed(myKeyObject, ctx -> {
        // ...
    });
}

public static void unregisterMyListener() {
    MY_EVENT.unregister(myKeyObject);
}
```

This also works for event buses.

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
