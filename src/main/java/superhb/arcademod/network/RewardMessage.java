package superhb.arcademod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.function.Supplier;
import superhb.arcademod.Arcade;

public class RewardMessage {
    private final ItemStack reward;

    public RewardMessage(ItemStack stack) {
        this.reward = stack;
    }

    public static void encode(RewardMessage message, FriendlyByteBuf buf) {
        buf.writeItem(message.reward);
    }

    public static RewardMessage decode(FriendlyByteBuf buf) {
        return new RewardMessage(buf.readItem());
    }

    public static void handle(RewardMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                Arcade.logger.warn("RewardMessage received but sender is null");
                return;
            }
            Arcade.logger.info("RewardMessage: giving " + message.reward + " to " + player.getGameProfile().getName());
            // Match 1.12.2 behaviour: simply add to player inventory (no automatic drop fallback)
            player.getInventory().add(message.reward);
        });
        context.setPacketHandled(true);
    }
}
