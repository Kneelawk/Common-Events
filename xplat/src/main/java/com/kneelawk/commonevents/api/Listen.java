package com.kneelawk.commonevents.api;

/**
 * Used to annotate methods that listen for events.
 * <p>
 * Annotated methods should be {@code public static} and have exactly the same argument types and return type as the
 * {@link Event}'s callback interface's single abstract method.
 */
public @interface Listen {
    /**
     * Gets the class of the callback interface that this listener implements.
     *
     * @return the class of the callback interface that this listener implements.
     */
    Class<?> value();

    /**
     * Gets this listener's phase.
     *
     * @return this listener's phase.
     */
    String phase() default "common_events:default";
}
