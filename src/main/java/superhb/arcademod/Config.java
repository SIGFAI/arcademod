package superhb.arcademod;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue disableCoins;
    public static final ForgeConfigSpec.BooleanValue requireRedstone;
    public static final ForgeConfigSpec.BooleanValue disableUpdateNotification;

    static {
        BUILDER.push("General");
        
        disableCoins = BUILDER
                .comment("Disable the need to use coins to play the arcade machines")
                .define("disableCoins", false);
                
        requireRedstone = BUILDER
                .comment("Require the machines to be powered by redstone to play")
                .define("requireRedstone", false);
                
        disableUpdateNotification = BUILDER
                .comment("Disable message in chat when update is available")
                .define("disableUpdateNotification", false);
                
        BUILDER.pop();
        
        SPEC = BUILDER.build();
    }
}
