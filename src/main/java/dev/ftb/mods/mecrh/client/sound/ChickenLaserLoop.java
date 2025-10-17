package dev.ftb.mods.mecrh.client.sound;

import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.entity.ai.ChickenLaserGoal;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;

public class ChickenLaserLoop extends AbstractTickableSoundInstance {
    private final EnderChicken chicken;

    public ChickenLaserLoop(SoundEvent sound, EnderChicken chicken, float volume, float pitch) {
        super(sound, chicken.getSoundSource(), chicken.getRandom());
        this.chicken = chicken;

        this.volume = volume;
        this.pitch = pitch;
        this.attenuation = Attenuation.NONE;
        this.looping = true;
        this.delay = 0;
    }

    @Override
    public void tick() {
        x = chicken.partBill.getX();
        y = chicken.partBill.getY();
        z = chicken.partBill.getZ();

        int firingProgress = chicken.getFiringProgress();
        int fadeInStart = ChickenLaserGoal.WARMUP_TIME - 9;
        if (firingProgress > fadeInStart && firingProgress <= ChickenLaserGoal.WARMUP_TIME) {
            // fade in
            volume = (firingProgress - fadeInStart) / 10.0F;
        } else if (firingProgress < 0 && firingProgress > -20) {
            // fade out
            volume = 1.0F - (firingProgress + 20) / 20.0F;
        }
    }

    @Override
    public boolean isStopped() {
        return chicken.getFiringProgress() == 0 || !chicken.isAlive();
    }
}
