package superhb.arcademod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import superhb.arcademod.Reference;

import java.util.function.Supplier;

public class ClientSoundMessage {
    private final String soundName;
    private final BlockPos pos;
    private final float volume;
    private final boolean loop;
    private final boolean play;

    public ClientSoundMessage(String soundName, BlockPos pos, float volume, boolean loop, boolean play) {
        this.soundName = soundName;
        this.pos = pos;
        this.volume = volume;
        this.loop = loop;
        this.play = play;
    }

    public static void encode(ClientSoundMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.soundName);
        buf.writeBlockPos(message.pos);
        buf.writeFloat(message.volume);
        buf.writeBoolean(message.loop);
        buf.writeBoolean(message.play);
    }

    public static ClientSoundMessage decode(FriendlyByteBuf buf) {
        return new ClientSoundMessage(buf.readUtf(), buf.readBlockPos(), buf.readFloat(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(ClientSoundMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (message.play) {
                ResourceLocation rl = new ResourceLocation(Reference.MODID, message.soundName);
                SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(rl);
                if (event != null) {
                    Minecraft.getInstance().level.playLocalSound(message.pos.getX(), message.pos.getY(), message.pos.getZ(), event, SoundSource.BLOCKS, message.volume, 1.0f, false);
                }
            }
            // Handling looping sounds would require a more complex system with a map of active sounds.
            // For now, let's keep it simple.
        });
        context.setPacketHandled(true);
    }
}
