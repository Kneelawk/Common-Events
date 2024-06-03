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

package com.kneelawk.commonevents.events.impl;

import net.fabricmc.api.ModInitializer;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.kneelawk.commonevents.events.api.command.CommandRegistrationCallback;

public class CommonEventsEventsMod implements ModInitializer {
    @Override
    public void onInitialize() {
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> CommandRegistrationCallback.EVENT.invoker()
                .register(new CommandRegistrationContext(dispatcher, environment, registryAccess)));
    }

    private record CommandRegistrationContext(CommandDispatcher<CommandSourceStack> dispatcher,
                                              Commands.CommandSelection commandSelection,
                                              CommandBuildContext registryAccess)
        implements CommandRegistrationCallback.Context {
    }
}
