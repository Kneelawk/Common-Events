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

/**
 * A collection of events, that selectively registers objects based on which callbacks they implement.
 * <p>
 * Event buses organize events by the event's callback interface type and qualifier. This is because objects registered
 * to an event bus are generally registered based on the type of callback they implement and which qualifier they are
 * annotated with.
 */
public final class EventBus {
    private EventBus() {}
}
