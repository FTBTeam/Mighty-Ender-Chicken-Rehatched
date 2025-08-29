package dev.ftb.mods.mecrh.net;

import dev.ftb.mods.mecrh.MECRHMod;
import dev.ftb.mods.mecrh.client.MECRHModClient;
import dev.ftb.mods.mecrh.entity.EnderChickenEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BreakEggMessage(int entityId) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, BreakEggMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, BreakEggMessage::entityId,
            BreakEggMessage::new
    );
    public static final Type<BreakEggMessage> TYPE = new Type<>(MECRHMod.id("break_egg"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BreakEggMessage message, IPayloadContext context) {
        if (context.player().level().getEntity(message.entityId) instanceof EnderChickenEntity chicken) {
            MECRHModClient.playEggBreakEffects(chicken);
        }
    }
}
