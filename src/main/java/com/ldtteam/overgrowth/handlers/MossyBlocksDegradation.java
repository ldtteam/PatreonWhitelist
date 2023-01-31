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

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.world.level.block.Block.*;

/**
 * Stonebrick and cobble types to mossy versions.
 */
public class MossyBlocksDegradation extends AbstractTransformationHandler
{
    /**
     * Mapping the transformations.
     */
    private static Map<Block, Block> transformationMapping = new HashMap<>();
    
    static
    {
        transformationMapping.put(Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS);
        transformationMapping.put(Blocks.STONE_BRICK_STAIRS, Blocks.MOSSY_STONE_BRICK_STAIRS);
        transformationMapping.put(Blocks.STONE_BRICK_SLAB, Blocks.MOSSY_STONE_BRICK_SLAB);
        transformationMapping.put(Blocks.STONE_BRICK_WALL, Blocks.MOSSY_STONE_BRICK_WALL);
        transformationMapping.put(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE);
        transformationMapping.put(Blocks.COBBLESTONE_STAIRS, Blocks.MOSSY_COBBLESTONE_STAIRS);
        transformationMapping.put(Blocks.COBBLESTONE_SLAB, Blocks.MOSSY_COBBLESTONE_SLAB);
        transformationMapping.put(Blocks.COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE_WALL);
    }

    @Override
    public ForgeConfigSpec.IntValue getMatchingSetting()
    {
        return Overgrowth.config.getServer().mossyblocks;
    }
    
    @Override
    public boolean transforms(final BlockState state)
    {
        return transformationMapping.containsKey(state.getBlock());
    }

    @Override
    public boolean ready(final long worldTick)
    {
        return getCachedSetting() != 0 && getCachedSetting() % 14 == 0;
    }

    @Override
    public void transformBlock(final BlockPos relativePos, final LevelChunk chunk, final int chunkSection, final BlockState input)
    {
        for (final Direction direction : Direction.values())
        {
            final BlockState relativeState = Utils.getBlockState(chunk, relativePos.relative(direction), chunkSection);
            if (relativeState.getBlock() == Blocks.WATER)
            {
                final LevelChunkSection section = chunk.getSections()[chunkSection];
                final BlockPos worldPos = Utils.getWorldPos(chunk, section, relativePos);


                chunk.getLevel().setBlock(worldPos, transformationMapping.get(input.getBlock()).withPropertiesOf(input), UPDATE_ALL_IMMEDIATE);
                return;
            }
        }
    }
}
