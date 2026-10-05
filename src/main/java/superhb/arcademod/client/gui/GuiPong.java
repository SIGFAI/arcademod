package superhb.arcademod.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArcade;
import superhb.arcademod.Arcade;
import superhb.arcademod.client.audio.ArcadeSounds;
import superhb.arcademod.client.tileentity.BlockEntityArcade;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Random;
import org.lwjgl.glfw.GLFW;
import superhb.arcademod.Arcade;

public class GuiPong extends GuiArcade {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/pong.png");
    
    private static final int BOARD_X = 240;
    private static final int BOARD_Y = 154;
    private static final int GUI_X = 252;
    private static final int GUI_Y = 166;
    private static final int PADDLE_X = 5;
    private static final int PADDLE_Y = 28;
    private static final int OUTLINE_X = 236;
    private static final int OUTLINE_Y = 150;
    private static final int BALL = 4;
    private static final int NUMBER_X = 12;
    private static final int NUMBER_Y = 20;
    private static final int WINNER_X = 86;
    private static final int WINNER_Y = 17;
    private static final int LOSER_X = 65;
    private static final int LOSER_Y = 17;
    
    private int boardX, boardY;
    private int mouseY;
    private Rectangle[] outlineBoundingBox = new Rectangle[4];
    private Rectangle[] outBoundingBox = new Rectangle[2];
    private boolean canMultiplayer;
    private int startOption;
    private int endGame;
    
    private Ball ball;
    private Paddle[] paddles = new Paddle[2];
    private Random rand = new Random();
    
    public GuiPong(Level world, BlockEntityArcade tileEntity, @Nullable BlockPos pos, Player player) {
        super(world, tileEntity, pos, player, Component.literal("Paddle Rally"));
        Arcade.logger.info("GuiPong: opened for player " + (player != null ? player.getName().getString() : "null"));
        setGuiSize(GUI_X, GUI_Y);
        setCost(1);
        setOffset(0, 0);
        setButtonPos((GUI_X / 2) - (buttonWidth / 2), GUI_Y - 30);
        setTexture(TEXTURE, 512, 512);
        setStartMenu(0);
        // addPlayer(player.getName().getString());
    }
    
    @Override
    public void tick() {
        try {
            super.tick();
            if (!inMenu) {
                if (endGame == 0) {
                    if (outlineBoundingBox[0] == null) startGame();
                    for (int i = 0; i < 3; i++) {
                        if (outlineBoundingBox[0] != null) outlineBoundingBox[0].setLocation(boardX, boardY);
                        if (outlineBoundingBox[1] != null) outlineBoundingBox[1].setLocation(boardX, boardY + (OUTLINE_Y - 5));
                        if (outBoundingBox[0] != null) outBoundingBox[0].setLocation(boardX - 5, boardY + 5);
                        if (outBoundingBox[1] != null) outBoundingBox[1].setLocation(boardX + OUTLINE_X, boardY + 5);

                        if (paddles[0] != null) {
                            paddles[0].updatePosition(boardX, boardY);
                            paddles[0].ai();
                        }
                        if (paddles[1] != null) {
                            paddles[1].updatePosition(boardX, boardY, mouseY);
                        }
                        if (ball != null) {
                            ball.updatePosition(boardX, boardY);
                        }
                    }
                    try {
                        for (int i = 0; i < 3; i++) if (ball != null) ball.collisionDetection();
                        if (ball != null) ball.updateAngle();
                    } catch (Throwable t) {
                        Arcade.logger.error("GuiPong: exception in ball logic", t);
                    }

                    if ((paddles[0].score / 3) == 10) {
                        endGame = 1;
                    } else if ((paddles[1].score / 3) == 10) {
                        endGame = 2;
                        // Match 1.12.2: flat 10 tickets on win
                        Arcade.logger.info("GuiPong: awarding tickets=10");
                        giveReward(new ItemStack(superhb.arcademod.init.ModRegistries.TICKET.get(), 10));
                    }
                }
            }
        } catch (Throwable t) {
            Arcade.logger.error("GuiPong: exception in tick", t);
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        try {
            super.render(guiGraphics, mouseX, mouseY, partialTicks);

            // xScaled/yScaled are computed in GuiArcade.render(); compute board coordinates after that.
            boardX = xScaled - (GUI_X / 2) + 8;
            boardY = yScaled - (GUI_Y / 2) + 8;
        
        if (inMenu) {
            if (minecraft.mouseHandler.isMouseGrabbed()) minecraft.mouseHandler.releaseMouse();
            int settingWidth = this.font.width(Component.translatable("option.arcademod.setting"));
            
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scale, scale, scale);
            switch (menu) {
                case 0:
                    guiGraphics.drawCenteredString(font, Component.translatable("game.arcademod.pong"), boardX + (GUI_X / 2), boardY + 2, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.start"), boardX + (GUI_X / 2), boardY + (GUI_Y / 2) - 10, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.control"), boardX + (GUI_X / 2), boardY + (GUI_Y / 2), 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.setting"), boardX + (GUI_X / 2), boardY + (GUI_Y / 2) + 10, 0xFFFFFF);
                    
                    if (menuOption == 0) drawRightArrow(guiGraphics, boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 12);
                    else if (menuOption == 1) drawRightArrow(guiGraphics, boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 2);
                    else drawRightArrow(guiGraphics, boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) + 8);
                    break;
                case 1:
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.start"), boardX + (GUI_X / 2), boardY + 2, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.translatable("option.arcademod.singleplayer"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 10, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.translatable("option.arcademod.multiplayer"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2), 0xFFFFFF);
                    
                    if (startOption == 0) drawRightArrow(guiGraphics, boardX + (GUI_X / 2) - 40, boardY + (GUI_Y / 2) - 12);
                    else drawRightArrow(guiGraphics, boardX + (GUI_X / 2) - 40, boardY + (GUI_Y / 2) - 2);
                    guiGraphics.drawString(font, Component.literal("[<-] Back"), boardX + 2, boardY + GUI_Y - 23, 0xFFFFFF);
                    break;
                case 2:
                    guiGraphics.drawCenteredString(font, Component.translatable("control.arcademod.mouse.pong"), boardX + (GUI_X / 2), boardY + (GUI_Y / 2), 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[<-] Back"), boardX + 2, boardY + GUI_Y - 23, 0xFFFFFF);
                    break;
                case 4:
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.setting"), boardX + (GUI_X / 2), boardY + 2, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("text.arcademod.volume"), boardX + (GUI_X / 2), boardY + (GUI_Y / 2) - 30, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[LEFT] " + (editVolume ? "Decrease" : "Back")), boardX + 2, boardY + GUI_Y - 23, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[RIGHT] " + (editVolume ? "Increase" : "")), boardX + 2, boardY + GUI_Y - 33, editVolume ? 0xFFFFFF : 0x404040);
                    guiGraphics.drawString(font, Component.literal("[ENTER] " + (editVolume ? "Save" : "Edit")), boardX + 2, boardY + GUI_Y - 43, 0xFFFFFF);
                    break;
                // Add more cases here (3)
            }
            guiGraphics.pose().popPose();
        } else {
            // Keep mouse released while a Screen is open. Grabbing it can make the game appear to "exit" the GUI.
            if (minecraft.mouseHandler.isMouseGrabbed()) minecraft.mouseHandler.releaseMouse();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scale, scale, scale);
            guiGraphics.blit(TEXTURE, boardX, boardY, GUI_X, 0, OUTLINE_X, OUTLINE_Y, 512, 512);
            drawScore(guiGraphics);
            ball.draw(guiGraphics);
            
            int scaledMouseY = (int) (mouseY / scale);
            if (scaledMouseY <= (boardY + 5)) this.mouseY = boardY + 5;
            else if (scaledMouseY >= (boardY + 117)) this.mouseY = boardY + 117;
            else this.mouseY = scaledMouseY;
            
            for (Paddle paddle : paddles) paddle.draw(guiGraphics);
            
            if (endGame > 0) {
                if (endGame == 2) guiGraphics.blit(TEXTURE, boardX + 10, boardY + 30, 0, GUI_Y + PADDLE_Y + NUMBER_Y, WINNER_X, WINNER_Y, 512, 512);
                else if (endGame == 1) guiGraphics.blit(TEXTURE, boardX + 10, boardY + 30, 0, GUI_Y + PADDLE_Y + NUMBER_Y + WINNER_Y, LOSER_X, LOSER_Y, 512, 512);
                guiGraphics.drawString(font, Component.literal("[Enter] Return Menu"), boardX + 2, boardY + GUI_Y - 30, 0xFFFFFF);
            }
            guiGraphics.pose().popPose();
        }
        } catch (Throwable t) {
            Arcade.logger.error("GuiPong: exception in render", t);
        }
    }
    
    private void drawScore(GuiGraphics guiGraphics) {
        if (paddles[0].score / 3 < 10) {
            guiGraphics.blit(TEXTURE, boardX + (BOARD_X / 2) - 30, boardY + 7, 12 * (paddles[0].score / 3), GUI_Y + PADDLE_Y, NUMBER_X, NUMBER_Y, 512, 512);
        } else {
            guiGraphics.blit(TEXTURE, boardX + (BOARD_X / 2) - 30, boardY + 7, 0, GUI_Y + PADDLE_Y, NUMBER_X, NUMBER_Y, 512, 512);
            guiGraphics.blit(TEXTURE, boardX + (BOARD_X / 2) - 40, boardY + 7, 12, GUI_Y + PADDLE_Y, NUMBER_X, NUMBER_Y, 512, 512);
        }
        if (paddles[1].score / 3 < 10) {
            guiGraphics.blit(TEXTURE, boardX + (BOARD_X / 2) + 14, boardY + 7, 12 * (paddles[1].score / 3), GUI_Y + PADDLE_Y, NUMBER_X, NUMBER_Y, 512, 512);
        } else {
            guiGraphics.blit(TEXTURE, boardX + (BOARD_X / 2) + 28, boardY + 7, 0, GUI_Y + PADDLE_Y, NUMBER_X, NUMBER_Y, 512, 512);
            guiGraphics.blit(TEXTURE, boardX + (BOARD_X / 2) + 14, boardY + 7, 12, GUI_Y + PADDLE_Y, NUMBER_X, NUMBER_Y, 512, 512);
        }
    }
    
    private void startGame() {
        try {
            Arcade.logger.info("GuiPong: startGame() called");
            inMenu = false;
            outlineBoundingBox[0] = new Rectangle(boardX, boardY, OUTLINE_X, 5);
            outlineBoundingBox[1] = new Rectangle(boardX, boardY + (OUTLINE_Y - 5), OUTLINE_X, 5);
            outBoundingBox[0] = new Rectangle(boardX - 5, boardY + 5, 5, OUTLINE_Y - 10);
            outBoundingBox[1] = new Rectangle(boardX + OUTLINE_X, boardY + 5, 5, OUTLINE_Y - 10);
            paddles[0] = new Paddle(0, 5);
            paddles[1] = new Paddle(BOARD_X - 9, this.mouseY);
            ball = new Ball(116, 5 + rand.nextInt(112), rand.nextInt(3));
            canGetCoinBack = false;
            Arcade.logger.info("GuiPong: startGame() done paddles0=" + (paddles[0] != null) + " paddles1=" + (paddles[1] != null) + " ball=" + (ball != null));
        } catch (Throwable t) {
            Arcade.logger.error("GuiPong: exception in startGame", t);
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Arcade.logger.info("GuiPong: keyPressed keyCode=" + keyCode + " menu=" + menu + " inMenu=" + inMenu);
        if (inMenu) {
            if (menu == 0) {
                if (keyCode == GLFW.GLFW_KEY_UP) {
                    menuOption = (menuOption == 0) ? 2 : menuOption - 1;
                }
                if (keyCode == GLFW.GLFW_KEY_DOWN) {
                    menuOption = (menuOption == 2) ? 0 : menuOption + 1;
                }
                if (keyCode == GLFW.GLFW_KEY_ENTER) {
                    if (menuOption == 0) startGame();
                    if (menuOption == 1) menu = 2;
                    if (menuOption == 2) menu = 4;
                }
            } else if (menu == 1) {
                if (keyCode == GLFW.GLFW_KEY_UP) startOption = (startOption == 0) ? 1 : 0;
                if (keyCode == GLFW.GLFW_KEY_DOWN) startOption = (startOption == 1) ? 0 : 1;
                if (keyCode == GLFW.GLFW_KEY_ENTER) {
                    if (startOption == 0) startGame();
                    else menu = 3;
                }
                if (keyCode == GLFW.GLFW_KEY_LEFT) menu = 0;
            } else if (menu == 2) {
                if (keyCode == GLFW.GLFW_KEY_LEFT) menu = 0;
            } else if (menu == 4) {
                if (keyCode == GLFW.GLFW_KEY_LEFT) {
                    if (editVolume) decreaseVolume();
                    else menu = 0;
                }
                if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                    if (editVolume) increaseVolume();
                }
                if (keyCode == GLFW.GLFW_KEY_ENTER) {
                    if (editVolume) {
                        editVolume = false;
                        saveVolume(true);
                    } else editVolume = true;
                }
            }
        } else {
            if (endGame > 0) {
                if (keyCode == GLFW.GLFW_KEY_ENTER) {
                    inMenu = true;
                    checkMenuAfterGameOver();
                    endGame = 0;
                }
            } else {
                // ESC key - give tickets when exiting mid-game if winning (matches 1.12.2 behavior)
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    if (paddles[1].score > paddles[0].score) {
                        int tickets = paddles[1].score / 3;
                        if (tickets > 0) {
                            giveReward(new ItemStack(superhb.arcademod.init.ModRegistries.TICKET.get(), tickets));
                        }
                    }
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public void onClose() {
        Arcade.logger.info("GuiPong: onClose() called");
        if (minecraft.mouseHandler.isMouseGrabbed()) minecraft.mouseHandler.releaseMouse();
        super.onClose();
    }
    
    private class Paddle {
        int x, y, extendedX, extendedY;
        Rectangle boundingBox;
        int score = 0;
        
        private Paddle(int x, int y) {
            this.x = x;
            this.y = y;
            this.boundingBox = new Rectangle(x, y, PADDLE_X, PADDLE_Y);
        }
        
        public void updatePosition(int x, int y) {
            this.extendedX = x + this.x;
            this.extendedY = y + this.y;
            this.boundingBox.setLocation(extendedX, extendedY);
        }
        
        public void updatePosition(int x, int y, int mouseY) {
            this.extendedX = x + this.x;
            this.extendedY = mouseY;
            this.boundingBox.setLocation(extendedX, extendedY);
        }
        
        public void ai() {
            if (extendedY > ball.extendedY && y != 5) y -= 1;
            else if (extendedY < ball.extendedY && y != 117) y += 1;
        }
        
        public void draw(GuiGraphics guiGraphics) {
            guiGraphics.blit(TEXTURE, extendedX, extendedY, 0, GUI_Y, PADDLE_X, PADDLE_Y, 512, 512);
        }
    }
    
    private class Ball {
        int x, y, extendedX, extendedY;
        int xV = 1, yV = 1, angle;
        int xA = 3, yA = 3;
        Rectangle boundingBox;
        
        private Ball(int x, int y, int angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.boundingBox = new Rectangle(x, y, BALL, BALL);
        }
        
        public void draw(GuiGraphics guiGraphics) {
            guiGraphics.blit(TEXTURE, extendedX, extendedY, PADDLE_X, GUI_Y, BALL, BALL, 512, 512);
        }
        
        public void updatePosition(int x, int y) {
            this.extendedX = x + this.x;
            this.extendedY = y + this.y;
            this.boundingBox.setLocation(extendedX, extendedY);
        }
        
        public void updateAngle() {
            for (int i = 0; i < xA; i++) x += xV;
            for (int i = 0; i < yA; i++) y += yV;
        }
        
        public void collisionDetection() {
            for (Rectangle box : outlineBoundingBox) {
                if (box == null) continue;
                if (boundingBox.intersects(box)) {
                    yV = -yV;
                    world.playSound(player, player.blockPosition(), superhb.arcademod.init.ModRegistries.PONG_WALL.get(), net.minecraft.sounds.SoundSource.BLOCKS, getVolume(), 1.0f);
                }
            }
            if (boundingBox.intersects(outBoundingBox[0])) {
                xV = 1;
                angle = rand.nextInt(3);
                x = 116;
                y = 5 + rand.nextInt(112);
                paddles[1].score++;
                world.playSound(player, player.blockPosition(), superhb.arcademod.init.ModRegistries.PONG_MISS.get(), net.minecraft.sounds.SoundSource.BLOCKS, getVolume(), 1.0f);
            } else if (boundingBox.intersects(outBoundingBox[1])) {
                xV = -1;
                angle = rand.nextInt(3);
                x = 116;
                y = 5 + rand.nextInt(112);
                paddles[0].score++;
                world.playSound(player, player.blockPosition(), superhb.arcademod.init.ModRegistries.PONG_MISS.get(), net.minecraft.sounds.SoundSource.BLOCKS, getVolume(), 1.0f);
            }
            for (Paddle paddle : paddles) {
                if (paddle == null) continue;
                if (boundingBox.intersects(paddle.boundingBox)) {
                    xV = -xV;
                    world.playSound(player, player.blockPosition(), superhb.arcademod.init.ModRegistries.PONG_HIT.get(), net.minecraft.sounds.SoundSource.BLOCKS, getVolume(), 1.0f);
                    int intersection = (paddle.extendedY + PADDLE_Y) - extendedY;
                    if (intersection > 14) {
                        if (intersection > (28 - 5)) angle = 1;
                        else if (intersection < (14 + 5)) angle = 2;
                        else xA = yA = 3;
                    } else if (intersection < 14) {
                        if (intersection > (14 - 5)) angle = 2;
                        else if (intersection < (14 - 9)) angle = 1;
                        else angle = 0;
                    }
                }
            }
            calculateAngle();
        }
        
        public void calculateAngle() {
            switch (angle) {
                case 0: xA = yA = 3; break;
                case 1: yA = 3; xA = 6; break;
                case 2: xA = 3; yA = 6; break;
            }
        }
    }
}
