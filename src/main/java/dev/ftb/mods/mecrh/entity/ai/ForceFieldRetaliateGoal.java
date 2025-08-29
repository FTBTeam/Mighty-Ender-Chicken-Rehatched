package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChickenEntity;
import dev.ftb.mods.mecrh.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.HashSet;

public class ForceFieldRetaliateGoal extends ChickenSkillGoal {
    private int attackTime;
    private double chargeLastDist;
    private BlockPos chargeStart;
    private float chargeYaw;
    private double lastX;
    private double lastZ;

    public ForceFieldRetaliateGoal(EnderChickenEntity chicken) {
        super(chicken, 1.0);

        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return chicken.isAlive() && chicken.canUseAbility() && chicken.getForceFieldAttacker() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return chicken.isAlive() && chicken.getForceFieldAttacker() != null;
    }

    @Override
    public void start() {
        chicken.useAbility();
        this.attackTime = -5;
        this.chargeLastDist = -1.0;
        chicken.setFlapping(true);
        chicken.setTarget(chicken.getForceFieldAttacker());
        super.start();
    }

    @Override
    public void stop() {
        chicken.endAbility();
        chicken.setFlapping(false);
        this.chargeStart = null;
        this.chargeYaw = 0.0F;
        this.attackTime = 0;
        super.stop();
    }

    @Override
    public void tick() {
        ++this.attackTime;
        if (this.attackTime >= 0) {
            if (this.attackTime < 100) {
                LivingEntity attacker = chicken.getForceFieldAttacker();
                chicken.getMoveControl().setWantedPosition(attacker.getX(), attacker.getY(), attacker.getZ(), 1.2);
                double dist = (double)chicken.distanceTo(attacker) - (double)chicken.getBbWidth() * 0.75;
                if (dist < 3.0) {
                    chicken.peck();
                }

                if (!chicken.isPecking()) {
                    chicken.getLookControl().setLookAt(attacker.getX(), attacker.getY() + attacker.getEyeHeight(), attacker.getZ(), chicken.getMaxHeadXRot(), chicken.getMaxHeadYRot());
                }
            } else {
                float scale = chicken.getScale();
                float speed = 0.41f + 0.044f * scale;
                if (chicken.isChaosChicken()) {
                    speed *= 1.1f;
                }

                float x = -Mth.sin((this.chargeStart != null ? this.chargeYaw : chicken.yBodyRot + 180.0F) * 0.017453292F);
                float z = Mth.cos((this.chargeStart != null ? this.chargeYaw : chicken.yBodyRot + 180.0F) * 0.017453292F);
                float dist = Mth.sqrt(x * x + z * z);
                x /= dist;
                z /= dist;
                x *= speed;
                z *= speed;
                chicken.setDeltaMovement(x, chicken.getDeltaMovement().y, z);
                if (this.attackTime == 100) {
                    chicken.setCharging(true);
                    if (chicken.isHeadAvailable()) {
                        chicken.playSound(ModSounds.CHARGE_START.get(), 1.0F + 0.2F * scale, 1.0F);
                    }

                    this.chargeStart = chicken.blockPosition();
                    this.chargeYaw = chicken.yBodyRot + 180.0F;
                    this.lastX = chicken.getX();
                    this.lastZ = chicken.getZ();
                } else if (this.attackTime > 100) {
                    chicken.yBodyRot = this.chargeYaw;
                    chicken.setYRot(chargeYaw);
                    chicken.setYHeadRot(chargeYaw);
                    chicken.setXRot(-10F);
                    double diffX = chicken.getX() - this.lastX;
                    double diffZ = chicken.getZ() - this.lastZ;
                    double newDist = Math.sqrt(diffX * diffX + diffZ * diffZ);
                    if (newDist > this.chargeLastDist) {
                        this.chargeLastDist = newDist;
                    } else if (newDist / this.chargeLastDist < 0.8) {
                        if (chicken.getEggState() < 0 && chicken.distanceToSqr(Vec3.atCenterOf(chargeStart)) < 9.0) {
                            chicken.explode(chicken.getX(), chicken.getY(), chicken.getZ(), 0.75F * chicken.getScale(), false, true);
                        }

                        chicken.breakEgg();
                        chicken.setCharging(false);
                        chicken.setForceFieldAttacker(null);
                        Vec3 motion = new Vec3(0, chicken.getDeltaMovement().y, 0);
                        chicken.setDeltaMovement(motion);
                        ChickenChargeGoal.destroyBlocksInAABB(chicken, chicken.partBody.getBoundingBox().expandTowards(motion.x, motion.y, motion.z).inflate((double)chicken.partBody.getBbWidth() / 3.0));
                        if (chicken.isHeadAvailable()) {
                            ChickenChargeGoal.destroyBlocksInAABB(chicken, chicken.partHead.getBoundingBox().expandTowards(motion.x, motion.y, motion.z).inflate((double)chicken.partBody.getBbWidth() / 3.0));
                            ChickenChargeGoal.destroyBlocksInAABB(chicken, chicken.partBill.getBoundingBox().expandTowards(motion.x, motion.y, motion.z).inflate((double)chicken.partBody.getBbWidth() / 3.0));
                        }
                    }

                    if (Double.isNaN(newDist / this.chargeLastDist)
                            || this.chargeStart != null && Math.sqrt(chicken.distanceToSqr(Vec3.atCenterOf(chargeStart))) > ServerConfig.CHARGE_CANCEL_DIST.get()
                            || this.attackTime > 200)
                    {
                        chicken.setCharging(false);
                        chicken.setForceFieldAttacker(null);
                    }

                    if (chicken.isCharging()) {
                        // TODO same code as in ChickenChargeGoal
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
        }
    }
}
