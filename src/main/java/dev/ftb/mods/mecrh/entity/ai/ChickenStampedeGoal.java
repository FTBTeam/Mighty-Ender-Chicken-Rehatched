package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.MECRHMod;
import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.registry.ModAttachments;
import dev.ftb.mods.mecrh.registry.ModSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class ChickenStampedeGoal extends ChickenGoal {
    public ChickenStampedeGoal(EnderChicken chicken) {
        super(chicken);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && chicken.getHealth() <= chicken.getMaxHealth() * 0.7f && chicken.isStampedeReady();
    }

    @Override
    public boolean canContinueToUse() {
        return false; // just takes one tick to do
    }

    @Override
    public void start() {
        int zombieCount = ServerConfig.getBabyZombieCount(chicken.getRandom());

        for (int i = 0; i < zombieCount; i++) {
            float angle = 360f / zombieCount * i * Mth.DEG_TO_RAD;
            float dist = 3 + chicken.getRandom().nextFloat() * 4.0f;
            Vec3 spawnPos = chicken.position().add(Mth.cos(angle) * dist, 0, Math.sin(angle) * dist);
            int y = chicken.level().getHeight(Heightmap.Types.WORLD_SURFACE, (int)spawnPos.x, (int)spawnPos.z);
            spawnPos = new Vec3(spawnPos.x, y + 1.0, spawnPos.z);

            Zombie zombie = new Zombie(chicken.level());
            zombie.setBaby(true);
            zombie.setPos(spawnPos);
            zombie.setData(ModAttachments.CHICKEN_ID, chicken.getId());

            chicken.level().addFreshEntity(zombie);
            chicken.playSound(ModSounds.CLEAR_WARN.get());
            if (chicken.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.8f, 0.8f, 0.2f), 1f), spawnPos.x, spawnPos.y, spawnPos.z, 15, 0.2, 0.2, 0.2, 0.1);
            }
        }

        chicken.scheduleNextStampede();
    }

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

    @EventBusSubscriber(modid = MECRHMod.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onTarget(LivingChangeTargetEvent event) {
            LivingEntity newTarget = event.getNewAboutToBeSetTarget();
            if (event.getEntity() instanceof EnderChicken && newTarget != null && hasChickenBoss(newTarget)
                    || hasChickenBoss(event.getEntity()) && newTarget instanceof EnderChicken)
            {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onMobDamage(LivingDamageEvent.Post event) {
            Entity sourceEntity = event.getSource().getEntity();
            if (sourceEntity instanceof LivingEntity && hasChickenBoss(sourceEntity)) {
                event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
            }
        }

        @SubscribeEvent
        public static void onMobDie(LivingDeathEvent event) {
            if (event.getEntity() instanceof LivingEntity living && hasChickenBoss(event.getEntity())) {
                AreaEffectCloud cloud = new AreaEffectCloud(living.level(), living.getX(), living.getY(), living.getZ());
                cloud.setPotionContents(new PotionContents(Optional.of(Potions.STRONG_HARMING), Optional.of(0xFF00C0FF), List.of()));
                cloud.setOwner(living);
                cloud.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
                cloud.setRadius(2.0f);
                cloud.setDuration(100);
                cloud.setRadiusOnUse(-0.5f);
                cloud.setWaitTime(20);
                cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
                event.getEntity().level().addFreshEntity(cloud);
            }
        }
    }
}
