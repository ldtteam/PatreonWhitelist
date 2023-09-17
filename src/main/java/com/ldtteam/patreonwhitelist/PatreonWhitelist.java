package com.ldtteam.patreonwhitelist;

import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;

@Mod("patreonwhitelist")
public class PatreonWhitelist {
    public static final String MOD_ID = "patreonwhitelist";

    public PatreonWhitelist() {
        ((IEventBus) Mod.EventBusSubscriber.Bus.FORGE.bus().get()).register(this.getClass());
    }

    @SubscribeEvent
    public static void ServerStart(ServerStartedEvent event) {
        event.getServer().getPlayerList().whitelist = new WhitelistOverride(new File("whitelist.json"));
    }
}
