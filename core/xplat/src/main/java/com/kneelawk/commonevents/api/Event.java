/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kneelawk.commonevents.api;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.phase.PhaseData;
import com.kneelawk.commonevents.api.phase.PhaseSorting;
import com.kneelawk.commonevents.impl.CEConstants;
import com.kneelawk.commonevents.impl.CommonEventsImpl;
import com.kneelawk.commonevents.impl.event.EventPhaseDataHolder;
import com.kneelawk.commonevents.impl.gen.ImplementationGenerator;
import com.kneelawk.commonevents.impl.scan.ScanManager;

/**
 * An object which stores event callbacks.
 * <p>
 * The factory methods for Event allows the user to provide an implementation of {@code T} which is used to
 * execute the callbacks stored in this event instance. This allows a user to control how iteration works, whether an
 * event is cancelled after a specific callback is executed or to make an event
 * {@link ParameterInvokingEvent parameter invoking}.
 * <p>
 * Generally {@code T} should be a type which is a
 * <a href="https://docs.oracle.com/javase/specs/jls/se16/html/jls-9.html#jls-9.8">functional interface</a>
 * to allow callbacks registered to the event to be specified in a lambda, method reference form or implemented onto a
 * class. A way to ensure that an interface is a functional interface is to place a {@link FunctionalInterface}
 * annotation on the type. You can let T not be a functional interface, however it heavily complicates the process
 * of implementing an invoker and only allows callback implementations to be done by implementing an interface onto a
 * class or extending a class. This will also prevent the callback from being fired in scanned classes.
 * <p>
 * You are strongly encouraged to use a {@code T} type which is unique for each event, because this type's class is the
 * key that allows listeners to register using annotations. Using the same type for multiple events will cause all events
 * created with that type to call the same annotation-based callbacks.
 * <p>
 * An Event can have phases, each callback is attributed to a phase ({@link Event#DEFAULT_PHASE} if unspecified),
 * and each phase can have a defined ordering. Each event phase is identified by a {@link ResourceLocation}, ordering is done
 * by explicitly stating that event phase A will run before event phase B, for example.
 * See {@link Event#addPhaseOrdering(ResourceLocation, ResourceLocation)} for more information.
 *
 * <h2>Example: Registering callbacks</h2>
 * <p>
 * The most common use of an event will be registering a callback which is executed by the event. To register a callback,
 * pass an instance of {@code T} into {@link #register} or use the {@link Scan} and {@link Listen} annotations.
 *
 * <pre>{@code
 * // Events should use a dedicated functional interface for T rather than overloading multiple events to the same type
 * // to allow those who implement using a class to implement multiple events.
 * @FunctionalInterface
 * public interface Example {
 *     void doSomething();
 * }
 *
 * // You can also return this instance of Event from a method, may be useful where a parameter is needed to get
 * // the right instance of Event.
 * public static final Event<Example> EXAMPLE = Event.create(...); // implementation
 *
 * public void registerEvents() {
 *     // Since T is a functional interface, we can use the lambda form.
 *     EXAMPLE.register(() -> {
 *         // Do something
 *     });
 *
 *     // Or we can use a method reference.
 *     EXAMPLE.register(this::runSomething);
 *
 *     // Or implement T using a class.
 *     // You can also use an anonymous class here; for brevity that is not included.
 *     EXAMPLE.register(new ImplementedOntoClass());
 * }
 *
 * public void runSomething() {
 *     // Do something else
 * }
 *
 * // When implementing onto a class, the class must implement the same type as the event invoker.
 * class ImplementedOntoClass implements Example {
 *     public void doSomething() {
 *         // Do something else again
 *     }
 * }
 *
 * // When using the scanning method, the class must be annotated with @Scan and have public, static methods
 * // annotated with @Listen. The class must also be publicly accessible.
 * @Scan
 * public class AnnotationListenerClass {
 *     @Listen(Example.class)
 *     public void doSomething() {
 *         // Do something else a third time
 *     }
 * }
 * }</pre>
 *
 * <b>Note:</b> Use of annotation-based registration requires that the registering mod has a
 * {@code common-events.json} file in its root directory, containing:
 * <pre>{@code
 * {
 *     "scan": true
 * }
 * }</pre>
 *
 * <h2>Example: Executing an event</h2>
 * <p>
 * Executing an event is done by calling a method on the event invoker. Where {@code T} is Example, executing an event
 * is done through the following:
 *
 * <pre>{@code
 * EXAMPLE.invoker().doSomething();
 * }</pre>
 *
 * @param <T> the type of the invoker used to execute an event and the type of the callback
 */
public final class Event<T> {
    /**
     * The name of the default phase.
     * Have a look at {@link Event#createWithPhases} for an explanation of event phases.
     */
    public static final ResourceLocation DEFAULT_PHASE = CEConstants.DEFAULT_PHASE;

    /**
     * The default qualifier used if no specific qualifier is specified.
     *
     * @see Listen#qualifier()
     */
    public static final String DEFAULT_QUALIFIER = CEConstants.DEFAULT_QUALIFIER;

    /**
     * Creates a new instance {@link Event} with a type and qualifier.
     * <p>
     * The qualifier given is used to differentiate otherwise indistinguishable event types when scanning for
     * annotation-based listeners.
     *
     * @param type           the class representing the type of the invoker that is executed by the event
     * @param qualifier      extra identifier for differentiating otherwise indistinguishable event types
     * @param implementation a function which generates an invoker implementation using an array of callbacks
     * @param <T>            the type of the invoker executed by the event
     * @return a new event instance
     */
    public static <T> Event<T> create(Class<? super T> type, String qualifier,
                                      Function<T[], T> implementation) {
        return new Event<>(type, qualifier, implementation, true, false);
    }

    /**
     * Creates a new instance of {@link Event}.
     *
     * @param type           the class representing the type of the invoker that is executed by the event
     * @param implementation a function which generates an invoker implementation using an array of callbacks
     * @param <T>            the type of the invoker executed by the event
     * @return a new event instance
     */
    public static <T> Event<T> create(Class<? super T> type,
                                      Function<T[], T> implementation) {
        return create(type, DEFAULT_QUALIFIER, implementation);
    }

    /**
     * Creates a new instance of {@link Event}.
     * <p>
     * This method adds a {@code emptyImplementation} parameter which provides an implementation of the invoker
     * when no callbacks are registered. Generally this method should only be used when the code path is very hot, such
     * as the render or tick loops. Otherwise the other {@link #create(Class, Function)} method should work
     * in 99% of cases with little to no performance overhead.
     *
     * @param type                the class representing the type of the invoker that is executed by the event
     * @param emptyImplementation the implementation of T to use when the array event has no callback registrations
     * @param implementation      a function which generates an invoker implementation using an array of callbacks
     * @param <T>                 the type of the invoker executed by the event
     * @return a new event instance
     */
    public static <T> Event<T> create(Class<? super T> type, T emptyImplementation,
                                      Function<T[], T> implementation) {
        return create(type, callbacks -> switch (callbacks.length) {
            case 0 -> emptyImplementation;
            case 1 -> callbacks[0];
            // We can ensure the implementation may not remove elements from the backing array since the array given to
            // this method is a copy of the backing array.
            default -> implementation.apply(callbacks);
        });
    }

    /**
     * Create a new instance of {@link Event} with a list of default phases that get invoked in order.
     * Exposing the {@link ResourceLocation} of the default phases as {@code public static final} constants is encouraged.
     * <p>
     * An event phase is a named group of callbacks, which may be ordered before or after other groups of callbacks.
     * This allows some callbacks to take priority over other callbacks.
     * Adding separate events should be considered before making use of multiple event phases.
     * <p>
     * Phases may be freely added to events created with any of the factory functions,
     * however using this function is preferred for widely used event phases.
     * If more phases are necessary, discussion with the author of the event is encouraged.
     * <p>
     * Refer to {@link Event#addPhaseOrdering} for an explanation of event phases.
     *
     * @param type           the class representing the type of the invoker that is executed by the event
     * @param implementation a function which generates an invoker implementation using an array of callbacks
     * @param defaultPhases  the default phases of this event, in the correct order. Must contain {@link Event#DEFAULT_PHASE}
     * @param <T>            the type of the invoker executed by the event
     * @return a new event instance
     */
    public static <T> Event<T> createWithPhases(Class<? super T> type,
                                                Function<T[], T> implementation,
                                                ResourceLocation... defaultPhases) {
        CommonEventsImpl.ensureContainsDefaultPhase(defaultPhases);
        CommonEventsImpl.ensureNoDuplicates(defaultPhases,
            id -> new IllegalArgumentException("Duplicate event phase: " + id));

        var event = create(type, implementation);

        for (int i = 1; i < defaultPhases.length; ++i) {
            event.addPhaseOrdering(defaultPhases[i - 1], defaultPhases[i]);
        }

        return event;
    }

    /**
     * Creates a new instance of {@link Event} that does not invoke any {@link Scan}s.
     * <p>
     * This is best for when there may be multiple instances of the same event, when the event callback type is too
     * vague to be useful to annotation-based listeners, or when registration should be limited to calls to
     * {@link #register(Object)} for any other reason.
     *
     * @param type           the class representing the type of the invoker that is executed by the event
     * @param implementation a function which generates an invoker implementation using an array of callbacks
     * @param <T>            the type of invoker executed by the event
     * @return a new event instance
     */
    public static <T> Event<T> createUnscanned(Class<? super T> type,
                                               Function<T[], T> implementation) {
        return new Event<>(type, DEFAULT_QUALIFIER, implementation, false, false);
    }

    /**
     * Creates a simple event that calls all registered listeners with the given arguments.
     * <p>
     * This requires that the callback interface be a functional interface with a method that returns {@code void}.
     *
     * @param type the callback interface type.
     * @param <T>  the callback interface type.
     * @return the created event.
     */
    public static <T> Event<T> createSimple(Class<? super T> type) {
        return new Event<>(type, DEFAULT_QUALIFIER, ImplementationGenerator.defineSimple(type), true, false);
    }

    /**
     * Creates an event builder with the given callback interface type.
     *
     * @param <T>            the type of the callback interface.
     * @param type           the callback interface type.
     * @param implementation a function which generates an invoker implementation using an array of callbacks.
     * @return the event builder.
     */
    public static <T> Builder<T> builder(Class<? super T> type, Function<T[], T> implementation) {
        return new Builder<>(type, implementation);
    }

    /**
     * Creates a simple event builder that calls all registered listeners with the given arguments.
     * <p>
     * This requires that the callback interface be a functional interface with a method that returns {@code void}.
     *
     * @param type the callback interface type.
     * @param <T>  the callback interface type.
     * @return the event builder.
     */
    public static <T> Builder<T> builderSimple(Class<? super T> type) {
        return new Builder<>(type, ImplementationGenerator.defineSimple(type));
    }

    /**
     * Event builder. Use {@link #builder(Class, Function)} to create new event builders.
     *
     * @param <T> the type of callback this builder builds events for.
     */
    public static class Builder<T> {
        private final Class<? super T> type;
        private final Function<T[], T> implementation;
        private @Nullable T emptyImplementation;
        private String qualifier = DEFAULT_QUALIFIER;
        private boolean scanned = true;
        private ResourceLocation[] defaultPhases = new ResourceLocation[0];
        private boolean optimizeRemoval = false;

        private Builder(Class<? super T> type, Function<T[], T> implementation) {
            this.type = type;
            this.implementation = implementation;
        }

        /**
         * Finalizes this builder into a built event.
         *
         * @return the built event.
         */
        public Event<T> build() {
            Function<T[], T> impl;
            if (emptyImplementation != null) {
                impl = callbacks -> switch (callbacks.length) {
                    case 0 -> emptyImplementation;
                    case 1 -> callbacks[0];
                    default -> implementation.apply(callbacks);
                };
            } else {
                impl = implementation;
            }

            Event<T> event = new Event<>(type, qualifier, impl, scanned, optimizeRemoval);

            for (int i = 1; i < defaultPhases.length; ++i) {
                event.addPhaseOrdering(defaultPhases[i - 1], defaultPhases[i]);
            }

            return event;
        }

        /**
         * Sets the implementation to use when there are no callback registrations.
         *
         * @param emptyImplementation the implementation of T to use when there are no callback registrations.
         * @return this builder.
         */
        public Builder<T> emptyImplementation(T emptyImplementation) {
            this.emptyImplementation = emptyImplementation;
            return this;
        }

        /**
         * Sets the event qualifier.
         * <p>
         * Event qualifiers are used for differentiating between otherwise indistinguishable events when scanning.
         *
         * @param qualifier the new event qualifier.
         * @return this builder.
         */
        public Builder<T> qualifier(String qualifier) {
            this.qualifier = qualifier;
            return this;
        }

        /**
         * Sets whether this event uses scanned {@link Listen} annotations.
         *
         * @param scanned whether the created event should use scanned annotations.
         * @return this builder.
         * @see #createUnscanned(Class, Function)
         */
        public Builder<T> scanned(boolean scanned) {
            this.scanned = scanned;
            return this;
        }

        /**
         * Appends default phases to this builder that the created event will invoke in order.
         * <p>
         * Exposing the {@link ResourceLocation} of the default phases as {@code public static final} constants is encouraged.
         * <p>
         * An event phase is a named group of callbacks, which may be ordered before or after other groups of callbacks.
         * This allows some callbacks to take priority over other callbacks.
         * Adding separate events should be considered before making use of multiple event phases.
         * <p>
         * Phases may be freely added to events created with any of the factory functions,
         * however using this function is preferred for widely used event phases.
         * If more phases are necessary, discussion with the author of the event is encouraged.
         * <p>
         * Refer to {@link Event#addPhaseOrdering} for an explanation of event phases.
         *
         * @param defaultPhases the new default phases to append.
         * @return this builder.
         */
        public Builder<T> defaultPhases(ResourceLocation... defaultPhases) {
            if (this.defaultPhases.length > 0 && defaultPhases.length > 0) {
                ResourceLocation[] newPhases = new ResourceLocation[this.defaultPhases.length + defaultPhases.length];
                System.arraycopy(this.defaultPhases, 0, newPhases, 0, this.defaultPhases.length);
                System.arraycopy(defaultPhases, 0, newPhases, this.defaultPhases.length, defaultPhases.length);
                this.defaultPhases = newPhases;
            } else if (this.defaultPhases.length == 0) {
                this.defaultPhases = defaultPhases;
            }

            return this;
        }

        /**
         * Sets whether the built event should be optimized for improved removal costs.
         * <p>
         * If the event is optimized for improved removal costs, then registration performance will be slightly worse.
         * Specifically, when {@code optimizeRemoval} is {@code false}, then {@link #unregister(Object)} will have
         * {@code O(n)} complexity but {@link #register(Object)} will have {@code O(1)} complexity. However, if
         * {@code optimizeRemoval} is {@code true} then both {@link #unregister(Object)} and {@link #register(Object)}
         * will have {@code O(log(n))} complexity.
         *
         * @param optimizeRemoval whether removals should have improved performance at the cost of registration performance.
         * @return this builder.
         */
        public Builder<T> optimizeRemoval(boolean optimizeRemoval) {
            this.optimizeRemoval = optimizeRemoval;
            return this;
        }
    }

    /**
     * The function used to generate the implementation of the invoker to execute events.
     */
    private final Class<? super T> type;
    private final EventKey key;
    private final Function<T[], T> implementation;
    private final boolean sortPhaseCallbacks;
    private final Lock lock = new ReentrantLock();
    /**
     * The invoker field used to execute callbacks.
     */
    private volatile T invoker;
    /**
     * Registered callbacks
     */
    private T[] callbacks;
    /**
     * Registered event phases.
     */
    private final Map<ResourceLocation, EventPhaseDataHolder<T>> phases = new LinkedHashMap<>();
    /**
     * Phases sorted in the correct dependency order.
     */
    private final List<EventPhaseDataHolder<T>> sortedPhases = new ArrayList<>();
    /**
     * Map of phases by the keys they contain.
     */
    private final Map<Object, EventPhaseDataHolder<T>> keysInPhases = new HashMap<>();

    @SuppressWarnings("unchecked")
    private Event(Class<? super T> type, String qualifier, Function<T[], T> implementation, boolean addScanned,
                  boolean sortPhaseCallbacks) {
        this.sortPhaseCallbacks = sortPhaseCallbacks;
        Objects.requireNonNull(type, "Class specifying the type of T in the event cannot be null");
        Objects.requireNonNull(implementation, "Function to generate invoker implementation for T cannot be null");

        this.type = type;
        this.key = EventKey.fromClass(type, qualifier);
        this.implementation = implementation;
        this.callbacks = (T[]) Array.newInstance(type, 0);
        this.update();

        if (addScanned) {
            ScanManager.addScannedListeners(this);
        }
    }

    /**
     * {@return the class of the type of the invoker used to execute an event and the class of the type of the callback}
     */
    @Contract(pure = true)
    public Class<? super T> getType() {
        return this.type;
    }

    /**
     * {@return the unique key describing this event}
     */
    public EventKey getKey() {
        return this.key;
    }

    /**
     * Register a callback to the event.
     * <p>
     * This uses the callback object as its own key.
     *
     * @param callback the callback
     * @see #register(ResourceLocation, Object)
     * @see #registerKeyed(Object, Object)
     * @see #registerKeyed(ResourceLocation, Object, Object)
     */
    public void register(T callback) {
        this.registerKeyed(callback, callback);
    }

    /**
     * Registers a callback to a specific phase of the event.
     * <p>
     * this uses the callback object as its own key.
     *
     * @param phase    the phase name
     * @param callback the callback
     * @see #register(Object)
     * @see #register(ResourceLocation, Object)
     * @see #registerKeyed(ResourceLocation, Object, Object)
     */
    public void register(ResourceLocation phase, T callback) {
        this.registerKeyed(phase, callback, callback);
    }

    /**
     * Register a keyed callback to the event.
     * <p>
     * The callback key is used for un-registering the callback. Only one callback can be registerd for a given key.
     *
     * @param key      the callback's key
     * @param callback the callback
     * @see #register(Object)
     * @see #register(ResourceLocation, Object)
     * @see #registerKeyed(ResourceLocation, Object, Object)
     */
    public void registerKeyed(Object key, T callback) {
        this.registerKeyed(DEFAULT_PHASE, key, callback);
    }

    /**
     * Registers a keyed callback to a specific phase of the event.
     * <p>
     * The callback key is used for un-registering the callback. Only one callback can be registered for a given key.
     *
     * @param key      the callback's key
     * @param phase    the phase name
     * @param callback the callback
     * @see #register(Object)
     * @see #register(ResourceLocation, Object)
     * @see #registerKeyed(Object, Object)
     */
    public void registerKeyed(ResourceLocation phase, Object key, T callback) {
        Objects.requireNonNull(phase, "Tried to register a callback for a null phase!");
        Objects.requireNonNull(callback, "Tried to register a null callback!");
        Objects.requireNonNull(key, "Tried to register a callback with a null key!");

        this.lock.lock();
        try {
            if (keysInPhases.containsKey(key)) return;

            EventPhaseDataHolder<T> phaseData = this.getOrCreatePhase(phase, true);
            phaseData.addListener(key, callback);
            keysInPhases.put(key, phaseData);
            this.rebuildInvoker(this.callbacks.length + 1);
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Removes the callback associated with the given key.
     *
     * @param key the key of the callback to unregister.
     */
    public void unregister(Object key) {
        EventPhaseDataHolder<T> phaseData = keysInPhases.remove(key);
        if (phaseData != null) phaseData.removeListener(key);
    }

    /**
     * Checks whether the given callback key is registered with this event.
     *
     * @param key the callback key to check.
     * @return whether the given callback key is registered.
     */
    public boolean isRegistered(Object key) {
        return keysInPhases.containsKey(key);
    }

    /**
     * Returns the invoker instance used to execute callbacks.
     *
     * <p>You should avoid storing the result of this method since the invoker may become invalid at any time. Use this
     * method to obtain the invoker when you intend to execute an event.
     *
     * @return the invoker instance
     */
    @Contract(pure = true)
    public T invoker() {
        return this.invoker;
    }

    /**
     * Request that callbacks registered for one phase be executed before callbacks registered for another phase.
     * Relying on the default phases supplied to {@link Event#createWithPhases} should be preferred over manually
     * registering phase ordering dependencies.
     * <p>
     * Incompatible ordering constraints such as cycles will lead to inconsistent behavior:
     * some constraints will be respected and some will be ignored. If this happens, a warning will be logged.
     *
     * @param firstPhase  the name of the phase that should run before the other. It will be created if it didn't exist yet
     * @param secondPhase the name of the phase that should run after the other. It will be created if it didn't exist yet
     */
    public void addPhaseOrdering(ResourceLocation firstPhase, ResourceLocation secondPhase) {
        Objects.requireNonNull(firstPhase, "Tried to add an ordering for a null phase.");
        Objects.requireNonNull(secondPhase, "Tried to add an ordering for a null phase.");

        if (firstPhase.equals(secondPhase)) {
            throw new IllegalArgumentException("Tried to add a phase that depends on itself.");
        }

        synchronized (this.lock) {
            var first = this.getOrCreatePhase(firstPhase, false);
            var second = this.getOrCreatePhase(secondPhase, false);
            PhaseData.link(first, second);
            PhaseSorting.sortPhases(this.sortedPhases);
            this.rebuildInvoker(this.callbacks.length);
        }
    }

    /* Implementation */

    private EventPhaseDataHolder<T> getOrCreatePhase(ResourceLocation id, boolean sortIfCreate) {
        var phase = this.phases.get(id);

        if (phase == null) {
            phase = new EventPhaseDataHolder<>(id, this.callbacks.getClass().getComponentType(), sortPhaseCallbacks);
            this.phases.put(id, phase);
            this.sortedPhases.add(phase);

            if (sortIfCreate) {
                PhaseSorting.sortPhases(this.sortedPhases);
            }
        }

        return phase;
    }

    private void rebuildInvoker(int newLength) {
        // Rebuild handlers.
        if (this.sortedPhases.size() == 1) {
            // Special case with a single phase: use the array of the phase directly.
            this.callbacks = this.sortedPhases.get(0).getData().getCallbacks();
        } else {
            @SuppressWarnings("unchecked")
            var newCallbacks = (T[]) Array.newInstance(this.callbacks.getClass().getComponentType(), newLength);
            int newHandlersIndex = 0;

            for (var existingPhase : this.sortedPhases) {
                T[] phaseCallbacks = existingPhase.getData().getCallbacks();
                int length = phaseCallbacks.length;
                System.arraycopy(phaseCallbacks, 0, newCallbacks, newHandlersIndex, length);
                newHandlersIndex += length;
            }

            this.callbacks = newCallbacks;
        }

        // Rebuild invoker.
        this.update();
    }

    private void update() {
        // Make a copy of the array we give to the invoker factory so entries cannot be removed from this event's
        // backing array
        this.invoker = this.implementation.apply(Arrays.copyOf(this.callbacks, this.callbacks.length));
    }

    @Override
    public String toString() {
        return "Event{" +
            "type=" + this.type +
            ", implementation=" + this.implementation +
            ", phases=" + this.phases +
            ", sortedPhases=" + this.sortedPhases +
            '}';
    }
}
