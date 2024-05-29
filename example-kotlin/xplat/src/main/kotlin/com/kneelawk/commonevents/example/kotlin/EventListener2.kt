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

package com.kneelawk.commonevents.example.kotlin

import com.kneelawk.commonevents.api.Listen
import com.kneelawk.commonevents.api.Scan

@Scan
class EventListener2 {
    companion object {
        init {
            CommonEventsExampleKotlin.LOGGER.info("# EventListener2 initialized")
        }

        @Listen(MyCallback::class, qualifier = "my_qualifier")
        fun onDoSomething(str: String) {
            CommonEventsExampleKotlin.LOGGER.info("> EventListener2 received event: $str")
        }
    }

    // This method will not be added to an event during scanning, but will be picked up when adding
    // an EventListener2 instance to an event bus.
    // IMPLEMENTATION NOTE: The presence of this annotation does mean that this class will be loaded when
    // the associated event is loaded, even though this listener is not valid to be added during event
    // initialization. This is because the EventListener2 class must be loaded in order for Kotlin to
    // determine that it is not a valid object class.
    @Listen(MyCallback::class, qualifier = "my_qualifier")
    fun instanceListener(str: String) {
        CommonEventsExampleKotlin.LOGGER.info("> EventListener2 instanceListener0 received event: $str")
    }

    @Listen(EventHolder.Callback::class)
    fun instanceListener1() {
        CommonEventsExampleKotlin.LOGGER.info("> EventListener2 instanceListener1 received event")
    }

    @Listen(EventHolder2.Callback::class)
    fun instanceListener2() {
        CommonEventsExampleKotlin.LOGGER.info("> EventListener2 instanceListener2 received event")
    }
}
