package net.flameslight.magiccircles;

import com.mojang.logging.LogUtils;
import net.flameslight.magiccircles.config.ClientConfig;
import net.flameslight.magiccircles.datagen.registery.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(MagicCircles.MOD_ID)
public class MagicCircles
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "magiccircles";
    public static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("removal")
    public MagicCircles() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, ClientConfig.FILE_NAME);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        if(FMLEnvironment.dist == Dist.CLIENT)
            ModEntities.ENTITY_TYPES.register(modBus);
    }
}
