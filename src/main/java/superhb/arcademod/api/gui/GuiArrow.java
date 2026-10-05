package superhb.arcademod.api.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import superhb.arcademod.Reference;

public class GuiArrow extends Button {
    private final int type;
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/button_arrows.png");

    private static final int LARGE_X = 14;
    private static final int LARGE_Y = 22;
    private static final int SMALL_HORIZONTAL_X = 7;
    private static final int SMALL_HORIZONTAL_Y = 11;
    private static final int SMALL_VERTICAL_X = 11;
    private static final int SMALL_VERTICAL_Y = 7;

    public GuiArrow(int x, int y, int width, int height, int type, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.type = type;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
            int hoverState = this.isHoveredOrFocused() ? 2 : 1;
            if (!this.active) hoverState = 0;

            // The button_arrows texture is a 256x256 atlas; use correct atlas size so UVs map correctly.
            final int atlasW = 256;
            final int atlasH = 256;
            switch (type) {
                case 0: // Large Right
                    guiGraphics.blit(TEXTURE, getX(), getY(), 0, (hoverState == 2 ? LARGE_Y : 0), LARGE_X, LARGE_Y, atlasW, atlasH);
                    break;
                case 1: // Large Left
                    guiGraphics.blit(TEXTURE, getX(), getY(), LARGE_X, (hoverState == 2 ? LARGE_Y : 0), LARGE_X, LARGE_Y, atlasW, atlasH);
                    break;
                case 2: // Small Right
                    guiGraphics.blit(TEXTURE, getX(), getY(), (LARGE_X * 2), (hoverState == 2 ? SMALL_HORIZONTAL_Y : 0), SMALL_HORIZONTAL_X, SMALL_HORIZONTAL_Y, atlasW, atlasH);
                    break;
                case 3: // Small Left
                    guiGraphics.blit(TEXTURE, getX(), getY(), (LARGE_X * 2) + SMALL_HORIZONTAL_X, (hoverState == 2 ? SMALL_HORIZONTAL_Y : 0), SMALL_HORIZONTAL_X, SMALL_HORIZONTAL_Y, atlasW, atlasH);
                    break;
                case 4: // Small Down
                    guiGraphics.blit(TEXTURE, getX(), getY(), (LARGE_X * 2) + (SMALL_HORIZONTAL_X * 2), (hoverState == 2 ? SMALL_VERTICAL_Y : 0), SMALL_VERTICAL_X, SMALL_VERTICAL_Y, atlasW, atlasH);
                    break;
                case 5: // Small Up
                    guiGraphics.blit(TEXTURE, getX(), getY(), (LARGE_X * 2) + (SMALL_HORIZONTAL_X * 2) + SMALL_VERTICAL_X, (hoverState == 2 ? SMALL_VERTICAL_Y : 0), SMALL_VERTICAL_X, SMALL_VERTICAL_Y, atlasW, atlasH);
                    break;
            }
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
