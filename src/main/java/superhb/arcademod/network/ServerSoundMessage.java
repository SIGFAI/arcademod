package superhb.arcademod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ServerSoundMessage {
    private final String soundName;
    private final BlockPos pos;
    private final float volume;
    private final boolean loop;
    private final boolean play;

    public ServerSoundMessage(ResourceLocation resource, BlockPos pos, float volume, boolean loop, boolean play) {
        this.soundName = resource.getPath();
        this.pos = pos;
        this.volume = volume;
        this.loop = loop;
        this.play = play;
    }

    public ServerSoundMessage(String name, BlockPos pos, float volume, boolean loop, boolean play) {
        this.soundName = name;
        this.pos = pos;
        this.volume = volume;
        this.loop = loop;
        this.play = play;
    }

    public static void encode(ServerSoundMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.soundName);
        buf.writeBlockPos(message.pos);
        buf.writeFloat(message.volume);
        buf.writeBoolean(message.loop);
        buf.writeBoolean(message.play);
    }

    public static ServerSoundMessage decode(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        BlockPos pos = buf.readBlockPos();
        float volume = buf.readFloat();
        boolean loop = buf.readBoolean();
        boolean play = buf.readBoolean();
        return new ServerSoundMessage(name, pos, volume, loop, play);
    }

    public static void handle(ServerSoundMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // Broadcast to nearby players
                ArcadePacketHandler.INSTANCE.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(message.pos.getX(), message.pos.getY(), message.pos.getZ(), 64.0D, player.level().dimension())), 
                    new ClientSoundMessage(message.soundName, message.pos, message.volume, message.loop, message.play));
            }
        });
        context.setPacketHandled(true);
    }
}
