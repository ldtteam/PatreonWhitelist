package com.ldtteam.overgrowth.handlers;

import com.ldtteam.overgrowth.Overgrowth;
import com.ldtteam.overgrowth.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashSet;
import java.util.Set;

import static net.minecraft.world.level.block.Block.UPDATE_ALL_IMMEDIATE;

/**
 * Declines coast.
 */
public class CoastDecline extends AbstractTransformationHandler
{
    /**
     * Mapping the transformations.
     */
    private static Set<Block> transformationMapping = new HashSet<>();
    
    static
    {
        transformationMapping.add(Blocks.SAND);
        transformationMapping.add(Blocks.GRAVEL);
        transformationMapping.add(Blocks.DIRT);
    }

    @Override
    public ForgeConfigSpec.IntValue getMatchingSetting()
    {
        return Overgrowth.config.getServer().coastdecline;
    }
    
    @Override
    public boolean transforms(final BlockState state)
    {
        return transformationMapping.contains(state.getBlock());
    }

    @Override
    public boolean ready(final long worldTick)
    {
        return getCachedSetting() != 0 && getCachedSetting() % 18 == 0;
    }

    @Override
    public void transformBlock(final BlockPos relativePos, final LevelChunk chunk, final int chunkSection, final BlockState input)
    {
        for (final Direction direction : Direction.Plane.HORIZONTAL)
        {
            BlockState relativeState = Utils.getBlockState(chunk, relativePos.relative(direction), chunkSection);
            if (relativeState.getBlock() == Blocks.WATER)
            {
                for (int i = 1; i <= 7; i++)
                {
                    relativeState = Utils.getBlockState(chunk, relativePos.relative(direction, i), chunkSection);
                    if (relativeState.getBlock() != Blocks.WATER)
                    {
                        break;
                    }

                    if (Utils.getBlockState(chunk, relativePos.relative(direction, i).relative(Direction.values()[chunk.getLevel().random.nextInt(Direction.values().length)]), chunkSection).getBlock() != Blocks.WATER)
                    {
                        break;
                    }

                    if (i == 7)
                    {
                        final LevelChunkSection section = chunk.getSections()[chunkSection];
                        final BlockPos worldPos = Utils.getWorldPos(chunk, section, relativePos);

                        chunk.getLevel().setBlock(worldPos, Blocks.WATER.defaultBlockState(), UPDATE_ALL_IMMEDIATE);
                        return;
                    }
                }
            }
        }
    }
}
