package superhb.arcademod.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import superhb.arcademod.Reference;
import superhb.arcademod.init.ModRegistries;

@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModRegistries.ARCADE_MACHINE_ITEM.get(), new ResourceLocation(Reference.MODID, "game"), (stack, level, entity, seed) -> {
                return stack.hasTag() ? (float)stack.getTag().getInt("Game") : 0.0F;
            });
            ItemProperties.register(ModRegistries.PLUSHIE_ITEM.get(), new ResourceLocation(Reference.MODID, "mob"), (stack, level, entity, seed) -> {
                return stack.hasTag() ? (float)stack.getTag().getInt("Mob") : 0.0F;
            });
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register entity renderers if needed
    }
}
