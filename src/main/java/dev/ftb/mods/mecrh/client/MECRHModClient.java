package dev.ftb.mods.mecrh.client;

import dev.ftb.mods.mecrh.MECRHMod;
import dev.ftb.mods.mecrh.entity.EnderChickenEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = MECRHMod.MOD_ID, dist = Dist.CLIENT)
public class MECRHModClient {
    public MECRHModClient(IEventBus eventBus, ModContainer container) {
    }

    public static void playEggBreakEffects(EnderChickenEntity chicken) {
        double eggX = chicken.getX();
        double eggY = chicken.getY() + (double)(chicken.getBbHeight() * 0.6F);
        double eggZ = chicken.getZ();

        Level level = chicken.level();
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DRAGON_EGG.defaultBlockState());
        for (int i = 0; i < 128; ++i) {
            level.addParticle(particle,
                    eggX + chicken.getRandom().nextGaussian() * (double)chicken.getBbWidth() * 0.15000000596046448,
                    eggY + chicken.getRandom().nextGaussian() * (double)chicken.getBbWidth() * 0.20000000298023224,
                    eggZ + chicken.getRandom().nextGaussian() * (double)chicken.getBbWidth() * 0.15000000596046448,
                    level.random.nextGaussian() * 0.20000000298023224,
                    level.random.nextDouble() * 0.4,
                    level.random.nextGaussian() * 0.20000000298023224
            );
        }
    }
}
