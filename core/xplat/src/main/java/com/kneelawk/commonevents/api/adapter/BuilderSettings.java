package com.kneelawk.commonevents.api.adapter;

/**
 * Extra settings describing how callbacks should be built.
 * <p>
 * If {@link #requireAllArgs()} is {@code false} then the listener method can have some (moving from left to right) or
 * none of the arguments that the callback interface method has.
 *
 * @param requireAllArgs whether the builder should require that the listener method have all the same arguments as the
 *                       callback interface method
 */
public record BuilderSettings(boolean requireAllArgs) {
    /**
     * A builder settings instance with default values.
     */
    public static final BuilderSettings DEFAULT = new BuilderSettings(false);
}
