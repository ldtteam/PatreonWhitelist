package com.ldtteam.patreonwhitelist;



import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.io.File;

@Mod("patreonwhitelist")
public class PatreonWhitelist
{
    public static final String MOD_ID = "patreonwhitelist";

    public PatreonWhitelist(final FMLModContainer modContainer, final Dist dist)
    {
        NeoForge.EVENT_BUS.register(this.getClass());
    }

    @SubscribeEvent
    public static void ServerStart(ServerStartedEvent event) {
        event.getServer().getPlayerList().whitelist = new WhitelistOverride(new File("whitelist.json"));
    }
}
