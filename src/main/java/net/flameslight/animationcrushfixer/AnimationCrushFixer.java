package net.flameslight.animationcrushfixer;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(AnimationCrushFixer.MOD_ID)
public class AnimationCrushFixer
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "animationcrushfixer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AnimationCrushFixer() {
    }
}
