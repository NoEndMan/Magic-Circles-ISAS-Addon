package net.flameslight.magiccircles.datagen.registery;

import net.flameslight.magiccircles.MagicCircles;
import net.flameslight.magiccircles.datagen.entity.MagicCircleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MagicCircles.MOD_ID);

    public static final RegistryObject<EntityType<MagicCircleEntity>> MAGIC_CIRCLE =
            ENTITY_TYPES.register("magic_circle",
                    () -> EntityType.Builder.<MagicCircleEntity>of(MagicCircleEntity::new, MobCategory.MISC)
                            .sized(0.0f, 0.0f)       // Zero size — no collision
                            .clientTrackingRange(64)
                            .updateInterval(Integer.MAX_VALUE) // Never sync (client only)
                            .build("magic_circle")
            );

    // This variable will hold our permanent reference, surviving the server sync wipe
    public static EntityType<MagicCircleEntity> CACHED_MAGIC_CIRCLE;
}
