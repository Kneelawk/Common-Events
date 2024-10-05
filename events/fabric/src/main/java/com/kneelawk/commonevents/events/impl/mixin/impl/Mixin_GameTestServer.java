package com.kneelawk.commonevents.events.impl.mixin.impl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.MinecraftServer;

import com.kneelawk.commonevents.events.api.lifecycle.ServerLifecycleEvents;

@Mixin(GameTestServer.class)
public class Mixin_GameTestServer {
    @Inject(method = "initServer", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/gametest/framework/GameTestServer;loadLevel()V"))
    private void onLoadingWorlds(CallbackInfoReturnable<Boolean> cir) {
        ServerLifecycleEvents.SERVER_LOADING_WORLDS.invoker().onWorldsLoading((MinecraftServer) (Object) this);
    }
}
