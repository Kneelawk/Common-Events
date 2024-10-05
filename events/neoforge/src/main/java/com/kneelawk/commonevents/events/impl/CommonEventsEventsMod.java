package com.kneelawk.commonevents.events.impl;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import com.kneelawk.commonevents.events.api.command.CommandRegistrationCallback;
import com.kneelawk.commonevents.events.impl.command.CommandRegistrationContext;

@Mod(CEEConstants.MOD_ID)
public class CommonEventsEventsMod {
    public CommonEventsEventsMod(IEventBus modBus) {
        modBus.addListener(this::onCommandRegistration);
    }

    private void onCommandRegistration(RegisterCommandsEvent event) {
        CommandRegistrationCallback.EVENT.invoker().register(
            new CommandRegistrationContext(event.getDispatcher(), event.getCommandSelection(),
                event.getBuildContext()));
    }
}
