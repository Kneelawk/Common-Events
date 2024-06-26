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

package com.kneelawk.commonevents.impl.scan.java;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.EventKey;
import com.kneelawk.commonevents.api.adapter.BusEventHandle;
import com.kneelawk.commonevents.api.adapter.ListenerHandle;
import com.kneelawk.commonevents.api.adapter.util.AdapterUtils;
import com.kneelawk.commonevents.impl.CEConstants;
import com.kneelawk.commonevents.impl.CELog;

public class ClassScanner extends ClassVisitor {
    public static void scan(URL classUrl, String modIds, boolean isClientSide, boolean forceScan,
                            Consumer<ListenerHandle> listenerFound, Consumer<BusEventHandle> busEventFound) {
        try (InputStream is = classUrl.openStream(); BufferedInputStream buffered = new BufferedInputStream(is)) {
            scan(buffered, isClientSide, forceScan, listenerFound, busEventFound);
        } catch (Exception e) {
            CELog.LOGGER.warn("[Common Events] Error scanning class {} in mod {}", classUrl, modIds, e);
        }
    }

    public static void scan(Path classPath, String modIds, boolean isClientSide, boolean forceScan,
                            Consumer<ListenerHandle> listenerFound, Consumer<BusEventHandle> busEventFound) {
        try (InputStream is = Files.newInputStream(classPath);
             BufferedInputStream buffered = new BufferedInputStream(is)) {
            scan(buffered, isClientSide, forceScan, listenerFound, busEventFound);
        } catch (Exception e) {
            CELog.LOGGER.warn("[Common Events] Error scanning class {} in mod {}", classPath, modIds, e);
        }
    }

    private static void scan(BufferedInputStream buffered, boolean isClientSide, boolean forceScan,
                             Consumer<ListenerHandle> listenerFound, Consumer<BusEventHandle> busEventFound)
        throws IOException {
        ClassReader cr = new ClassReader(buffered);
        cr.accept(new ClassScanner(isClientSide, forceScan, listenerFound, busEventFound),
            ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }

    private final boolean isClientSide;
    private final Consumer<ListenerHandle> listenerFound;
    private final Consumer<BusEventHandle> busEventFound;
    private Type visitingClass = null;
    private boolean shouldScan;

    protected ClassScanner(boolean isClientSide, boolean forceScan, Consumer<ListenerHandle> listenerFound,
                           Consumer<BusEventHandle> busEventFound) {
        super(AdapterUtils.API);
        this.isClientSide = isClientSide;
        this.listenerFound = listenerFound;
        this.busEventFound = busEventFound;
        shouldScan = forceScan;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        visitingClass = Type.getObjectType(name);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (AdapterUtils.SCAN_ANNOTATION_NAME.equals(descriptor)) {
            CELog.LOGGER.debug("[Common Events] Found annotated class: {}", descriptor);
            return new ClassAnnotationScanner();
        } else {
            return null;
        }
    }

    private class ClassAnnotationScanner extends AnnotationVisitor {
        private boolean isValidSide = true;

        protected ClassAnnotationScanner() {
            super(AdapterUtils.API);
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            if (AdapterUtils.SCAN_SIDE_FIELD_NAME.equals(name)) {
                isValidSide = AdapterUtils.SCAN_SIDE_BOTH_VALUE.equals(value) ||
                    (AdapterUtils.SCAN_SIDE_CLIENT_VALUE.equals(value) == isClientSide);
            }
        }

        @Override
        public void visitEnd() {
            shouldScan = isValidSide;
            if (!isValidSide) {
                CELog.LOGGER.debug("[Common Events] Skipping {} because it is on the wrong side.",
                    visitingClass.getInternalName());
            }
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        if (!shouldScan) return null;

        if ((access & Opcodes.ACC_PUBLIC) != 0 && ((access & Opcodes.ACC_STATIC) != 0)) {
            return new MethodScanner(name, Type.getMethodType(descriptor));
        } else {
            return null;
        }
    }

    private class MethodScanner extends MethodVisitor {
        private final String name;
        private final Type descriptor;

        protected MethodScanner(String name, Type descriptor) {
            super(AdapterUtils.API);
            this.name = name;
            this.descriptor = descriptor;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (AdapterUtils.LISTEN_ANNOTATION_NAME.equals(descriptor)) {
                return new MethodAnnotationScanner();
            } else {
                return null;
            }
        }

        private class MethodAnnotationScanner extends AnnotationVisitor {
            private Type keyType = null;
            private String qualifier = CEConstants.DEFAULT_QUALIFIER;
            private ResourceLocation phase = CEConstants.DEFAULT_PHASE;

            protected MethodAnnotationScanner() {
                super(AdapterUtils.API);
            }

            @Override
            public void visit(String name, Object value) {
                if (AdapterUtils.LISTEN_VALUE_FIELD_NAME.equals(name) && value instanceof Type type) {
                    keyType = type;
                } else if (AdapterUtils.LISTEN_QUALIFIER_FIELD_NAME.equals(name) && value instanceof String str) {
                    qualifier = str;
                } else if (AdapterUtils.LISTEN_PHASE_FIELD_NAME.equals(name) && value instanceof String str) {
                    try {
                        phase = ResourceLocation.parse(str);
                    } catch (ResourceLocationException e) {
                        CELog.LOGGER.warn("[Common Events] Encountered invalid phase '{}' in {}.{}{}", str,
                            visitingClass.getInternalName(), name, descriptor, e);
                    }
                }
            }

            @Override
            public void visitEnd() {
                if (keyType != null) {
                    listenerFound.accept(
                        new JavaListenerHandle(new EventKey(keyType, qualifier), phase, visitingClass, name,
                            descriptor));
                }
            }
        }
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (!shouldScan) return null;

        if ((access & Opcodes.ACC_STATIC) != 0 && (access & Opcodes.ACC_PUBLIC) != 0) {
            return new FieldScanner(name);
        } else {
            return null;
        }
    }

    private class FieldScanner extends FieldVisitor {
        private final String fieldName;

        protected FieldScanner(String fieldName) {
            super(AdapterUtils.API);
            this.fieldName = fieldName;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (AdapterUtils.BUS_EVENT_ANNOTATION_NAME.equals(descriptor)) {
                return new FieldAnnotationScanner();
            } else {
                return null;
            }
        }

        private class FieldAnnotationScanner extends AnnotationVisitor {
            private final List<ResourceLocation> eventBusNames = new ArrayList<>();
            private final Set<ResourceLocation> eventBusSet = new HashSet<>();

            protected FieldAnnotationScanner() {
                super(AdapterUtils.API);
            }

            @Override
            public AnnotationVisitor visitArray(String name) {
                if (AdapterUtils.BUS_EVENT_VALUE_FIELD_NAME.equals(name)) {
                    return new FieldAnnotationArrayScanner();
                } else {
                    return null;
                }
            }

            private class FieldAnnotationArrayScanner extends AnnotationVisitor {
                protected FieldAnnotationArrayScanner() {
                    super(AdapterUtils.API);
                }

                @Override
                public void visit(String name, Object value) {
                    if (value instanceof String str) {
                        ResourceLocation busName;
                        try {
                            busName = ResourceLocation.parse(str);
                        } catch (ResourceLocationException e) {
                            CELog.LOGGER.warn(
                                "[Common Events] Encountered invalid event bus name '{}' in {}.{} annotation", str,
                                visitingClass.getInternalName(), fieldName, e);
                            return;
                        }

                        if (!eventBusSet.add(busName)) {
                            CELog.LOGGER.warn(
                                "[Common Events] Event bus name '{}' mentioned multiple times in {}.{} annotation. Ignoring...",
                                busName, visitingClass.getInternalName(), fieldName);
                            return;
                        }

                        eventBusNames.add(busName);
                    }
                }
            }

            @Override
            public void visitEnd() {
                if (!eventBusNames.isEmpty()) {
                    busEventFound.accept(
                        new JavaBusEventHandle(eventBusNames.toArray(ResourceLocation[]::new), visitingClass,
                            fieldName));
                } else {
                    CELog.LOGGER.warn("[Common Events] No bus names present in {}.{} annotation. Ignoring...",
                        visitingClass.getInternalName(), fieldName);
                }
            }
        }
    }
}
