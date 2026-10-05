package superhb.arcademod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ServerCoinMessage {
    private final int cost;
    private final ItemStack stack;

    public ServerCoinMessage(ItemStack stack, int cost) {
        this.cost = cost;
        this.stack = stack;
    }

    public static void encode(ServerCoinMessage message, FriendlyByteBuf buf) {
        buf.writeItem(message.stack);
        buf.writeInt(message.cost);
    }

    public static ServerCoinMessage decode(FriendlyByteBuf buf) {
        return new ServerCoinMessage(buf.readItem(), buf.readInt());
    }

    public static void handle(ServerCoinMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            int totalCount = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if (ItemStack.isSameItem(invStack, message.stack)) {
                    totalCount += invStack.getCount();
                }
            }

            if (totalCount >= message.cost) {
                int remainingToRemove = message.cost;
                for (int i = 0; i < player.getInventory().getContainerSize() && remainingToRemove > 0; i++) {
                    ItemStack invStack = player.getInventory().getItem(i);
                    if (ItemStack.isSameItem(invStack, message.stack)) {
                        int toRemove = Math.min(invStack.getCount(), remainingToRemove);
                        invStack.shrink(toRemove);
                        remainingToRemove -= toRemove;
                    }
                }
                ArcadePacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientCoinMessage(true, 0));
            } else {
                ArcadePacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientCoinMessage(false, -1));
            }
        });
        context.setPacketHandled(true);
    }
}