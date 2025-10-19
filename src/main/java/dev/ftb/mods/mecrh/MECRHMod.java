package dev.ftb.mods.mecrh;

import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.mecrh.config.ServerConfig;
import dev.ftb.mods.mecrh.datagen.DataGenerators;
import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.net.MightyChickenNet;
import dev.ftb.mods.mecrh.registry.ModAttachments;
import dev.ftb.mods.mecrh.registry.ModEntityTypes;
import dev.ftb.mods.mecrh.registry.ModItems;
import dev.ftb.mods.mecrh.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Mod(MECRHMod.MOD_ID)
public class MECRHMod {
    public static final String MOD_ID = "mecrh";

    private static final Logger LOGGER = LoggerFactory.getLogger(MECRHMod.class);

    public MECRHMod(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(MightyChickenNet::register);
        modEventBus.addListener(DataGenerators::gatherData);
        modEventBus.addListener(this::registerEntityAttributes);
        modEventBus.addListener(this::addSpawnEggsToCreativeTab);

        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::onEntityDamage);
        NeoForge.EVENT_BUS.addListener(this::handleExplosion);

        ConfigManager.getInstance().registerServerConfig(ServerConfig.CONFIG, MOD_ID + ".server_config", false, ServerConfig::onConfigChanged);
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.ENDER_CHICKEN.get(), EnderChicken.createAttributes().build());
    }

    private void addSpawnEggsToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            ModItems.ITEMS.getEntries().forEach(entry -> {
                if (entry.get() instanceof SpawnEggItem egg) {
                    event.accept(egg);
                }
            });
        }
    }

    private void onEntityDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().is(ChickenDamageTypes.LASER)) {
            event.setInvulnerabilityTicks(ServerConfig.LASER_INVULN_TICKS.get());
        }
    }

    private void handleExplosion(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getDirectSourceEntity() instanceof EnderChicken) {
            List<BlockPos> affectedBlocks = event.getAffectedBlocks();
            affectedBlocks.removeIf(pos -> event.getLevel().getBlockState(pos).is(MECRHTags.Blocks.CHICKEN_UNBREAKABLE));
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
