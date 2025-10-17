package dev.ftb.mods.mecrh.datagen;

import dev.ftb.mods.mecrh.MECRHMod;
import dev.ftb.mods.mecrh.registry.ModSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class ModSoundProvider extends SoundDefinitionsProvider {
    protected ModSoundProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, MECRHMod.MOD_ID, helper);
    }

    @Override
    public void registerSounds() {
        add(ModSounds.FF_ON, SoundDefinition.definition()
                .with(sound(MECRHMod.id("forcefield_on")))
                .subtitle("mecrh.subtitle.forcefield_on"));
        add(ModSounds.FF_OFF, SoundDefinition.definition()
                .with(sound(MECRHMod.id("forcefield_off")))
                .subtitle("mecrh.subtitle.forcefield_off"));
        add(ModSounds.CHICKEN_SPIN, SoundDefinition.definition()
                .with(sound(MECRHMod.id("chicken_spin")))
                .subtitle("mecrh.subtitle.chicken_spin"));
        add(ModSounds.CHARGE_START, SoundDefinition.definition()
                .with(sound(MECRHMod.id("charge_start")))
                .subtitle("mecrh.subtitle.charge_start"));
        add(ModSounds.CLEAR_WARN, SoundDefinition.definition()
                .with(sound(MECRHMod.id("clear_warn_1")))
                .with(sound(MECRHMod.id("clear_warn_2")))
                .with(sound(MECRHMod.id("clear_warn_3")))
                .subtitle("mecrh.subtitle.clear_warn"));
        add(ModSounds.LASER_START, SoundDefinition.definition()
                .with(sound(MECRHMod.id("laser_start")))
                .subtitle("mecrh.subtitle.laser_start"));
        add(ModSounds.LASER_LOOP, SoundDefinition.definition()
                .with(sound(MECRHMod.id("laser_loop")).stream())
                .subtitle("mecrh.subtitle.laser_loop"));
        add(ModSounds.LASER_END, SoundDefinition.definition()
                .with(sound(MECRHMod.id("laser_end")))
                .subtitle("mecrh.subtitle.laser_end"));
        add(ModSounds.CHAOS_MUSIC, SoundDefinition.definition()
                .with(sound(MECRHMod.id("chicken_of_chaos")).stream()));
    }
}
