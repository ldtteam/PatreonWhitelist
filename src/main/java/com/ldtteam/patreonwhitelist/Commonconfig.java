package com.ldtteam.patreonwhitelist;

import com.mojang.authlib.GameProfile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Commonconfig
{
    private ModConfigSpec.ConfigValue<List<? extends String>> loginTimes;
    private ModConfigSpec.ConfigValue<Integer>                offlineAuthValidHours;

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
    }

    /**
     * Checks if the id has been authenticated recently
     *
     * @param id
     * @return
     */
    public boolean hasOfflineAuth(final UUID id)
    {
        final NameTime nameTime = lastLoginTimes.get(id);
        if (nameTime == null)
        {
            return false;
        }

        return Duration.between(LocalDateTime.now(), nameTime.time).abs().getSeconds() < offlineAuthValidHours.get() * 60L * 60L;
    }

    private static record NameTime(
        String name,
        LocalDateTime time) {}
}
