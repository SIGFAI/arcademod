package superhb.arcademod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArcade;
import superhb.arcademod.Arcade;
import superhb.arcademod.client.audio.ArcadeSounds;
import superhb.arcademod.client.tileentity.BlockEntityArcade;
import superhb.arcademod.client.audio.LoopingSound;

import org.lwjgl.glfw.GLFW;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import net.minecraft.world.item.ItemStack;

public class GuiTetrominoes extends GuiArcade {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/tetrominoes.png");

    private static final int GUI_X = 210;
    private static final int GUI_Y = 254;
    private static final int PLAY_BLOCK = 13;
    private static final int PREVIEW_BLOCK = 11;

    private int score = 0, row = 0, level = 1;
    private boolean gameOver = false;
    private int rotation = 0;
    private int playX, playY, nextX, nextY;
    private int nextShape = 0, curShape;
    private boolean giveNextPiece = true;
    private Point piecePoint = new Point(3, 0);
    private int[][] board;
    private int[] speed = { 1, 10, 10, 9, 8, 7, 6, 5, 4, 3, 2 };

    private int prevControlTick = 0, controlSpeed = 2;
    private int prevGameTick = 0;

    private final Point[][][] pieces = {
            {
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) },
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) }
            },
            {
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2) },
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 0) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 0) }
            },
            {
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 2) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 0) },
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 0) },
                    { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2) }
            },
            {
                    { new Point(1, 1), new Point(2, 1), new Point(0, 2), new Point(1, 2) },
                    { new Point(0, 1), new Point(0, 2), new Point(1, 2), new Point(1, 3) },
                    { new Point(1, 1), new Point(2, 1), new Point(0, 2), new Point(1, 2) },
                    { new Point(0, 1), new Point(0, 2), new Point(1, 2), new Point(1, 3) }
            },
            {
                    { new Point(0, 1), new Point(1, 1), new Point(1, 2), new Point(2, 2) },
                    { new Point(1, 1), new Point(0, 2), new Point(1, 2), new Point(0, 3) },
                    { new Point(0, 1), new Point(1, 1), new Point(1, 2), new Point(2, 2) },
                    { new Point(1, 1), new Point(0, 2), new Point(1, 2), new Point(0, 3) }
            },
            {
                    { new Point(0, 1), new Point(0, 2), new Point(1, 1), new Point(1, 2) },
                    { new Point(0, 1), new Point(0, 2), new Point(1, 1), new Point(1, 2) },
                    { new Point(0, 1), new Point(0, 2), new Point(1, 1), new Point(1, 2) },
                    { new Point(0, 1), new Point(0, 2), new Point(1, 1), new Point(1, 2) }
            },
            {
                    { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(1, 2) },
                    { new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(1, 2) },
                    { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1) },
                    { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) }
            }
    };

    private final Color[] colors = {
            Color.cyan,
            Color.blue,
            new Color(1.0F, 0.549F, 0.0F),
            Color.GREEN,
            Color.RED,
            Color.yellow,
            new Color(1.0F, 0.078F, 0.576F)
    };

    public GuiTetrominoes(Level world, BlockEntityArcade tileEntity, Player player) {
        super(world, tileEntity, null, player, Component.literal("Tetrominoes"));
        setGuiSize(GUI_X, GUI_Y, 0.9F);
        setTexture(TEXTURE);
        setOffset(-30, 0);
        setButtonPos((GUI_X / 2) - (buttonWidth / 2) - 30, GUI_Y - 32);
        setStartMenu(0);
        setCost(2);

        nextShape = world.random.nextInt(7);

        board = new int[10][18];
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 18; y++) {
                board[x][y] = -1;
            }
        }
    }

    private LoopingSound theme;

    @Override
    public void tick() {
        super.tick();
        if (!inMenu) {
            if (theme == null) {
                // Match 1.12.2 behavior: play as a block/arcade sound (not global MUSIC volume).
                theme = new LoopingSound(tileEntity, superhb.arcademod.init.ModRegistries.TETROMINOES.get(), net.minecraft.sounds.SoundSource.BLOCKS, getVolume());
            }
            if (!minecraft.getSoundManager().isActive(theme)) {
                minecraft.getSoundManager().play(theme);
            }
        } else {
            if (theme != null) {
                if (minecraft.getSoundManager().isActive(theme)) {
                    minecraft.getSoundManager().stop(theme);
                }
                theme = null;  // Nullify so a fresh instance is created if the game restarts
            }
        }

        if (inMenu) {
            if (menu == 3) {
                if (tickCounter >= 60) {
                    tickCounter = 0;
                    prevGameTick = 0;
                    prevControlTick = 0;
                    checkMenuAfterGameOver();
                    nextShape = world.random.nextInt(7);
                    score = 0;
                    row = 0;
                    level = 1;
                    rotation = 0;
                    giveNextPiece = false;
                    for (int x = 0; x < 10; x++) {
                        for (int y = 0; y < 18; y++) {
                            board[x][y] = -1;
                        }
                    }
                }
            }
        } else {
            // TODO: Sound playing

            if (gameOver) {
                menu = 3;
                giveNextPiece = false;
                inMenu = true;
                gameOver = false;
                // Match 1.12.2: 1 ticket per row
                if (row > 0) {
                    Arcade.logger.info("GuiTetrominoes: awarding tickets=" + row);
                    giveReward(new ItemStack(superhb.arcademod.init.ModRegistries.TICKET.get(), row));
                }
            }

            if (giveNextPiece) {
                rotation = 0;
                curShape = nextShape;
                nextShape = world.random.nextInt(7);
                piecePoint = new Point(3, 0);
                giveNextPiece = false;
            }

            if ((tickCounter - prevGameTick) >= (GLFW.glfwGetKey(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS ? speed[0] : speed[level])) {
                prevGameTick = tickCounter;

                if (canMoveDown()) piecePoint.y++;
                else place();
            }

            if ((tickCounter - prevControlTick) >= controlSpeed) {
                prevControlTick = tickCounter;
                if (GLFW.glfwGetKey(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
                    if (canMoveLeft()) piecePoint.x--;
                } else if (GLFW.glfwGetKey(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
                    if (canMoveRight()) piecePoint.x++;
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        playX = xScaled - (GUI_X / 2) + 10;
        playY = yScaled - (GUI_Y / 2) + 10;
        nextX = playX + 140;
        nextY = playY + 8;

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);

        if (inMenu) {
            switch (menu) {
                case 0:
                    guiGraphics.drawCenteredString(font, Component.translatable("game.arcademod.tetrominoes"), playX + (130 / 2), playY + 2, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.start"), playX + (130 / 2), (height / 2), 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.control"), playX + (130 / 2), (height / 2) + 10, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.setting"), playX + (130 / 2), (height / 2) + 20, 0xFFFFFF);

                    if (menuOption == 0) drawRightArrow(guiGraphics, playX + (130 / 2) - 40, (height / 2) - 2);
                    else if (menuOption == 1) drawRightArrow(guiGraphics, playX + (130 / 2) - 40, (height / 2) + 8);
                    else if (menuOption == 2) drawRightArrow(guiGraphics, playX + (130 / 2) - 40, (height / 2) + 18);
                    break;
                case 1:
                    guiGraphics.drawCenteredString(font, Component.translatable("text.arcademod.level_select.tetrominoes"), playX + (130 / 2) - 10, yScaled, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[" + level + "]"), playX + (130 / 2) + 25, yScaled, 0xFFFFFF);

                    drawUpArrow(guiGraphics, playX + (130 / 2) + 29, yScaled - 10);
                    drawDownArrow(guiGraphics, playX + (130 / 2) + 29, yScaled + 10);

                    guiGraphics.drawString(font, Component.literal("[LEFT] Back"), playX + 2, yScaled + (GUI_Y / 2) - 20, 0xFFFFFF);
                    break;
                case 2:
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.control"), playX + (130 / 2), playY + 2, 0xFFFFFF);

                    guiGraphics.drawString(font, Component.literal("[UP] Rotate"), playX + (130 / 2) - 40, yScaled - 10, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[DOWN] Down"), playX + (130 / 2) - 40, yScaled, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[LEFT] Left"), playX + (130 / 2) - 40, yScaled + 10, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[RIGHT] Right"), playX + (130 / 2) - 40, yScaled + 20, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[ENTER] Select"), playX + (130 / 2) - 40, yScaled + 30, 0xFFFFFF);

                    guiGraphics.drawString(font, Component.literal("[LEFT] Back"), playX + 2, yScaled + (GUI_Y / 2) - 20, 0xFFFFFF);
                    break;
                case 3:
                    guiGraphics.drawCenteredString(font, Component.translatable("text.arcademod.gameover"), playX + (130 / 2), yScaled - 20, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.literal("Score: " + score), playX + (130 / 2), yScaled - 10, 0xFFFFFF);
                    break;
                case 4:
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.setting"), playX + (130 / 2), playY + 2, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("text.arcademod.volume"), playX + (130 / 2), height / 2, 0xFFFFFF);
                    // drawVolumeBar
                    guiGraphics.drawString(font, Component.literal("[LEFT] Back"), playX + 2, yScaled + (GUI_Y / 2) - 20, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[ENTER] " + (editVolume ? "Save" : "Edit")), playX + 2, yScaled + (GUI_Y / 2) - 30, 0xFFFFFF);
                    break;
            }
        } else {
            drawTetromino(guiGraphics, curShape, rotation, piecePoint.x, piecePoint.y);
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 18; y++) {
                    if (board[x][y] != -1) drawBlock(guiGraphics, board[x][y], x, y);
                }
            }
            drawPreview(guiGraphics, nextShape);

            guiGraphics.drawString(font, Component.translatable("text.arcademod.next.tetrominoes"), playX + 140, playY, 0x404040);
            guiGraphics.drawString(font, Component.literal("Level: " + level), playX + 140, nextY + 38, 0x404040);
            guiGraphics.drawString(font, Component.literal("Row: " + row), playX + 140, nextY + 63, 0x404040);
            guiGraphics.drawString(font, Component.literal("Score: " + score), playX + 140, nextY + 88, 0x404040);
        }

        guiGraphics.pose().popPose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_UP) {
            if (inMenu) {
                if (menu == 0) {
                    if (menuOption == 0) menuOption = 2;
                    else menuOption--;
                } else if (menu == 1) {
                    if (level != 10) level++;
                }
            } else {
                if (canRotate()) {
                    if (rotation == 3) rotation = 0;
                    else rotation++;
                }
            }
        } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
            if (inMenu) {
                if (menu == 0) {
                    if (menuOption == 2) menuOption = 0;
                    else menuOption++;
                } else if (menu == 1) {
                    if (level != 1) level--;
                }
            }
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (inMenu) {
                if (menu == 1 || menu == 2 || (menu == 4 && !editVolume)) menu = 0;
                if (menu == 4 && editVolume) decreaseVolume();
            }
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (inMenu) {
                if (menu == 4 && editVolume) increaseVolume();
            }
        } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (inMenu) {
                if (menu == 0) {
                    switch (menuOption) {
                        case 0: menu = 1; break;
                        case 1: menu = 2; break;
                        case 2: menu = 4; break;
                    }
                } else if (menu == 1) {
                    inMenu = false;
                    if ((tickCounter - prevGameTick) >= 1) {
                        prevGameTick = tickCounter;
                        canGetCoinBack = false;
                        giveNextPiece = true;
                    }
                } else if (menu == 4) {
                    if (editVolume) {
                        editVolume = false;
                        saveVolume(true);
                    } else editVolume = true;
                }
            }
        }
        // ESC key - give tickets when exiting mid-game (matches 1.12.2 behavior)
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (!inMenu && row > 0) {
                giveReward(new ItemStack(superhb.arcademod.init.ModRegistries.TICKET.get(), row));
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void checkLevel() {
        if (row >= 10 && level == 1) level = 2;
        else if (row >= 20 && level == 2) level = 3;
        else if (row >= 30 && level == 3) level = 4;
        else if (row >= 40 && level == 4) level = 5;
        else if (row >= 50 && level == 5) level = 6;
        else if (row >= 60 && level == 6) level = 7;
        else if (row >= 70 && level == 7) level = 8;
        else if (row >= 80 && level == 8) level = 9;
        else if (row >= 90 && level == 9) level = 10;
    }

    private void setGuiColor(GuiGraphics guiGraphics, Color color) {
        guiGraphics.setColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 1.0f);
    }

    private void drawTetromino(GuiGraphics guiGraphics, int shape, int rotation, int x, int y) {
        setGuiColor(guiGraphics, colors[shape]);
        for (int i = 0; i < 4; i++) {
            guiGraphics.blit(TEXTURE, playX + (x * PLAY_BLOCK) + (pieces[shape][rotation][i].x * PLAY_BLOCK), playY + (y * PLAY_BLOCK) + (pieces[shape][rotation][i].y * PLAY_BLOCK) - PLAY_BLOCK, GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK, 512, 512);
        }
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawBlock(GuiGraphics guiGraphics, int shape, int x, int y) {
        setGuiColor(guiGraphics, colors[shape]);
        guiGraphics.blit(TEXTURE, playX + (x * PLAY_BLOCK), playY + (y * PLAY_BLOCK), GUI_X, 0, PLAY_BLOCK, PLAY_BLOCK, 512, 512);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawPreview(GuiGraphics guiGraphics, int shape) {
        int[][] pos = {
                { 3, -2 }, { 8, -8 }, { 8, -8 }, { 8, -8 }, { 8, -8 }, { 14, -8 }, { 8, -8 }
        };

        setGuiColor(guiGraphics, colors[shape]);
        for (int i = 0; i < 4; i++) {
            guiGraphics.blit(TEXTURE, nextX + pos[shape][0] + (pieces[shape][0][i].x * PREVIEW_BLOCK), nextY + pos[shape][1] + (pieces[shape][0][i].y * PREVIEW_BLOCK), (GUI_X + PLAY_BLOCK), 0, PREVIEW_BLOCK, PREVIEW_BLOCK, 512, 512);
        }
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private boolean canRotate() {
        int nextRot = (rotation == 3) ? 0 : rotation + 1;
        if (piecePoint.y == 0 && (curShape < 3 || curShape == 6)) return false;
        for (int i = 0; i < 4; i++) {
            int px = piecePoint.x + pieces[curShape][nextRot][i].x;
            int py = piecePoint.y + pieces[curShape][nextRot][i].y - 1;
            if (px < 0 || px > 9) return false;
            if (py >= 0 && board[px][py] != -1) return false;
        }
        return true;
    }

    private boolean canMoveLeft() {
        for (int i = 0; i < 4; i++) {
            int px = piecePoint.x + pieces[curShape][rotation][i].x;
            int py = piecePoint.y + pieces[curShape][rotation][i].y - 1;
            if (px == 0) return false;
            if (py >= 0 && board[px - 1][py] != -1) return false;
        }
        return true;
    }

    private boolean canMoveRight() {
        for (int i = 0; i < 4; i++) {
            int px = piecePoint.x + pieces[curShape][rotation][i].x;
            int py = piecePoint.y + pieces[curShape][rotation][i].y - 1;
            if (px == 9) return false;
            if (py >= 0 && board[px + 1][py] != -1) return false;
        }
        return true;
    }

    private boolean canMoveDown() {
        for (int i = 0; i < 4; i++) {
            int px = piecePoint.x + pieces[curShape][rotation][i].x;
            int py = piecePoint.y + pieces[curShape][rotation][i].y;
            if (py - 1 == 17) return false;
            if (py >= 0 && board[px][py] != -1) return false;
        }
        return true;
    }

    private void place() {
        for (int i = 0; i < 4; i++) {
            int px = piecePoint.x + pieces[curShape][rotation][i].x;
            int py = piecePoint.y + pieces[curShape][rotation][i].y - 1;
            if (py >= 0) board[px][py] = curShape;
        }
        for (int x = 0; x < 10; x++) {
            if (board[x][0] != -1) {
                gameOver = true;
                return;
            }
        }
        score += 17;
        checkForRow();
        giveNextPiece = true;
    }

    private void checkForRow() {
        ArrayList<int[]> boardList = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            boardList.add(new int[] { board[0][i], board[1][i], board[2][i], board[3][i], board[4][i], board[5][i], board[6][i], board[7][i], board[8][i], board[9][i]});
        }

        for (int y = 0; y < boardList.size(); y++) {
            int[] rowData = boardList.get(y);
            boolean full = true;
            for (int x = 0; x < 10; x++) {
                if (rowData[x] == -1) full = false;
            }
            if (full) {
                row++;
                checkLevel();
                score += 41;
                boardList.remove(y);
                boardList.add(0, new int[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 });
                y--;  // Re-check same index since rows shifted down
            }
        }

        for (int y = 0; y < 18; y++) {
            int[] boardX = boardList.get(y);
            for (int x = 0; x < 10; x++) {
                board[x][y] = boardX[x];
            }
        }
    }

    @Override
    public void onClose() {
        if (theme != null) {
            minecraft.getSoundManager().stop(theme);
        }
        super.onClose();
    }
}
