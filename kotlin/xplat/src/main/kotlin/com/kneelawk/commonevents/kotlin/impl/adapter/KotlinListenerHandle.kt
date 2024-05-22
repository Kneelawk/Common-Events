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

import com.kneelawk.commonevents.api.adapter.ListenerHandle
import com.kneelawk.commonevents.api.adapter.ListenerKey
import com.kneelawk.commonevents.api.adapter.util.AdapterUtils
import com.kneelawk.commonevents.impl.CELog
import net.minecraft.resources.ResourceLocation
import org.objectweb.asm.Type
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodType

class KotlinListenerHandle(
    private val key: ListenerKey, private val phase: ResourceLocation, private val listenerClass: Type,
    private val methodName: String, private val methodDescriptor: Type, private val static: Boolean
) : ListenerHandle {
    override fun getKey(): ListenerKey = key

    override fun getPhase(): ResourceLocation = phase

    override fun <T : Any> createCallback(
        callbackClass: Class<T>, singularMethodName: String, singularMethodType: MethodType
    ): T {
        val listenerClazz = Class.forName(listenerClass.className)
        val methodType = AdapterUtils.getMethodType(methodDescriptor)

        if (!singularMethodType.returnType().isAssignableFrom(methodType.returnType())) {
            val singularType = AdapterUtils.getMethodType(singularMethodType)
            CELog.LOGGER.warn(
                "[Common Events] Callback listener {}.{}{} has return type that is incompatible with callback interface {}.{}{}. " +
                        "The associated event may throw a ClassCastException when called.",
                listenerClass.internalName, methodName, methodDescriptor,
                callbackClass.name.replace('.', '/'), singularMethodName, singularType
            )
        }

        if (static) {
            val handle = AdapterUtils.LOOKUP.findStatic(listenerClazz, methodName, methodType)

            return callbackClass.cast(
                LambdaMetafactory.metafactory(
                    AdapterUtils.LOOKUP, singularMethodName, MethodType.methodType(callbackClass), singularMethodType,
                    handle, singularMethodType
                ).target.invoke()
            )
        } else {
            val handle = AdapterUtils.LOOKUP.findVirtual(listenerClazz, methodName, methodType)

            val objectInstance = listenerClazz.kotlin.objectInstance
                ?: throw IllegalArgumentException(listenerClazz.name + " is not a kotlin object but has non-static listener method " + methodName)

            return callbackClass.cast(
                LambdaMetafactory.metafactory(
                    AdapterUtils.LOOKUP, singularMethodName, MethodType.methodType(callbackClass, listenerClazz),
                    singularMethodType, handle, singularMethodType
                ).target.invoke(objectInstance)
            )
        }
    }

    override fun toString(): String {
        val staticStr = if (static) "static " else ""
        return """KotlinListenerHandle{$key($phase) -> $staticStr${listenerClass.internalName}.$methodName$methodDescriptor}"""
    }
}
