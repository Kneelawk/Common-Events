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
import com.kneelawk.commonevents.api.adapter.util.AdapterUtils.*
import com.kneelawk.commonevents.impl.CEConstants
import com.kneelawk.commonevents.impl.CELog
import net.minecraft.ResourceLocationException
import net.minecraft.resources.ResourceLocation
import org.objectweb.asm.*
import java.io.BufferedInputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

class KotlinClassScanner(
    private val isClientSide: Boolean, forceScan: Boolean, private val listenerFound: (ListenerHandle) -> Unit,
    private val markScannedType: (Type) -> Unit, private val queueType: (Type) -> Unit
) : ClassVisitor(API) {
    companion object {
        private fun scan(
            bis: BufferedInputStream, isClientSide: Boolean, forceScan: Boolean,
            listenerFound: (ListenerHandle) -> Unit, markScannedType: (Type) -> Unit, queueType: (Type) -> Unit
        ) {
            val cr = ClassReader(bis)
            cr.accept(
                KotlinClassScanner(isClientSide, forceScan, listenerFound, markScannedType, queueType),
                ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
            )
        }

        fun scan(
            classUrl: URL, modIds: String, isClientSide: Boolean, forceScan: Boolean,
            listenerFound: (ListenerHandle) -> Unit, markScannedType: (Type) -> Unit, queueType: (Type) -> Unit
        ) {
            try {
                classUrl.openStream().use { stream ->
                    BufferedInputStream(stream).use { bis ->
                        scan(
                            bis, isClientSide, forceScan, listenerFound, markScannedType, queueType
                        )
                    }
                }
            } catch (e: Exception) {
                CELog.LOGGER.warn("[Common Events] Error scanning class {} in mod {}", classUrl, modIds, e);
            }
        }

        fun scan(
            classPath: Path, modIds: String, isClientSide: Boolean, forceScan: Boolean,
            listenerFound: (ListenerHandle) -> Unit, markScannedType: (Type) -> Unit, queueType: (Type) -> Unit
        ) {
            try {
                Files.newInputStream(classPath).use { stream ->
                    BufferedInputStream(stream).use { bis ->
                        scan(
                            bis, isClientSide, forceScan, listenerFound, markScannedType, queueType
                        )
                    }
                }
            } catch (e: Exception) {
                CELog.LOGGER.warn("[Commmon Events] Error scanning class {} in mod {}", classPath, modIds, e)
            }
        }
    }

    var shouldScan = forceScan
    var visitingClass: Type? = null
    val innerClasses = mutableSetOf<Type>()

    override fun visit(
        version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?
    ) {
        visitingClass = Type.getObjectType(name)
    }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        if (LISTENER_ANNOTATION_NAME == descriptor) {
            CELog.LOGGER.debug("[Common Events] Found annotated class: {}", descriptor)
            markScannedType(visitingClass!!)
            return ClassAnnotationScanner()
        } else return null
    }

    private inner class ClassAnnotationScanner : AnnotationVisitor(API) {
        private var isValidSide = true
        override fun visitEnum(name: String?, descriptor: String?, value: String?) {
            if (LISTENER_SIDE_FIELD_NAME == name) {
                isValidSide =
                    (LISTENER_SIDE_BOTH_VALUE == value) || ((LISTENER_SIDE_CLIENT_VALUE == value) == isClientSide)
            }
        }

        override fun visitEnd() {
            shouldScan = isValidSide
            if (!isValidSide) {
                CELog.LOGGER.debug(
                    "[Common Events] Skipping {} because it is on the wrong side.", visitingClass?.internalName
                )
            }
        }
    }

    override fun visitInnerClass(name: String, outerName: String, innerName: String, access: Int) {
        if (!shouldScan) return

        val nameType = Type.getObjectType(name)
        if (visitingClass == nameType) return

        // mechanism to detect companion objects
        innerClasses.add(nameType)
    }

    override fun visitField(
        access: Int, name: String, descriptor: String, signature: String?, value: Any?
    ): FieldVisitor? {
        if (!shouldScan) return null

        val fieldType = Type.getType(descriptor)
        if (innerClasses.contains(fieldType)) {
            // fair to assume this is a companion object field
            queueType(fieldType)
        }

        // TODO: event field scanning for auto-adding to event busses
        return null
    }

    override fun visitMethod(
        access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?
    ): MethodVisitor? {
        if (!shouldScan) return null

        if ((access and Opcodes.ACC_PUBLIC) != 0) {
            val static = (access and Opcodes.ACC_STATIC) != 0

            return MethodScanner(name, Type.getMethodType(descriptor), static)
        }

        return null
    }

    private inner class MethodScanner(
        private val name: String, private val descriptor: Type, private val static: Boolean
    ) : MethodVisitor(API) {
        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            if (LISTEN_ANNOTATION_NAME == descriptor) {
                return MethodAnnotationScanner()
            }
            return null
        }


        private inner class MethodAnnotationScanner : AnnotationVisitor(API) {
            private var keyType: Type? = null
            private var qualifier = CEConstants.DEFAULT_QUALIFIER
            private var phase = CEConstants.DEFAULT_PHASE

            override fun visit(name: String, value: Any) {
                if (LISTEN_VALUE_FIELD_NAME == name && value is Type) {
                    keyType = value
                } else if (LISTEN_QUALIFIER_FIELD_NAME == name && value is String) {
                    qualifier = value
                } else if (LISTEN_PHASE_FIELD_NAME == name && value is String) {
                    try {
                        phase = ResourceLocation(value)
                    } catch (e: ResourceLocationException) {
                        CELog.LOGGER.warn(
                            "[Common Events] Encountered invalid phase '{}' in {}.{}{}",
                            value, visitingClass!!.internalName, name, descriptor, e
                        )
                    }
                }
            }

            override fun visitEnd() {
                keyType?.let { ty ->
                    listenerFound(
                        KotlinListenerHandle(
                            ListenerKey(ty, qualifier), phase, visitingClass!!, name, descriptor, static
                        )
                    )
                }
            }
        }
    }
}