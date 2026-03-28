package net.flameslight.magiccircles;

import com.mojang.logging.LogUtils;
import net.flameslight.magiccircles.datagen.logger.ModLogger;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(MagicCircles.MOD_ID)
public class MagicCircles
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "magiccircles";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MagicCircles() {
    }
}
