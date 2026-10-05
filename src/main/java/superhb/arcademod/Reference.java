package superhb.arcademod;

import net.minecraft.resources.ResourceLocation;

public class Reference {
    public static final String MODID = "arcademod";
    public static final String NAME = "Arcade Reloaded";
    public static final String VERSION = "1.0-1.20.1";
    public static final String DESCRIPTION = "Adds various arcade games to Minecraft";
    public static final String AUTHOR = "Bay4lly (1.20.1 port), SuperHB (1.12.2 original)";

    public static ResourceLocation createResource(String key) {
        return new ResourceLocation(MODID, key);
    }
}
