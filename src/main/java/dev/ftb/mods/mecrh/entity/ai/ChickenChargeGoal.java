package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.registry.ModSounds;
import dev.ftb.mods.mecrh.util.ChickenUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.function.Consumer;

public class ChickenChargeGoal extends ChickenGoal {
    private static final double KNOCKBACK_AMP = 7.0;

    private final boolean peckOfDoom;
    private final int totalDuration;
    private final int warmupTime;
    private Vec3 chargeStart;

    private int chargeTime;
    private Vec3 chargePos;
    private boolean dropPhase;

    public ChickenChargeGoal(EnderChicken chicken, boolean peckOfDoom) {
        super(chicken);

        this.peckOfDoom = peckOfDoom;

        totalDuration = peckOfDoom ?
                ServerConfig.MAX_PECK_CHARGE_TIME.get() + ServerConfig.PECK_DROP_TIME.get() :
                ServerConfig.MAX_CHARGE_TIME.get();
        warmupTime = peckOfDoom ? ServerConfig.PECK_WARMUP_TIME.get() : ServerConfig.CHARGE_WARMUP_TIME.get();

        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return super.canUse() && chicken.isChargeReady(peckOfDoom) && !chicken.isCharging();
    }

    @Override
    public boolean canContinueToUse() {
        return chicken.isAlive()
                && chargeTime < totalDuration
                && chargeStart.distanceToSqr(chicken.position()) < ServerConfig.MAX_CHARGE_DIST.get() * ServerConfig.MAX_CHARGE_DIST.get()
                && (peckOfDoom || chargePos == null || chargePos.distanceToSqr(chicken.position()) > 2.0)
                && chicken.isWithinRestriction();
    }

    @Override
    public void start() {
        chargeTime = 0;
        dropPhase = false;
        chargeStart = chicken.position();

        chicken.useAbility();
        chicken.setCharging(true, peckOfDoom);
    }

    @Override
    public void stop() {
        chicken.setSprinting(false);
        chicken.setCharging(false, peckOfDoom);
        chicken.endAbility();
    }

    @Override
    public void tick() {
        LivingEntity target = chicken.getTarget();

        if (chargeTime < warmupTime) {
            if (chargeTime == 0) {
                float p = peckOfDoom ? 1.2F : 0.8F;
                chicken.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.2F, p + chicken.getRandom().nextFloat() * 0.4F);
            }
            chicken.setYRot(chicken.yBodyRot);
            if (target != null) {
                chicken.getLookControl().setLookAt(target);
            }
        } else {
            chicken.setSprinting(true);
            if (chargeTime == warmupTime) {
                chargePos = target == null ?
                        chicken.position().add(chicken.getLookAngle().normalize().scale(6f)) :
                        target.position(); //calcChargePos(chicken, target);

                if (peckOfDoom) {
                    // fly to above target's head
                    chargePos = chargePos.add(0.0, ServerConfig.DIST_ABOVE_TARGET.get(), 0.0);
                } else {
                    chicken.playSound(ModSounds.CHARGE_START.get(), 1.2F, 0.8F);
                }
                chargeStart = chicken.position();
            }
            chicken.getLookControl().setLookAt(target == null ? chargePos : target.position());

            if (peckOfDoom && (chargeTime > ServerConfig.MAX_PECK_CHARGE_TIME.get() || chicken.position().distanceToSqr(chargePos) < 1.0)) {
                dropPhase = true;
            }

            if (dropPhase) {
                if (!chicken.onGround()) {
                    chicken.setDeltaMovement(0.0, -1.0, 0.0);
                }
                if (chicken.onGround() || chargeTime >= totalDuration - 1) {
                    doPeckOfDoomShockwave();
                    chargeTime = totalDuration;
                }
            } else {
                // chicken smashes through terrain
                Vec3 vec = chargePos.subtract(chicken.position()).normalize();
                ChickenUtils.destroyBlocksInAABB(chicken, chicken.partBody.getBlockDestructionAABB().expandTowards(vec));
                ChickenUtils.destroyBlocksInAABB(chicken, chicken.partHead.getBlockDestructionAABB().expandTowards(vec));
                ChickenUtils.destroyBlocksInAABB(chicken, chicken.partBill.getBlockDestructionAABB().expandTowards(vec));
                ChickenUtils.destroyBlocksInAABB(chicken, chicken.partFootL.getBlockDestructionAABB().expandTowards(vec).move(0, 1, 0));
                ChickenUtils.destroyBlocksInAABB(chicken, chicken.partFootR.getBlockDestructionAABB().expandTowards(vec).move(0, 1, 0));

                if (peckOfDoom) {
                    Vec3 offset = chargePos.subtract(chicken.position());
                    double len = offset.length() / ServerConfig.CHARGE_SPEED.get();
                    chicken.setDeltaMovement(offset.x / len, offset.y / len, offset.z / len);
                } else {
                    chicken.getNavigation().moveTo(chargePos.x, chargePos.y, chargePos.z, 2.0);
                }
            }

            // chicken knocks nearby entities away
            if (chargeTime % 2 == 0) {
                knockbackEntities(e -> {});
            }
        }

        ++chargeTime;
    }

    private static Vec3 calcChargePos(PathfinderMob mob, LivingEntity target) {
        Vec3 offset = target.position().subtract(mob.position());
        // should send the charger 2.5 blocks past the target's position
        return mob.position().add(offset.add(offset.normalize().scale(2.5)));
    }

    private void doPeckOfDoomShockwave() {
        knockbackEntities(e -> {
            if (e instanceof LivingEntity l) {
                l.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 2));
                l.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 2));
            }
        });
        Vec3 pos = chicken.position();
        chicken.level().explode(chicken, pos.x, pos.y, pos.z, 3f, Level.ExplosionInteraction.NONE);
        if (chicken.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ASH, pos.x, pos.y, pos.z, 25, 2, 1, 2, 0.5);
        }
    }

    private void knockbackEntities(Consumer<Entity> consumer) {
        Level level = chicken.level();
        Vec3 diff = chicken.getDeltaMovement();

        HashSet<Entity> collided = new HashSet<>();
        collided.addAll(level.getEntities(chicken, chicken.partFootL.getBoundingBox().expandTowards(diff), EnderChicken.PREDICATE_TARGETS));
        collided.addAll(level.getEntities(chicken, chicken.partFootR.getBoundingBox().expandTowards(diff), EnderChicken.PREDICATE_TARGETS));
        collided.addAll(level.getEntities(chicken, chicken.partLegL.getBoundingBox().expandTowards(diff), EnderChicken.PREDICATE_TARGETS));
        collided.addAll(level.getEntities(chicken, chicken.partLegR.getBoundingBox().expandTowards(diff), EnderChicken.PREDICATE_TARGETS));
        collided.addAll(level.getEntities(chicken, chicken.partBody.getBoundingBox().expandTowards(diff), EnderChicken.PREDICATE_TARGETS));

        collided.forEach(e -> {
            consumer.accept(e);
            double xOff = e.getX() - chicken.getX();
            double yOff = e.getY() + (e.getBoundingBox().maxY - e.getBoundingBox().minY) / 2.0 - chicken.getY();
            double zOff = e.getZ() - chicken.getZ();
            double distSq = xOff * xOff + yOff * yOff + zOff * zOff + chicken.getRandom().nextGaussian() * 1.0E-5;
            if (distSq < chicken.getBbHeight() * chicken.getBbHeight()) {
                Vec3 knockback = new Vec3(
                        KNOCKBACK_AMP / distSq * (xOff * xOff / distSq) * (xOff > 0.0 ? 1.0 : -1.0),
                        KNOCKBACK_AMP * 1.5 / distSq * (yOff * yOff / distSq) * (yOff > 0.0 ? 1.0 : -1.0) + 0.07,
                        KNOCKBACK_AMP / distSq * (zOff * zOff / distSq) * (zOff > 0.0 ? 1.0 : -1.0)
                );
                e.setDeltaMovement(e.getDeltaMovement().add(knockback).add(chicken.getDeltaMovement()));
                if (e instanceof ServerPlayer sp) {
                    sp.connection.send(new ClientboundSetEntityMotionPacket(e.getId(), e.getDeltaMovement()));
                }
                e.hasImpulse = true;
            }
            e.hurt(level.damageSources().mobAttack(chicken), ServerConfig.CHARGE_DAMAGE.get().floatValue());
        });
    }
}
