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
import dev.ftb.mods.mecrh.util.ChickenUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

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
        NeoForge.EVENT_BUS.addListener(this::onTarget);
        NeoForge.EVENT_BUS.addListener(this::onMobDamage);
        NeoForge.EVENT_BUS.addListener(this::onMobDie);

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

    private void onTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (event.getEntity() instanceof EnderChicken && newTarget != null && ChickenUtils.hasChickenBoss(newTarget)
                || ChickenUtils.hasChickenBoss(event.getEntity()) && newTarget instanceof EnderChicken)
        {
            event.setCanceled(true);
        }
    }

    private void onMobDamage(LivingDamageEvent.Post event) {
        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity instanceof LivingEntity && ChickenUtils.hasChickenBoss(sourceEntity)) {
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
        }
    }

    private void onMobDie(LivingDeathEvent event) {
        if (event.getEntity() instanceof LivingEntity living && ChickenUtils.hasChickenBoss(event.getEntity())) {
            AreaEffectCloud cloud = new AreaEffectCloud(living.level(), living.getX(), living.getY(), living.getZ());
            cloud.setPotionContents(new PotionContents(Optional.of(Potions.STRONG_HARMING), Optional.of(0xFF00C0FF), List.of()));
            cloud.setOwner(living);
            cloud.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
            cloud.setRadius(2.0f);
            cloud.setDuration(100);
            cloud.setRadiusOnUse(-0.5f);
            cloud.setWaitTime(20);
            cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
            event.getEntity().level().addFreshEntity(cloud);
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
