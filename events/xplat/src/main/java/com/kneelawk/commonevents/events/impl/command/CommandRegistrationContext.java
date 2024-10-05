package com.kneelawk.commonevents.events.impl.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.kneelawk.commonevents.events.api.command.CommandRegistrationCallback;

public record CommandRegistrationContext(CommandDispatcher<CommandSourceStack> dispatcher,
                                         Commands.CommandSelection commandSelection,
                                         CommandBuildContext registryAccess)
    implements CommandRegistrationCallback.Context {
    @Override
    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return dispatcher;
    }

    @Override
    public Commands.CommandSelection getCommandSelection() {
        return commandSelection;
    }

    @Override
    public CommandBuildContext getRegistryAccess() {
        return registryAccess;
    }
}
