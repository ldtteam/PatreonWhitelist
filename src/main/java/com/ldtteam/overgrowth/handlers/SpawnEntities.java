package com.ldtteam.overgrowth.handlers;

import com.ldtteam.overgrowth.Overgrowth;
import com.ldtteam.overgrowth.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Grows different type of plants on grass.
 */
public class SpawnEntities extends AbstractTransformationHandler
{
    @Override
    public ForgeConfigSpec.IntValue getMatchingSetting()
    {
        return Overgrowth.config.getServer().entityspawn;
    }

    @Override
    public boolean transforms(final BlockState state)
    {
        return state.getBlock() == Blocks.GRASS_BLOCK;
    }

    @Override
    public boolean ready(final long worldTick, final LevelChunk chunk)
    {
        return getCachedSetting() != 0 && getCachedSetting() % 23 == 0;
    }

    @Override
    public void transformBlock(final BlockPos relativePos, final LevelChunk chunk, final int chunkSection, final BlockState input)
    {
        if (!((ServerLevel) chunk.getLevel()).isNaturalSpawningAllowed(chunk.getPos()))
        {
            return;
        }

        final EntitySectionStorage sectionStorage = ((LevelEntityGetterAdapter)((ServerLevel) chunk.getLevel()).entityManager.getEntityGetter()).sectionStorage;
        for (int x = chunk.getPos().x - 3; x <= chunk.getPos().x + 3; x++)
        {
            for (int z = chunk.getPos().z - 3; z <= chunk.getPos().z + 3; z++)
            {
                for (int i = 0; i <= 12; i++)
                {
                    final EntitySection section = sectionStorage.getSection(SectionPos.asLong(x, i, z));
                    if (section == null)
                    {
                        continue;
                    }

                    if (!section.storage.byClass.isEmpty())
                    {
                        return;
                    }
                }
            }
        }

        final EntityType type;
        final int rand = chunk.getLevel().random.nextInt(4);
        switch (rand)
        {
            case 0:
                type = EntityType.COW;
                break;
            case 1:
                type = EntityType.SHEEP;
                break;
            case 2:
                type = EntityType.CHICKEN;
                break;
            default:
                type = EntityType.PIG;
                break;
        }

        final LevelChunkSection section = chunk.getSections()[chunkSection];
        final BlockPos worldPos = Utils.getWorldPos(chunk, section, relativePos.above());

        for (int i = 0; i < 2; i++)
        {
            final Entity entity = type.create(chunk.getLevel());
            entity.setPos(worldPos.getX() + 0.5, worldPos.getY(), worldPos.getZ() + 0.5);
            chunk.getLevel().addFreshEntity(entity);
        }
    }
}
