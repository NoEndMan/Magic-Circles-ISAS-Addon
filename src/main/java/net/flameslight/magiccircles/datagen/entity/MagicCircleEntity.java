package net.flameslight.magiccircles.datagen.entity;

import net.flameslight.magiccircles.datagen.logger.ModLogger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class MagicCircleEntity extends Entity {
    private LivingEntity caster;

    public MagicCircleEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true; // Don't cull based on bounding box
        this.noPhysics = true;
    }

    @Override
    public void tick() {
        // Don't call super.tick() movement logic
        // Position is managed externally
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public boolean shouldBeSaved() {
        return false; // Never write this entity to disk
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distSq) {
        // Always pass — noCulling handles frustum, and we manage our own lifecycle
        return true;
    }

    // Client-only entity — no server packet needed
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        // This should never be called since entity is client-only
        ModLogger.error("MagicCircleEntity is client-side only");
        throw new UnsupportedOperationException();
    }

    public LivingEntity getCaster() {
        return this.caster;
    }

    public void setCaster(LivingEntity caster) {
        this.caster = caster;
    }
}
