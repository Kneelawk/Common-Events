package com.kneelawk.commonevents.events.impl;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.kneelawk.commonevents.events.api.command.CommandRegistrationCallback;

@Mod("common_events_events")
public class CommonEventsEventsMod {
    public CommonEventsEventsMod(IEventBus modBus) {
        modBus.addListener(this::onCommandRegistration);
    }

    private void onCommandRegistration(RegisterCommandsEvent event) {
        CommandRegistrationCallback.EVENT.invoker().register(
            new CommandRegistrationContext(event.getDispatcher(), event.getCommandSelection(),
                event.getBuildContext()));
    }

    private record CommandRegistrationContext(CommandDispatcher<CommandSourceStack> dispatcher,
                                              Commands.CommandSelection commandSelection,
                                              CommandBuildContext registryAccess)
        implements CommandRegistrationCallback.Context {
    }
}
