package superhb.arcademod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import superhb.arcademod.Reference;

public class ArcadePacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Reference.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(packetId++, ServerCoinMessage.class, ServerCoinMessage::encode, ServerCoinMessage::decode, ServerCoinMessage::handle);
        INSTANCE.registerMessage(packetId++, ClientCoinMessage.class, ClientCoinMessage::encode, ClientCoinMessage::decode, ClientCoinMessage::handle);
        INSTANCE.registerMessage(packetId++, RewardMessage.class, RewardMessage::encode, RewardMessage::decode, RewardMessage::handle);
        INSTANCE.registerMessage(packetId++, ServerBuyMessage.class, ServerBuyMessage::encode, ServerBuyMessage::decode, ServerBuyMessage::handle);
        INSTANCE.registerMessage(packetId++, ClientBuyMessage.class, ClientBuyMessage::encode, ClientBuyMessage::decode, ClientBuyMessage::handle);
        INSTANCE.registerMessage(packetId++, ServerSoundMessage.class, ServerSoundMessage::encode, ServerSoundMessage::decode, ServerSoundMessage::handle);
        INSTANCE.registerMessage(packetId++, ClientSoundMessage.class, ClientSoundMessage::encode, ClientSoundMessage::decode, ClientSoundMessage::handle);
        // Pong messages
        INSTANCE.registerMessage(packetId++, superhb.arcademod.network.pong.GetPlayerMessage.class, superhb.arcademod.network.pong.GetPlayerMessage::encode, superhb.arcademod.network.pong.GetPlayerMessage::decode, superhb.arcademod.network.pong.GetPlayerMessage::handle);
        INSTANCE.registerMessage(packetId++, superhb.arcademod.network.pong.ServerPongMessage.class, superhb.arcademod.network.pong.ServerPongMessage::encode, superhb.arcademod.network.pong.ServerPongMessage::decode, superhb.arcademod.network.pong.ServerPongMessage::handle);
        INSTANCE.registerMessage(packetId++, superhb.arcademod.network.pong.ClientPongMessage.class, superhb.arcademod.network.pong.ClientPongMessage::encode, superhb.arcademod.network.pong.ClientPongMessage::decode, superhb.arcademod.network.pong.ClientPongMessage::handle);
    }
}
