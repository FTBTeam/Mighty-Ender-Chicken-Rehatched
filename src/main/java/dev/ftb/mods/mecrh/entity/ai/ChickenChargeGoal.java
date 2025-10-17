package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.HashSet;

public class ChickenChargeGoal extends ChickenGoal {
    private static final double KNOCKBACK_AMP = 7.0;

    private int chargeTime;
    private Vec3 chargeStart;
    private float chargeYaw;
    private float chargePitch;

    public ChickenChargeGoal(EnderChicken chicken) {
        super(chicken);

        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return super.canUse() && chicken.isChargeReady();
    }

    @Override
    public boolean canContinueToUse() {
        return chicken.isAlive()
                && chargeTime < ServerConfig.MAX_CHARGE_TIME.get()
                && chargeStart.distanceToSqr(chicken.position()) < ServerConfig.MAX_CHARGE_DIST.get() * ServerConfig.MAX_CHARGE_DIST.get()
                && chicken.isWithinRestriction();
    }

    @Override
    public void start() {
        chicken.useAbility();
        chicken.setCharging(true);
        chargeStart = chicken.position();
    }

    @Override
    public void stop() {
        chicken.setCharging(false);
        chicken.endAbility();
        chargeStart = null;
        chargeTime = 0;
        chargeYaw = chargePitch = 0.0F;
    }

    @Override
    public void tick() {
        Entity target = chicken.getTarget();
        if (chargeTime < ServerConfig.CHARGE_WARMUP_TIME.get()) {
            if (chargeTime == 0) {
                chicken.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.2F, 0.8F + chicken.getRandom().nextFloat() * 0.4F);
            }
            chicken.setYRot(chicken.yBodyRot);

            if (target != null) {
                chicken.getLookControl().setLookAt(target);
            }
        } else {
            if (chargeTime == ServerConfig.CHARGE_WARMUP_TIME.get()) {
                chicken.playSound(ModSounds.CHARGE_START.get(), 1.2F, 0.8F);
                chargeStart = chicken.position();
                chargeYaw = chicken.yBodyRot;
                chargePitch = chicken.getXRot();
            }
            if (chargeTime < 30 && target != null) {
                chicken.getLookControl().setLookAt(target);
            }
            double x = -Mth.sin((chargeStart != null ? chargeYaw : chicken.yBodyRot + 180.0F) * Mth.DEG_TO_RAD);
            double y = -Mth.sin((chargeStart != null ? chargePitch : chicken.getXRot() + 180.0F) * Mth.DEG_TO_RAD);
            double z = Mth.cos((chargeStart != null ? chargeYaw : chicken.yBodyRot + 180.0F) * Mth.DEG_TO_RAD);
            double len = (float) Math.sqrt(x * x + y * y + z * z) / ServerConfig.CHARGE_SPEED.get();
            chicken.setDeltaMovement(x / len, y / len, z / len);
            chicken.setSprinting(true);

            // chicken smashes through terrain
            destroyBlocksInAABB(chicken, chicken.partBody.getBlockDestructionAABB());
            destroyBlocksInAABB(chicken, chicken.partHead.getBlockDestructionAABB());
            destroyBlocksInAABB(chicken, chicken.partBill.getBlockDestructionAABB());

            // chicken knocks nearby entities away
            if (chargeTime % 2 == 0) {
                chargeThroughEntities();
            }
        }

        ++chargeTime;
    }

    private void chargeThroughEntities() {
        Level level = chicken.level();
        Vec3 diff = chicken.getDeltaMovement();

        HashSet<Entity> collided = new HashSet<>();
        collided.addAll(level.getEntities(chicken, chicken.partFootL.getBoundingBox().expandTowards(diff), EnderChicken.PREDICATE_TARGETS));
        collided.addAll(level.getEntities(chicken, chicken.partFootR.getBoundingBox().expandTowards(diff), EnderChicken.PREDICATE_TARGETS));
        collided.addAll(level.getEntities(chicken, chicken.partLegL.getBoundingBox().expandTowards(diff), EnderChicken.PREDICATE_TARGETS));
        collided.addAll(level.getEntities(chicken, chicken.partLegR.getBoundingBox().expandTowards(diff), EnderChicken.PREDICATE_TARGETS));
        collided.addAll(level.getEntities(chicken, chicken.partBody.getBoundingBox().expandTowards(diff), EnderChicken.PREDICATE_TARGETS));

        collided.forEach(ent -> {
            double xOff = ent.getX() - chicken.getX();
            double yOff = ent.getY() + (ent.getBoundingBox().maxY - ent.getBoundingBox().minY) / 2.0 - chicken.getY();
            double zOff = ent.getZ() - chicken.getZ();
            double distSq = xOff * xOff + yOff * yOff + zOff * zOff + chicken.getRandom().nextGaussian() * 1.0E-5;
            if (distSq < chicken.getBbWidth() * chicken.getBbWidth()) {
                Vec3 knockback = new Vec3(
                        KNOCKBACK_AMP / distSq * (xOff * xOff / distSq) * (xOff > 0.0 ? 1.0 : -1.0),
                        KNOCKBACK_AMP * 1.5 / distSq * (yOff * yOff / distSq) * (yOff > 0.0 ? 1.0 : -1.0) + 0.07,
                        KNOCKBACK_AMP / distSq * (zOff * zOff / distSq) * (zOff > 0.0 ? 1.0 : -1.0)
                );
                ent.setDeltaMovement(ent.getDeltaMovement().add(knockback).add(chicken.getDeltaMovement()));
                if (ent instanceof ServerPlayer sp) {
                    sp.connection.send(new ClientboundSetEntityMotionPacket(ent.getId(), ent.getDeltaMovement()));
                }
            }
            ent.hurt(level.damageSources().mobAttack(chicken), ServerConfig.CHARGE_DAMAGE.get().floatValue());
        });
    }

    static void destroyBlocksInAABB(Entity ent, AABB aabb) {
        BlockPos.betweenClosedStream(aabb).forEach(pos -> {
            BlockState state = ent.level().getBlockState(pos);
            if (!state.isAir() && !state.is(BlockTags.FIRE)
                    && state.getBlock().canEntityDestroy(state, ent.level(), pos, ent)
                    && state.getDestroySpeed(ent.level(), pos) >= 0)
            {
                ent.level().destroyBlock(pos, ServerConfig.CHARGE_DROPS_BLOCKS.get());
            }
        });
    }
}
