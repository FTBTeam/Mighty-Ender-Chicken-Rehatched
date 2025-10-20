package dev.ftb.mods.mecrh.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.entity.PartEntity;

import javax.annotation.Nullable;

public class EnderChickenPart extends PartEntity<EnderChicken> {
    private final EntityDimensions size;
    private final PartType type;

    public EnderChickenPart(EnderChicken parent, PartType type, float width, float height) {
        super(parent);
        this.type = type;

        size = EntityDimensions.scalable(width, height);
        noPhysics = true;
        refreshDimensions();
    }

    public PartType getPartType() {
        return type;
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

    @Override
    public boolean isPickable() {
        return true;
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return getParent().getPickResult();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return source.getEntity() != getParent() && !this.isInvulnerableTo(source) && getParent().attackFromPart(source, this, amount);
    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || getParent() == entity;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return size;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    public enum PartType {
        FOOT_L,
        FOOT_R,
        LEG_L,
        LEG_R,
        BODY,
        WING_L,
        WING_R,
        HEAD,
        BILL,
    }

    public AABB getBlockDestructionAABB() {
        return getBoundingBox().expandTowards(getParent().getDeltaMovement()).inflate(getBbWidth() / 3.0);
    }
}
