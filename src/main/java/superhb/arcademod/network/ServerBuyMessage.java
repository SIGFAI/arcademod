package superhb.arcademod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ServerBuyMessage {
    private final ItemStack stack;
    private final ItemStack currency;
    private final int cost;

    public ServerBuyMessage(ItemStack stack, ItemStack currency, int amount, int cost) {
        this.stack = stack.copy();
        this.stack.setCount(amount);
        this.currency = currency;
        this.cost = cost;
    }

    public static void encode(ServerBuyMessage message, FriendlyByteBuf buf) {
        buf.writeItem(message.stack);
        buf.writeItem(message.currency);
        buf.writeInt(message.cost);
    }

    public static ServerBuyMessage decode(FriendlyByteBuf buf) {
        ItemStack stack = buf.readItem();
        ItemStack currency = buf.readItem();
        int cost = buf.readInt();
        return new ServerBuyMessage(stack, currency, stack.getCount(), cost);
    }

    public static void handle(ServerBuyMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            int totalCurrency = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if (ItemStack.isSameItem(invStack, message.currency)) {
                    totalCurrency += invStack.getCount();
                }
            }

            if (totalCurrency >= message.cost) {
                int remainingToRemove = message.cost;
                for (int i = 0; i < player.getInventory().getContainerSize() && remainingToRemove > 0; i++) {
                    ItemStack invStack = player.getInventory().getItem(i);
                    if (ItemStack.isSameItem(invStack, message.currency)) {
                        int toRemove = Math.min(invStack.getCount(), remainingToRemove);
                        invStack.shrink(toRemove);
                        remainingToRemove -= toRemove;
                    }
                }
                player.getInventory().add(message.stack);
                ArcadePacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBuyMessage(true));
            } else {
                ArcadePacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientBuyMessage(false));
            }
        });
        context.setPacketHandled(true);
    }
}
