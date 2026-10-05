package superhb.arcademod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import superhb.arcademod.api.gui.GuiArcade;

import java.util.function.Supplier;

public class ClientCoinMessage {
    private final boolean enoughCoins;
    private final int menu;

    public ClientCoinMessage(boolean enoughCoins, int menu) {
        this.enoughCoins = enoughCoins;
        this.menu = menu;
    }

    public static void encode(ClientCoinMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.menu);
        buf.writeBoolean(message.enoughCoins);
    }

    public static ClientCoinMessage decode(FriendlyByteBuf buf) {
        int menu = buf.readInt();
        boolean enough = buf.readBoolean();
        return new ClientCoinMessage(enough, menu);
    }

    public static void handle(ClientCoinMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof GuiArcade arcade) {
                if (message.menu == -1) {
                    arcade.isEnoughCoins(message.enoughCoins);
                } else {
                    arcade.menu = message.menu;
                }
            }
        });
        context.setPacketHandled(true);
    }
}
