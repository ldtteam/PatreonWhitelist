package com.ldtteam.overgrowth.handlers;

import com.ldtteam.overgrowth.Overgrowth;
import com.ldtteam.overgrowth.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * Grows different type of plants on grass.
 */
public class GrowPlantsOnGrass extends AbstractTransformationHandler
{
    @Override
    public ForgeConfigSpec.IntValue getMatchingSetting()
    {
        return Overgrowth.config.getServer().growplants;
    }

    @Override
    public boolean transforms(final BlockState state)
    {
        return state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.PODZOL;
    }

    @Override
    public boolean ready(final long worldTick, final LevelChunk chunk)
    {
        return getCachedSetting() != 0 && chunk.getLevel().isRaining() ? getCachedSetting()/2 % 8 == 0 : getCachedSetting() % 16 == 0;
    }

    @Override
    public void transformBlock(final BlockPos relativePos, final LevelChunk chunk, final int chunkSection, final BlockState input)
    {
        final BlockState upState = Utils.getBlockState(chunk, relativePos.above(), chunkSection);
        if (upState.isAir())
        {
            final int randomNum = random.nextInt(100);
            final LevelChunkSection section = chunk.getSections()[chunkSection];
            final BlockPos worldPos = Utils.getWorldPos(chunk, section, relativePos.above());

            final Holder<PlacedFeature> holder;
            if (randomNum < 95)
            {
                holder = VegetationPlacements.GRASS_BONEMEAL;
            }
            else
            {
                List<ConfiguredFeature<?, ?>> list = chunk.getLevel().getBiome(worldPos).value().getGenerationSettings().getFlowerFeatures();
                if (list.isEmpty())
                {
                    return;
                }
                holder = ((RandomPatchConfiguration) list.get(0).config()).feature();
            }
            holder.value().place((WorldGenLevel) chunk.getLevel(), ((ServerLevel) chunk.getLevel()).getChunkSource().getGenerator(), chunk.getLevel().random, worldPos);
        }
        else if (upState.getBlock() instanceof TallGrassBlock && upState.getBlock() instanceof BonemealableBlock)
        {
            final LevelChunkSection section = chunk.getSections()[chunkSection];
            final BlockPos worldPos = Utils.getWorldPos(chunk, section, relativePos.above());

            ((TallGrassBlock) upState.getBlock()).performBonemeal((ServerLevel) chunk.getLevel(), chunk.getLevel().getRandom(), worldPos, upState);
        }
    }
}
