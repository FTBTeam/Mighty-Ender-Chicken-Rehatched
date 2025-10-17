package dev.ftb.mods.mecrh.entity.ai;

import com.google.common.base.Predicate;
import dev.ftb.mods.mecrh.entity.EggBomb;
import dev.ftb.mods.mecrh.entity.EnderChicken;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ClearSurroundingEntitiesGoal extends ChickenGoal {
    public static final Predicate<Entity> SPECIAL = e ->
            (!(e instanceof Player) || !e.isSpectator())
                    && (e instanceof LivingEntity || e instanceof Projectile && (!(e instanceof Arrow) || !((Arrow) e).onGround()))
                    && !(e instanceof EggBomb)
                    && !(e instanceof EnderChicken);

    private int flapTime;

    public ClearSurroundingEntitiesGoal(EnderChicken chicken) {
        super(chicken);
    }

    @Override
    public boolean canUse() {
        if (chicken.onGround() && chicken.canUseAbility()) {
            for (Player player : chicken.level().players()) {
                if (!player.getAbilities().invulnerable && player.isAlive()
                        && !player.isSpectator()
                        && player.getMainHandItem().getItem() instanceof BowItem && player.isUsingItem())
                {
                    return true;
                }
            }

            if (chicken.canUseAbility()) {
                AABB aabb = chicken.getBoundingBox()
                        .move(0.0, -chicken.getBbHeight() * 0.15F, 0.0)
                        .inflate(chicken.getBbWidth() * 0.45F, 0.0, chicken.getBbWidth() * 0.45F);
                List<Entity> entities = chicken.level().getEntities(chicken, aabb, SPECIAL);

                return entities.stream().anyMatch(e -> chicken.getSensing().hasLineOfSight(e));
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return flapTime > 0 && chicken.onGround() && chicken.isAlive();
    }

    @Override
    public void start() {
        this.chicken.useAbility();
        this.chicken.setFlapping(true);
        this.flapTime = 60;
    }

    @Override
    public void stop() {
        this.chicken.endAbility();
        this.chicken.setFlapping(false);
        this.flapTime = 0;
    }

    @Override
    public void tick() {
        --flapTime;
    }
}
