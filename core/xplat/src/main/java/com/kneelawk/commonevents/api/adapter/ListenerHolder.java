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

import com.kneelawk.commonevents.api.EventKey;

/**
 * Holds an instance of a listener along with the {@link EventKey} for the listener.
 * <p>
 * This is analogous to {@link ListenerHandle} but for already instantiated listeners.
 *
 * @param key      the key of the event the listener wants to subscribe to.
 * @param listener the listener implementing the event's callback interface.
 */
public record ListenerHolder(EventKey key, Object listener) {
}
