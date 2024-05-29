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

object EventListener3 {
    // These listeners are not scanned and are instead added through an event bus
    @Listen(MyCallback::class, qualifier = "my_qualifier")
    fun listener0(str: String) {
        CommonEventsExampleKotlin.LOGGER.info("> EventListener3 listener0 received event $str")
    }

    @Listen(EventHolder.Callback::class)
    fun listener1() {
        CommonEventsExampleKotlin.LOGGER.info("> EventListener3 listener1 received event")
    }

    @Listen(EventHolder2.Callback::class)
    fun listener2() {
        CommonEventsExampleKotlin.LOGGER.info("> EventListener3 listener2 received event")
    }
}
