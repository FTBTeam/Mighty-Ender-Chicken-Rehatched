package dev.ftb.mods.mecrh.util;

import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.entity.EnderChickenPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

public class Raytracing {
    public static HitResult getFocusedEntityOrBlock(EnderChicken chicken, double range) {
        BlockHitResult result = getFocusedBlock(chicken, range);
        double rangeSq = range * range;
        Pair<Vec3, Vec3> startAndEnd = getStartAndEndLookVec(chicken, range);
        Vec3 eyePos = startAndEnd.getLeft();

        if (result.getType() != HitResult.Type.MISS) {
            rangeSq = result.getLocation().distanceToSqr(eyePos);
        }

        double rangeSq2 = rangeSq;
        Vec3 hitVec = null;
        Entity focusedEntity = null;

        Vec3 lookVec = chicken.getLookAngle().scale(range + 1);
        AABB box = chicken.getBoundingBox().inflate(lookVec.x, lookVec.y, lookVec.z);

        for (Entity entity : chicken.level().getEntities(chicken, box, e -> isValidTarget(e, chicken))) {
            AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> vec = aabb.clip(eyePos, startAndEnd.getRight());

            if (aabb.contains(eyePos)) {
                if (rangeSq2 >= 0.0D) {
                    focusedEntity = entity;
                    hitVec = vec.orElse(eyePos);
                    rangeSq2 = 0.0D;
                }
            } else if (vec.isPresent()) {
                double rangeSq3 = eyePos.distanceToSqr(vec.get());

                if (rangeSq3 < rangeSq2 || rangeSq2 == 0.0D) {
                    if (entity == entity.getVehicle() && !entity.canRiderInteract()) {
                        if (rangeSq2 == 0.0D) {
                            focusedEntity = entity;
                            hitVec = vec.get();
                        }
                    } else {
                        focusedEntity = entity;
                        hitVec = vec.get();
                        rangeSq2 = rangeSq3;
                    }
                }
            }
        }

        return focusedEntity != null && rangeSq2 < rangeSq ? new EntityHitResult(focusedEntity, hitVec) : result;
    }

    private static boolean isValidTarget(Entity e, EnderChicken chicken) {
        return e.isPickable() && e != chicken && !(e instanceof EnderChickenPart) && e != chicken.getFirstPassenger();
    }

    public static BlockHitResult getFocusedBlock(EnderChicken chicken, double range) {
        Pair<Vec3, Vec3> vecs = getStartAndEndLookVec(chicken, range);
        ClipContext ctx = new ClipContext(vecs.getLeft(), vecs.getRight(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, chicken);
        return chicken.level().clip(ctx);
    }

    private static Pair<Vec3, Vec3> getStartAndEndLookVec(EnderChicken chicken, double range) {
        Vec3 entityVec = chicken.partBill.position();
        Vec3 maxDistVec = entityVec.add(chicken.getViewVector(1F).scale(range));
        return new ImmutablePair<>(chicken.getEyePosition(1F), maxDistVec);
    }
}
