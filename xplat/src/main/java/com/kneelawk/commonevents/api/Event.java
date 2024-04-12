package com.kneelawk.commonevents.api;

// Based on QSL's event implementation.
// https://github.com/QuiltMC/quilt-standard-libraries/blob/9e4245b96272819510b2aea50033ff0ff6d9fbe8/library/core/qsl_base/src/main/java/org/quiltmc/qsl/base/api/event/Event.java

/**
 * Holder for event callbacks.
 * <p>
 * Note: Constructing one of these will also collect all annotation-based event subscribers.
 * Use {@link Listener} to mark classes containing callback listeners. Use {@link Listen} to mark individual
 * methods as callback listeners.
 *
 * @param <T> the type of callback this holds.
 */
public final class Event<T> {
}
