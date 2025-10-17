package dev.ftb.mods.mecrh.registry;

import dev.ftb.mods.mecrh.MECRHMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS
            = DeferredRegister.createItems(MECRHMod.MOD_ID);

    private static final List<DeferredItem<Item>> SPAWN_EGGS = new ArrayList<>();

    public static final DeferredItem<Item> ENDER_CHICKEN_SPAWN_EGG
            = registerSpawnEgg("ender_chicken", ModEntityTypes.ENDER_CHICKEN, 0xFF202020, 0xFFF770DC);


    public static List<DeferredItem<Item>> getSpawnEggs() {
        return Collections.unmodifiableList(SPAWN_EGGS);
    }

    private static DeferredItem<Item> registerSpawnEgg(String name, Supplier<? extends EntityType<? extends Mob>> type, int bgColor, int hiColor) {
        DeferredItem<Item> egg = ITEMS.register(name + "_spawn_egg",  () -> new DeferredSpawnEggItem(type, bgColor, hiColor, new Item.Properties()));
        SPAWN_EGGS.add(egg);
        return egg;
    }
}
