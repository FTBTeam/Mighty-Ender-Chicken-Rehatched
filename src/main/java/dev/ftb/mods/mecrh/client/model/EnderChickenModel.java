package dev.ftb.mods.mecrh.client.model;

import dev.ftb.mods.mecrh.MECRHMod;
import dev.ftb.mods.mecrh.entity.EnderChicken;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class EnderChickenModel extends DefaultedEntityGeoModel<EnderChicken> {
    private static final ResourceLocation ID = MECRHMod.id("ender_chicken");
    private static final ResourceLocation FORCEFIELD_TEX = MECRHMod.id("textures/entity/ender_chicken_shield.png");

    public EnderChickenModel() {
        super(ID, true);
    }

    @Override
    public ResourceLocation getTextureResource(EnderChicken animatable) {
        return animatable.isForceField() ? FORCEFIELD_TEX : super.getTextureResource(animatable);
    }
}
