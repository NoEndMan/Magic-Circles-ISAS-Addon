package net.flameslight.magiccircles.datagen.types;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntitySnapshot {
    public final float eyeHeight;

    // Position
    public double x;
    public double y;
    public double z;

    public double xo;
    public double yo;
    public double zo;

    public float xRotO;
    public float yRotO;

    // Camera rotations
    public float yRot;      // yaw
    public float xRot;      // pitch

    // Level reference (or just the dimension key if you don't want a hard ref)
    public ResourceKey<Level> levelKey;

    // Timestamp (optional but useful)
    public long gameTick;

    public EntitySnapshot(LivingEntity caster) {
        this.capture(caster);
        this.eyeHeight = caster.getEyeHeight();
    }

    /**
     * Factory method — call this on the client thread when you want to capture
     */
    public void capture(LivingEntity entity) {
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
        this.yRot = entity.getYRot();
        this.xRot = entity.getXRot();
        this.xo = entity.xo;
        this.yo = entity.yo;
        this.zo = entity.zo;
        this.xRotO = entity.xRotO;
        this.yRotO = entity.yRotO;
        this.levelKey = entity.level().dimension();   // ResourceKey<Level>
        this.gameTick = entity.level().getGameTime();
    }
}
