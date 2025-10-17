package dev.ftb.mods.mecrh.datagen;

import dev.ftb.mods.mecrh.MECRHMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DataGenerators {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getGenerator().addProvider(
                event.includeServer(),
                (DataProvider.Factory<DatapackBuiltinEntriesProvider>) output -> new DatapackBuiltinEntriesProvider(
                        output,
                        event.getLookupProvider(),
                        new RegistrySetBuilder().add(Registries.DAMAGE_TYPE, ModDamageTypesProvider::bootstrap),
                        Set.of(MECRHMod.MOD_ID)
                )
        ).getRegistryProvider();

//        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new ModLangProvider(generator.getPackOutput()));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(generator, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModSoundProvider(generator.getPackOutput(), existingFileHelper));

        BlockTagsProvider blockTagsProvider = new ModTagsProvider.Blocks(generator.getPackOutput(), lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new ModTagsProvider.Items(generator.getPackOutput(), lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), new ModDamageTypeTagsProvider(generator.getPackOutput(), lookupProvider, existingFileHelper));
//        generator.addProvider(event.includeServer(), new ModLootTableProvider(generator.getPackOutput(), lookupProvider));
    }
}
