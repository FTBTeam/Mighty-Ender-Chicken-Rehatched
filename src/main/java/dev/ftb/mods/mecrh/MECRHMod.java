package dev.ftb.mods.mecrh;

import dev.ftb.mods.mecrh.net.MightyChickenNet;
import dev.ftb.mods.mecrh.registry.ModSounds;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MECRHMod.MOD_ID)
public class MECRHMod {
    public static final String MOD_ID = "mecrh";

    private static final Logger LOGGER = LoggerFactory.getLogger(MECRHMod.class);

    public MECRHMod(IEventBus eventBus, ModContainer container) {
        eventBus.addListener(MightyChickenNet::register);

        ModSounds.SOUNDS.register(eventBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
