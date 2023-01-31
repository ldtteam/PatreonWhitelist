package com.ldtteam.overgrowth.handlers;

import com.ldtteam.overgrowth.Overgrowth;
import com.ldtteam.overgrowth.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Grows different type of underwater plants.
 */
public class GrowWaterPlants extends AbstractTransformationHandler
{
    @Override
    public ForgeConfigSpec.IntValue getMatchingSetting()
    {
        return Overgrowth.config.getServer().underwaterplants;
    }

    @Override
    public boolean transforms(final BlockState state)
    {
        return state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.SEAGRASS;
    }

    @Override
    public boolean ready(final long worldTick)
    {
        return getCachedSetting() != 0 && getCachedSetting() % 12 == 0;
    }

    @Override
    public void transformBlock(final BlockPos relativePos, final LevelChunk chunk, final int chunkSection, final BlockState input)
    {
        final BlockState state = Utils.getBlockState(chunk, relativePos, chunkSection);
        final LevelChunkSection section = chunk.getSections()[chunkSection];
        final BlockPos worldPos = Utils.getWorldPos(chunk, section, relativePos);
        final int randomNum = random.nextInt(100);

        if (state.getBlock() == Blocks.SEAGRASS)
        {
            ((SeagrassBlock) state.getBlock()).performBonemeal((ServerLevel) chunk.getLevel(), chunk.getLevel().getRandom(), worldPos, state);
        }
        else
        {
            final BlockState belowState = Utils.getBlockState(chunk, relativePos.below(), chunkSection);

            BlockState blockstate = Blocks.SEAGRASS.defaultBlockState();

            if (!belowState.isCollisionShapeFullBlock(chunk.getLevel(), worldPos.below()))
            {
                if (randomNum < 5)
                {
                    Holder<Biome> holder = chunk.getLevel().getBiome(worldPos);
                    if (holder.is(BiomeTags.PRODUCES_CORALS_FROM_BONEMEAL))
                    {
                        for (final Direction direction : Direction.Plane.HORIZONTAL)
                        {
                            final BlockState relativeState = Utils.getBlockState(chunk, relativePos.relative(direction), chunkSection);
                            if (relativeState.isCollisionShapeFullBlock(chunk.getLevel(), worldPos.relative(direction)))
                            {
                                blockstate = Registry.BLOCK.getTag(BlockTags.WALL_CORALS)
                                               .flatMap((set) -> set.getRandomElement(chunk.getLevel().random))
                                               .map((blockHolder) -> blockHolder.value().defaultBlockState()).orElse(blockstate);
                                if (blockstate.hasProperty(BaseCoralWallFanBlock.FACING))
                                {
                                    chunk.getLevel().setBlock(worldPos.below(), blockstate.setValue(BaseCoralWallFanBlock.FACING, direction.getOpposite()), 3);
                                }
                                return;
                            }
                        }
                    }
                }
                return;
            }

            Holder<Biome> holder = chunk.getLevel().getBiome(worldPos);
            if (holder.is(BiomeTags.PRODUCES_CORALS_FROM_BONEMEAL) || randomNum < 5)
            {
                blockstate = Registry.BLOCK.getTag(BlockTags.UNDERWATER_BONEMEALS)
                                   .flatMap((set) -> set.getRandomElement(chunk.getLevel().random))
                                   .map((blockHolder) -> blockHolder.value().defaultBlockState()).orElse(blockstate);
            }

            if (blockstate.is(BlockTags.WALL_CORALS, (p_204093_) -> p_204093_.hasProperty(BaseCoralWallFanBlock.FACING)))
            {
                for (int k = 0; !blockstate.canSurvive(chunk.getLevel(), worldPos) && k < 4; ++k)
                {
                    blockstate = blockstate.setValue(BaseCoralWallFanBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(chunk.getLevel().getRandom()));
                }
            }

            if (blockstate.canSurvive(chunk.getLevel(), worldPos))
            {
                if (state.is(Blocks.WATER) && chunk.getLevel().getFluidState(worldPos).getAmount() == 8)
                {
                    chunk.getLevel().setBlock(worldPos, blockstate, 3);
                }
            }
        }
    }
}
