package com.kneelawk.commonevents.events.impl;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import com.kneelawk.commonevents.events.api.lifecycle.ServerLifecycleEvents;

@EventBusSubscriber(modid = CEEConstants.MOD_ID)
public class CommenEventsEventsListeners {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        ServerLifecycleEvents.SERVER_LOADING_WORLDS.invoker().onWorldsLoading(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLifecycleEvents.SERVER_STARTED.invoker().onServerStarted(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppedEvent event) {
        ServerLifecycleEvents.SERVER_STOPPED.invoker().onServerStopped(event.getServer());
    }
}
