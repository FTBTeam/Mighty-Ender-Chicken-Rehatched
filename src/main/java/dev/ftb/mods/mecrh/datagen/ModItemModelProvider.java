package dev.ftb.mods.mecrh.datagen;

import dev.ftb.mods.mecrh.MECRHMod;
import dev.ftb.mods.mecrh.registry.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    private static final ResourceLocation TEMPLATE_SPAWN_EGG = ResourceLocation.parse("item/template_spawn_egg");
    private static final ResourceLocation GENERATED = ResourceLocation.parse("item/generated");

    public ModItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), MECRHMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ModItems.getSpawnEggs().forEach(egg -> withExistingParent(egg.getId().getPath(), TEMPLATE_SPAWN_EGG));
    }
}
