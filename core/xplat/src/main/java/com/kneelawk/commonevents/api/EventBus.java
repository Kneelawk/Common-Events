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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.adapter.ListenerHolder;

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

    private EventBus(ResourceLocation name) {this.name = name;}

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
     * Registers a listener for the given event type with this bus.
     *
     * @param callbackInterface the callback interface the event handles and the listener implements.
     * @param listener          the event listener to register.
     * @param <T>               the type of the callback interface the listener implements.
     * @throws IllegalArgumentException if the specified callback interface and qualifier do not match any events in
     *                                  this bus.
     */
    public <T> void registerListener(Class<T> callbackInterface, T listener) {
        registerListener(callbackInterface, Event.DEFAULT_QUALIFIER, Event.DEFAULT_PHASE, listener, listener);
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
        registerListener(callbackInterface, Event.DEFAULT_QUALIFIER, phase, listener, listener);
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
    public <T> void getisterListener(Class<T> callbackInterface, Object key, T listener) {
        registerListener(callbackInterface, Event.DEFAULT_QUALIFIER, Event.DEFAULT_PHASE, key, listener);
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
    public <T> void registerListener(Class<T> callbackInterface, ResourceLocation phase, Object key, T listener) {
        registerListener(callbackInterface, Event.DEFAULT_QUALIFIER, phase, key, listener);
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
        registerListener(callbackInterface, qualifier, Event.DEFAULT_PHASE, listener, listener);
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
        registerListener(callbackInterface, qualifier, phase, listener, listener);
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
    public <T> void registerListener(Class<T> callbackInterface, String qualifier, Object key, T listener) {
        registerListener(callbackInterface, qualifier, Event.DEFAULT_PHASE, key, listener);
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
    public <T> void registerListener(Class<T> callbackInterface, String qualifier, ResourceLocation phase, Object key,
                                     T listener) {
        Objects.requireNonNull(callbackInterface, "Tried to register a listener with a null callback interface class!");
        Objects.requireNonNull(qualifier, "Tried to register a listener with a null event qualifier!");
        Objects.requireNonNull(phase, "Tried to register a listener to a null phase!");
        Objects.requireNonNull(listener, "Tried to register a null listener!");
        Objects.requireNonNull(key, "Tried to register a listener with a null key!");

        EventKey eventKey = EventKey.fromClass(callbackInterface, qualifier);

        Event<T> event = (Event<T>) events.get(eventKey);
        if (event == null)
            throw new IllegalArgumentException(
                "This event bus does not contain event for the key: '" + eventKey + "'. Contained events: " +
                    events.keySet());

        event.registerKeyed(phase, key, listener);
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

    @SuppressWarnings("unchecked")
    private void registerListeners(Object key, List<ListenerHolder> holders) {
        for (ListenerHolder holder : holders) {
            ((Event<Object>) events.get(holder.key())).registerKeyed(key, holder.listener());
        }
    }
}
