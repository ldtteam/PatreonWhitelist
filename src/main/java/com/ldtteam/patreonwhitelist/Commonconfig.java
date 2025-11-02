package com.ldtteam.patreonwhitelist;

import com.mojang.authlib.GameProfile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Commonconfig
{
    private ModConfigSpec.ConfigValue<List<? extends String>> loginTimes;
    private ModConfigSpec.ConfigValue<Integer>                offlineAuthValidHours;

    /**
     * Map of UUID to last successfull patreon auth time
     */
    private Map<UUID, NameTime> lastLoginTimes = new HashMap<>();
    private DateTimeFormatter   timeFormatter  = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    public  ModConfigSpec       holder         = null;

    public Commonconfig(ModConfigSpec.Builder builder)
    {
        offlineAuthValidHours = builder.define("Amount of hours authentication stays valid for after last successfull auth", 24 * 7);
        loginTimes = builder.defineList("List of player UUID to last auth time", new ArrayList<>(), () -> "", v -> true);
        builder.build();
    }

    /**
     * Reloads the uuid to time map from config string values
     */
    private void reload()
    {
        Map<UUID, NameTime> temp = new HashMap<>();
        for (final String entry : loginTimes.get())
        {
            if (entry == null)
            {
                continue;
            }

            String[] data = entry.split(";");
            if (data.length < 3)
            {
                continue;
            }

            final UUID id = UUID.fromString(data[0]);
            final String name = data[1];
            final LocalDateTime localDateTime = LocalDateTime.parse(data[2], timeFormatter);

            temp.put(id, new NameTime(name, localDateTime));
        }

        lastLoginTimes = temp;
    }

    @SubscribeEvent
    public void onReload(ModConfigEvent.Reloading event)
    {
        reload();
    }

    @SubscribeEvent
    public void onLoad(ModConfigEvent.Loading event)
    {
        reload();
    }

    @SubscribeEvent
    public void onShutdown(ServerStoppingEvent event)
    {
        holder.save();
    }

    /**
     * Resets the auth date
     *
     * @param profile
     */
    public void updateAuthFor(final @NotNull GameProfile profile)
    {
        lastLoginTimes.put(profile.getId(), new NameTime(profile.getName(), LocalDateTime.now()));

        List<String> stringData = new ArrayList<>();

        for (final Map.Entry<UUID, NameTime> entry : lastLoginTimes.entrySet())
        {
            stringData.add(entry.getKey().toString() + ";" + entry.getValue().name + ";" + timeFormatter.format(entry.getValue().time));
        }

        loginTimes.set(stringData);
        holder.save();
    }

    /**
     * Checks if the id has been authenticated recently
     *
     * @param profile
     * @return
     */
    public boolean checkAuth(final @NotNull GameProfile profile)
    {
        final UUID id = profile.getId();
        final NameTime nameTime = lastLoginTimes.get(id);
        if (nameTime == null)
        {
            return checkOnlineAuth(profile);
        }

        long seconds = Duration.between(LocalDateTime.now(), nameTime.time).getSeconds();
        if (seconds < offlineAuthValidHours.get() * 60L * 60L)
        {
            if (seconds < 60 * 120)
            {
                // Skip auth request when last successful was less than two hours ago
                Log.getLogger().log(Level.INFO, "Offline authenticated, recently logged in");
                return true;
            }
            else
            {
                if (checkOnlineAuth(profile))
                {
                    return true;
                }
            }

            Log.getLogger().log(Level.INFO, "Offline authenticated");
            return true;
        }
        else
        {
            return checkOnlineAuth(profile);
        }
    }

    /**
     * Checks for authentication online and updates the config cache
     *
     * @param profile
     * @return
     */
    private boolean checkOnlineAuth(final GameProfile profile)
    {
        if (this.checkUrl("https://auth.minecolonies.com/api/minecraft/" + profile.getId().toString() + "/whitelist"))
        {
            PatreonWhitelist.config.updateAuthFor(profile);
            Log.getLogger().log(Level.INFO, "Online authenticated");
            return true;
        }

        return false;
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
            conn.setConnectTimeout(5000);
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

    private static record NameTime(
        String name,
        LocalDateTime time) {}
}
