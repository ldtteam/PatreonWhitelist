package com.ldtteam.overgrowth.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Transformation handler types (e.g. stonebricks to mossy)
 */
public interface ITransformationHandler
{
    /**
     * Static list of handlers.
     */
    List<ITransformationHandler> HANDLERS = new ArrayList<>();

    /**
     * Random seed to use.
     */
    Random random = new Random();

    /**
     * Check if this blockstate can be transformed by this handler.
     * @param state the state to check.
     * @return true if so.
     */
    boolean transforms(final BlockState state);

    /**
     * Check if the transformation handler is ready to do a transformation now.
     * @param worldTick the relative world tick to do a transformation in.
     * @return true if so.
     */
    default boolean ready(final long worldTick)
    {
        return true;
    }

    /**
     * Check if the transformation handler is ready to do a transformation now.
     * @param worldTick the relative world tick to do a transformation in.
     * @return true if so.
     */
    default boolean ready(final long worldTick, final LevelChunk chunk)
    {
        return ready(worldTick);
    }

    /**
     * Transform a given block in a given chunk.
     * @param relativePos the relative position in the chunk section.
     * @param chunk the chunk itself.
     * @param chunkSection the chunk section id.
     * @param input the input state.
     */
    void transformBlock(final BlockPos relativePos, final LevelChunk chunk, final int chunkSection, final BlockState input);

    /**
     * Add a new handler to the list of handlers.
     * @param handler the new handler.
     */
    static void registerHandler(final ITransformationHandler handler)
    {
        HANDLERS.add(handler);
    }

    /**
     * Get the matching setting for this.
     * @return the setting.
     */
    ForgeConfigSpec.IntValue getMatchingSetting();
}
