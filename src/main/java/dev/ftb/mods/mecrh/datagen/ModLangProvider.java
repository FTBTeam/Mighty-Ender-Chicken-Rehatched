package dev.ftb.mods.mecrh.datagen;

import dev.ftb.mods.mecrh.MECRHMod;
import dev.ftb.mods.mecrh.registry.ModEntityTypes;
import dev.ftb.mods.mecrh.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput output) {
        super(output, MECRHMod.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ModEntityTypes.ENDER_CHICKEN.get(), "Mighty Ender Chicken");
        add(ModEntityTypes.EGG_BOMB.get(), "Egg Bomb");

        add(ModItems.ENDER_CHICKEN_SPAWN_EGG.get(), "Ender Chicken Spawn Egg");
        add(ModItems.RECORD_CHAOS_MUSIC.get(), "Music Disc");
        add(ModItems.TEST_STICK.get(), "Test Stick");

        add("item.mecrh.music_disc_chicken_of_chaos.desc", "FTB Team - Chicken of Chaos");

        add("mecrh.message.wrong_forcefield_item", "Invalid weapon for the forcefield!");

        add("mecrh.subtitle.forcefield_on", "Forcefield activates");
        add("mecrh.subtitle.forcefield_off", "Forcefield deactivates");
        add("mecrh.subtitle.chicken_spin", "Chicken starts spinning");
        add("mecrh.subtitle.charge_start", "Chicken charges");
        add("mecrh.subtitle.laser_start", "Laser powers up");
        add("mecrh.subtitle.laser_loop", "Laser firing");
        add("mecrh.subtitle.laser_end", "Laser powers down");
    }
}
