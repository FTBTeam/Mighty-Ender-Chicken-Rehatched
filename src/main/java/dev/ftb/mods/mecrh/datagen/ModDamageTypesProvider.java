package dev.ftb.mods.mecrh.datagen;

import dev.ftb.mods.mecrh.ChickenDamageTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypesProvider {
    public static void bootstrap(BootstrapContext<DamageType> ctx) {
        ctx.register(ChickenDamageTypes.LASER, new DamageType("chicken_laser", 0.1F));
    }
}
