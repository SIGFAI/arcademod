package superhb.arcademod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import superhb.arcademod.client.entity.EntityCamera;

public class GuiPusher extends Screen {
    private final Level world;
    private final double x, y, z;
    private final Player player;

    public GuiPusher(Level world, double x, double y, double z, Player player) {
        super(Component.literal("Pusher"));
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.player = player;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        // In 1.20.1, we might need a persistent camera entity or handle this in a tick/event.
        // For now, let's keep it similar to the original.
        if (this.minecraft != null && !(this.minecraft.getCameraEntity() instanceof EntityCamera)) {
             this.minecraft.setCameraEntity(new EntityCamera(world, x, y, z));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            if (this.minecraft != null) {
                this.minecraft.setCameraEntity(player);
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setCameraEntity(player);
        }
        super.onClose();
    }
}
