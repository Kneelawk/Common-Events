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
