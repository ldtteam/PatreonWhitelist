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

import static net.minecraft.world.level.block.Block.UPDATE_ALL_IMMEDIATE;

/**
 * Cracks stonebrick and other blocks next to lava.
 */
public class CrackedBlocksDegradation extends AbstractTransformationHandler
{
    /**
     * Mapping the transformations.
     */
    private static Map<Block, Block> transformationMapping = new HashMap<>();
    
    static
    {
        transformationMapping.put(Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS);
        transformationMapping.put(Blocks.DEEPSLATE_BRICKS, Blocks.CRACKED_DEEPSLATE_BRICKS);
        transformationMapping.put(Blocks.NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS);
        transformationMapping.put(Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);

        transformationMapping.put(Blocks.CRACKED_STONE_BRICKS, Blocks.COBBLESTONE);
        transformationMapping.put(Blocks.STONE, Blocks.COBBLESTONE);

        transformationMapping.put(Blocks.STONE_SLAB, Blocks.COBBLESTONE_SLAB);
        transformationMapping.put(Blocks.STONE_STAIRS, Blocks.COBBLESTONE_STAIRS);

        transformationMapping.put(Blocks.COBBLESTONE_STAIRS, Blocks.AIR);
        transformationMapping.put(Blocks.COBBLESTONE_SLAB, Blocks.AIR);
        transformationMapping.put(Blocks.COBBLESTONE, Blocks.AIR);

    }

    @Override
    public ForgeConfigSpec.IntValue getMatchingSetting()
    {
        return Overgrowth.config.getServer().lavadegration;
    }

    @Override
    public boolean transforms(final BlockState state)
    {
        return transformationMapping.containsKey(state.getBlock());
    }

    @Override
    public boolean ready(final long worldTick)
    {
        return getCachedSetting() != 0 && getCachedSetting() % 17 == 0;
    }

    @Override
    public void transformBlock(final BlockPos relativePos, final LevelChunk chunk, final int chunkSection, final BlockState input)
    {
        for (final Direction direction : Direction.values())
        {
            final BlockState relativeState = Utils.getBlockState(chunk, relativePos.relative(direction), chunkSection);
            if (relativeState.getBlock() == Blocks.LAVA)
            {
                final LevelChunkSection section = chunk.getSections()[chunkSection];
                final BlockPos worldPos = Utils.getWorldPos(chunk, section, relativePos);

                chunk.getLevel().setBlock(worldPos, transformationMapping.get(input.getBlock()).withPropertiesOf(input), UPDATE_ALL_IMMEDIATE);
                return;
            }
        }
    }
}
