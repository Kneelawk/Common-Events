Changes:

* Fixed thread-safety issues with phase registration and listener unregistration.
* Fixed events with optimized removal throwing errors on key hash conflicts.
* Allowed `Event`s to have more than one listener registered with the same key.
* Added the ability to register 'weak' listeners that do not hold a strong reference to the object being registered, and
  that will unregister when the registered object gets garbage-collected.
* Added methods to `Event`s that scan objects registered for `@Listen` annotations like `EventBus`es do.
