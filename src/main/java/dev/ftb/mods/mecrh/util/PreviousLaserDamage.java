package dev.ftb.mods.mecrh.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.mecrh.registry.ModAttachments;
import net.minecraft.world.entity.Entity;

public record PreviousLaserDamage(long when, float damage) {
    public static final Codec<PreviousLaserDamage> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.LONG.fieldOf("when").forGetter(PreviousLaserDamage::when),
            Codec.FLOAT.fieldOf("damage").forGetter(PreviousLaserDamage::damage)
    ).apply(builder, PreviousLaserDamage::new));

    public static final PreviousLaserDamage NONE = new PreviousLaserDamage(0L, 0F);

    public static PreviousLaserDamage forEntity(Entity entity) {
        return entity.hasData(ModAttachments.PREV_LASER_DMG) ? entity.getData(ModAttachments.PREV_LASER_DMG) : NONE;
    }

    public static void setPreviousLaserDamage(Entity entity, float amount) {
        entity.setData(ModAttachments.PREV_LASER_DMG, new PreviousLaserDamage(entity.level().getGameTime(), amount));
    }
}
