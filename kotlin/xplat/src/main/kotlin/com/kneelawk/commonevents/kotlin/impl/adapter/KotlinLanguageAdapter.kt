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
import com.kneelawk.commonevents.api.Scan
import com.kneelawk.commonevents.api.adapter.BusEventHandle
import com.kneelawk.commonevents.api.adapter.LanguageAdapter
import com.kneelawk.commonevents.api.adapter.ListenerHandle
import com.kneelawk.commonevents.api.adapter.scan.ScanRequest
import com.kneelawk.commonevents.api.adapter.scan.ScanResult
import com.kneelawk.commonevents.api.adapter.scan.ScannableInfo
import com.kneelawk.commonevents.impl.CELog
import net.minecraft.resources.ResourceLocation
import org.objectweb.asm.Type
import java.nio.file.Files

class KotlinLanguageAdapter : LanguageAdapter {
    override fun getId(): String = LanguageAdapter.KOTLIN_ADAPTER_ID

    override fun scan(request: ScanRequest): ScanResult {
        val mod = request.mod
        val modFile = mod.modFile
        val modIds = modFile.modIdStr
        val info = mod.info

        val listeners = mutableMapOf<EventKey, MutableList<ListenerHandle>>()
        val busEvents = mutableMapOf<ResourceLocation, MutableList<BusEventHandle>>()
        val scanned = mutableSetOf<Type>()
        val queued = mutableListOf<Type>()

        fun put(handle: ListenerHandle) = listeners.computeIfAbsent(handle.key) { mutableListOf() }.add(handle)
        fun put(handle: BusEventHandle) =
            handle.busNames.forEach { busName -> busEvents.computeIfAbsent(busName) { mutableListOf() }.add(handle) }

        fun markScanned(type: Type) = scanned.add(type)
        fun queueType(type: Type) = queued.add(type)

        val loader = javaClass.classLoader

        if (info is ScannableInfo.All) {
            val classesToScan = modFile.getAnnotatedClasses(Scan::class.java);
            if (classesToScan == null) {
                for (root in modFile.rootPaths) {
                    try {
                        Files.walk(root).use { stream ->
                            for (classPath in stream) {
                                val fileName = classPath.fileName
                                if (fileName != null && fileName.toString().endsWith(".class")) {
                                    KotlinClassScanner.scan(
                                        classPath, modIds, request.isClientSide, false, ::put, ::put,
                                        ::markScanned, ::queueType
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        CELog.LOGGER.warn("[Common Events] Error scanning classes in mod {}.", modIds, e)
                    }
                }
            } else {
                for (classToScan in classesToScan) {
                    val classUrl = loader.getResource(classToScan.internalName + ".class")
                    if (classUrl != null) {
                        KotlinClassScanner.scan(
                            classUrl, modIds, request.isClientSide, true, ::put, ::put, ::markScanned, ::queueType
                        )
                    }
                }
            }
        } else if (info is ScannableInfo.Only && info.classes.isNotEmpty()) {
            for (classStr in info.classes) {
                val classUrl = loader.getResource(classStr.replace('.', '/') + ".class")
                if (classUrl != null) {
                    KotlinClassScanner.scan(
                        classUrl, modIds, request.isClientSide, true, ::put, ::put, ::markScanned, ::queueType
                    )
                } else {
                    CELog.LOGGER.warn(
                        "[Common Events] Scan class {} not found in mod {}. Skipping...", classStr, modIds
                    )
                }
            }
        }

        // scan companion objects
        for (ty in queued) {
            if (!scanned.contains(ty)) {
                val classUrl = loader.getResource(ty.internalName + ".class")
                if (classUrl != null) {
                    KotlinClassScanner.scan(
                        classUrl, modIds, request.isClientSide, true, ::put, ::put, ::markScanned
                    ) {}
                } else {
                    CELog.LOGGER.warn(
                        "[Common Events] Kotlin companion object class {} not found in mod {}. Skipping...",
                        ty.className,
                        modIds
                    )
                }
            }
        }

        return ScanResult(listeners, busEvents)
    }
}
