Changes:

* Added exception-handling `createSimple` and `builderSimple` `Event` constructors.
    * These constructors have an extra argument for a consumer of exceptions, useful for logging.
* Added `CommandRegistrationCallback` as a cross-platform command registration callback.
* Added `ServerLifecycleEvents` for cross-platform server lifecycle events.
* Added `LevelRenderingEvents` for cross-platform level rendering events.
