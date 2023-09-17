package com.ldtteam.patreonwhitelist;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.players.UserWhiteList;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WhitelistOverride extends UserWhiteList
{
    public WhitelistOverride(File file)
    {
        super(file);
    }

    @Override
    public boolean isWhiteListed(@NotNull GameProfile profile)
    {
        boolean isWhitelisted = super.isWhiteListed(profile);
        Log.getLogger().log(Level.INFO, "Patreon Whitelist validating, File value: " + isWhitelisted);
        return isWhitelisted || this.checkUrl("https://auth.minecolonies.com/api/minecraft/" + profile.getId().toString() + "/whitelist");
    }

    private boolean checkUrl(String urlString)
    {
        try
        {
            Log.getLogger().log(Level.INFO, "Patreon Whitelist validating, contacting API: " + urlString);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000);
            int statusCode = conn.getResponseCode();
            Log.getLogger().log(Level.INFO, "Patreon Whitelist validating, status code: " + statusCode);
            conn.getInputStream();

            String content;
            try
            {
                BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                try
                {
                    content = input.readLine();
                }
                catch (Throwable var15)
                {
                    try
                    {
                        input.close();
                    }
                    catch (Throwable var14)
                    {
                        var15.addSuppressed(var14);
                    }

                    throw var15;
                }

                input.close();
            }
            finally
            {
                conn.disconnect();
            }

            Log.getLogger().log(Level.INFO, "Patreon Whitelist validating, request content: " + content);
            return statusCode == 200 && Boolean.parseBoolean(content);
        }
        catch (IOException var17)
        {
            var17.printStackTrace();
            return false;
        }
    }
}
