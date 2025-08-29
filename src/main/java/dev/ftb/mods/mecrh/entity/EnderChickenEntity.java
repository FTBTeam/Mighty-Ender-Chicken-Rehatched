package dev.ftb.mods.mecrh.entity;

import dev.ftb.mods.mecrh.ChickenSkill;
import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.ai.BreakEggGoal;
import dev.ftb.mods.mecrh.entity.ai.ChickenWanderGoal;
import dev.ftb.mods.mecrh.entity.ai.LookAtTargetGoal;
import dev.ftb.mods.mecrh.net.BreakEggMessage;
import dev.ftb.mods.mecrh.registry.ModEntityTypes;
import dev.ftb.mods.mecrh.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Predicate;

public class EnderChickenEntity extends Monster {
    public static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(EnderChickenEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Boolean> FIRING = SynchedEntityData.defineId(EnderChickenEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> EGG_STATE = SynchedEntityData.defineId(EnderChickenEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(EnderChickenEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> FLAPPING = SynchedEntityData.defineId(EnderChickenEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CLEAR_AREA = SynchedEntityData.defineId(EnderChickenEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> SPINNING = SynchedEntityData.defineId(EnderChickenEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> FORCEFIELD = SynchedEntityData.defineId(EnderChickenEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> INTRO_STATE = SynchedEntityData.defineId(EnderChickenEntity.class, EntityDataSerializers.INT);

    public static final Predicate<? super Entity> PREDICATE_TARGETS
            = EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e.canBeCollidedWith() && e.tickCount > 60);

    private boolean doingIntro;
    private int abilityInUse;
    private boolean doneIntro;
    private boolean clearArea;
    private int firingProgress;
    private int maxStrafingRunTime;
    private LivingEntity forceFieldAttacker;
    private boolean isPecking;
    private int peckProgress;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.mecrh.ender_chicken"),
            BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.NOTCHED_12
    );
    private final EnderChickenPart[] subParts;
    public final EnderChickenPart partFootL;
    public final EnderChickenPart partFootR;
    public final EnderChickenPart partLegL;
    public final EnderChickenPart partLegR;
    public final EnderChickenPart partBody;
    public final EnderChickenPart partWingL;
    public final EnderChickenPart partWingR;
    public final EnderChickenPart partHead;
    public final EnderChickenPart partBill;
    public final EnderChickenPart partForcefield;

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FLYING_SPEED, 0.25)
                .add(Attributes.MAX_HEALTH, 300)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 48)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9);
    }

    public EnderChickenEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);

        moveControl = new FlyingMoveControl(this, 10, true);

        partFootL = new EnderChickenPart(this, "footL", 0.1875F, 0.0625F);
        partFootR = new EnderChickenPart(this, "footR", 0.1875F, 0.0625F);
        partLegL = new EnderChickenPart(this, "legL", 0.0625F, 0.3125F);
        partLegR = new EnderChickenPart(this, "legR", 0.0625F, 0.3125F);
        partBody = new EnderChickenPart(this, "body", 0.375F, 0.375F);
        partWingL = new EnderChickenPart(this, "wingL", 0.25F, 0.1875F);
        partWingR = new EnderChickenPart(this, "wingR", 0.25F, 0.1875F);
        partHead = new EnderChickenPart(this, "head", 0.21875F, 0.375F);
        partBill = new EnderChickenPart(this, "bill", 0.1875F, 0.25F);
        partForcefield = new EnderChickenPart(this, "forcefield", 0.9375F, 0.9375F);

        subParts = new EnderChickenPart[]{
                partFootL, partFootR, partLegL, partLegR, partBody,
                partWingL, partWingR, partHead, partBill, partForcefield
        };
    }

    @Override
    public void setId(int id) {
        super.setId(id);

        for (int i = 0; i < subParts.length; i++) {
            subParts[i].setId(id + i + 1);
        }
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return subParts;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new BreakEggGoal(this, 0.0075F));
        goalSelector.addGoal(2, new RandomSwimmingGoal(this, 1.0, 10));
        addAttackSkills();
        goalSelector.addGoal(4, new LookAtTargetGoal(this));
        goalSelector.addGoal(5, new ChickenWanderGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        if (ServerConfig.TARGET_ALL_LIVING.get()) {
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true));
        } else {
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(SCALE, isChaosChicken() ? ServerConfig.CHAOS_CHICKEN_SCALE.get().floatValue() : ServerConfig.ENDER_CHICKEN_SCALE.get().floatValue());
        builder.define(FIRING, false);
        builder.define(EGG_STATE, 0);
        builder.define(CHARGING, false);
        builder.define(FLAPPING, false);
        builder.define(CLEAR_AREA, false);
        builder.define(SPINNING, false);
        builder.define(FORCEFIELD, false);
        builder.define(INTRO_STATE, 0);
    }

    private void addAttackSkills() {
        List<String> names = isChaosChicken() ? ServerConfig.CHAOS_SKILL_SET.get() : ServerConfig.ENDER_SKILL_SET.get();
        names.forEach(name ->
                ChickenSkill.forName(name).ifPresent(skill -> goalSelector.addGoal(3, skill.createGoal(this)))
        );
    }

    public boolean canUseAbility() {
        return !doingIntro
                && isAlive()
                && (abilityInUse < ServerConfig.MAX_SKILLS_AT_ONCE.get() || isChaosChicken());
    }

    public void useAbility() {
        ++abilityInUse;
    }

    public void endAbility() {
        --abilityInUse;
    }

    public boolean isChaosChicken() {
        return false;  // TODO stub
    }

    public int getEggState() {
        return getEntityData().get(EGG_STATE);
    }

    private void setEggState(int state) {
        getEntityData().set(EGG_STATE, state);
    }

    public boolean isCharging() {
        return getEntityData().get(CHARGING);
    }

    public void setCharging(boolean charging) {
        getEntityData().set(CHARGING, charging);
    }

    public boolean isClearingArea() {
        return getEntityData().get(CLEAR_AREA);
    }

    public boolean isFiring() {
        return getEntityData().get(FIRING);
    }

    public void setFiring(boolean firing) {
        getEntityData().set(FIRING, firing);
    }

    public boolean isFlapping() {
        return getEntityData().get(FLAPPING);
    }

    public void setFlapping(boolean flapping) {
        getEntityData().set(FLAPPING, flapping);
    }

    public void setClearingArea(boolean clearing) {
        getEntityData().set(CLEAR_AREA, clearing);
    }

    public boolean isHeadAvailable() {
        return (float) getEggState() > (float) ServerConfig.EGG_BREAKS_REQUIRED.get() / 2.0F || getEggState() < 0;
    }

    public boolean isForceField() {
        return getEntityData().get(FORCEFIELD);
    }

    public void setForceField(boolean forcefield) {
        getEntityData().set(FORCEFIELD, forcefield);
    }

    public boolean isSpinning() {
        return getEntityData().get(SPINNING);
    }

    public void setSpinning(boolean spinning) {
        getEntityData().set(SPINNING, spinning);
    }

    public int getFiringProgress() {
        return firingProgress;
    }

    public int getMaxStrafingRunTime() {
        return maxStrafingRunTime;
    }

    public void setMaxStrafingRunTime(int maxStrafingRunTime) {
        this.maxStrafingRunTime = maxStrafingRunTime;
    }

    public void explode(double x, double y, double z, float strength, boolean fire, boolean damageTerrain) {
        if (!level().isClientSide) {
            Explosion.BlockInteraction blockInteraction = damageTerrain ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP;
            Explosion explosion = new Explosion(level(), this, x, y, z, strength, fire, blockInteraction);
            if (EventHooks.onExplosionStart(level(), explosion)) {
                return;
            }

            explosion.explode();
            explosion.finalizeExplosion(false);
            if (!damageTerrain) {
                explosion.clearToBlow();
            }

            level().players().forEach(player -> {
                if (player instanceof ServerPlayer sp && sp.distanceToSqr(x, y, z) < 4096.0) {
                    sp.connection.send(new ClientboundExplodePacket(x, y, z, strength, explosion.getToBlow(), explosion.getHitPlayers().get(player),
                            blockInteraction, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION, SoundEvents.GENERIC_EXPLODE));
                }
            });
        }
    }

    public void breakEgg() {
        if (getEggState() >= 0) {
            if (!isChaosChicken() || !isHeadAvailable() || doneIntro || doingIntro) {
                setEggState(getEggState() + 1);
            }

            if (getEggState() >= ServerConfig.EGG_BREAKS_REQUIRED.get()) {
                setEggState(-1);
                playSound(ModSounds.CRACK_OPEN.get(), 0.2F * getScale(), 0.8F + getRandom().nextFloat() * 0.3F);
                PacketDistributor.sendToPlayersTrackingEntity(this, new BreakEggMessage(getId()));
                setForceField(true);
            } else {
                playSound(ModSounds.CRACK.get(), 0.2F * getScale(), 0.8F + getRandom().nextFloat() * 0.3F);
            }
        }
    }

    public LivingEntity getForceFieldAttacker() {
        return forceFieldAttacker;
    }

    public void setForceFieldAttacker(LivingEntity attacker) {
        forceFieldAttacker = attacker;
    }

    public void resetShouldClearArea() {
        clearArea = false;
    }

    public boolean shouldClearArea() {
        if (clearArea) {
            clearArea = false;
            return true;
        } else {
            return isHeadAvailable() && partInWall(partHead) || partInWall(partWingL) || partInWall(partWingR);
        }
    }

    private boolean partInWall(EnderChickenPart part) {
        float width = part.getBbWidth() * 0.8F;
        BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 8; ++i) {
            int x = Mth.floor(part.getX() + (((i >> 1) % 2) - 0.5) * width * 0.8);
            int y = Mth.floor(part.getY() + (((i >> 0) % 2) - 0.5) * 0.1 + (double) part.getBbHeight() * 0.8);
            int z = Mth.floor(part.getZ() + (((i >> 2) % 2) - 0.5) * width * 0.8);
            if (mutPos.getX() != x || mutPos.getY() != y || mutPos.getZ() != z) {
                mutPos.set(x, y, z);
                if (level().getBlockState(mutPos).isSuffocating(level(), mutPos)) {
                    return true;
                }
            }
        }

        return false;
    }

    public Vec3 getHeadPos(double horiOffset, double vertOffset) {
        float scale = getScale();
        double ryoSin = Math.sin(Math.toRadians(this.yBodyRot));
        double ryoCos = Math.cos(Math.toRadians(this.yBodyRot));
        double offset = isHeadAvailable() ? 0.25 : 0.0;
        return new Vec3(
                getX() - ryoSin * (offset + horiOffset) * scale,
                getY() + scale / 1.8 + vertOffset * scale,
                getZ() + ryoCos * (offset + horiOffset) * scale
        );
    }

    public void peck() {
        if (!this.isPecking) {
            this.isPecking = true;
            this.peckProgress = 0;
        }
    }

    public boolean isPecking() {
        return isPecking;
    }

    public void launchEggBomb(float speed) {
        float scale = getScale();
        Vec3 buttPos = getHeadPos(-0.5, 0.02);
        EggBomb egg = new EggBomb(ModEntityTypes.EGG_BOMB.get(), level());
        egg.setup(scale, getTarget(), getRandom().nextDouble() < ServerConfig.STRAFE_AIMBOT_CHANCE.get(), isChaosChicken());
        egg.setPos(buttPos);
        egg.setXRot(0f);
        egg.setYRot(yBodyRot - 180.0f);
        float x = -Mth.sin((yBodyRot + 180.0F) * Mth.DEG_TO_RAD);
        float z = Mth.cos((yBodyRot + 180.0F) * Mth.DEG_TO_RAD);
        float dist = Mth.sqrt(x * x + z * z);
        x /= dist;
        z /= dist;
        x = x * speed * scale;
        z = z * speed * scale;
        Vec3 chickenVel = getDeltaMovement();
        egg.setDeltaMovement(x + chickenVel.x, 0, z + chickenVel.z);
        playSound(SoundEvents.CHICKEN_EGG, 1.0F + 0.2F * scale, (getRandom().nextFloat() - getRandom().nextFloat()) * 0.2F + 1.0F);
        level().addFreshEntity(egg);
    }
}
