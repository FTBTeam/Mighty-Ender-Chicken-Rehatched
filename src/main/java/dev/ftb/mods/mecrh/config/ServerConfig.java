package dev.ftb.mods.mecrh.config;

import dev.ftb.mods.ftblibrary.snbt.config.*;
import dev.ftb.mods.mecrh.ChickenSkill;
import dev.ftb.mods.mecrh.MECRHMod;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;

public interface ServerConfig {
    String KEY = MECRHMod.MOD_ID + "-server";
    SNBTConfig CONFIG = SNBTConfig.create(MECRHMod.MOD_ID)
            .comment("Server-specific configuration for Mighty Ender Chicken Rehatched",
                    "Modpack defaults should be defined in <instance>/config/" + KEY + ".snbt",
                    "  (may be overwritten on modpack update)",
                    "Server admins may locally override this by copying into <instance>/world/serverconfig/" + KEY + ".snbt",
                    "  (will NOT be overwritten on modpack update)"
            );

    SNBTConfig GENERAL = CONFIG.addGroup("general");

    BooleanValue TARGET_ALL_LIVING = GENERAL.addBoolean("target_all_living", true);
    IntValue MAX_SKILLS_AT_ONCE = GENERAL.addInt("max_skills_at_once", 2, 0, Integer.MAX_VALUE);
    IntValue MAX_INCOMING_DAMAGE = GENERAL.addInt("max_incoming_damage", 25, 1, Integer.MAX_VALUE);
    IntValue ARENA_RADIUS = GENERAL.addInt("arena_radius", 24, 10, Integer.MAX_VALUE);

    SNBTConfig FORCEFIELD = CONFIG.addGroup("forcefield");
    IntValue FORCEFIELD_LEVEL = FORCEFIELD.addInt("forcefield_level", 4, 1, Integer.MAX_VALUE);
    IntValue FORCEFIELD_INTERVAL = FORCEFIELD.addInt("forcefield_interval", 600, 100, Integer.MAX_VALUE);

    SNBTConfig SPIN_CONFIG = CONFIG.addGroup("spin_attack");
    DoubleValue EGG_BOMB_SPEED_MIN = SPIN_CONFIG.addDouble("egg_bomb_speed_min", 0.05, .001, 5.0);
    DoubleValue EGG_BOMB_SPEED_MAX = SPIN_CONFIG.addDouble("egg_bomb_speed_max", 0.25, .001, 5.0);
    DoubleValue EGG_BOMB_EXPLOSION_POWER = SPIN_CONFIG.addDouble("egg_bomb_explosion_power", 3.0, .01, 100.0);
    BooleanValue EGG_BOMB_DAMAGE_TERRAIN = SPIN_CONFIG.addBoolean("egg_bomb_explosion_power", true);
    IntValue SPIN_ATTACK_MIN_TIME = SPIN_CONFIG.addInt("min_interval", 400, 20, Integer.MAX_VALUE);
    IntValue SPIN_ATTACK_MAX_TIME = SPIN_CONFIG.addInt("max_interval", 600, 20, Integer.MAX_VALUE);

    SNBTConfig CHARGE_CONFIG = CONFIG.addGroup("charge_attack");
    IntValue CHARGE_MIN_INTERVAL = CHARGE_CONFIG.addInt("min_interval", 240, 20, Integer.MAX_VALUE);
    IntValue CHARGE_MAX_INTERVAL = CHARGE_CONFIG.addInt("max_interval", 300, 20, Integer.MAX_VALUE);
    IntValue CHARGE_WARMUP_TIME = CHARGE_CONFIG.addInt("charge_warmup_time", 12, 1, 600);
    DoubleValue MAX_CHARGE_DIST = CHARGE_CONFIG.addDouble("max_charge_dist", 16, 5.0, Double.MAX_VALUE);
    IntValue MAX_CHARGE_TIME = CHARGE_CONFIG.addInt("max_charge_time", 100, 20, Integer.MAX_VALUE);
    DoubleValue CHARGE_SPEED = CHARGE_CONFIG.addDouble("charge_speed", 1.0, 0.1, 10.0);
    DoubleValue CHARGE_DAMAGE = CHARGE_CONFIG.addDouble("charge_damage", 15.0, 0.0, Double.MAX_VALUE);
    BooleanValue CHARGE_DROPS_BLOCKS = CHARGE_CONFIG.addBoolean("drop_blocks", false);

    SNBTConfig STAMPEDE_CONFIG = CONFIG.addGroup("stampede_attack");
    IntValue ZOMBIE_COUNT_MIN = STAMPEDE_CONFIG.addInt("zombie_count_min", 6, 1, 64);
    IntValue ZOMBIE_COUNT_MAX = STAMPEDE_CONFIG.addInt("zombie_count_max", 10, 1, 64);
    IntValue STAMPEDE_MIN_TIME = STAMPEDE_CONFIG.addInt("min_interval", 500, 20, Integer.MAX_VALUE);
    IntValue STAMPEDE_MAX_TIME = STAMPEDE_CONFIG.addInt("max_interval", 500, 20, Integer.MAX_VALUE);

    SNBTConfig LASER_CONFIG = CONFIG.addGroup("laser_attack");
    IntValue LASER_MIN_TIME = LASER_CONFIG.addInt("min_interval", 180, 20, Integer.MAX_VALUE);
    IntValue LASER_MAX_TIME = LASER_CONFIG.addInt("max_interval", 360, 20, Integer.MAX_VALUE);
    DoubleValue LASER_DAMAGE = LASER_CONFIG.addDouble("damage", 4.0, 1.0, Double.MAX_VALUE);
    DoubleValue LASER_DAMAGE_INCREASE = LASER_CONFIG.addDouble("damage_increase", 1.25, 1.0, 10.0);
    IntValue LASER_INVULN_TICKS = LASER_CONFIG.addInt("invulnerability_ticks", 10, 1, Integer.MAX_VALUE);
    IntValue LASER_FIRE_TICKS = LASER_CONFIG.addInt("fire_ticks", 60, 0, Integer.MAX_VALUE);

    SNBTConfig SKILL_CONFIG = CONFIG.addGroup("skills");

    StringListValue ENDER_SKILL_SET = CONFIG.addStringList("skill_set", new ArrayList<>(ChickenSkill.skillNames()));
    StringListValue CHAOS_SKILL_SET = CONFIG.addStringList("skill_set", new ArrayList<>(ChickenSkill.skillNames()));

    DoubleValue CLEAR_AREA_CHANCE = SKILL_CONFIG.addDouble("clear_area_chance", 0.05, 0.0, 1.0);
    DoubleValue FLAP_CHANCE = SKILL_CONFIG.addDouble("flap_chance", 0.3, 0.0, 1.0);
    DoubleValue LASER_CHANCE = SKILL_CONFIG.addDouble("laser_chance", 0.015, 0.0, 1.0);
    DoubleValue REGEN_FORCEFIELD_CHANCE = SKILL_CONFIG.addDouble("regen_forcefield_chance", 0.004, 0.0, 1.0);
    IntValue REGEN_FORCEFIELD_COOLDOWN = SKILL_CONFIG.addInt("regen_forcefield_cooldown", 60, 1, Integer.MAX_VALUE);
    DoubleValue STRAFE_CHANCE = SKILL_CONFIG.addDouble("strafe_chance", 0.015, 0.0, 1.0);
    DoubleValue STRAFE_AIMBOT_CHANCE = SKILL_CONFIG.addDouble("strafe_aimbot_chance", 0.6, 0.0, 1.0);
    DoubleValue STRAFE_EGG_CHANCE = SKILL_CONFIG.addDouble("strafe_egg_chance", 0.1, 0.0, 1.0);
    IntValue STRAFE_MIN_TIME = SKILL_CONFIG.addInt("strafe_min_time", 160, 60, Integer.MAX_VALUE);
    IntValue STRAFE_MAX_TIME = SKILL_CONFIG.addInt("strafe_max_time", 240, 60, Integer.MAX_VALUE);
    DoubleValue SPIN_CHANCE = SKILL_CONFIG.addDouble("spin_chance", 0.01, 0.0, 1.0);

    static void onConfigChanged(boolean fromServer) {
    }

    static int getSpinInterval(RandomSource random) {
        return getInterval(SPIN_ATTACK_MIN_TIME.get(), SPIN_ATTACK_MAX_TIME.get(), random);
    }

    static int getChargeInterval(RandomSource random) {
        return getInterval(CHARGE_MIN_INTERVAL.get(), CHARGE_MAX_INTERVAL.get(), random);
    }

    static float getEggSpeed(RandomSource random) {
        return getInterval(EGG_BOMB_SPEED_MIN.get().floatValue(), EGG_BOMB_SPEED_MAX.get().floatValue(), random);
    }

    static int getBabyZombieCount(RandomSource random) {
        return getInterval(ZOMBIE_COUNT_MIN.get(), ZOMBIE_COUNT_MAX.get(), random);
    }

    static int getStampedeInterval(RandomSource random) {
        return getInterval(STAMPEDE_MIN_TIME.get(), STAMPEDE_MAX_TIME.get(), random);
    }

    static int getLaserInterval(RandomSource random) {
        return getInterval(LASER_MIN_TIME.get(), LASER_MAX_TIME.get(), random);
    }

    private static int getInterval(int m1, int m2, RandomSource random) {
        return m1 == m2 ? m1 : Math.min(m1, m2) + random.nextInt(Mth.abs(m2 - m1));
    }

    private static float getInterval(float m1, float m2, RandomSource random) {
        return m1 == m2 ? m1 : Math.min(m1, m2) + random.nextFloat() * Math.abs(m2 - m1);
    }

    static int getArenaRadiusSq() {
        return ARENA_RADIUS.get() * ARENA_RADIUS.get();
    }
}
