package dev.ftb.mods.mecrh.util;

import dev.ftb.mods.mecrh.MECRHTags;
import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.event.EnderChickenEvent;
import dev.ftb.mods.mecrh.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public class ChickenUtils {
    public static boolean hasChickenBoss(Entity entity) {
        return entity.hasData(ModAttachments.CHICKEN_ID);
    }

    public static boolean hasChickenBoss(Entity entity, EnderChicken chicken) {
        return hasChickenBoss(entity) && entity.getData(ModAttachments.CHICKEN_ID) == chicken.getId();
    }

    @Nullable
    private static EnderChicken getChickenBoss(Entity entity) {
        if (!hasChickenBoss(entity)) return null;

        int id = entity.getData(ModAttachments.CHICKEN_ID);
        return entity.level().getEntity(id) instanceof EnderChicken chicken ? chicken : null;
    }

    public static void postChickenEvent(EnderChicken chicken, EnderChickenEvent.Phase phase) {
        NeoForge.EVENT_BUS.post(new EnderChickenEvent(chicken, phase));
    }

    public static void destroyBlocksInAABB(Entity ent, AABB aabb) {
        BlockPos.betweenClosedStream(aabb).forEach(pos -> destroyBlock(ent, pos));
    }

    public static void destroyBlock(Entity ent, BlockPos pos) {
        BlockState state = ent.level().getBlockState(pos);
        if (!state.isAir() && !state.is(BlockTags.FIRE) && !state.is(MECRHTags.Blocks.CHICKEN_UNBREAKABLE)
                && state.getBlock().canEntityDestroy(state, ent.level(), pos, ent)
                && state.getDestroySpeed(ent.level(), pos) >= 0)
        {
            ent.level().destroyBlock(pos, ServerConfig.DROP_BROKEN_BLOCKS.get());
        }
    }

    public static boolean isEnderChickenFriend(Entity e) {
        return e != null && (ChickenUtils.hasChickenBoss(e) || e.getType().is(MECRHTags.Entities.CHICKEN_FRIENDS));
    }

}
