package dev.ftb.mods.mecrh.client.sound;

import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.entity.ai.ChickenLaserGoal;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class ChickenMusicLoop extends AbstractTickableSoundInstance {
    private final EnderChicken chicken;

    public ChickenMusicLoop(SoundEvent sound, EnderChicken chicken, float volume, float pitch) {
        super(sound, SoundSource.RECORDS, chicken.getRandom());

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

        if (chicken.isDeadOrDying()) {
            volume = 0.5F - chicken.deathTime / 160F;
        }
    }

    @Override
    public boolean isStopped() {
        return chicken.isRemoved();
    }
}
