package net.flameslight.magiccircles;

import com.mojang.logging.LogUtils;
import net.flameslight.magiccircles.config.ClientConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
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
    }
}
