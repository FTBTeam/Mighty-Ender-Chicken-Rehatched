package dev.ftb.mods.mecrh.util;

import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.registry.ModAttachments;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ChickenUtils {
    public static boolean canPathfindToTarget(Mob mob, LivingEntity target, float dist) {
        Path path = mob.getNavigation().createPath(target, 0);
        return path != null && path.getDistToTarget() < dist;
    }

    public static void doParticleSpray(LivingEntity from, LivingEntity to, ParticleOptions particle, int nParticles) {
        Vec3 start = from.getEyePosition().add(from.getLookAngle().normalize().scale(0.5));
        Vec3 vel = to.getEyePosition().subtract(start).normalize();
        RandomSource random = from.getRandom();
        for (int i = 0; i < nParticles; i++) {
            Vec3 vel2 = vel.add(random.nextDouble() * 0.4 - 0.2, random.nextDouble() * 0.4 - 0.2, random.nextDouble() * 0.4 - 0.2);
            from.level().addParticle(particle, start.x, start.y, start.z, vel2.x, vel2.y, vel2.z);
        }
    }

    public static boolean isLookingAtMe(LivingEntity target, LivingEntity looker, double minAngle) {
        Vec3 viewVec = looker.getViewVector(1.0F).normalize();
        Vec3 offsetVec = new Vec3(target.getX() - looker.getX(), target.getEyeY() - looker.getEyeY(), target.getZ() - looker.getZ());
        offsetVec = offsetVec.normalize();
        double dot = viewVec.dot(offsetVec);
        return dot > 1.0 - minAngle && looker.hasLineOfSight(target);
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
}
