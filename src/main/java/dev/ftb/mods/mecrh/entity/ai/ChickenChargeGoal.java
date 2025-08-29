package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChickenEntity;
import dev.ftb.mods.mecrh.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.HashSet;

public class ChickenChargeGoal extends ChickenSkillGoal {
    public int chargeTime;
    public BlockPos chargeStart;
    public float chargeYaw;
    public double chargeLastDist;
    public double lastX;
    public double lastZ;

    public ChickenChargeGoal(EnderChickenEntity chicken) {
        super(chicken, ServerConfig.CHARGE_CHANCE.get());

        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return chicken.canUseAbility()
                && (chicken.getEggState() >= 0 || chicken.getTarget() != null)
                && chicken.getRandom().nextFloat() < chanceToUse;
    }

    @Override
    public void start() {
        chicken.useAbility();
        this.chargeLastDist = -1.0;
        chicken.setCharging(true);
    }

    @Override
    public void stop() {
        chicken.endAbility();
        this.chargeStart = null;
        this.chargeTime = 0;
        this.chargeYaw = 0.0F;
    }

    @Override
    public void tick() {
        float scale = chicken.getScale();
        if (this.chargeTime < ServerConfig.CHARGE_WARN_TIME.get()) {
            if (this.chargeTime == 0) {
                chicken.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.0F + 0.2F * chicken.getScale(), 0.8F + chicken.getRandom().nextFloat() * 0.4F);
            }

            chicken.setYRot(chicken.yBodyRot);
            if (!chicken.isFiring()) {
                Entity ent = chicken.getTarget();
                if (ent != null) {
                    chicken.getLookControl().setLookAt(ent.getX(), ent.getY() + (double) ent.getEyeHeight(), ent.getZ(), (float)chicken.getMaxHeadXRot(), (float)chicken.getMaxHeadYRot());
                }
            }
        } else {
            double speed = 0.41 + 0.044 * (double)scale;
            if (chicken.isChaosChicken()) {
                speed *= 1.1;
            }

            double x = -Mth.sin((this.chargeStart != null ? this.chargeYaw : chicken.yBodyRot + 180.0F) * Mth.DEG_TO_RAD);
            double z = Mth.cos((this.chargeStart != null ? this.chargeYaw : chicken.yBodyRot + 180.0F) * Mth.DEG_TO_RAD);
            float f1 = (float) Math.sqrt(x * x + z * z);
            x /= f1;
            z /= f1;
            x *= speed;
            z *= speed;
            chicken.setDeltaMovement(x, chicken.getDeltaMovement().y, z);
            if (this.chargeTime == ServerConfig.CHARGE_WARN_TIME.get()) {
                if (chicken.isHeadAvailable()) {
                    chicken.playSound(ModSounds.CHARGE_START.get(), 1.0F + 0.2F * scale, chicken.isChaosChicken() ? 0.8F : 1.0F);
                }

                this.chargeStart = chicken.blockPosition();
                this.chargeYaw = chicken.yBodyRot;
                this.lastX = chicken.getX();
                this.lastZ = chicken.getZ();
            } else if (this.chargeTime > ServerConfig.CHARGE_WARN_TIME.get()) {
                chicken.yBodyRot = chargeYaw;
                chicken.setYRot(chicken.yBodyRot);
                double diffX = chicken.getX() - this.lastX;
                double diffZ = chicken.getZ() - this.lastZ;
                double newDist = Math.sqrt(diffX * diffX + diffZ * diffZ);
                if (newDist > this.chargeLastDist) {
                    this.chargeLastDist = newDist;
                } else if (newDist / this.chargeLastDist < 0.8) {
                    if (chicken.getEggState() < 0 && chicken.distanceToSqr(Vec3.atCenterOf(this.chargeStart)) < 9.0) {
                        chicken.explode(chicken.getX(), chicken.getY(), chicken.getZ(), 0.75F * chicken.getScale(), false, true);
                    }

                    chicken.breakEgg();
                    chicken.setCharging(false);
                    chicken.setDeltaMovement(new Vec3(0, chicken.getDeltaMovement().y, 0));
                    Vec3 motion = chicken.getDeltaMovement();
                    destroyBlocksInAABB(chicken, chicken.partBody.getBoundingBox().expandTowards(motion.x, motion.y, motion.z).inflate((double)chicken.partBody.getBbWidth() / 3.0));
                    if (chicken.isHeadAvailable()) {
                        destroyBlocksInAABB(chicken, chicken.partHead.getBoundingBox().expandTowards(motion.x, motion.y, motion.z).inflate((double)chicken.partHead.getBbWidth() / 3.0));
                        destroyBlocksInAABB(chicken, chicken.partBill.getBoundingBox().expandTowards(motion.x, motion.y, motion.z).inflate((double)chicken.partBill.getBbWidth() / 3.0));
                    }
                }

                if (Double.isNaN(newDist / this.chargeLastDist)
                        || this.chargeStart != null && Math.sqrt(chicken.distanceToSqr(Vec3.atCenterOf(chargeStart))) > ServerConfig.CHARGE_CANCEL_DIST.get()
                        || this.chargeTime > 100 + ServerConfig.CHARGE_WARN_TIME.get()) {
                    chicken.setCharging(false);
                }

                if (chicken.isCharging()) {
                    Level level = chicken.level();
                    double diffY = chicken.getY() - chicken.yOld;
                    HashSet<Entity> collidedEnts = new HashSet<>();
                    collidedEnts.addAll(level.getEntities(chicken, chicken.partFootL.getBoundingBox().expandTowards(diffX, diffY, diffZ), EnderChickenEntity.PREDICATE_TARGETS));
                    collidedEnts.addAll(level.getEntities(chicken, chicken.partFootR.getBoundingBox().expandTowards(diffX, diffY, diffZ), EnderChickenEntity.PREDICATE_TARGETS));
                    collidedEnts.addAll(level.getEntities(chicken, chicken.partLegL.getBoundingBox().expandTowards(diffX, diffY, diffZ), EnderChickenEntity.PREDICATE_TARGETS));
                    collidedEnts.addAll(level.getEntities(chicken, chicken.partLegR.getBoundingBox().expandTowards(diffX, diffY, diffZ), EnderChickenEntity.PREDICATE_TARGETS));
                    collidedEnts.addAll(level.getEntities(chicken, chicken.partBody.getBoundingBox().expandTowards(diffX, diffY, diffZ), EnderChickenEntity.PREDICATE_TARGETS));

                    for (Entity ent : collidedEnts) {
                        double xOff = ent.getX() - chicken.getX();
                        double yOff = ent.getY() + (ent.getBoundingBox().maxY - ent.getBoundingBox().minY) / 2.0 - chicken.getY();
                        double zOff = ent.getZ() - chicken.getZ();
                        double dist2 = xOff * xOff + yOff * yOff + zOff * zOff + chicken.getRandom().nextGaussian() * 1.0E-5;
                        if (dist2 < (double)(chicken.getBbWidth() * chicken.getBbWidth())) {
                            double amp = 7.0;
                            Vec3 v = new Vec3(
                                    amp / dist2 * (xOff * xOff / dist2) * (xOff > 0.0 ? 1.0 : -1.0),
                                    amp * 1.5 / dist2 * (yOff * yOff / dist2) * (yOff > 0.0 ? 1.0 : -1.0) + 0.07 * scale,
                                    amp / dist2 * (zOff * zOff / dist2) * (zOff > 0.0 ? 1.0 : -1.0)
                            );
                            ent.setDeltaMovement(ent.getDeltaMovement().add(v).add(chicken.getDeltaMovement()));
                            if (ent instanceof ServerPlayer sp) {
                                sp.connection.send(new ClientboundSetEntityMotionPacket(ent.getId(), ent.getDeltaMovement()));
                            }
                        }

                        float damage = chicken.getScale();
                        if (chicken.isChaosChicken()) {
                            damage *= 2.5F;
                            damage *= ServerConfig.CHAOS_CHICKEN_DAMAGE_MULT.get();
                        }
                        ent.hurt(level.damageSources().mobAttack(chicken), damage);
                    }
                }

                this.chargeLastDist = newDist;
                this.lastX = chicken.getX();
                this.lastZ = chicken.getZ();
            }
        }

        ++this.chargeTime;
    }

    static void destroyBlocksInAABB(Entity ent, AABB aabb) {
        BlockPos.betweenClosedStream(aabb).forEach(pos -> {
            BlockState state = ent.level().getBlockState(pos);
            Block block = state.getBlock();
            if (!state.isAir() && !state.is(BlockTags.FIRE)
                    && block.canEntityDestroy(state, ent.level(), pos, ent)
                    && state.getDestroySpeed(ent.level(), pos) >= 0)
            {
                if (ent.getRandom().nextFloat() < 0.3F) {
                    ent.level().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
                }
                ent.level().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        });
    }
}
