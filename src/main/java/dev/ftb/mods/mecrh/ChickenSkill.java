package dev.ftb.mods.mecrh;

import dev.ftb.mods.mecrh.entity.EnderChicken;
import dev.ftb.mods.mecrh.entity.ai.*;
import net.minecraft.Util;
import net.minecraft.world.entity.ai.goal.Goal;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public enum ChickenSkill {
    CHARGE("charge", ChickenChargeGoal::new),
    CLEAR_AREA("clear_area", ClearAreaGoal::new),
    CLEAR_ENTITIES("clear_entities", ClearSurroundingEntitiesGoal::new),
    LASER("laser", ChickenLaserGoal::new),
    STRAFE("strafe", ChickenStrafingGoal::new),
    SPIN("spin", ChickenSpinGoal::new);

    private static final Lazy<Map<String,ChickenSkill>> SKILL_NAMES
            = Lazy.of(() -> Util.make(new HashMap<>(), map -> { for (var s : values()) map.put(s.name(), s);} ));

    private final String name;
    private final Function<EnderChicken, Goal> factory;

    ChickenSkill(String name, Function<EnderChicken, Goal> factory) {
        this.name = name;
        this.factory = factory;
    }

    public String getName() {
        return name;
    }

    public Goal createGoal(EnderChicken chicken) {
        return factory.apply(chicken);
    }

    public static Collection<String> skillNames() {
        return SKILL_NAMES.get().keySet();
    }

    public static Optional<ChickenSkill> forName(String name) {
        return Optional.ofNullable(SKILL_NAMES.get().get(name));
    }
}
