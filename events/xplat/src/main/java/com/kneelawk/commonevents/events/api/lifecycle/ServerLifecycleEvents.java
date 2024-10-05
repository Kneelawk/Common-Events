package com.kneelawk.commonevents.events.api.lifecycle;

import net.minecraft.server.MinecraftServer;

import com.kneelawk.commonevents.api.BusEvent;
import com.kneelawk.commonevents.api.Event;
import com.kneelawk.commonevents.api.Scan;
import com.kneelawk.commonevents.mainbus.api.CommonEventsMainBus;

/**
 * General purpose server lifecycle events.
 */
@Scan
public final class ServerLifecycleEvents {
    private ServerLifecycleEvents() {}

    /**
     * Called when the Minecraft server is starting.
     * <p>
     * This is called before the {@link net.minecraft.server.players.PlayerList player list} and any worlds are loaded.
     */
    @BusEvent(CommonEventsMainBus.NAME)
    public static final Event<ServerStarting> SERVER_STARTING = Event.createSimple(ServerStarting.class);

    /**
     * Called when the Minecraft server is about to load worlds.
     * <p>
     * This is called after the {@link net.minecraft.server.players.PlayerList player list} has loaded but before any worlds are loaded.
     */
    @BusEvent(CommonEventsMainBus.NAME)
    public static final Event<ServerLoadingWorlds> SERVER_LOADING_WORLDS =
        Event.createSimple(ServerLoadingWorlds.class);

    /**
     * Called after the Minecraft server has finished loading worlds and started.
     */
    @BusEvent(CommonEventsMainBus.NAME)
    public static final Event<ServerStarted> SERVER_STARTED = Event.createSimple(ServerStarted.class);

    /**
     * Called when the server is beginning its shutdown sequence.
     */
    @BusEvent(CommonEventsMainBus.NAME)
    public static final Event<ServerStopping> SERVER_STOPPING = Event.createSimple(ServerStopping.class);

    /**
     * Called when the server has finished its shutdown sequence.
     */
    @BusEvent(CommonEventsMainBus.NAME)
    public static final Event<ServerStopped> SERVER_STOPPED = Event.createSimple(ServerStopped.class);

    /**
     * Callback fired when the server is starting.
     */
    @FunctionalInterface
    public interface ServerStarting {
        /**
         * Called when the server is starting.
         *
         * @param server the minecraft server.
         */
        void onServerStarting(MinecraftServer server);
    }

    /**
     * Callback fired when the server is about to load worlds.
     */
    @FunctionalInterface
    public interface ServerLoadingWorlds {
        /**
         * Called when the server is about to load worlds.
         *
         * @param server the minecraft server.
         */
        void onWorldsLoading(MinecraftServer server);
    }

    /**
     * Callback fired when the server has finished starting.
     */
    @FunctionalInterface
    public interface ServerStarted {
        /**
         * Called after the server has finished starting.
         *
         * @param server the minecraft server.
         */
        void onServerStarted(MinecraftServer server);
    }

    /**
     * Callback fired when the server is beginning its shutdown sequence.
     */
    @FunctionalInterface
    public interface ServerStopping {
        /**
         * Called when the server is beginning its shutdown sequence.
         *
         * @param server the minecraft server.
         */
        void onServerStopping(MinecraftServer server);
    }

    /**
     * Callback fired when the server has finished its shutdown sequence.
     */
    @FunctionalInterface
    public interface ServerStopped {
        /**
         * Called when the server has finished its shutdown sequence.
         *
         * @param server the minecraft server.
         */
        void onServerStopped(MinecraftServer server);
    }
}
