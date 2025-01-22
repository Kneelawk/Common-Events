Changes:

* Added caching to the reflection scanner, so registering the same class or class of instances multiple times should be
  much faster.
* Fixed the reflection scanner to not pick up static methods when scanning an instance.
