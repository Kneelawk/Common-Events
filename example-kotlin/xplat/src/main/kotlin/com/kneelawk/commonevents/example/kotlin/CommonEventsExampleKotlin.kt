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

import com.kneelawk.commonevents.api.Event
import com.kneelawk.commonevents.api.EventBus
import net.minecraft.resources.ResourceLocation
import org.slf4j.LoggerFactory

object CommonEventsExampleKotlin {
    const val MOD_ID = "common_events_example_kotlin"
    val LOGGER = LoggerFactory.getLogger(MOD_ID)

    init {
        LOGGER.info("# Creating EVENT_BUS...")
    }

    val EVENT_BUS = EventBus.builder(ResourceLocation(MOD_ID, "bus")).build()

    init {
        LOGGER.info("# EVENT_BUS created")
        LOGGER.info("# Creating MY_EVENT...")
    }

    val MY_EVENT =
        Event.create(
            MyCallback::class.java,
            "my_qualifier"
        ) { listeners -> MyCallback { str -> listeners.forEach { it.doSomething(str) } } }

    init {
        LOGGER.info("# MY_EVENT created")
        EVENT_BUS.addEvent(MY_EVENT)
    }

    fun init() {
        LOGGER.info("  Firing MY_EVENT...")
        MY_EVENT.invoker().doSomething("Hello world!")
        LOGGER.info("  MY_EVENT fired")
    }
}
