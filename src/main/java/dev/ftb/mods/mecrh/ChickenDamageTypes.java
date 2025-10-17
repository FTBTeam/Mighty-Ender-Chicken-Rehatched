package dev.ftb.mods.mecrh;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class ChickenDamageTypes {
    public static final ResourceKey<DamageType> LASER
            = ResourceKey.create(Registries.DAMAGE_TYPE, MECRHMod.id("laser"));
}
