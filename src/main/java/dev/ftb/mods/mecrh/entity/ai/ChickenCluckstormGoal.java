package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.MECRHMod;
import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.registry.ModAttachments;
import dev.ftb.mods.mecrh.util.Raytracing;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.EnumSet;

public class ChickenCluckstormGoal extends ChickenGoal {
    private static final ResourceLocation STRAY_SCALE_MOD_ID = MECRHMod.id("stray_scale");
    public static final AttributeModifier STRAY_SCALE_MOD
            = new AttributeModifier(STRAY_SCALE_MOD_ID, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private float targetYaw;

    public ChickenCluckstormGoal(EnderChicken chicken) {
        super(chicken);

        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && chicken.canUseAbility()
                && !chicken.isFiringLaser()
                && !chicken.hasCluckstormed()
                && chicken.getHealth() / chicken.getMaxHealth() <= 0.5;
    }

    @Override
    public boolean canContinueToUse() {
        return chicken.isFiringLaser() && chicken.isAlive() && chicken.getFiringProgress() < ChickenLaserGoal.LASER_DURATION;
    }

    @Override
    public void start() {
        chicken.useAbility();
        chicken.setFiringLaser(true);

        chicken.setHasCluckstormed();

        var bhr = Raytracing.getFocusedBlock(chicken, ServerConfig.ARENA_RADIUS.get() * 2);
        Vec3 startTarget;
        if (bhr.getType() == HitResult.Type.BLOCK) {
            startTarget = Vec3.atCenterOf(bhr.getBlockPos());
        } else {
            startTarget = chicken.partBill.position().add(chicken.getLookAngle().normalize().scale(ServerConfig.ARENA_RADIUS.get() / 2.0));
        }

        Vec3 offset = startTarget.subtract(chicken.partBill.position());
        targetYaw = (float) Math.atan2(offset.z, offset.x);
    }

    @Override
    public void stop() {
        chicken.setFiringLaser(false);
        chicken.endAbility();
    }

    @Override
    public void tick() {
        super.tick();

        float warmup = ChickenLaserGoal.WARMUP_TIME;
        float prog = chicken.getFiringProgress() > warmup ?
                (chicken.getFiringProgress() - warmup) / (ChickenLaserGoal.LASER_DURATION - warmup) :
                0f;

        double x = chicken.position().x + Mth.cos(targetYaw + prog * Mth.TWO_PI) * 10.0;
        double z = chicken.position().z + Mth.sin(targetYaw + prog * Mth.TWO_PI) * 10.0;
        double y = chicken.level().getHeight(Heightmap.Types.WORLD_SURFACE, (int) x, (int) z);
        chicken.getLookControl().setLookAt(x, y, z);

        if (chicken.getFiringProgress() == ChickenLaserGoal.WARMUP_TIME) {
            spawnStrayRiders();
        }
    }

    private void spawnStrayRiders() {
        for (int i = 0; i < 6; i++) {
            float angle = 360f / 6 * i * Mth.DEG_TO_RAD;
            float dist = 3 + chicken.getRandom().nextFloat() * 4.0f;
            Vec3 spawnPos = chicken.position().add(Mth.cos(angle) * dist, 0, Math.sin(angle) * dist);
            int y = chicken.level().getHeight(Heightmap.Types.WORLD_SURFACE, (int)spawnPos.x, (int)spawnPos.z);
            spawnPos = new Vec3(spawnPos.x, y + 1.0, spawnPos.z);

            Chicken chicken1 = new Chicken(EntityType.CHICKEN, chicken.level());
            chicken1.setData(ModAttachments.CHICKEN_ID, chicken.getId());
            chicken1.setPos(spawnPos);

            Stray stray = new Stray(EntityType.STRAY, chicken.level());
            stray.setPos(spawnPos);
            stray.setData(ModAttachments.CHICKEN_ID, chicken.getId());

            chicken.level().addFreshEntity(chicken1);
            chicken.level().addFreshEntity(stray);
            var scale = stray.getAttribute(Attributes.SCALE);
            if (scale != null) {
                scale.addPermanentModifier(STRAY_SCALE_MOD);
            }
            stray.startRiding(chicken1);

            chicken.playSound(SoundEvents.VILLAGER_HURT);
            if (chicken.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.8f, 0.8f, 0.2f), 1f), spawnPos.x, spawnPos.y, spawnPos.z, 15, 0.2, 0.2, 0.2, 0.1);
            }
        }
    }
}
