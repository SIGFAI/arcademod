package superhb.arcademod.api.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class GuiButtonScalable extends Button {
    private final float scale;

    public GuiButtonScalable(int x, int y, int width, int height, float scale, String text, OnPress onPress) {
        super(x, y, width, height, Component.literal(text), onPress, Button.DEFAULT_NARRATION);
        this.scale = scale;
    }

    public GuiButtonScalable(int x, int y, float scale, String text, OnPress onPress) {
        this(x, y, 200, 20, scale, text, onPress);
    }
}
