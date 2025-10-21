package dev.ftb.mods.mecrh.event;

import dev.ftb.mods.mecrh.entity.EnderChicken;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class EnderChickenEvent extends LivingEvent {
    private final Phase phase;

    public EnderChickenEvent(LivingEntity entity, Phase phase) {
        super(entity);
        this.phase = phase;
    }

    public EnderChicken getChicken() {
        return (EnderChicken) getEntity();
    }

    public Phase getPhase() {
        return phase;
    }

    public enum Phase {
        SHIELDED_ENTRY("shielded_entry"),
        VULNERABLE_ASSAULT("vulnerable_assault"),
        SHIELD_CYCLE("shield_cycle"),
        STAMPEDE_THRESHOLD("stampede_threshold"),
        CLUCKSTORM("cluckstorm_cataclysm"),
        ENRAGE("enrage_final"),
        DEATH_SEQUENCE("death_sequence"),
        ;

        private final String id;

        Phase(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }
}
