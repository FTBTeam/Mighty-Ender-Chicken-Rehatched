package dev.ftb.mods.mecrh.config;

import dev.ftb.mods.ftblibrary.snbt.config.*;
import dev.ftb.mods.mecrh.ChickenSkill;
import dev.ftb.mods.mecrh.MECRHMod;

import java.util.ArrayList;

public interface ServerConfig {
    String KEY = MECRHMod.MOD_ID + "-server";
    SNBTConfig CONFIG = SNBTConfig.create(MECRHMod.MOD_ID);

    SNBTConfig GENERAL = CONFIG.addGroup("general")
            .comment("Server-specific configuration for Mighty Ender Chicken Rehatched",
                    "Modpack defaults should be defined in <instance>/config/" + KEY + ".snbt",
                    "  (may be overwritten on modpack update)",
                    "Server admins may locally override this by copying into <instance>/world/serverconfig/" + KEY + ".snbt",
                    "  (will NOT be overwritten on modpack update)"
            );

    BooleanValue TARGET_ALL_LIVING = GENERAL.addBoolean("target_all_living", true);
    IntValue MAX_SKILLS_AT_ONCE = GENERAL.addInt("max_skills_at_once", 2, 0, Integer.MAX_VALUE);
    DoubleValue CHAOS_CHICKEN_SCALE = GENERAL.addDouble("ender_max_skills_at_once", 10.0, 0.1, 20.0);
    DoubleValue ENDER_CHICKEN_SCALE = GENERAL.addDouble("chaos_max_skills_at_once", 14.0, 0.1, 20.0);
    DoubleValue CHAOS_CHICKEN_DAMAGE_MULT = GENERAL.addDouble("chaos_chicken_damage_mult", 4.0, 1.0, Double.MAX_VALUE);
    DoubleValue CHAOS_SKILL_CHANCE_MULT = GENERAL.addDouble("chaos_chicken_skill_chance_mult", 1.2, 1.0, Double.MAX_VALUE);
    IntValue EGG_BREAKS_REQUIRED = GENERAL.addInt("egg_breaks_required", 4, 1, 10);

    SNBTConfig SKILL_CONFIG = CONFIG.addGroup("skills");

    StringListValue ENDER_SKILL_SET = CONFIG.addStringList("skill_set", new ArrayList<>(ChickenSkill.skillNames()));
    StringListValue CHAOS_SKILL_SET = CONFIG.addStringList("skill_set", new ArrayList<>(ChickenSkill.skillNames()));

    DoubleValue CHARGE_CHANCE = SKILL_CONFIG.addDouble("charge_chance", 0.015, 0.0, 1.0);
    DoubleValue CHARGE_CANCEL_DIST = SKILL_CONFIG.addDouble("charge_cancel_dist", 64.0, 5.0, Double.MAX_VALUE);
    IntValue CHARGE_WARN_TIME = SKILL_CONFIG.addInt("charge_warn_time", 65, 10, 600);
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
}
