package dev.ftb.mods.mecrh.client;

import dev.ftb.mods.mecrh.MECRHMod;
import dev.ftb.mods.mecrh.client.render.EggBombRenderer;
import dev.ftb.mods.mecrh.client.render.EnderChickenRenderer;
import dev.ftb.mods.mecrh.client.sound.ChickenLaserLoop;
import dev.ftb.mods.mecrh.client.sound.ChickenMusicLoop;
import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.registry.ModEntityTypes;
import dev.ftb.mods.mecrh.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = MECRHMod.MOD_ID, dist = Dist.CLIENT)
public class MECRHModClient {
    public MECRHModClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(MECRHModClient::registerRenderers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.ENDER_CHICKEN.get(), ctx -> EnderChickenRenderer.scaled(ctx, 1.6f));
        event.registerEntityRenderer(ModEntityTypes.EGG_BOMB.get(), EggBombRenderer::new);
    }

    public static void playEggBreakEffects(EnderChicken chicken) {
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

    public static void startLaserLoopSound(EnderChicken chicken) {
        var sound = new ChickenLaserLoop(ModSounds.LASER_LOOP.get(), chicken, 0.001F, 1.0F);
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    public static void startMusicLoop(EnderChicken chicken) {
        var sound = new ChickenMusicLoop(ModSounds.CHAOS_MUSIC.get(), chicken, 1.0F, 1.0F);
        Minecraft.getInstance().getMusicManager().stopPlaying();
        Minecraft.getInstance().getSoundManager().play(sound);
    }
}
