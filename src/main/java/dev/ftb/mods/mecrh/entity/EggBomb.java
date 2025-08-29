package dev.ftb.mods.mecrh.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EggBomb extends Entity {
    private float scale;
    private LivingEntity target;
    private boolean huntTarget;
    private boolean chaosChicken;

    public EggBomb(EntityType<EggBomb> entityEntityType, Level level) {
        super(entityEntityType, level);
    }

    public void setup(float scale, LivingEntity target, boolean huntTarget, boolean chaosChicken) {
        this.scale = scale;
        this.target = target;
        this.huntTarget = huntTarget;
        this.chaosChicken = chaosChicken;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {

    }
}
