package superhb.arcademod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import superhb.arcademod.client.gui.*;
import superhb.arcademod.client.tileentity.BlockEntityArcade;
import superhb.arcademod.client.tileentity.BlockEntityPrize;

public class ClientAccess {
    public static void openArcadeGui(Level level, BlockEntityArcade arcade, Player player) {
        int gameId = arcade.getGameID();
        BlockPos pos = arcade.getBlockPos();
        // Ensure GUI is opened on the client/main thread. If this is invoked from a network handler thread,
        // setting the screen directly can behave inconsistently (flash then close, or not open at all).
        Minecraft.getInstance().execute(() -> {
            switch (gameId) {
                case 0: Minecraft.getInstance().setScreen(new GuiSnake(level, arcade, player)); break;
                case 1: Minecraft.getInstance().setScreen(new GuiTetrominoes(level, arcade, player)); break;
                case 2: Minecraft.getInstance().setScreen(new GuiPacMan(level, arcade, pos, player)); break;
                case 3: Minecraft.getInstance().setScreen(new GuiPong(level, arcade, pos, player)); break;
                case 4: Minecraft.getInstance().setScreen(new GuiSpaceInvaders(level, arcade, pos, player)); break;
                case 5: Minecraft.getInstance().setScreen(new GuiKong(level, arcade, pos, player)); break;
                default: break;
            }
        });
    }

    public static void openPrizeGui(BlockEntityPrize prize) {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new GuiPrize(prize)));
    }

    public static void openPusherGui(Level level, BlockPos pos, Player player) {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new GuiPusher(level, pos.getX(), pos.getY(), pos.getZ(), player)));
    }
}
