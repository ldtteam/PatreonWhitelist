package com.ldtteam.overgrowth.configuration;

import com.ldtteam.overgrowth.Overgrowth;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec.*;

import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractConfiguration
{
    private final static String INT_DEFAULT_KEY     = "[Default: %d, min: %d, max: %d]";

    protected void createCategory(final Builder builder, final String key)
    {
        builder.comment(key + " category").push(key);
    }

    protected void swapToCategory(final Builder builder, final String key)
    {
        finishCategory(builder);
        createCategory(builder, key);
    }

    protected void finishCategory(final Builder builder)
    {
        builder.pop();
    }

    private static Builder buildBase(final Builder builder, final String comment, final String name, final String defaultDesc)
    {
        return builder.comment(name, comment + " " + defaultDesc);
    }

    protected static IntValue defineInteger(final Builder builder, final String key, final String comment, final String name, final int defaultValue, final int min, final int max)
    {
        return buildBase(builder, comment, name, String.format(INT_DEFAULT_KEY, defaultValue, min, max)).defineInRange(key, defaultValue, min, max);
    }
}
