package dev.ftb.mods.mecrh;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MECRHTags {
    public static class Items {
        public static final TagKey<Item> CHICKEN_STICKS
                = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("ftb", "chicken_stick"));
    }
}
