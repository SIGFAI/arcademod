package superhb.arcademod.network.pong;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientPongMessage {
    private final BlockPos pos;
    private final int paddleId;
    private final String playerName;

    public ClientPongMessage(BlockPos pos, int paddleId, String playerName) {
        this.pos = pos;
        this.paddleId = paddleId;
        this.playerName = playerName;
    }

    public static void encode(ClientPongMessage message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        buf.writeInt(message.paddleId);
        buf.writeUtf(message.playerName);
    }

    public static ClientPongMessage decode(FriendlyByteBuf buf) {
        return new ClientPongMessage(buf.readBlockPos(), buf.readInt(), buf.readUtf());
    }

    public static void handle(ClientPongMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Implementation for client-side pong logic
        });
        context.setPacketHandled(true);
    }
}
