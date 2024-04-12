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
 * Annotate classes containing methods marked with {@link Listen} annotation to allow those classes to be scanned.
 */
public @interface Listener {

    /**
     * Used to restrict registration and loading of this listener class to a single physical side.
     * <p>
     * <b>Note:</b> If none of the events being listened to in this class are loaded on the wrong side, then this class
     * will never be loaded on the wrong side.
     *
     * @return the physical side this listener should be allowed to load on.
     */
    Side side() default Side.BOTH;

    /**
     * Describes a physical side.
     */
    enum Side {
        /**
         * Represents both client and server sides.
         */
        BOTH,
        /**
         * Represents the client side only.
         */
        CLIENT,
        /**
         * Represents the dedicated server side only.
         */
        SERVER
    }
}
