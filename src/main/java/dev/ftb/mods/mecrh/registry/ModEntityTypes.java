package dev.ftb.mods.mecrh.registry;

import dev.ftb.mods.mecrh.MECRHMod;
import dev.ftb.mods.mecrh.entity.EggBomb;
import dev.ftb.mods.mecrh.entity.EnderChickenEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES
            = DeferredRegister.create(Registries.ENTITY_TYPE, MECRHMod.MOD_ID);

    public static final Supplier<EntityType<EnderChickenEntity>> ENDER_CHICKEN
            = register("ender_chicken", ModEntityTypes::enderChicken);
    public static final Supplier<EntityType<EggBomb>> EGG_BOMB
            = register("egg_bomb", ModEntityTypes::eggBomb);

    private static <E extends Entity> Supplier<EntityType<E>> register(final String name, final Supplier<EntityType.Builder<E>> sup) {
        return ENTITY_TYPES.register(name, () -> sup.get().build(name));
    }

    private static EntityType.Builder<EnderChickenEntity> enderChicken() {
        return EntityType.Builder.of(EnderChickenEntity::new, MobCategory.MONSTER)
                .sized(8F, 8F)
                .eyeHeight(7F)
                .clientTrackingRange(10);
    }

    private static EntityType.Builder<EggBomb> eggBomb() {
        return EntityType.Builder.<EggBomb>of(EggBomb::new, MobCategory.MISC)
                .sized(1F, 1F)
                .eyeHeight(0.5F)
                .clientTrackingRange(10);
    }
}
