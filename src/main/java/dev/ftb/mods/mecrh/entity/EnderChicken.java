package dev.ftb.mods.mecrh.entity;

import dev.ftb.mods.mecrh.ChickenDamageTypes;
import dev.ftb.mods.mecrh.MECRHMod;
import dev.ftb.mods.mecrh.MECRHTags;
import dev.ftb.mods.mecrh.client.MECRHModClient;
import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChickenPart.PartType;
import dev.ftb.mods.mecrh.entity.ai.*;
import dev.ftb.mods.mecrh.event.EnderChickenEvent.Phase;
import dev.ftb.mods.mecrh.registry.ModAttachments;
import dev.ftb.mods.mecrh.registry.ModSounds;
import dev.ftb.mods.mecrh.util.ChickenUtils;
import dev.ftb.mods.mecrh.util.PreviousLaserDamage;
import dev.ftb.mods.mecrh.util.Raytracing;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public class EnderChicken extends Monster implements GeoEntity {
    public static final EntityDataAccessor<Boolean> FIRING = SynchedEntityData.defineId(EnderChicken.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(EnderChicken.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> FLAPPING = SynchedEntityData.defineId(EnderChicken.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CLEAR_AREA = SynchedEntityData.defineId(EnderChicken.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> SPINNING = SynchedEntityData.defineId(EnderChicken.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> FORCEFIELD = SynchedEntityData.defineId(EnderChicken.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> ENRAGED = SynchedEntityData.defineId(EnderChicken.class, EntityDataSerializers.BOOLEAN);

    public static final RawAnimation LASER_ANIMATION = RawAnimation.begin().thenPlay("attack.laser");
    //    public static final RawAnimation SPIN_ANIMATION = RawAnimation.begin().thenPlay("misc.spin");
    public static final RawAnimation ENRAGED_ANIMATION = RawAnimation.begin().thenPlay("misc.enraged");
    public static final RawAnimation PECK_ANIMATION = RawAnimation.begin().thenPlay("attack.peck");

    public static final Predicate<? super Entity> PREDICATE_TARGETS
            = EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE).and(e -> !e.isPassenger());//.and(e -> e.canBeCollidedWith() && e.tickCount > 60);

    public static final int SPAWNING_INTRO_TIME = 80; // ticks

    public static final ResourceLocation CHICKEN_SCALE_MOD = MECRHMod.id("chicken_scale");
    private static final ResourceLocation RIDER_SCALE_MOD = MECRHMod.id("zombie_rider_scale");
    private static final double ZOMBIE_RIDER_SCALE = 3.0;

    private int abilityInUse;
    private boolean clearAreaNeeded;
    private int firingProgress;
    private boolean inIntroPhase;
    private int forcefieldLevel; // positive -> number of hits needed to break it, negative -> time till activation
    private final Object2LongMap<UUID> forceFieldInformed = new Object2LongOpenHashMap<>();
    private Zombie zombieRider;
    private int spinTime;
    private int nextSpinTime;
    private int nextChargeTime;
    private int nextPeckTime;
    private int nextStampedeTime;
    private int nextLaserTime;
    private double laserLength;
    private int projectileImmuneTicks;
    private boolean hasCluckstormed;
    private int despawnTimer;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.mecrh.ender_chicken"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_12
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

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FLYING_SPEED, 0.25)
                .add(Attributes.MAX_HEALTH, 1024)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 96)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    public EnderChicken(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);

        partFootL = new EnderChickenPart(this, PartType.FOOT_L, 1.25F, 0.125F);
        partFootR = new EnderChickenPart(this, PartType.FOOT_R, 1.25F, 0.125F);
        partLegL = new EnderChickenPart(this, PartType.LEG_L, 0.5F, 3F);
        partLegR = new EnderChickenPart(this, PartType.LEG_R, 0.5F, 3F);
        partBody = new EnderChickenPart(this, PartType.BODY, 3.7F, 3.25F);
        partWingL = new EnderChickenPart(this, PartType.WING_L, 2F, 2F);
        partWingR = new EnderChickenPart(this, PartType.WING_R, 2F, 2F);
        partHead = new EnderChickenPart(this, PartType.HEAD, 1.7F, 3F);
        partBill = new EnderChickenPart(this, PartType.BILL, 1.7F, 1F);

        subParts = new EnderChickenPart[]{
                partFootL, partFootR, partLegL, partLegR, partBody,
                partWingL, partWingR, partHead, partBill
        };

        inIntroPhase = true;

        lookControl = new ChickenLookControl();

        setSizeModifier(-0.975);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
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
    public boolean isPickable() {
        // subparts are pickable
        return false;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(2, new ChickenStampedeGoal(this));
        goalSelector.addGoal(2, new ChickenCluckstormGoal(this));
        goalSelector.addGoal(3, new ChickenSpinGoal(this));
        goalSelector.addGoal(3, new ChickenChargeGoal(this, false));
        goalSelector.addGoal(3, new ChickenChargeGoal(this, true));
        goalSelector.addGoal(3, new ChickenLaserGoal(this));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.5, true));

        goalSelector.addGoal(5, new LookAtTargetGoal(this));
        goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        if (ServerConfig.TARGET_ALL_LIVING.get()) {
            targetSelector.addGoal(2, new ChickenNearestTargetGoal<>(this, LivingEntity.class, true, e -> !isInIntroPhase()));
        } else {
            targetSelector.addGoal(2, new ChickenNearestTargetGoal<>(this, Player.class, true, e -> !isInIntroPhase()));
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(FIRING, false);
        builder.define(CHARGING, false);
        builder.define(FLAPPING, false);
        builder.define(CLEAR_AREA, false);
        builder.define(SPINNING, false);
        builder.define(FORCEFIELD, false);
        builder.define(ENRAGED, false);
    }

    @Override
    public int getCurrentSwingDuration() {
        return 10;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Primary", 0, this::getPrimaryAnimationState));
        controllers.add(DefaultAnimations.genericAttackAnimation(this, PECK_ANIMATION));
    }

    private PlayState getPrimaryAnimationState(AnimationState<EnderChicken> state) {
        if (isDeadOrDying()) {
            return state.setAndContinue(DefaultAnimations.DIE);
        } else if (isFiringLaser()) {
            return state.setAndContinue(LASER_ANIMATION);
        } else if (onGround()) {
            if (state.isMoving()) {
                return state.setAndContinue(ENRAGED_ANIMATION);
            } else {
                return state.setAndContinue(DefaultAnimations.IDLE);
            }
        } else if (state.isMoving()) {
            return state.setAndContinue(DefaultAnimations.ATTACK_CHARGE);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    protected AABB getAttackBoundingBox() {
        // big mob...
        return super.getAttackBoundingBox().inflate(2.0);
    }

    @Override
    public void checkDespawn() {
        // do nothing, don't despawn naturally
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (tickCount == SPAWNING_INTRO_TIME + 1 && level().isClientSide && !isNoAi()) {
            MECRHModClient.startMusicLoop(this);
        }
        if (isSpinning()) {
            if (tickCount % 3 == 0) {
                spinTime = Math.min(35, spinTime + 1);
            }
            absRotateTo(tickCount * (5 + spinTime), getXRot());
            setYBodyRot(getYRot());
            setYHeadRot(getYRot());
        } else {
            spinTime = 0;
        }

        Vec3 vec3 = this.getDeltaMovement();
        if (!this.onGround() && vec3.y < 0.0) {
            this.setDeltaMovement(vec3.multiply(1.0, 0.6, 1.0));
        }

        handleLaserFiring();

        positionSubparts();

        if (isEnraged()) {
            if (!level().isClientSide) {
                if (level().getGameTime() % 20 == 0) {
                    addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2));
                    MobEffectInstance miningFatigue = new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 1);
                    MobEffectUtil.addEffectToPlayersAround((ServerLevel) level(), this, position(), 8.0, miningFatigue, 40);
                }
            } else {
                Vec3 vec = position().add(random.nextDouble() * 8 - 4, 2 + random.nextDouble() * 6, random.nextDouble() * 8 - 4);
                level().addParticle(random.nextBoolean() ? ParticleTypes.FLAME : ParticleTypes.LAVA, vec.x, vec.y, vec.z, 0, 0, 0);
            }
        }
    }

    private void handleLaserFiring() {
        firingProgress++;
        if (isFiringLaser()) {
            if (firingProgress == 1) {
                playSound(ModSounds.LASER_START.get(), 1.2F, 1F);
                if (level().isClientSide()) {
                    MECRHModClient.startLaserLoopSound(this);
                }
            } else if (firingProgress > ChickenLaserGoal.WARMUP_TIME && firingProgress < ChickenLaserGoal.LASER_DURATION) {
                HitResult hitResult = Raytracing.getFocusedEntityOrBlock(this, ServerConfig.ARENA_RADIUS.get() * 2.5);
                if (hitResult instanceof EntityHitResult ehr) {
                    applyLaserEntityDamage(ehr);
                } else if (hitResult instanceof BlockHitResult bhr && level().getGameTime() % 4 == 0) {
                    applyLaserBlockDamage(bhr);
                }
                laserLength = hitResult.getType() == HitResult.Type.MISS ? 16.0 : hitResult.getLocation().distanceTo(partBill.position());
            }
        } else {
            if (firingProgress > 5) {
                firingProgress = -20;
                playSound(ModSounds.LASER_END.get(), 1.2F, 1.0F);
            } else if (this.firingProgress > 0) {
                firingProgress = 0;
            }
        }
    }

    private void applyLaserEntityDamage(EntityHitResult ehr) {
        ehr.getEntity().setRemainingFireTicks(ServerConfig.LASER_FIRE_TICKS.get());
        if (isEnraged() && ehr.getEntity() instanceof LivingEntity l) {
            l.addEffect(new MobEffectInstance(MobEffects.WITHER, ServerConfig.LASER_FIRE_TICKS.get(), 1));
        }

        float laserDmg = ServerConfig.LASER_DAMAGE.get().floatValue();

        if (ehr.getEntity().hasData(ModAttachments.PREV_LASER_DMG)) {
            PreviousLaserDamage prevDmg = PreviousLaserDamage.forEntity(ehr.getEntity());
            if (level().getGameTime() - prevDmg.when() < 20L) {
                laserDmg = prevDmg.damage() * ServerConfig.LASER_DAMAGE_INCREASE.get().floatValue();
            }
        }

        float effectiveDmg = ehr.getEntity() instanceof LivingEntity l && l.isBlocking() ? laserDmg * 0.6F : laserDmg;
        ehr.getEntity().hurt(level().damageSources().source(ChickenDamageTypes.LASER, this), effectiveDmg);

        PreviousLaserDamage.setPreviousLaserDamage(ehr.getEntity(), laserDmg);
    }

    private void applyLaserBlockDamage(BlockHitResult bhr) {
        BlockPos pos = bhr.getBlockPos();
        BlockState state = level().getBlockState(pos);

        if (!state.isAir() && state.getDestroySpeed(level(), pos) > 0 && !state.is(MECRHTags.Blocks.CHICKEN_UNBREAKABLE)) {
            ChickenUtils.destroyBlock(this, pos);
            for (Direction dir : Direction.values()) {
                BlockPos pos2 = pos.relative(dir);
                if (random.nextFloat() < 0.5f) {
                    if (!state.isAir() && state.getDestroySpeed(level(), pos2) > 0 && !state.is(MECRHTags.Blocks.CHICKEN_UNBREAKABLE)) {
                        ChickenUtils.destroyBlock(this, pos);
                    }
                }
            }
            Vec3 vec = Vec3.atCenterOf(bhr.getBlockPos().relative(bhr.getDirection()));
            level().explode(this, vec.x, vec.y, vec.z, 2f, Level.ExplosionInteraction.NONE);
        }
    }

    @Override
    protected void customServerAiStep() {
        if (!hasRestriction()) {
            restrictTo(blockPosition(), ServerConfig.ARENA_RADIUS.get());
        }

        if (isInIntroPhase() && tickCount < SPAWNING_INTRO_TIME) {
            int halfIntroTime = SPAWNING_INTRO_TIME / 2;
            if (tickCount <= halfIntroTime) {
                setSizeModifier(-0.975);
                if (tickCount % 20 == 5) {
                    playSound(SoundEvents.CHICKEN_AMBIENT);
                }
                bossEvent.setProgress(0f);
            } else {
                setSizeModifier((SPAWNING_INTRO_TIME - tickCount) / (double) -halfIntroTime);
                if (tickCount % 7 == 0) {
                    playSound(SoundEvents.CHICKEN_AMBIENT, 1f, 0.6f + tickCount / 80f);
                }
                bossEvent.setProgress((tickCount - halfIntroTime) / (float) halfIntroTime);
            }
        } else {
            bossEvent.setProgress(getHealth() / getMaxHealth());
        }

        if (tickCount == 1) {
            if (!inIntroPhase && !hasPassenger(e -> e.getType() == EntityType.ZOMBIE)) {
                // restoring from NBT, make sure zombie rider exists
                spawnZombieRider(true);
            }
        } else if (tickCount == SPAWNING_INTRO_TIME - 20) {
            if (inIntroPhase && !hasPassenger(e -> e.getType() == EntityType.ZOMBIE)) {
                // drop the zombie rider in from above (when newly spawned)
                spawnZombieRider(false);
            }
        } else if (isInIntroPhase()) {
            if (tickCount == SPAWNING_INTRO_TIME) {
                nextSpinTime = tickCount + ServerConfig.getSpinInterval(getRandom());
                nextChargeTime = tickCount + ServerConfig.getChargeInterval(getRandom());
                nextPeckTime = tickCount + ServerConfig.getPeckInterval(getRandom());
                nextLaserTime = tickCount + ServerConfig.getLaserInterval(getRandom());
                nextStampedeTime = 0; // will trigger as soon as chicken is below 70% health
                playSound(SoundEvents.BELL_BLOCK, 2f, 0.6f);
                setForceField(true);
                inIntroPhase = false;
                ChickenUtils.postChickenEvent(this, Phase.SHIELDED_ENTRY);
            } else if (tickCount == SPAWNING_INTRO_TIME + 10) {
                playSound(SoundEvents.CHICKEN_HURT, 2f, 0.7f);
            }
        }

        if (forcefieldLevel < 0 && ++forcefieldLevel == 0) {
            setForceField(true);
        }

        if (zombieRider != null) {
            updateZombieRider();
        }

        if (getHealth() < getMaxHealth() * 0.15F && !isEnraged()) {
            setEnraged();
        }

        if (projectileImmuneTicks > 0) {
            --projectileImmuneTicks;
        }

        if (hasRestriction() && position().distanceToSqr(Vec3.atCenterOf(getRestrictCenter())) > ServerConfig.getArenaRadiusSq()) {
            Vec3 dest = Vec3.atBottomCenterOf(getRestrictCenter());
            teleportTo(dest.x, dest.y, dest.z);
            setProjectileImmuneTicks(40);
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.PORTAL,
                        getRandomX(1.0), getRandomY(), getRandomZ(1.0),
                        10,
                        (random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 2.0,
                        0.1);
                playSound(SoundEvents.ENDERMAN_TELEPORT);
            }
        }

        if (tickCount % 20 == 0 && ServerConfig.DESPAWN_TIME_NO_PLAYERS.get() > 0) {
            if (countPlayersInArena() == 0) {
                if (++despawnTimer >= ServerConfig.DESPAWN_TIME_NO_PLAYERS.get()) {
                    discard();
                }
            } else {
                despawnTimer = 0;
            }
        }
    }

    private void spawnZombieRider(boolean immediate) {
        zombieRider = new Zombie(level());
        zombieRider.setBaby(true);
        zombieRider.setPos(position().add(0, ServerConfig.ZOMBIE_RIDER_SPAWN_HEIGHT.get(), 0));
        zombieRider.setXRot(getXRot());
        zombieRider.setYRot(getYRot());
        zombieRider.setInvulnerable(true);
        zombieRider.setSilent(true);
        AttributeInstance scaleAttr = zombieRider.getAttribute(Attributes.SCALE);
        if (scaleAttr != null) {
            scaleAttr.addPermanentModifier(new AttributeModifier(RIDER_SCALE_MOD, ZOMBIE_RIDER_SCALE, Operation.ADD_MULTIPLIED_BASE));
        }
        level().addFreshEntity(zombieRider);
        if (immediate) {
            zombieRider.setNoAi(true);
            zombieRider.startRiding(this);
        }
    }

    private void updateZombieRider() {
        if (zombieRider.getVehicle() == null) {
            if (zombieRider.distanceToSqr(getPassengerRidingPosition(this)) < 4.0) {
                zombieRider.setNoAi(true);
                zombieRider.startRiding(this);
            } else {
                // keep zombie above chicken in case it moves
                zombieRider.setPos(position().x, zombieRider.position().y, position().z);
            }
        }
        zombieRider.setYRot(getYRot());
        zombieRider.setXRot(getXRot());
        zombieRider.setYHeadRot(getYHeadRot());
        zombieRider.setRemainingFireTicks(0);
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return null;  // zombie rider never controls the chicken
    }

    @Override
    protected void tickDeath() {
        deathTime++;

        if (deathTime == 1) {
            if (level().isClientSide()) {
                AABB aabb = new AABB(blockPosition()).inflate(ServerConfig.ARENA_RADIUS.get());
                level().getNearbyPlayers(TargetingConditions.forNonCombat(), this, aabb)
                        .forEach(player -> player.playSound(SoundEvents.ENDER_DRAGON_DEATH));
            } else {
                ChickenUtils.postChickenEvent(this, Phase.DEATH_SEQUENCE);
            }
        } else if (deathTime == 10 && !level().isClientSide()) {
            BlockPos pos = Objects.requireNonNullElse(getRestrictCenter(), blockPosition());
            AABB aabb = new AABB(pos).inflate(ServerConfig.ARENA_RADIUS.get() * 2);
            level().getNearbyEntities(LivingEntity.class, TargetingConditions.forNonCombat(), this, aabb).forEach(entity -> {
                if (ChickenUtils.hasChickenBoss(entity, this)) {
                    entity.kill();
                }
            });
        }

        if (deathTime == 10 && !level().isClientSide()) {
            getPassengers().forEach(Entity::discard);
            zombieRider = null;
        }

        if (deathTime < 80 && level().isClientSide()) {
            Vec3 vec = position().add(random.nextDouble() * 8 - 4, 2 + random.nextDouble() * 6, random.nextDouble() * 8 - 4);
            level().addParticle(random.nextBoolean() ? ParticleTypes.FLAME : ParticleTypes.ASH, vec.x, vec.y, vec.z, 0, 0, 0);
            CampfireBlock.makeParticles(level(), blockPosition().above(3), false, true);
            if (deathTime % 5 == 0) {
                double x = getBoundingBox().minX + random.nextDouble() * getBoundingBox().getXsize();
                double y = getBoundingBox().minY + random.nextDouble() * getBoundingBox().getYsize();
                double z = getBoundingBox().minZ + random.nextDouble() * getBoundingBox().getZsize();
                level().addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 0.0, 0.0, 0.0);
            }
        }

        if (deathTime >= 80 && !level().isClientSide()) {
            remove(RemovalReason.KILLED);
            gameEvent(GameEvent.ENTITY_DIE);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putInt("ForceFieldLevel", forcefieldLevel);
        compound.putInt("NextSpinTime", nextSpinTime - tickCount);
        compound.putInt("NextChargeTime", nextChargeTime - tickCount);
        compound.putInt("NextPeckTime", nextPeckTime - tickCount);
        compound.putInt("NextLaserTime", nextLaserTime - tickCount);
        if (inIntroPhase) compound.putBoolean("IntroPhase", true);
        if (projectileImmuneTicks > 0) compound.putInt("ProjectileImmuneTicks", projectileImmuneTicks);
        if (hasRestriction()) compound.put("RestrictPos", NbtUtils.writeBlockPos(getRestrictCenter()));
        if (isEnraged()) compound.putBoolean("Enraged", true);
        if (hasCluckstormed) compound.putBoolean("HasCluckstormed", true);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        forcefieldLevel = compound.getInt("ForceFieldLevel");
        setForceField(forcefieldLevel > 0, false);
        nextSpinTime = compound.getInt("NextSpinTime");
        nextChargeTime = compound.getInt("NextChargeTime");
        nextPeckTime = compound.getInt("NextPeckTime");
        nextLaserTime = compound.getInt("NextLaserTime");
        inIntroPhase = compound.getBoolean("IntroPhase");
        projectileImmuneTicks = compound.getInt("ProjectileImmuneTicks");
        hasCluckstormed = compound.getBoolean("HasCluckstormed");
        NbtUtils.readBlockPos(compound, "RestrictPos")
                .ifPresent(pos -> restrictTo(pos, ServerConfig.ARENA_RADIUS.get()));
        entityData.set(ENRAGED, compound.getBoolean("Enraged"));
        if (!inIntroPhase) {
            setSizeModifier(1.0);
        }
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float partialTick) {
        // chicken body dips when it does the peck attack "swing"
        int a = Math.abs(swingTime - getCurrentSwingDuration() / 2);
        double yOff = 2.25 + (swinging ? (double) a / getCurrentSwingDuration() : 0.0);
        return super.getPassengerAttachmentPoint(entity, dimensions, partialTick).subtract(0, yOff, 0);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        // TODO anything needed here?
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (shouldIgnoreDamage(source)) {
            return false;
        } else if (source.is(DamageTypes.IN_WALL)) {
            clearAreaNeeded = true;
            return false;
        } else {
            if (!source.is(Tags.DamageTypes.IS_TECHNICAL)) {
                amount = Math.min(amount, ServerConfig.MAX_INCOMING_DAMAGE.get() * (isEnraged() ? 0.3F : 1F));
            }
            return super.hurt(source, amount);
        }
    }

    @Override
    public float maxUpStep() {
        return 2F;
    }

    public boolean attackFromPart(DamageSource source, EnderChickenPart part, float amount) {
        // Note: entities which can't break the shield can never hurt the chicken
        // - includes living entities not holding a chicken stick, and all non-entity damage sources
        if (isForceField()) {
            if (attackerCanBreakForcefield(source.getEntity())) {
                playSound(ModSounds.CHAOS_HURT.get());
                if (!level().isClientSide() && --forcefieldLevel <= 0) {
                    setForceField(false);
                    playSound(SoundEvents.SHIELD_BREAK);
                }
            } else {
                if (source.getEntity() instanceof Player player) {
                    player.hurt(source, amount);
                    long delta = level().getGameTime() - forceFieldInformed.getOrDefault(player.getUUID(), 0L);
                    if (delta > 100) {
                        player.displayClientMessage(Component.translatable("mecrh.message.wrong_forcefield_item"), true);
                        forceFieldInformed.put(player.getUUID(), level().getGameTime());
                    }
                }
            }
            return false;
        }
        return hurt(source, amount);
    }

    private boolean attackerCanBreakForcefield(Entity entity) {
        return ServerConfig.NON_PLAYERS_IGNORE_SHIELD.get() ?
                !(entity instanceof Player) :
                entity instanceof LivingEntity l && l.getMainHandItem().is(MECRHTags.Items.CHICKEN_STICKS);
    }

    private boolean inIntroPhase() {
        return tickCount < SPAWNING_INTRO_TIME;
    }

    private boolean isForceFieldBreakingItem(ItemStack stack) {
        return stack.is(MECRHTags.Items.CHICKEN_STICKS);
    }

    public boolean shouldIgnoreDamage(DamageSource source) {
        return source.is(net.minecraft.world.damagesource.DamageTypes.DRAGON_BREATH)
                || source.getEntity() == this
                || source.getEntity() instanceof EnderChicken
                || isProjectileImmune() && (source.getDirectEntity() instanceof AbstractArrow || source.getDirectEntity() instanceof WindCharge)
                || inIntroPhase();
    }

    public boolean isProjectileImmune() {
        return projectileImmuneTicks > 0 || isFiringLaser() || isSpinning();
    }

    public void setProjectileImmuneTicks(int immuneTicks) {
        projectileImmuneTicks = immuneTicks;
    }

    private void positionSubparts() {
        Vec3[] prevPartPos = new Vec3[subParts.length];
        for (int i = 0; i < subParts.length; i++) {
            prevPartPos[i] = new Vec3(subParts[i].getX(), subParts[i].getY(), subParts[i].getZ());
        }

        float yawRad = yBodyRot * Mth.DEG_TO_RAD;
        float xOff = Mth.cos(yawRad);
        float zOff = Mth.sin(yawRad);
        float headYawRad = (yHeadRot * Mth.DEG_TO_RAD) + Mth.HALF_PI;
        float headPitchRad = lerpTargetXRot() * Mth.DEG_TO_RAD;
        float xOffHead = Mth.cos(yawRad + Mth.HALF_PI);
        float zOffHead = Mth.sin(yawRad + Mth.HALF_PI);
        float yOffHead = -Mth.sin(headPitchRad);
        float distOffHead = Mth.sin(headPitchRad) / 2f;

        updatePartPos(partFootL, xOff * 0.8f, 0f, zOff * 0.8f);
        updatePartPos(partFootR, xOff * -0.8f, 0f, zOff * -0.8f);
        updatePartPos(partLegL, xOff * 0.7f, 0.125f, zOff * 0.7f);
        updatePartPos(partLegR, xOff * -0.7f, 0.125f, zOff * -0.7f);
        updatePartPos(partBody, xOff * 0f, 2.5f, zOff * 0f);
        updatePartPos(partWingL, xOff * 2f, 3.5f, zOff * 2f);
        updatePartPos(partWingR, xOff * -2f, 3.5f, zOff * -2f);
        updatePartPos(partHead, xOffHead * (2f + distOffHead), 4.5f + yOffHead, zOffHead * (2f + distOffHead));

        Vec3 headPos = partHead.position();
        partBill.setPos(headPos.x + Mth.cos(headYawRad), headPos.y + 1.0, headPos.z + Mth.sin(headYawRad));

        for (int i = 0; i < subParts.length; i++) {
            subParts[i].xo = prevPartPos[i].x;
            subParts[i].yo = prevPartPos[i].y;
            subParts[i].zo = prevPartPos[i].z;
            subParts[i].xOld = prevPartPos[i].x;
            subParts[i].yOld = prevPartPos[i].y;
            subParts[i].zOld = prevPartPos[i].z;
        }
    }

    private void updatePartPos(EnderChickenPart part, float xOff, float yOff, float zOff) {
        part.setPos(getX() + xOff, getY() + yOff, getZ() + zOff);
    }

    public boolean canUseAbility() {
        return !inIntroPhase()
                && isAlive()
                && (abilityInUse < ServerConfig.MAX_SKILLS_AT_ONCE.get());
    }

    public void useAbility() {
        ++abilityInUse;
    }

    public void endAbility() {
        --abilityInUse;
    }

    public boolean isInIntroPhase() {
        return inIntroPhase;
    }

    public boolean isCharging() {
        return getEntityData().get(CHARGING);
    }

    public void setCharging(boolean charging, boolean peckOfDoom) {
        getEntityData().set(CHARGING, charging);
        if (!charging) {
            if (peckOfDoom) {
                nextPeckTime = tickCount + ServerConfig.getPeckInterval(getRandom());
            } else {
                nextChargeTime = tickCount + ServerConfig.getChargeInterval(getRandom());
            }
        }
    }

    public boolean isClearingArea() {
        return getEntityData().get(CLEAR_AREA);
    }

    public void setClearingArea(boolean clearing) {
        getEntityData().set(CLEAR_AREA, clearing);
    }

    public boolean isFiringLaser() {
        return getEntityData().get(FIRING);
    }

    public void setFiringLaser(boolean firing) {
        if (firing && !isFiringLaser()) {
            firingProgress = 0;
        }
        getEntityData().set(FIRING, firing);
        if (!firing) {
            nextLaserTime = tickCount + ServerConfig.getLaserInterval(getRandom());
        }
    }

    public boolean isFlapping() {
        return getEntityData().get(FLAPPING);
    }

    public void setFlapping(boolean flapping) {
        getEntityData().set(FLAPPING, flapping);
    }

    public boolean isForceField() {
        return getEntityData().get(FORCEFIELD);
    }

    public void setForceField(boolean forcefield) {
        setForceField(forcefield, true);
    }

    public void setForceField(boolean forcefield, boolean postEvent) {
        if (forcefield != isForceField()) {
            getEntityData().set(FORCEFIELD, forcefield);
            forcefieldLevel = forcefield ? ServerConfig.FORCEFIELD_LEVEL.get() : -ServerConfig.FORCEFIELD_INTERVAL.get();
            playSound(forcefield ? ModSounds.FF_ON.get() : ModSounds.FF_OFF.get(), 1.5f, 1f);

            if (postEvent) {
                ChickenUtils.postChickenEvent(this, forcefield ? Phase.SHIELD_CYCLE : Phase.VULNERABLE_ASSAULT);
            }
        }
    }

    public boolean isSpinning() {
        return getEntityData().get(SPINNING);
    }

    public void setSpinning(boolean spinning) {
        getEntityData().set(SPINNING, spinning);
        setYBodyRot(getYHeadRot());
        if (!spinning) {
            nextSpinTime = tickCount + ServerConfig.getSpinInterval(getRandom());
        }
    }

    public int getFiringProgress() {
        return firingProgress;
    }

    public void scheduleNextStampede() {
        nextStampedeTime = tickCount + ServerConfig.getStampedeInterval(getRandom());
    }

    public void setEnraged() {
        entityData.set(ENRAGED, true);
        playSound(SoundEvents.CHICKEN_DEATH, 1f, 0.5f);
        ChickenUtils.postChickenEvent(this, Phase.ENRAGE);
    }

    public boolean isEnraged() {
        return entityData.get(ENRAGED);
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

    public void resetShouldClearArea() {
        clearAreaNeeded = false;
    }

    public boolean shouldClearArea() {
        if (clearAreaNeeded) {
            clearAreaNeeded = false;
            return true;
        } else {
            return partInWall(partHead) || partInWall(partWingL) || partInWall(partWingR);
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
        float ryoSin = Mth.sin(getYRot() * Mth.DEG_TO_RAD);
        float ryoCos = Mth.cos(getYRot() * Mth.DEG_TO_RAD);
        return new Vec3(
                getX() - ryoSin * horiOffset * scale,
                getEyeY() + vertOffset * scale,
                getZ() + ryoCos * horiOffset * scale
        );
    }

    public void launchEggBomb(float speed, boolean huntTarget) {
        float scale = getScale();
        Vec3 launchPos = getHeadPos(-3.5, -2.0);
        EggBomb egg = new EggBomb(level(), this);
        egg.huntTarget(huntTarget);
        egg.setPos(launchPos);
        egg.shootFromRotation(this, 0f, getYRot() - 180f, 0f, speed, 1f);
        playSound(SoundEvents.CHICKEN_EGG, 1.0F + 0.2F * scale, (getRandom().nextFloat() - getRandom().nextFloat()) * 0.2F + 1.0F);
        level().addFreshEntity(egg);
    }

    public boolean isSpinReady() {
        return tickCount >= nextSpinTime;
    }

    public boolean isChargeReady(boolean peckOfDoom) {
        return tickCount >= (peckOfDoom ? nextPeckTime : nextChargeTime);
    }

    public boolean isStampedeReady() {
        return tickCount >= nextStampedeTime;
    }

    public boolean isLaserReady(boolean targetAbove) {
        // laser more likely to get used if target is above the chicken
        return tickCount >= nextLaserTime - (targetAbove ? 40 : 0);
    }

    private void setSizeModifier(double modifier) {
        var attr = getAttribute(Attributes.SCALE);
        if (attr != null) {
            attr.removeModifier(CHICKEN_SCALE_MOD);
            if (modifier != 1.0) {
                attr.addTransientModifier(new AttributeModifier(CHICKEN_SCALE_MOD, modifier, Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
    }

    public double getLaserLength() {
        return laserLength;
    }

    public boolean hasCluckstormed() {
        return hasCluckstormed;
    }

    public void setHasCluckstormed() {
        hasCluckstormed = true;
    }

    private int countPlayersInArena() {
        AABB aabb = new AABB(blockPosition()).inflate(ServerConfig.ARENA_RADIUS.get());
        return (int) level().getNearbyPlayers(TargetingConditions.forNonCombat(), this, aabb).stream()
                .filter(this::isInArena)
                .count();
    }

    public boolean isInArena(Entity entity) {
        return !hasRestriction() || getRestrictCenter().distToCenterSqr(entity.getX(), entity.getY(), entity.getZ()) < ServerConfig.getArenaRadiusSq();
    }

    private static class ChickenNearestTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        public ChickenNearestTargetGoal(Mob mob, Class<T> targetType, boolean mustSee, Predicate<LivingEntity> targetPredicate) {
            super(mob, targetType, mustSee, targetPredicate);
        }

        @Override
        public boolean canUse() {
            return super.canUse()
                    && !((EnderChicken) mob).isInIntroPhase()
                    && target != null && !target.getType().is(MECRHTags.Entities.CHICKEN_FRIENDS);
        }

        @Override
        protected AABB getTargetSearchArea(double targetDistance) {
            return this.mob.getBoundingBox().inflate(targetDistance, 16.0, targetDistance);
        }
    }

    private class ChickenLookControl extends LookControl {
        public ChickenLookControl() {
            super(EnderChicken.this);
        }

        @Override
        protected boolean resetXRotOnTick() {
            return !isFiringLaser();
        }

        @Override
        public void tick() {
            if (!isSpinning()) {
                super.tick();
            }
        }
    }
}
