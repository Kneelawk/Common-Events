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

import com.kneelawk.commonevents.api.EventKey
import com.kneelawk.commonevents.api.adapter.CallbackSettings
import com.kneelawk.commonevents.api.adapter.ListenerHandle
import com.kneelawk.commonevents.api.adapter.util.AdapterUtils
import com.kneelawk.commonevents.api.adapter.util.ListenerBuilder
import net.minecraft.resources.ResourceLocation
import org.objectweb.asm.Type

class KotlinListenerHandle(
    private val key: EventKey, private val phase: ResourceLocation, private val listenerClass: Type,
    private val methodName: String, private val methodDescriptor: Type, private val static: Boolean
) : ListenerHandle {
    override fun getKey(): EventKey = key

    override fun getPhase(): ResourceLocation = phase

    override fun <T : Any> createCallback(
        callbackSettings: CallbackSettings<T>
    ): T? {
        val listenerClazz = Class.forName(listenerClass.className)

        if (static) {
            return ListenerBuilder.buildStaticListener(
                callbackSettings.interfaceClass,
                listenerClazz,
                AdapterUtils.getDeclaredMethod(listenerClazz, methodName, methodDescriptor),
                callbackSettings.builderSettings
            )
        } else {
            val objectInstance = listenerClazz.kotlin.objectInstance ?: return null

            return ListenerBuilder.buildInstanceListener(
                callbackSettings.interfaceClass,
                objectInstance,
                AdapterUtils.getMethod(listenerClazz, methodName, methodDescriptor),
                callbackSettings.builderSettings
            )
        }
    }

    override fun toString(): String {
        val staticStr = if (static) "static " else ""
        return """KotlinListenerHandle{$key($phase) -> $staticStr${listenerClass.internalName}.$methodName$methodDescriptor}"""
    }
}
