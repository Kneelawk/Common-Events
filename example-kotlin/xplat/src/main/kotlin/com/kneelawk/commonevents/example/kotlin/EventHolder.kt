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

import com.kneelawk.commonevents.api.BusEvent
import com.kneelawk.commonevents.api.Event
import com.kneelawk.commonevents.api.Scan
import com.kneelawk.commonevents.example.kotlin.EventHolder.Callback

@Scan
object EventHolder {
    @BusEvent("common_events_example_kotlin:bus")
    val EVENT = Event.create(Callback::class.java) { callbacks -> Callback { callbacks.forEach { it.call() } } }

    fun interface Callback {
        fun call()
    }
    
    fun fire() {
        CommonEventsExampleKotlin.LOGGER.info("  Firing EventHolder.EVENT")
        EVENT.invoker().call()
        CommonEventsExampleKotlin.LOGGER.info("  EventHolder.EVENT fired")
    }
}