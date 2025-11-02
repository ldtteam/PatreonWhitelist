package com.ldtteam.patreonwhitelist;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.players.UserWhiteList;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class WhitelistOverride extends UserWhiteList
{
    public WhitelistOverride(File file)
    {
        super(file);
    }

    @Override
    public boolean isWhiteListed(@NotNull GameProfile profile)
    {
        if (super.isWhiteListed(profile))
        {
            Log.getLogger().log(Level.INFO, "Whitelist authenticated");
            return true;
        }

        return PatreonWhitelist.config.checkAuth(profile);
    }
}
