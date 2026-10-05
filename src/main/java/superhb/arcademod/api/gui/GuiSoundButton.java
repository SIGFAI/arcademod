package superhb.arcademod.api.gui;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class GuiSoundButton extends GuiButtonScalable {
    private final SoundEvent sound;

    public GuiSoundButton(int x, int y, int width, int height, float scale, String text, SoundEvent sound, OnPress onPress) {
        super(x, y, width, height, scale, text, onPress);
        this.sound = sound;
    }

    public GuiSoundButton(int x, int y, int width, int height, float scale, String text, OnPress onPress) {
        this(x, y, width, height, scale, text, SoundEvents.UI_BUTTON_CLICK.value(), onPress);
    }

    @Override
    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(sound, 1.0F));
    }
}
