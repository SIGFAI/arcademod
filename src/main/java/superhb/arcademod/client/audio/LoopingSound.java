package superhb.arcademod.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import superhb.arcademod.api.gui.GuiArcade;
import superhb.arcademod.client.tileentity.BlockEntityArcade;

@OnlyIn(Dist.CLIENT)
public class LoopingSound extends AbstractTickableSoundInstance {
    private final BlockEntityArcade tile;

    public LoopingSound(BlockEntityArcade tileEntity, SoundEvent sound, SoundSource category, float volume) {
        super(sound, category, net.minecraft.util.RandomSource.create());
        this.tile = tileEntity;
        this.attenuation = net.minecraft.client.resources.sounds.SoundInstance.Attenuation.NONE;
        this.looping = true;
        this.delay = 0;
        this.volume = volume;
    }

    @Override
    public void tick() {
        if (!(Minecraft.getInstance().screen instanceof GuiArcade)) {
            this.stop();
        }
    }
}
