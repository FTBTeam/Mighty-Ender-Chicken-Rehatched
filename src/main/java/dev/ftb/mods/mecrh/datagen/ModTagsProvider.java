package dev.ftb.mods.mecrh.datagen;

import dev.ftb.mods.mecrh.MECRHMod;
import dev.ftb.mods.mecrh.MECRHTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModTagsProvider {
    public static class Blocks extends BlockTagsProvider {
        public Blocks(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, lookupProvider, MECRHMod.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
        }
    }

    public static class Items extends ItemTagsProvider {
        public Items(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, lookupProvider, blockTags, MECRHMod.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            // TODO temp testing item
            tag(MECRHTags.Items.CHICKEN_STICKS).add(net.minecraft.world.item.Items.STICK);
        }
    }
}
