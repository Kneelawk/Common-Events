/*
 * Copyright (c) 2024 Cyan Kneelawk.
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

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.adapter.ListenerHolder;
import com.kneelawk.commonevents.api.adapter.util.AdapterUtils;
import com.kneelawk.commonevents.impl.CELog;
import com.kneelawk.commonevents.impl.scan.ScanManager;

/**
 * A convenience object holding a collection of events, that selectively registers objects based on which callbacks
 * they implement.
 * <p>
 * Event buses organize events by the event's callback interface type and qualifier. This is because objects registered
 * to an event bus are generally registered based on the type of callback they implement and which qualifier they are
 * annotated with.
 */
public final class EventBus {
    private final ResourceLocation name;
    private final Map<EventKey, Event<?>> events = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Event fired when an event bus is created.
     */
    public static final Event<Created> CREATED_EVENT = Event.create(Created.class, callbacks -> bus -> {
        for (Created callback : callbacks) {
            callback.onCreated(bus);
        }
    });

    /**
     * Callback for when an event bus is created.
     */
    @FunctionalInterface
    public interface Created {
        /**
         * Called when an event bus is created.
         *
         * @param bus the bus that has been created.
         */
        void onCreated(EventBus bus);
    }

    /**
     * Creates a new {@link EventBus} builder with the given name.
     *
     * @param name the name for the event bus to be built.
     * @return the event bus builder.
     */
    public static Builder builder(ResourceLocation name) {
        return new Builder(name);
    }

    /**
     * {@link EventBus} builder. Use {@link #builder(ResourceLocation)} to create new builders.
     */
    public static class Builder {
        private final ResourceLocation name;
        private boolean scanned = true;
        private boolean fireEvent = true;

        private Builder(ResourceLocation name) {
            this.name = name;
        }

        /**
         * Finalizes this builder into a built event bus.
         *
         * @return the built event bus.
         */
        public EventBus build() {
            return new EventBus(name, scanned, fireEvent);
        }

        /**
         * Sets whether the resulting event bus should add scanned events.
         *
         * @param scanned whether the built bus should add scanned events.
         * @return this builder.
         */
        public Builder scanned(boolean scanned) {
            this.scanned = scanned;
            return this;
        }

        /**
         * Sets whether the resulting event bus should fire the {@link #CREATED_EVENT}.
         *
         * @param fireEvent whether to fire the created event when the bus is created.
         * @return this builder.
         */
        public Builder fireEvent(boolean fireEvent) {
            this.fireEvent = fireEvent;
            return this;
        }
    }

    private EventBus(ResourceLocation name, boolean scanned, boolean fireEvent) {
        this.name = name;

        if (scanned) {
            ScanManager.addScannedEvents(this);
        }

        if (fireEvent) {
            CREATED_EVENT.invoker().onCreated(this);
        }
    }

    /**
     * {@return this event bus's name}
     */
    public ResourceLocation getName() {
        return name;
    }

    /**
     * Add an event to this event bus. Any future listeners registered with this event bus will be able to be registered
     * to this event.
     *
     * @param event the event to add to the bus.
     */
    public void addEvent(Event<?> event) {
        events.put(event.getKey(), event);
    }

    /**
     * Checks whether this event bus has the event described by the given callback type and qualifier.
     *
     * @param callbackType the callback interface class of the event.
     * @return whether this event bus has the event.
     */
    public boolean hasEvent(Class<?> callbackType) {
        return hasEvent(EventKey.fromClass(callbackType, Event.DEFAULT_QUALIFIER));
    }

    /**
     * Checks whether this event bus has the event described by the given callback type and qualifier.
     *
     * @param callbackType the callback interface class of the event.
     * @param qualifier    the qualifier of the event.
     * @return whether this event bus has the event.
     */
    public boolean hasEvent(Class<?> callbackType, String qualifier) {
        return hasEvent(EventKey.fromClass(callbackType, qualifier));
    }

    /**
     * Checks whether this event bus has the event described by the given key.
     *
     * @param key the key of the event to check.
     * @return whether this event bus has the event.
     */
    public boolean hasEvent(EventKey key) {
        return events.containsKey(key);
    }

    /**
     * Gets the invoker for the specified event.
     *
     * @param callbackType the class of the callback interface of the event.
     * @param <T>          the type of the callback interface.
     * @return the event with the given callback interface type and qualifier.
     * @throws EventNotFoundException if no event is present that matches the given specifiers.
     */
    public <T> T getInvoker(Class<T> callbackType) {
        return getInvoker(callbackType, Event.DEFAULT_QUALIFIER);
    }

    /**
     * Gets the invoker for the specified event.
     *
     * @param callbackType the class of the callback interface of the event.
     * @param qualifier    the qualifier of the event.
     * @param <T>          the type of the callback interface.
     * @return the event with the given callback interface type and qualifier.
     * @throws EventNotFoundException if no event is present that matches the given specifiers.
     */
    @SuppressWarnings("unchecked")
    public <T> T getInvoker(Class<T> callbackType, String qualifier) {
        EventKey key = EventKey.fromClass(callbackType, qualifier);
        Event<T> event = (Event<T>) events.get(key);
        if (event == null) throw new EventNotFoundException("Event " + key + " not found in event bus " + name);
        return event.invoker();
    }

    /**
     * Tries to get the invoker for the specified event.
     *
     * @param callbackType the class of the callback interface of the event.
     * @param <T>          the type of the callback interface.
     * @return the event with the given callback interface type and qualifier, or {@code null} if no event is present
     * that matches the given specifiers.
     */
    public <T> @Nullable T tryGetInvoker(Class<T> callbackType) {
        return tryGetInvoker(callbackType, Event.DEFAULT_QUALIFIER);
    }

    /**
     * Tries to get the invoker for the specified event.
     *
     * @param callbackType the class of the callback interface of the event.
     * @param qualifier    the qualifier of the event.
     * @param <T>          the type of the callback interface.
     * @return the event with the given callback interface type and qualifier, or {@code null} if no event is present
     * that matches the given specifiers.
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable T tryGetInvoker(Class<T> callbackType, String qualifier) {
        Event<T> event = (Event<T>) events.get(EventKey.fromClass(callbackType, qualifier));
        if (event == null) return null;
        return event.invoker();
    }

    /**
     * Registers a listener for the given event type with this bus.
     *
     * @param callbackInterface the callback interface the event handles and the listener implements.
     * @param listener          the event listener to register.
     * @param <T>               the type of the callback interface the listener implements.
     * @throws IllegalArgumentException if the specified callback interface and qualifier do not match any events in
     *                                  this bus.
     */
    public <T> void registerListener(Class<T> callbackInterface, T listener) {
        registerKeyedListener(callbackInterface, Event.DEFAULT_QUALIFIER, Event.DEFAULT_PHASE, listener, listener);
    }

    /**
     * Registers a listener for the given event type with this bus.
     *
     * @param callbackInterface the callback interface the event handles and the listener implements.
     * @param phase             the phase to register the listener to.
     * @param listener          the event listener to register.
     * @param <T>               the type of the callback interface the listener implements.
     * @throws IllegalArgumentException if the specified callback interface and qualifier do not match any events in
     *                                  this bus.
     */
    public <T> void registerListener(Class<T> callbackInterface, ResourceLocation phase, T listener) {
        registerKeyedListener(callbackInterface, Event.DEFAULT_QUALIFIER, phase, listener, listener);
    }

    /**
     * Registers a listener for the given event type with this bus.
     *
     * @param callbackInterface the callback interface the event handles and the listener implements.
     * @param qualifier         the event's qualifier to distinguish between events with the same callback interface.
     * @param listener          the event listener to register.
     * @param <T>               the type of the callback interface the listener implements.
     * @throws IllegalArgumentException if the specified callback interface and qualifier do not match any events in
     *                                  this bus.
     */
    public <T> void registerListener(Class<T> callbackInterface, String qualifier, T listener) {
        registerKeyedListener(callbackInterface, qualifier, Event.DEFAULT_PHASE, listener, listener);
    }

    /**
     * Registers a listener for the given event type with this bus.
     *
     * @param callbackInterface the callback interface the event handles and the listener implements.
     * @param qualifier         the event's qualifier to distinguish between events with the same callback interface.
     * @param phase             the phase to register the listener to.
     * @param listener          the event listener to register.
     * @param <T>               the type of the callback interface the listener implements.
     * @throws IllegalArgumentException if the specified callback interface and qualifier do not match any events in
     *                                  this bus.
     */
    public <T> void registerListener(Class<T> callbackInterface, String qualifier, ResourceLocation phase, T listener) {
        registerKeyedListener(callbackInterface, qualifier, phase, listener, listener);
    }

    /**
     * Registers a listener for the given event type with this bus.
     *
     * @param callbackInterface the callback interface the event handles and the listener implements.
     * @param key               the key used to remove the listener.
     * @param listener          the event listener to register.
     * @param <T>               the type of the callback interface the listener implements.
     * @throws IllegalArgumentException if the specified callback interface and qualifier do not match any events in
     *                                  this bus.
     */
    public <T> void registerKeyedListener(Class<T> callbackInterface, Object key, T listener) {
        registerKeyedListener(callbackInterface, Event.DEFAULT_QUALIFIER, Event.DEFAULT_PHASE, key, listener);
    }

    /**
     * Registers a listener for the given event type with this bus.
     *
     * @param callbackInterface the callback interface the event handles and the listener implements.
     * @param phase             the phase to register the listener to.
     * @param key               the key used to remove the listener.
     * @param listener          the event listener to register.
     * @param <T>               the type of the callback interface the listener implements.
     * @throws IllegalArgumentException if the specified callback interface and qualifier do not match any events in
     *                                  this bus.
     */
    public <T> void registerKeyedListener(Class<T> callbackInterface, ResourceLocation phase, Object key, T listener) {
        registerKeyedListener(callbackInterface, Event.DEFAULT_QUALIFIER, phase, key, listener);
    }

    /**
     * Registers a listener for the given event type with this bus.
     *
     * @param callbackInterface the callback interface the event handles and the listener implements.
     * @param qualifier         the event's qualifier to distinguish between events with the same callback interface.
     * @param key               the key used to remove the listener.
     * @param listener          the event listener to register.
     * @param <T>               the type of the callback interface the listener implements.
     * @throws IllegalArgumentException if the specified callback interface and qualifier do not match any events in
     *                                  this bus.
     */
    public <T> void registerKeyedListener(Class<T> callbackInterface, String qualifier, Object key, T listener) {
        registerKeyedListener(callbackInterface, qualifier, Event.DEFAULT_PHASE, key, listener);
    }

    /**
     * Registers a listener for the given event type with this bus.
     *
     * @param callbackInterface the callback interface the event handles and the listener implements.
     * @param qualifier         the event's qualifier to distinguish between events with the same callback interface.
     * @param phase             the phase to register the listener to.
     * @param key               the key used to remove the listener.
     * @param listener          the event listener to register.
     * @param <T>               the type of the callback interface the listener implements.
     * @throws IllegalArgumentException if the specified callback interface and qualifier do not match any events in
     *                                  this bus.
     */
    @SuppressWarnings("unchecked")
    public <T> void registerKeyedListener(Class<T> callbackInterface, String qualifier, ResourceLocation phase,
                                          Object key, T listener) {
        Objects.requireNonNull(callbackInterface, "Tried to register a listener with a null callback interface class!");
        Objects.requireNonNull(qualifier, "Tried to register a listener with a null event qualifier!");
        Objects.requireNonNull(phase, "Tried to register a listener to a null phase!");
        Objects.requireNonNull(listener, "Tried to register a null listener!");
        Objects.requireNonNull(key, "Tried to register a listener with a null key!");

        EventKey eventKey = EventKey.fromClass(callbackInterface, qualifier);

        Event<T> event = (Event<T>) events.get(eventKey);
        if (event == null) throw new IllegalArgumentException(
            "This event bus does not contain event for the key: '" + eventKey + "'. Contained events: " +
                events.keySet());

        event.registerKeyed(phase, key, listener);
    }

    /**
     * Registers multiple listeners to this event bus, using the given removal key.
     * <p>
     * Listeners are found by scanning the {@code listeners} parameter. If the parameter is a {@link Class}, then
     * listener methods are found by scanning the given class for static methods annotated with {@link Listen}.
     * Otherwise, the listener method are found by scanning the passed object for instance methods annotated with
     * {@link Listen}. Instance scanning includes annotated methods in superclasses and implemented interfaces.
     *
     * @param listeners the class or instance to search for listener methods.
     */
    public void registerListeners(Object listeners) {
        registerListeners(listeners, listeners);
    }

    /**
     * Registers multiple listeners to this event bus, using the given removal key.
     * <p>
     * Listeners are found by scanning the {@code listeners} parameter. If the parameter is a {@link Class}, then
     * listener methods are found by scanning the given class for static methods annotated with {@link Listen}.
     * Otherwise, the listener method are found by scanning the passed object for instance methods annotated with
     * {@link Listen}. Instance scanning includes annotated methods in superclasses and implemented interfaces.
     *
     * @param key       the removal key to associate all found listeners with.
     * @param listeners the class or instance to search for listener methods.
     */
    public void registerListeners(Object key, Object listeners) {
        List<ListenerHolder> holders = findListeners(listeners);
        registerListeners(key, holders);
    }

    /**
     * Unregisters all listeners associated with the given key.
     *
     * @param key the key of the listeners to unregister.
     */
    public void unregisterListeners(Object key) {
        for (Event<?> event : events.values()) {
            event.unregister(key);
        }
    }

    private List<ListenerHolder> findListeners(Object listeners) {
        List<ListenerHolder> holders = new ObjectArrayList<>();

        if (listeners instanceof Class<?> clazz) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())) {
                    Listen l = m.getAnnotation(Listen.class);
                    if (l != null) {
                        holders.add(buildHolder(l, clazz, m, null));
                    }
                }
            }
        } else {
            Class<?> clazz = listeners.getClass();
            for (Method m : clazz.getMethods()) {
                if (Modifier.isPublic(m.getModifiers())) {
                    Listen l = m.getAnnotation(Listen.class);
                    if (l != null) {
                        holders.add(buildHolder(l, clazz, m, listeners));
                    }
                }
            }
        }

        return holders;
    }

    private ListenerHolder buildHolder(Listen annotation, Class<?> listenerClass, Method listenerMethod,
                                       @Nullable Object instance) {
        Class<?> callbackInterface = annotation.value();
        ResourceLocation phase = ResourceLocation.parse(annotation.phase());

        Method interfaceMethod = AdapterUtils.getSingularMethod(callbackInterface);
        if (interfaceMethod == null) throw new IllegalArgumentException(
            "Tried to listen to callback interface " + callbackInterface + " which is not a functional interface");

        MethodType expectedMethodType =
            MethodType.methodType(interfaceMethod.getReturnType(), interfaceMethod.getParameterTypes());
        MethodType actualMethodType =
            MethodType.methodType(listenerMethod.getReturnType(), listenerMethod.getParameterTypes());

        checkReturnTypes(callbackInterface, listenerClass, interfaceMethod, listenerMethod, expectedMethodType,
            actualMethodType);

        try {
            if (instance == null) {
                MethodHandle handle =
                    AdapterUtils.LOOKUP.findStatic(listenerClass, listenerMethod.getName(), actualMethodType);

                Object listener = callbackInterface.cast(
                    LambdaMetafactory.metafactory(AdapterUtils.LOOKUP, interfaceMethod.getName(),
                            MethodType.methodType(callbackInterface), expectedMethodType, handle, expectedMethodType)
                        .getTarget().invoke());

                return new ListenerHolder(EventKey.fromClass(callbackInterface, annotation.qualifier()), phase,
                    listener);
            } else {
                MethodHandle handle =
                    AdapterUtils.LOOKUP.findVirtual(listenerClass, listenerMethod.getName(), actualMethodType);

                Object listener = callbackInterface.cast(
                    LambdaMetafactory.metafactory(AdapterUtils.LOOKUP, interfaceMethod.getName(),
                        MethodType.methodType(callbackInterface, listenerClass), expectedMethodType, handle,
                        expectedMethodType).getTarget().invoke(instance));

                return new ListenerHolder(EventKey.fromClass(callbackInterface, annotation.qualifier()), phase,
                    listener);
            }
        } catch (Throwable e) {
            throw handleError(callbackInterface, listenerClass, interfaceMethod, listenerMethod, expectedMethodType,
                actualMethodType, e);
        }
    }

    private static void checkReturnTypes(Class<?> callbackInterface, Class<?> listenerClass, Method interfaceMethod,
                                         Method staticMethod, MethodType expectedMethodType,
                                         MethodType actualMethodType) {
        if (!expectedMethodType.returnType().isAssignableFrom(actualMethodType.returnType())) {
            String callbackClassName = callbackInterface.getName().replace('.', '/');
            String listenerClassName = listenerClass.getName().replace('.', '/');
            Type expectedType = AdapterUtils.getMethodType(expectedMethodType);
            Type actualType = AdapterUtils.getMethodType(actualMethodType);
            CELog.LOGGER.warn(
                "[Common Events] Callback listener {}.{}{} has return type that is incompatible with callback interface {}.{}{}. " +
                    "The associated event may throw a ClassCastException when called.", listenerClassName,
                staticMethod.getName(), actualType, callbackClassName, interfaceMethod.getName(), expectedType);
        }
    }

    private static RuntimeException handleError(Class<?> callbackInterface, Class<?> listenerClass,
                                                Method interfaceMethod, Method staticMethod,
                                                MethodType expectedMethodType, MethodType actualMethodType,
                                                Throwable e) {
        String callbackClassName = callbackInterface.getName().replace('.', '/');
        String listenerClassName = listenerClass.getName().replace('.', '/');
        Type expectedType = AdapterUtils.getMethodType(expectedMethodType);
        Type actualType = AdapterUtils.getMethodType(actualMethodType);
        return new RuntimeException(
            "Error connecting listener method " + listenerClassName + "." + staticMethod.getName() + actualType +
                " with callback interface " + callbackClassName + "." + interfaceMethod.getName() + expectedType, e);
    }

    @SuppressWarnings("unchecked")
    private void registerListeners(Object key, List<ListenerHolder> holders) {
        for (ListenerHolder holder : holders) {
            Event<Object> event = (Event<Object>) events.get(holder.key());
            if (event != null) event.registerKeyed(key, holder.listener());
        }
    }
}
