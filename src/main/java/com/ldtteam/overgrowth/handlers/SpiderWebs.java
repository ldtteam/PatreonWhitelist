package com.ldtteam.overgrowth.handlers;

import com.ldtteam.overgrowth.Overgrowth;
import com.ldtteam.overgrowth.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.minecraft.world.level.block.Block.UPDATE_ALL_IMMEDIATE;
import static net.minecraft.world.level.block.Blocks.BROWN_MUSHROOM;

/**
 * Distributes spiderwebs.
 */
public class SpiderWebs extends AbstractTransformationHandler
{
    @Override
    public ForgeConfigSpec.IntValue getMatchingSetting()
    {
        return Overgrowth.config.getServer().spiderwebs;
    }

    @Override
    public boolean transforms(final BlockState state)
    {
        return state.is(BlockTags.PLANKS) || state.is(BlockTags.STONE_BRICKS);
    }
    @Override
    public boolean ready(final long worldTick)
    {
        return getCachedSetting() != 0 && getCachedSetting() % 15 == 0;
    }

    @Override
    public void transformBlock(final BlockPos relativePos, final LevelChunk chunk, final int chunkSection, final BlockState input)
    {
        for (final Direction direction : Direction.values())
        {
            final BlockState relativeState = Utils.getBlockState(chunk, relativePos.relative(direction), chunkSection);
            if (relativeState.isAir())
            {
                for (final Direction otherDir : Direction.values())
                {
                    if (otherDir != direction.getOpposite() && otherDir != Direction.DOWN)
                    {
                        final BlockState otherRelativeState = Utils.getBlockState(chunk, relativePos.relative(direction).relative(otherDir), chunkSection);
                        final LevelChunkSection section = chunk.getSections()[chunkSection];
                        final BlockPos worldPos = Utils.getWorldPos(chunk, section, relativePos.relative(direction).relative(otherDir));
                        if (otherRelativeState.isCollisionShapeFullBlock(chunk.getLevel(), worldPos))
                        {
                            for (int i = 0; i <= 3; i++)
                            {
                                final BlockState belowState = Utils.getBlockState(chunk, relativePos.relative(direction).below(i), chunkSection);
                                if (!belowState.isAir())
                                {
                                    return;
                                }
                            }
                            final BlockPos worldSetPos = Utils.getWorldPos(chunk, section, relativePos.relative(direction));

                            chunk.getLevel().setBlock(worldSetPos, Blocks.COBWEB.defaultBlockState(), UPDATE_ALL_IMMEDIATE);
                            return;
                        }
                    }
                }
            }
        }
    }
}
