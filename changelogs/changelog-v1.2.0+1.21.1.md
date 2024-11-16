Changes:

* Fixed thread-safety issues with phase registration and listener unregistration.
* Fixed events with optimized removal throwing errors on key hash conflicts.
* Allowed `Event`s to have more than one listener registered with the same key.
