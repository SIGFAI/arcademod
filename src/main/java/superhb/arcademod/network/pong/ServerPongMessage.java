package superhb.arcademod.network.pong;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerPongMessage {
    private final BlockPos pos;
    private final int paddleId;
    private final String playerName;

    public ServerPongMessage(BlockPos pos, int paddleId, String playerName) {
        this.pos = pos;
        this.paddleId = paddleId;
        this.playerName = playerName;
    }

    public static void encode(ServerPongMessage message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        buf.writeInt(message.paddleId);
        buf.writeUtf(message.playerName);
    }

    public static ServerPongMessage decode(FriendlyByteBuf buf) {
        return new ServerPongMessage(buf.readBlockPos(), buf.readInt(), buf.readUtf());
    }

    public static void handle(ServerPongMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // Implementation for server-side pong logic
            }
        });
        context.setPacketHandled(true);
    }
}
