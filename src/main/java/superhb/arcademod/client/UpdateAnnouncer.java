package superhb.arcademod.client;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import superhb.arcademod.Reference;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Reference.MODID)
public class UpdateAnnouncer {
    @SubscribeEvent
    public static void onPlayerJoin(TickEvent.PlayerTickEvent event) {
        // Disabled for now as Forge has its own update checker
    }
}
