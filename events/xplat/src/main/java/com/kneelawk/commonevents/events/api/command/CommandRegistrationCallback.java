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

package com.kneelawk.commonevents.events.api.command;

import org.jetbrains.annotations.ApiStatus;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.kneelawk.commonevents.api.BusEvent;
import com.kneelawk.commonevents.api.Event;
import com.kneelawk.commonevents.mainbus.api.CommonEventsMainBus;

/**
 * Callback for registering commands.
 */
@FunctionalInterface
public interface CommandRegistrationCallback {
    /**
     * Event fired when the server is registering commands.
     */
    @BusEvent(CommonEventsMainBus.NAME)
    Event<CommandRegistrationCallback> EVENT = Event.createSimple(CommandRegistrationCallback.class);

    /**
     * Called when the server is registering commands.
     *
     * @param ctx the context for this callback.
     */
    void register(Context ctx);

    /**
     * The context for the {@link CommandRegistrationCallback}.
     */
    @ApiStatus.NonExtendable
    interface Context {
        /**
         * {@return the command dispatcher for registering commands}
         */
        CommandDispatcher<CommandSourceStack> dispatcher();

        /**
         * {@return the command environment}
         */
        Commands.CommandSelection commandSelection();

        /**
         * {@return the registry access}
         */
        CommandBuildContext registryAccess();
    }
}
