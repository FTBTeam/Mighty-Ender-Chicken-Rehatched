package dev.ftb.mods.mecrh.net;

import dev.ftb.mods.mecrh.MECRHMod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class MightyChickenNet {
    private static final String NETWORK_VERSION = "1";

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MECRHMod.MOD_ID)
                .versioned(NETWORK_VERSION);

        registrar.playToClient(BreakEggMessage.TYPE, BreakEggMessage.STREAM_CODEC, BreakEggMessage::handle);
    }
}
