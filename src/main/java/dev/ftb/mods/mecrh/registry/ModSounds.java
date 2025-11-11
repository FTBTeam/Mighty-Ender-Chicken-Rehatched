package dev.ftb.mods.mecrh.registry;

import dev.ftb.mods.mecrh.MECRHMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MECRHMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> LASER_START = register("laser_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> LASER_LOOP = register("laser_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> LASER_END = register("laser_end");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHARGE_START = register("charge_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> FF_ON = register("ff_on");
    public static final DeferredHolder<SoundEvent, SoundEvent> FF_OFF = register("ff_off");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAOS_MUSIC = register("chicken_of_chaos");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAOS_HURT = register("chaos_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLEAR_WARN = register("clear_warn");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHICKEN_SPIN = register("ender_death");

    private static DeferredHolder<SoundEvent,SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(MECRHMod.id(name)));
    }

    private static DeferredHolder<SoundEvent,SoundEvent> registerFixed(String name, float range) {
        return SOUNDS.register(name, () -> SoundEvent.createFixedRangeEvent(MECRHMod.id(name), range));
    }

    public static class Musics {
        public static final Music CHICKEN_OF_CHAOS = new Music(CHAOS_MUSIC, 0, 0, true);
    }

    public static class JukeboxSongs {
        public static final ResourceKey<JukeboxSong> CHICKEN_OF_CHAOS
                = ResourceKey.create(Registries.JUKEBOX_SONG, MECRHMod.id("chicken_of_chaos"));
    }
}
