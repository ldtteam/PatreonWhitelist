package com.ldtteam.overgrowth.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class Utils
{
    /**
     * Get the worldPos from a relative chunkpos.
     * @param chunk the relative chunk.
     * @param section its section.
     * @param relativePos the relative pos.
     * @return the in world pos.
     */
    public static BlockPos getWorldPos(final LevelChunk chunk, final LevelChunkSection section, final BlockPos relativePos)
    {
        return new BlockPos(chunk.getPos().getMinBlockX() + relativePos.getX(), section.bottomBlockY() + relativePos.getY(), chunk.getPos().getMinBlockZ() + relativePos.getZ());
    }

    /**
     * Checks if the block is loaded for block access
     *
     * @param world world to use
     * @param pos   position to check
     * @return true if block is accessible/loaded
     */
    public static boolean isBlockLoaded(final LevelAccessor world, final BlockPos pos)
    {
        return isChunkLoaded(world, pos.getX() >> 4, pos.getZ() >> 4);
    }

    /**
     * Returns whether a chunk is fully loaded
     *
     * @param world world to check on
     * @param x     chunk position
     * @param z     chunk position
     * @return true if loaded
     */
    public static boolean isChunkLoaded(final LevelAccessor world, final int x, final int z)
    {
        if (world.getChunkSource() instanceof ServerChunkCache)
        {
            final ChunkHolder holder = ((ServerChunkCache) world.getChunkSource()).chunkMap.visibleChunkMap.get(ChunkPos.asLong(x, z));
            if (holder != null)
            {
                return holder.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left().isPresent();
            }

            return false;
        }
        return world.getChunk(x, z, ChunkStatus.FULL, false) != null;
    }

    /**
     * Get the blockstate in a chunk in a section. Fall back to asking world if outside of the chunk we're ticking.
     * @param chunk the chunk we're ticking.
     * @param pos the relative pos in the chunk section to get the blockstate from.
     * @param sectionId the id of the section.
     * @return the blockstate or, if not in a loaded chunk or outside of world height a barrier block.
     */
    public static BlockState getBlockState(final LevelChunk chunk, final BlockPos pos, final int sectionId)
    {
        if (pos.getX() >= 16 || pos.getZ() >= 16 || pos.getX() < 0 || pos.getZ() < 0)
        {
            final BlockPos worldPos = Utils.getWorldPos(chunk, chunk.getSections()[sectionId], pos);
            if (Utils.isBlockLoaded(chunk.getLevel(), worldPos))
            {
                return chunk.getLevel().getBlockState(worldPos);
            }
            else
            {
                return Blocks.BARRIER.defaultBlockState();
            }
        }

        if (pos.getY() < 0)
        {
            if (sectionId > 0)
            {
                final int sectionDif = (int) Math.floor(pos.getY() / 16.0);
                if (sectionId+sectionDif >= 0)
                {
                    return chunk.getSections()[sectionId + sectionDif].getBlockState(pos.getX(), (sectionDif * -1 * 16) + pos.getY(), pos.getZ());
                }
            }

            return Blocks.BARRIER.defaultBlockState();
        }
        else if (pos.getY() >= 16)
        {
            if (sectionId + 1 < chunk.getSections().length)
            {
                final int sectionDif = (int) (pos.getY() / 16.0);
                if (sectionDif + sectionId < chunk.getSections().length)
                {
                    return chunk.getSections()[sectionId + sectionDif].getBlockState(pos.getX(), pos.getY() - (sectionDif * 16), pos.getZ());
                }
            }
            return Blocks.BARRIER.defaultBlockState();
        }
        else
        {
            return chunk.getSections()[sectionId].getBlockState(pos.getX(), pos.getY(), pos.getZ());
        }
    }

    /**
     * Get the blockstate if chunk is loaded.
     * @param level the level to check in.
     * @param worldPos the pos to check it.
     * @return valid blockstate or barrier.
     */
    public static BlockState getBlockState(final Level level, final BlockPos worldPos)
    {
        if (Utils.isBlockLoaded(level, worldPos))
        {
            return  level.getBlockState(worldPos);
        }
        return Blocks.BARRIER.defaultBlockState();
    }
}
