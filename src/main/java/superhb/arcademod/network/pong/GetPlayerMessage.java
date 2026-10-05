package superhb.arcademod.network.pong;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import superhb.arcademod.client.gui.GuiPong;

import java.util.function.Supplier;

public class GetPlayerMessage {
    private final BlockPos pos;

    public GetPlayerMessage(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(GetPlayerMessage message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
    }

    public static GetPlayerMessage decode(FriendlyByteBuf buf) {
        return new GetPlayerMessage(buf.readBlockPos());
    }

    public static void handle(GetPlayerMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof GuiPong) {
                // Implementation details for player fetching
            }
        });
        context.setPacketHandled(true);
    }
}
