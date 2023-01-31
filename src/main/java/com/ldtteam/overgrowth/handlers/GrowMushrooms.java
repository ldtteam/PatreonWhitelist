package com.ldtteam.overgrowth.handlers;

import com.ldtteam.overgrowth.Overgrowth;
import com.ldtteam.overgrowth.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static net.minecraft.world.level.block.Block.UPDATE_ALL_IMMEDIATE;
import static net.minecraft.world.level.block.Blocks.*;

/**
 * Grow mushrooms next to humiditz
 */
public class GrowMushrooms extends AbstractTransformationHandler
{
    /**
     * Mapping the transformations.
     */
    private static Set<Block> transformationMapping = new HashSet<>();

    static
    {
        transformationMapping.add(Blocks.MOSSY_STONE_BRICKS);
        transformationMapping.add(Blocks.MOSSY_STONE_BRICK_STAIRS);
        transformationMapping.add(Blocks.MOSSY_STONE_BRICK_SLAB);
        transformationMapping.add(Blocks.MOSSY_STONE_BRICK_WALL);
        transformationMapping.add(Blocks.MOSSY_COBBLESTONE);
        transformationMapping.add(Blocks.MOSSY_COBBLESTONE_STAIRS);
        transformationMapping.add(Blocks.MOSSY_COBBLESTONE_SLAB);
        transformationMapping.add(Blocks.MOSSY_COBBLESTONE_WALL);
        transformationMapping.add(Blocks.WATER);
        transformationMapping.add(Blocks.WATER_CAULDRON);
    }

    @Override
    public ForgeConfigSpec.IntValue getMatchingSetting()
    {
        return Overgrowth.config.getServer().mushrooms;
    }

    @Override
    public boolean transforms(final BlockState state)
    {
        return transformationMapping.contains(state.getBlock());
    }

    @Override
    public boolean ready(final long worldTick)
    {
        return getCachedSetting() != 0 && getCachedSetting() % 23 == 0;
    }

    @Override
    public void transformBlock(final BlockPos relativePos, final LevelChunk chunk, final int chunkSection, final BlockState input)
    {
        for (final Direction direction : Direction.values())
        {
            final BlockState relativeState = Utils.getBlockState(chunk, relativePos.relative(direction), chunkSection);

            final LevelChunkSection section = chunk.getSections()[chunkSection];
            final BlockPos worldPos = Utils.getWorldPos(chunk, section, relativePos.relative(direction).above());
            if (relativeState.isCollisionShapeFullBlock(chunk.getLevel(), worldPos.relative(direction))
                  && Utils.getBlockState(chunk, relativePos.relative(direction).above(), chunkSection).isAir()
                  && BROWN_MUSHROOM.canSurvive(BROWN_MUSHROOM.defaultBlockState(), chunk.getLevel(), worldPos))
            {

                if (Math.random() > 0.5)
                {
                    chunk.getLevel().setBlock(worldPos, BROWN_MUSHROOM.defaultBlockState(), UPDATE_ALL_IMMEDIATE);
                }
                else
                {
                    chunk.getLevel().setBlock(worldPos, RED_MUSHROOM.defaultBlockState(), UPDATE_ALL_IMMEDIATE);
                }

                return;
            }
        }
    }
}
