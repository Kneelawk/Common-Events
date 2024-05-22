/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021 The Quilt Project
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

package com.kneelawk.commonevents.api.phase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Contract;

import net.minecraft.resources.ResourceLocation;

/**
 * Data of a phase.
 *
 * @param <T> the type of data held in a phase
 * @param <P> the type of the phase data
 */
public class PhaseData<T, P extends PhaseData<T, P>> {
    final ResourceLocation name;
    /**
     * The data held within this phase.
     */
    protected T data;
    /**
     * A list of phases that must come after this one.
     */
    protected final List<P> subsequentPhases = new ArrayList<>();
    /**
     * A list of phases that must come before this one.
     */
    protected final List<P> previousPhases = new ArrayList<>();
    VisitStatus visitStatus = VisitStatus.NOT_VISITED;

    /**
     * Constructs a new phase.
     *
     * @param name the name of this phase.
     * @param data the data held within this phase.
     */
    public PhaseData(ResourceLocation name, T data) {
        Objects.requireNonNull(name);

        this.name = name;
        this.data = data;
    }

    /**
     * {@return the identifier of this phase}
     */
    @Contract(pure = true)
    public ResourceLocation getName() {
        return this.name;
    }

    /**
     * {@return the data held by this phase}
     */
    @Contract(pure = true)
    public T getData() {
        return this.data;
    }

    /**
     * Adds a phase that must come after this one.
     *
     * @param phase the phase that must come after this one.
     */
    protected void addSubsequentPhase(P phase) {
        this.subsequentPhases.add(phase);
    }

    /**
     * Adds a phase that must come before this one.
     *
     * @param phase the phase that must come before this one.
     */
    protected void addPreviousPhase(P phase) {
        this.previousPhases.add(phase);
    }

    /**
     * Links two given phases together.
     *
     * @param first  the phase that should be ordered first
     * @param second the phase that should be ordered second
     * @param <T>    the type of data held by the phases
     * @param <P>    this phase-data subclass
     */
    public static <T, P extends PhaseData<T, P>> void link(P first, P second) {
        first.addSubsequentPhase(second);
        second.addPreviousPhase(first);
    }

    enum VisitStatus {
        NOT_VISITED,
        VISITING,
        VISITED
    }
}
