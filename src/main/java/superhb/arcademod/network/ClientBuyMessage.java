package superhb.arcademod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import superhb.arcademod.client.gui.GuiPrize;

import java.util.function.Supplier;

public class ClientBuyMessage {
    private final boolean isEnough;

    public ClientBuyMessage(boolean isEnough) {
        this.isEnough = isEnough;
    }

    public static void encode(ClientBuyMessage message, FriendlyByteBuf buf) {
        buf.writeBoolean(message.isEnough);
    }

    public static ClientBuyMessage decode(FriendlyByteBuf buf) {
        return new ClientBuyMessage(buf.readBoolean());
    }

    public static void handle(ClientBuyMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof GuiPrize prize) {
                prize.isEnough(message.isEnough);
            }
        });
        context.setPacketHandled(true);
    }
}
