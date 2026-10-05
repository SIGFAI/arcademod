package superhb.arcademod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArcade;
import superhb.arcademod.Arcade;
import superhb.arcademod.client.tileentity.BlockEntityArcade;

import org.lwjgl.glfw.GLFW;

public class GuiSnake extends GuiArcade {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/snake.png");

    private int snakePosX = 0, snakePosY = 0;
    private int pointPosX = 0, pointPosY = 0;
    private int[] tailX = new int[200], tailY = new int[200];
    private int direction = 0, difficulty = 0;
    private int tail = 0, score = 0;
    private boolean start = true, gameOver = false;
    private int prevTick = 0, tick = 4;

    private static final int GUI_X = 150;
    private static final int GUI_Y = 207;
    private static final int ARROW_X = 7;
    private static final int ARROW_Y = 11;
    private static final int SNAKE = 5;
    private static final int POINT = 3;

    public GuiSnake(Level world, BlockEntityArcade tileEntity, Player player) {
        super(world, tileEntity, null, player, Component.literal("Snake"));
        setGuiSize(GUI_X, GUI_Y);
        setButtonPos((GUI_X / 2) - (buttonWidth / 2), GUI_Y - 32);
        setTexture(TEXTURE);
        setStartMenu(0);
    }

    @Override
    public void tick() {
        super.tick();

        if (inMenu) {
            if (menu == 3) {
                if ((tickCounter - prevTick) >= 60) {
                    prevTick = tickCounter;
                    checkMenuAfterGameOver();
                    direction = 0;
                    start = true;
                    tail = 0;
                }
            }
        } else {
            if ((tickCounter - prevTick) >= tick) {
                prevTick = tickCounter;

                int preX = tailX[0];
                int preY = tailY[0];
                int pre2X, pre2Y;

                if (start) {
                    pointPosX = world.random.nextInt(25);
                    pointPosY = world.random.nextInt(36);
                    start = false;
                }

                if (direction == 0) {
                    snakePosX = (130 / 5) / 2;
                    snakePosY = (185 / 5) / 2;
                } else if (direction == 1) {
                    tailX[0] = snakePosX;
                    tailY[0] = snakePosY;
                    snakePosY -= 1;
                } else if (direction == 2) {
                    tailX[0] = snakePosX;
                    tailY[0] = snakePosY;
                    snakePosY += 1;
                } else if (direction == 3) {
                    tailX[0] = snakePosX;
                    tailY[0] = snakePosY;
                    snakePosX -= 1;
                } else if (direction == 4) {
                    tailX[0] = snakePosX;
                    tailY[0] = snakePosY;
                    snakePosX += 1;
                }

                if (snakePosX < 0 || snakePosX > 25 || snakePosY < 0 || snakePosY > 36) {
                    direction = -1;
                    gameOver = true;
                }

                for (int i = 0; i < tail; i++) {
                    if (i < tailX.length) {
                        if (tailX[i] == snakePosX && tailY[i] == snakePosY) {
                            direction = -1;
                            gameOver = true;
                        }
                    }
                }

                if (snakePosX == pointPosX && snakePosY == pointPosY) {
                    tail++;
                    pointPosX = world.random.nextInt(25);
                    pointPosY = world.random.nextInt(36);
                    for (int i = 0; i < tail; i++) {
                        if (i < tailX.length) {
                            if ((pointPosX == tailX[i] && pointPosY == tailY[i]) || (pointPosX == snakePosX && pointPosY == snakePosY)) {
                                pointPosX = world.random.nextInt(25);
                                pointPosY = world.random.nextInt(36);
                            }
                        }
                    }
                }

                for (int i = 1; i < tail; i++) {
                    if (i < tailX.length) {
                        pre2X = tailX[i];
                        pre2Y = tailY[i];
                        tailX[i] = preX;
                        tailY[i] = preY;
                        preX = pre2X;
                        preY = pre2Y;
                    }
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int minX = (width / 2) - (GUI_X / 2) + 10;
        int minY = (height / 2) - (GUI_Y / 2) + 10;

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        if (inMenu) {
            guiGraphics.pose().pushPose();
            switch (menu) {
                case 0:
                    guiGraphics.drawCenteredString(font, Component.translatable("game.arcademod.snake"), width / 2, (height / 2) - (GUI_Y / 2) + 11, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.start"), width / 2, height / 2, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.difficulty"), width / 2, height / 2 + 10, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.control"), width / 2, height / 2 + 20, 0xFFFFFF);

                    switch (menuOption) {
                        case 0:
                            guiGraphics.blit(TEXTURE, minX + 30, height / 2 - 2, 0, GUI_Y, ARROW_X, ARROW_Y);
                            break;
                        case 1:
                            guiGraphics.blit(TEXTURE, minX + 30, height / 2 - 2 + 10, 0, GUI_Y, ARROW_X, ARROW_Y);
                            break;
                        case 2:
                            guiGraphics.blit(TEXTURE, minX + 30, height / 2 - 2 + 20, 0, GUI_Y, ARROW_X, ARROW_Y);
                            break;
                    }
                    break;
                case 1:
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.difficulty"), width / 2, (height / 2) - (GUI_Y / 2) + 11, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.difficulty.easy"), width / 2, height / 2, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.difficulty.medium"), width / 2, height / 2 + 10, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.difficulty.hard"), width / 2, height / 2 + 20, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.difficulty.extreme"), width / 2, height / 2 + 30, 0xFFFFFF);

                    switch (difficulty) {
                        case 0:
                            guiGraphics.blit(TEXTURE, minX + 30, height / 2 - 2, 0, GUI_Y, ARROW_X, ARROW_Y);
                            break;
                        case 1:
                            guiGraphics.blit(TEXTURE, minX + 30, height / 2 - 2 + 10, 0, GUI_Y, ARROW_X, ARROW_Y);
                            break;
                        case 2:
                            guiGraphics.blit(TEXTURE, minX + 30, height / 2 - 2 + 20, 0, GUI_Y, ARROW_X, ARROW_Y);
                            break;
                        case 3:
                            guiGraphics.blit(TEXTURE, minX + 30, height / 2 - 2 + 30, 0, GUI_Y, ARROW_X, ARROW_Y);
                            break;
                    }
                    break;
                case 2:
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.control"), width / 2, (height / 2) - (GUI_Y / 2) + 11, 0xFFFFFF);

                    guiGraphics.drawString(font, Component.literal("[UP] Up"), (width / 2) - 40, (height / 2) - 10, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[DOWN] Down"), (width / 2) - 40, height / 2, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[LEFT] Left"), (width / 2) - 40, (height / 2) + 10, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[RIGHT] Right"), (width / 2) - 40, (height / 2) + 20, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[ENTER] Select"), (width / 2) - 40, (height / 2) + 30, 0xFFFFFF);

                    guiGraphics.drawString(font, Component.literal("[LEFT] Back"), (width / 2) - (GUI_X / 2) + 12, (height / 2) + (GUI_Y / 2) - 20, 0xFFFFFF);
                    break;
                case 3:
                    guiGraphics.drawCenteredString(font, Component.translatable("text.arcademod.gameover"), width / 2, (height / 2) - 20, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.literal("Score: " + tail), width / 2, (height / 2) - 10, 0xFFFFFF);
                    break;
            }
            guiGraphics.pose().popPose();
        } else {
            if (gameOver) {
                menu = 3;
                score = tail;
                inMenu = true;
                gameOver = false;
                int tickets = score / 10;
                if (tickets > 0) {
                    Arcade.logger.info("GuiSnake: awarding tickets=" + tickets);
                    giveReward(new ItemStack(superhb.arcademod.init.ModRegistries.TICKET.get(), tickets));
                }
            }

            guiGraphics.blit(TEXTURE, minX + (snakePosX * 5), minY + (snakePosY * 5), ARROW_X, GUI_Y, SNAKE, SNAKE);
            guiGraphics.blit(TEXTURE, minX + 1 + (pointPosX * 5), minY + 1 + (pointPosY * 5), ARROW_X + SNAKE, GUI_Y, POINT, POINT);

            for (int i = 1; i <= tail; i++) {
                if (i < tailX.length) {
                    guiGraphics.blit(TEXTURE, minX + (tailX[i - 1] * 5), minY + (tailY[i - 1] * 5), ARROW_X, GUI_Y, SNAKE, SNAKE);
                }
            }

            guiGraphics.drawCenteredString(font, Component.literal("Score: " + tail), width / 2, (height / 2) + 93, 0x404040);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_UP) {
            if (inMenu) {
                if (menu == 0) {
                    if (menuOption == 0) menuOption = 2;
                    else menuOption -= 1;
                } else if (menu == 1) {
                    if (difficulty == 0) difficulty = 3;
                    else difficulty -= 1;
                }
            } else {
                if (tail > 0) {
                    if (direction != 2) direction = 1;
                    else direction = 2;
                } else {
                    direction = 1;
                }
            }
        } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
            if (inMenu) {
                if (menu == 0) {
                    if (menuOption == 2) menuOption = 0;
                    else menuOption += 1;
                } else if (menu == 1) {
                    if (difficulty == 3) difficulty = 0;
                    else difficulty += 1;
                }
            } else {
                if (tail > 0) {
                    if (direction != 1) direction = 2;
                    else direction = 1;
                } else {
                    direction = 2;
                }
            }
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (inMenu) {
                if (menu == 2) menu = 0;
            } else {
                if (tail > 0) {
                    if (direction != 4) direction = 3;
                    else direction = 4;
                } else {
                    direction = 3;
                }
            }
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (!inMenu) {
                if (tail > 0) {
                    if (direction != 3) direction = 4;
                    else direction = 3;
                } else {
                    direction = 4;
                }
            }
        } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (inMenu) {
                if (menu == 0) {
                    switch (menuOption) {
                        case 0:
                            inMenu = false;
                            canGetCoinBack = false;
                            break;
                        case 1:
                            menu = 1;
                            break;
                        case 2:
                            menu = 2;
                            break;
                    }
                } else if (menu == 1) {
                    switch (difficulty) {
                        case 0: tick = 4; break;
                        case 1: tick = 3; break;
                        case 2: tick = 2; break;
                        case 3: tick = 1; break;
                    }
                    menu = 0;
                }
            }
        }
        // ESC key - give tickets when exiting mid-game (matches 1.12.2 behavior)
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (!inMenu) {
                int tickets = tail / 10;
                if (tickets > 0) {
                    giveReward(new ItemStack(superhb.arcademod.init.ModRegistries.TICKET.get(), tickets));
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
