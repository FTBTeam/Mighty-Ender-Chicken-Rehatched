package dev.ftb.mods.mecrh.entity;

import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.registry.ModEntityTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EggBomb extends ThrowableProjectile {
    public int explodeTimer;
    private boolean huntTarget;

    public EggBomb(EntityType<EggBomb> entityEntityType, Level level) {
        super(entityEntityType, level);
    }

    public EggBomb(Level level, LivingEntity shooter) {
        super(ModEntityTypes.EGG_BOMB.get(), shooter, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    public void huntTarget(boolean huntTarget) {
        this.huntTarget = huntTarget;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            if (explodeTimer > 0 && --explodeTimer == 0) {
                explode();
            }

            if (tickCount > 100) {
                explode();
            }

            if (huntTarget && getRandom().nextFloat() < 0.6f && getOwner() instanceof EnderChicken chicken && chicken.getTarget() != null) {
                Vec3 offset = chicken.getTarget().position().subtract(position());
                setDeltaMovement(getDeltaMovement().add(offset.normalize().scale(0.1).add(0, 0.1, 0)));
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        explodeTimer = 10;
        return true;
    }

    @Override
    protected void onHit(HitResult result) {
        if (result instanceof EntityHitResult ehr && (ehr.getEntity() instanceof EnderChicken || ehr.getEntity() instanceof EnderChickenPart)) {
            return;
        }
        if (tickCount > 5 && !level().isClientSide && isAlive()) {
            explode();
        }
    }

    private void explode() {
        discard();

        float radius = ServerConfig.EGG_BOMB_EXPLOSION_POWER.get().floatValue();
        Level.ExplosionInteraction interaction = ServerConfig.EGG_BOMB_DAMAGE_TERRAIN.get() ?
                Level.ExplosionInteraction.MOB :
                Level.ExplosionInteraction.NONE;
        level().explode(getOwner(), getX(), getY(), getZ(), radius, false, interaction);
    }
}
