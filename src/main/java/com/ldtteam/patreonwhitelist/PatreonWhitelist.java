package com.ldtteam.patreonwhitelist;



import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;

@Mod("patreonwhitelist")
public class PatreonWhitelist
{
    public static final String MOD_ID = "patreonwhitelist";

    public static Commonconfig config;

    public PatreonWhitelist(final FMLModContainer modContainer, final Dist dist)
    {
        NeoForge.EVENT_BUS.register(this.getClass());

        Pair<Commonconfig, ModConfigSpec> pair =
            new ModConfigSpec.Builder().configure(Commonconfig::new);
        modContainer.registerConfig(ModConfig.Type.COMMON, pair.getRight());
        config = pair.getLeft();
        config.holder = pair.getRight();
        modContainer.getEventBus().addListener(config::onReload);
        modContainer.getEventBus().addListener(config::onLoad);
        NeoForge.EVENT_BUS.addListener(config::onShutdown);
    }

    @SubscribeEvent
    public static void ServerStart(ServerStartedEvent event) {
        event.getServer().getPlayerList().whitelist = new WhitelistOverride(new File("whitelist.json"));
    }
}
