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

package com.kneelawk.commonevents.kotlin.impl.adapter

import com.kneelawk.commonevents.api.Event
import com.kneelawk.commonevents.api.adapter.BusEventHandle
import com.kneelawk.commonevents.api.adapter.scan.BadEventException
import net.minecraft.resources.ResourceLocation
import org.objectweb.asm.Type
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.jvm.kotlinProperty

class KotlinBusEventHandle(
    private val busNames: Array<ResourceLocation>, private val holderClass: Type, private val fieldName: String
) : BusEventHandle {
    override fun getBusNames(): Array<ResourceLocation> = busNames

    override fun getEvent(): Event<*> {
        val holderClazz = Class.forName(holderClass.className)
        val field = holderClazz.getDeclaredField(fieldName)
        val prop = field.kotlinProperty

        if (prop == null) {
            return field.get(null) as Event<*>?
                ?: throw BadEventException("Encountered @BusEvent annotated field that has not been statically initialized")
        } else {
            val holderKlass = holderClazz.kotlin
            val obj = holderKlass.objectInstance ?: holderKlass.companionObjectInstance
            ?: throw BadEventException("Encountered @BusEvent annotated field not located in an object or companion object")

            return prop.getter.call(obj) as Event<*>?
                ?: throw BadEventException("Encountered @BusEvent annotated property that has not been statically initialized")
        }
    }

    override fun toString(): String {
        return "KotlinBusEventHandle{" + holderClass.internalName + "." + fieldName + " -> " + busNames.contentToString() + "}"
    }
}
