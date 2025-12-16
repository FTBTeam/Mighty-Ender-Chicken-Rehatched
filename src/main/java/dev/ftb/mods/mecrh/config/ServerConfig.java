package dev.ftb.mods.mecrh.config;

import dev.ftb.mods.ftblibrary.snbt.config.*;
import dev.ftb.mods.mecrh.MECRHMod;
import io.netty.util.Attribute;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

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

    BooleanValue TARGET_ALL_LIVING = GENERAL.addBoolean("target_all_living", true)
            .comment("If true, Ender Chicken targets all living entities not in the `ftb:ender_chicken_friends` entity tag",
                    "If false, Ender Chicken only targets players, and entities which attack it");
    IntValue MAX_SKILLS_AT_ONCE = GENERAL.addInt("max_skills_at_once", 2, 0, Integer.MAX_VALUE)
            .comment("Max number of special attacks which can be used at one time");
    IntValue MAX_INCOMING_DAMAGE = GENERAL.addInt("max_incoming_damage", 25, 1, Integer.MAX_VALUE)
            .comment("Max amount of incoming damage that the chicken can take in one hit");
    IntValue ARENA_RADIUS = GENERAL.addInt("arena_radius", 24, 10, Integer.MAX_VALUE)
            .comment("The radius of the fight arena: the chicken is spawned in the center of this arena,",
                    "and will stay within the arena (teleporting back to center if it somehow ends up outside)",
                    "Chicken will despawn if no players are in the arena for 30 seconds");
    IntValue DESPAWN_TIME_NO_PLAYERS = GENERAL.addInt("despawn_time_no_players", 30, 0, Integer.MAX_VALUE)
            .comment("Time in seconds after which the chicken will despawn if no players are within the arena",
                    "Set to 0 to disable despawn");
    BooleanValue DROP_BROKEN_BLOCKS = GENERAL.addBoolean("drop_blocks", false)
            .comment("If true, any blocks broken by the chicken will drop items as normal");
    IntValue ZOMBIE_RIDER_SPAWN_HEIGHT = GENERAL.addInt("zombie_rider_spawn_height", 10, 0, 50)
            .comment("Height in blocks above chicken's foot position that the zombie rider initially spawns");
    BooleanValue ONLY_PLAYERS_CAN_HURT_CHICKEN = GENERAL.addBoolean("only_players_can_hurt_chicken", true)
            .comment("If true, only players can hurt the chicken");
    BooleanValue FAKE_PLAYERS_CAN_HURT_CHICKEN = GENERAL.addBoolean("fake_players_can_hurt_chicken", false)
            .comment("If true, fake player entities can hurt the chicken");

    SNBTConfig FORCEFIELD = CONFIG.addGroup("forcefield");
    IntValue FORCEFIELD_LEVEL = FORCEFIELD.addInt("forcefield_level", 4, 1, Integer.MAX_VALUE)
            .comment("Number of hits with an item in the `ftb:chicken_stick` item tag to break the chicken's forcefield");
    IntValue FORCEFIELD_INTERVAL = FORCEFIELD.addInt("forcefield_interval", 600, 100, Integer.MAX_VALUE)
            .comment("Number of ticks after breaking the chicken's forcefield that it comes back up");
    BooleanValue NON_PLAYERS_IGNORE_SHIELD = FORCEFIELD.addBoolean("non_players_ignore_shield", false)
            .comment("If true, non-player damage sources ignore the chicken shield",
                    "Intended for testing; normally leave this as false.");

    SNBTConfig SPIN_CONFIG = CONFIG.addGroup("spin_attack");
    IntValue SPIN_ATTACK_MIN_TIME = SPIN_CONFIG.addInt("min_interval", 400, 20, Integer.MAX_VALUE)
            .comment("Minimum interval in ticks for the spin/egg-bomb attack");
    IntValue SPIN_ATTACK_MAX_TIME = SPIN_CONFIG.addInt("max_interval", 600, 20, Integer.MAX_VALUE)
            .comment("Maximum interval in ticks for the spin/egg-bomb attack");
    DoubleValue EGG_BOMB_SPEED_MIN = SPIN_CONFIG.addDouble("egg_bomb_speed_min", 0.05, .001, 5.0)
            .comment("Minimum speed for launched egg bombs");
    DoubleValue EGG_BOMB_SPEED_MAX = SPIN_CONFIG.addDouble("egg_bomb_speed_max", 0.25, .001, 5.0)
            .comment("Maximum speed for launched egg bombs");
    DoubleValue EGG_BOMB_EXPLOSION_POWER = SPIN_CONFIG.addDouble("egg_bomb_explosion_power", 2.0, .01, 100.0)
            .comment("Egg bomb explosion power (2 = creeper, 4 = TNT");
    BooleanValue EGG_BOMB_DAMAGE_TERRAIN = SPIN_CONFIG.addBoolean("egg_bomb_damage_terrain", true)
            .comment("Should egg bomb explosions damage terrain?");

    SNBTConfig CHARGE_CONFIG = CONFIG.addGroup("charge_attack");
    IntValue CHARGE_MIN_INTERVAL = CHARGE_CONFIG.addInt("min_interval", 240, 20, Integer.MAX_VALUE)
            .comment("Minimum interval in ticks for the charge attack");
    IntValue CHARGE_MAX_INTERVAL = CHARGE_CONFIG.addInt("max_interval", 300, 20, Integer.MAX_VALUE)
            .comment("Minimum interval in ticks for the charge attack");
    IntValue CHARGE_WARMUP_TIME = CHARGE_CONFIG.addInt("charge_warmup_time", 12, 1, 600)
            .comment("Warmup/warning time for the charge attack");
    DoubleValue MAX_CHARGE_DIST = CHARGE_CONFIG.addDouble("max_charge_dist", 16, 5.0, Double.MAX_VALUE)
            .comment("Max distance in blocks that the chicken will charge");
    IntValue MAX_CHARGE_TIME = CHARGE_CONFIG.addInt("max_charge_time", 100, 20, Integer.MAX_VALUE)
            .comment("Max time in ticks that the chicken will spend charging");
    DoubleValue CHARGE_SPEED = CHARGE_CONFIG.addDouble("charge_speed", 1.0, 0.1, 10.0)
            .comment("Speed multiplier for the charge attack");
    DoubleValue CHARGE_DAMAGE = CHARGE_CONFIG.addDouble("charge_damage", 15.0, 0.0, Double.MAX_VALUE)
            .comment("Damage the chicken will do to entities hit by the charge attack");

    SNBTConfig STAMPEDE_CONFIG = CONFIG.addGroup("stampede_attack");
    IntValue STAMPEDE_MIN_INTERVAL = STAMPEDE_CONFIG.addInt("min_interval", 500, 20, Integer.MAX_VALUE)
            .comment("Minimum interval in ticks for the stampede/zombie spawn attack");
    IntValue STAMPEDE_MAX_INTERVAL = STAMPEDE_CONFIG.addInt("max_interval", 500, 20, Integer.MAX_VALUE)
            .comment("Maximum interval in ticks for the stampede/zombie spawn attack");
    IntValue ZOMBIE_COUNT_MIN = STAMPEDE_CONFIG.addInt("zombie_count_min", 6, 1, 64)
            .comment("Minimum number of baby zombies spawned in the stampede");
    IntValue ZOMBIE_COUNT_MAX = STAMPEDE_CONFIG.addInt("zombie_count_max", 10, 1, 64)
            .comment("Maximum number of baby zombies spawned in the stampede");

    SNBTConfig LASER_CONFIG = CONFIG.addGroup("laser_attack");
    IntValue LASER_MIN_INTERVAL = LASER_CONFIG.addInt("min_interval", 180, 20, Integer.MAX_VALUE)
            .comment("Minimum interval in ticks for the laser attack");
    IntValue LASER_MAX_INTERVAL = LASER_CONFIG.addInt("max_interval", 360, 20, Integer.MAX_VALUE)
            .comment("Maximum interval in ticks for the laser attack");
    DoubleValue LASER_DAMAGE = LASER_CONFIG.addDouble("damage", 4.0, 1.0, Double.MAX_VALUE)
            .comment("Damage dealt by the laser to entities it hits (ignores armor)");
    DoubleValue LASER_DAMAGE_INCREASE = LASER_CONFIG.addDouble("damage_increase", 1.25, 1.0, 10.0)
            .comment("Damage increase (multiplicative) for the laser every second an entity remains in the laser beam");
    IntValue LASER_INVULN_TICKS = LASER_CONFIG.addInt("invulnerability_ticks", 10, 1, Integer.MAX_VALUE)
            .comment("Invulnerability ticks (iframes) for entities hit by the laser");
    IntValue LASER_FIRE_TICKS = LASER_CONFIG.addInt("fire_ticks", 60, 0, Integer.MAX_VALUE)
            .comment("Number of ticks the laser will set entities on fire for");

    SNBTConfig PECK_OF_DOOM_CONFIG = CONFIG.addGroup("peck_of_doom");
    IntValue PECK_MIN_INTERVAL = PECK_OF_DOOM_CONFIG.addInt("min_interval", 600, 20, Integer.MAX_VALUE)
            .comment("Minimum interval in ticks for the peck of doom (leap/drop) attack");
    IntValue PECK_MAX_INTERVAL = PECK_OF_DOOM_CONFIG.addInt("max_interval", 600, 20, Integer.MAX_VALUE)
            .comment("Maximum interval in ticks for the peck of doom (leap/drop) attack");
    DoubleValue DIST_ABOVE_TARGET = PECK_OF_DOOM_CONFIG.addDouble("distance_above_target", 6.0, 1.0, Double.MAX_VALUE)
            .comment("Number of blocks above target the chicken will fly to");
    IntValue MAX_PECK_CHARGE_TIME = PECK_OF_DOOM_CONFIG.addInt("max_charge_time", 100, 20, Integer.MAX_VALUE)
            .comment("Maximum time in ticks the chicken will spend trying to reach the target");
    IntValue PECK_DROP_TIME = PECK_OF_DOOM_CONFIG.addInt("drop_time", 20, 5, Integer.MAX_VALUE)
            .comment("Maximum time the chicken will take to drop onto the target");
    IntValue PECK_WARMUP_TIME = PECK_OF_DOOM_CONFIG.addInt("warmup_time", 12, 1, 600)
            .comment("Warmup/warning time for the peck of doom attack");

    SNBTConfig CLUCKSTORM_CONFIG = CONFIG.addGroup("cluckstorm");
    IntValue STRAY_COUNT = CLUCKSTORM_CONFIG.addInt("stray_count", 6, 1, 64)
            .comment("Number of chicken-riding strays which are spawned in");

    static void onConfigChanged(boolean ignoredFromServer) {
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
        return getInterval(STAMPEDE_MIN_INTERVAL.get(), STAMPEDE_MAX_INTERVAL.get(), random);
    }

    static int getLaserInterval(RandomSource random) {
        return getInterval(LASER_MIN_INTERVAL.get(), LASER_MAX_INTERVAL.get(), random);
    }

    static int getPeckInterval(RandomSource random) {
        return getInterval(PECK_MIN_INTERVAL.get(), PECK_MAX_INTERVAL.get(), random);
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
