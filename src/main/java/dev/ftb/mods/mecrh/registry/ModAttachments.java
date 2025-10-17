package dev.ftb.mods.mecrh.registry;

import com.mojang.serialization.Codec;
import dev.ftb.mods.mecrh.MECRHMod;
import dev.ftb.mods.mecrh.util.PreviousLaserDamage;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES
            = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MECRHMod.MOD_ID);

    // ID of ender chicken for spawned minion mobs
    public static final Supplier<AttachmentType<Integer>> CHICKEN_ID = ATTACHMENT_TYPES.register(
            "chicken_id", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build()
    );

    // tracks recent laser damage done to entities
    public static final Supplier<AttachmentType<PreviousLaserDamage>> PREV_LASER_DMG = ATTACHMENT_TYPES.register(
            "prev_laser_dmg", () -> AttachmentType.builder(() -> PreviousLaserDamage.NONE).serialize(PreviousLaserDamage.CODEC).build()
    );
}
