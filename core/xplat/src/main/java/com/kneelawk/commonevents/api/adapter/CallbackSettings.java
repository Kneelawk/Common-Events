package com.kneelawk.commonevents.api.adapter;

import java.lang.reflect.Method;

/**
 * Describes how a listener handle should be converted into a callback for a specific event.
 *
 * @param interfaceClass  the class of the functional interface that these settings describe
 * @param interfaceMethod a reflection of the singular functional interface method of the interface used by these settings
 * @param builderSettings the builder settings to be passed to listener builders
 * @param <T>             the interface type of the event.
 */
public record CallbackSettings<T>(Class<T> interfaceClass, Method interfaceMethod, BuilderSettings builderSettings) {
}
