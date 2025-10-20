package dev.ftb.mods.mecrh.entity.ai;

import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.registry.ModAttachments;
import dev.ftb.mods.mecrh.registry.ModSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

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
}
