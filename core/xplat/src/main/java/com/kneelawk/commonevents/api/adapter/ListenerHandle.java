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

package com.kneelawk.commonevents.api.adapter;

import java.lang.invoke.MethodType;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.EventKey;

/**
 * Holds a reference to a callback handler that can be converted into a callback interface when needed.
 */
public interface ListenerHandle {
    /**
     * Gets the event key this handle is associated with.
     * <p>
     * This translates into the event type this listener is listening for.
     *
     * @return the event key this handle is associated with.
     */
    EventKey getKey();

    /**
     * Gets the event phase during which this handle's listener should be notified.
     *
     * @return the phase of this handle's listener.
     */
    ResourceLocation getPhase();

    /**
     * Creates a callback instance that can actually be registered with the event.
     *
     * @param <T>                the type of the callback interface.
     * @param callbackClass      the class of the callback interface that the handle should be converted into.
     * @param singularMethodName the name of the callback interface's singular method.
     * @param singularMethodType the type of the callback interface's singular method.
     * @return an instance of the specified callback interface.
     * @throws ClassNotFoundException if the class this handle references does not exist.
     */
    <T> T createCallback(Class<T> callbackClass, String singularMethodName, MethodType singularMethodType)
        throws Throwable;
}
