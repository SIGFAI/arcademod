package superhb.arcademod.util;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import org.lwjgl.glfw.GLFW;
import superhb.arcademod.Reference;

@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyHandler {
    public static KeyMapping up, down, left, right, select, jump;

    static {
        up = new KeyMapping("control.arcademod:up.name", GLFW.GLFW_KEY_UP, "mod.arcademod:name.locale");
        down = new KeyMapping("control.arcademod:down.name", GLFW.GLFW_KEY_DOWN, "mod.arcademod:name.locale");
        left = new KeyMapping("control.arcademod:left.name", GLFW.GLFW_KEY_LEFT, "mod.arcademod:name.locale");
        right = new KeyMapping("control.arcademod:right.name", GLFW.GLFW_KEY_RIGHT, "mod.arcademod:name.locale");
        select = new KeyMapping("control.arcademod:select.name", GLFW.GLFW_KEY_ENTER, "mod.arcademod:name.locale");
        jump = new KeyMapping("control.arcademod:jump.name", GLFW.GLFW_KEY_SPACE, "mod.arcademod:name.locale");
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(up);
        event.register(down);
        event.register(left);
        event.register(right);
        event.register(select);
        event.register(jump);
    }
}
