package superhb.arcademod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import org.lwjgl.glfw.GLFW;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArcade;
import superhb.arcademod.Arcade;
import superhb.arcademod.client.audio.ArcadeSounds;
import net.minecraft.sounds.SoundSource;
import superhb.arcademod.client.audio.LoopingSound;
import superhb.arcademod.client.tileentity.BlockEntityArcade;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Random;

public class GuiSpaceInvaders extends GuiArcade {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/spaceinvaders.png");

    private static final int GUI_X = 234, GUI_Y = 284;
    private static final int MAZE_X = 224, MAZE_Y = 248;

    private static final int ALIEN = 14;
    private static final int TANK = 15;

    private int boardX, boardY;
    private int score;
    private final Tile[][] tiles = new Tile[31][28];
    private byte level;
    private boolean gameOver, mazeBlink, nextLevel;
    private int mazeBlinkTick = 0;
    private int mazeBlinks = 0;
    private boolean playing, updatePos, allDead;
    private int startTick = 0;
    private int bonusTick, bonusTime;
    private boolean showBonus;
    private int backTick;
    private int activeBombs;
    private int ufoScore = 0;

    private ArcadePlayer tank;
    private final Alien[] aliens = new Alien[32];
    private Bullet bullet;
    private final Bomb[] bombs = new Bomb[8];
    private UFO ufo;
    private Direction lastDirection, desiredDirection = Direction.LEFT;
    private int deathTick = 0, gameOverTick = 0;

    public GuiSpaceInvaders(Level world, BlockEntityArcade tile, @Nullable BlockPos pos, Player player) {
        super(world, tile, pos, player, Component.literal("Alien Barrage"));
        setGuiSize(GUI_X, GUI_Y, 0.8F);
        setTexture(TEXTURE, 512, 512);
        setCost(4);
        setOffset(0, 0);
        setButtonPos((GUI_X / 2) - (buttonWidth / 2), GUI_Y - 30);
        setStartMenu(0);
    }

    private LoopingSound theme;

    @Override
    public void tick() {
        super.tick();

        if (inMenu) {
            if (theme != null) {
                if (minecraft.getSoundManager().isActive(theme)) {
                    minecraft.getSoundManager().stop(theme);
                }
                theme = null;
            }
            if (menu == 3) {
                if ((tickCounter - backTick) == 60) {
                    tickCounter = score = mazeBlinks = mazeBlinkTick = deathTick = gameOverTick = level = 0;
                    checkMenuAfterGameOver();
                }
            }
        } else {
            if (theme == null) {
                theme = new LoopingSound(tileEntity, superhb.arcademod.init.ModRegistries.SPACEINVADERS.get(), SoundSource.BLOCKS, Math.min(1.0f, getVolume() + 0.2f));
            }
            if (!minecraft.getSoundManager().isActive(theme)) {
                minecraft.getSoundManager().play(theme);
            }
            if (playing) {
                for (int i = 0; i < 3; i++) tank.move().updatePosition(boardX, boardY);
                tank.update();

                if (desiredDirection == Direction.DOWN) {
                    for (int i = 0; i < aliens.length; i++) {
                        if (aliens[i].isVisible) {
                            aliens[i].ai().move().updatePosition(boardX, boardY);
                        }
                    }
                    if (lastDirection == Direction.LEFT) {
                        desiredDirection = Direction.RIGHT;
                        lastDirection = Direction.RIGHT;
                    } else {
                        desiredDirection = Direction.LEFT;
                        lastDirection = Direction.LEFT;
                    }
                } else {
                    for (int i = 0; i < aliens.length; i++) {
                        if (aliens[i].isVisible) aliens[i].ai().move().updatePosition(boardX, boardY);
                    }
                }
                for (int i = 0; i < aliens.length; i++) {
                    aliens[i].current = desiredDirection;
                    aliens[i].update();
                }

                net.minecraft.util.RandomSource random = world.random;
                activeBombs = 0;
                for (int i = 0; i < bombs.length; i++) {
                    if (bombs[i].isVisible) activeBombs++;
                }

                for (int i = 0; i < level; i++) {
                    if (activeBombs < level) {
                        int id = random.nextInt(32);
                        if (aliens[id].isVisible) {
                            for (int j = 0; j < bombs.length; j++) {
                                if (!bombs[j].isVisible) {
                                    bombs[j] = new Bomb(j, aliens[id].x, aliens[id].y, aliens[id].aliencolor, true);
                                    bombs[j].canMove = true;
                                    activeBombs++;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (bullet != null) bullet.move().updatePosition(boardX, boardY);

                for (int i = 0; i < bombs.length; i++) {
                    if (bombs[i].isVisible) {
                        bombs[i].move().updatePosition(boardX, boardY);
                        bombs[i].update();
                    }
                }

                if (score >= (ufoScore + 100)) {
                    ufoScore = score;
                    if (ufo == null) {
                        ufo = new UFO(0, 26, 4, new Color(100, random.nextInt(256), random.nextInt(256)), true);
                        ufo.canMove = true;
                    }
                }

                if (ufo != null) {
                    ufo.move().updatePosition(boardX, boardY);
                    if (ufo != null) ufo.update();
                }

                collisionDetection();

                allDead = true;
                for (int i = 0; i < aliens.length; i++) {
                    if (aliens[i].isVisible) {
                        allDead = false;
                    }
                }

                if (allDead) {
                    if (!nextLevel) {
                        tank.canMove = false;
                        for (int i = 0; i < aliens.length; i++) aliens[i].canMove = false;
                        nextLevel = true;
                        mazeBlinkTick = tickCounter;
                    }
                    if (((tickCounter - mazeBlinkTick) == 40 && mazeBlinks == 0) || ((tickCounter - mazeBlinkTick) == 5 && mazeBlinks < 5)) {
                        mazeBlinkTick = tickCounter;
                        mazeBlink = !mazeBlink;
                        mazeBlinks++;
                    }
                    if (mazeBlinks == 5) {
                        level++;
                        mazeBlink = false;
                        mazeBlinks = 0;
                        allDead = false;
                        tickCounter = deathTick = gameOverTick = 0;
                        setupTiles();
                        setupGame();
                    }
                }

                if (showBonus && (tickCounter - bonusTick) == bonusTime) showBonus = false;
            } else {
                if ((tickCounter - startTick) == 35) {
                    playing = true;
                    tank.canMove = true;
                    for (int i = 0; i < aliens.length; i++) aliens[i].canMove = true;
                }
                if (!updatePos) {
                    updatePos = true;
                    tank.updatePosition(boardX, boardY);
                    for (int i = 0; i < aliens.length; i++) aliens[i].updatePosition(boardX, boardY);
                }
            }
        }
    }

    private void setGuiColor(GuiGraphics guiGraphics, Color color) {
        guiGraphics.setColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 1.0f);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        boardX = xScaled - (GUI_X / 2) + 5;
        boardY = yScaled - (GUI_Y / 2) + 14;

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);

        if (inMenu) {
            switch (menu) {
                case 0:
                    guiGraphics.drawCenteredString(font, Component.translatable("game.arcademod.spaceinvaders"), boardX + (GUI_X / 2), boardY + 2, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.start"), boardX + (GUI_X / 2), boardY + (GUI_Y / 2) - 30, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.control"), boardX + (GUI_X / 2), boardY + (GUI_Y / 2) - 20, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.setting"), boardX + (GUI_X / 2), boardY + (GUI_Y / 2) - 10, 0xFFFFFF);

                    if (menuOption == 0) drawRightArrow(guiGraphics, boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 32);
                    else if (menuOption == 1) drawRightArrow(guiGraphics, boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 22);
                    else if (menuOption == 2) drawRightArrow(guiGraphics, boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 12);
                    break;
                case 1:
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.control"), boardX + (GUI_X / 2), boardY + 2, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[UP] Shoot"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 20, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[LEFT] Left"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2), 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[RIGHT] Right"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) + 10, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[LEFT] Back"), boardX + 2, boardY + GUI_Y - 30, 0xFFFFFF);
                    break;
                case 2:
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.setting"), boardX + (GUI_X / 2), boardY + 2, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("text.arcademod.volume"), boardX + (GUI_X / 2), boardY + (GUI_Y / 2) - 30, 0xFFFFFF);
                    // drawVolumeBar(boardX + (GUI_X / 2), boardY + (GUI_Y / 2) - 20);
                    guiGraphics.drawString(font, Component.literal("[ENTER] " + (editVolume ? "Save" : "Edit")), boardX + 2, boardY + GUI_Y - 40, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[LEFT] Back"), boardX + 2, boardY + GUI_Y - 30, 0xFFFFFF);
                    break;
                case 3:
                    guiGraphics.drawCenteredString(font, Component.translatable("text.arcademod.gameover"), boardX + (GUI_X / 2), boardY + (GUI_Y / 2) - 20, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.literal("Score: " + score), boardX + (GUI_X / 2), boardY + (GUI_Y / 2), 0xFFFFFF);
                    break;
            }
        } else {
            drawMaze(guiGraphics);

            for (int y = 0; y < 31; y++) {
                for (int x = 0; x < 28; x++) {
                    if (tiles[y][x] != null) tiles[y][x].updatePosition(boardX, boardY).drawTile(guiGraphics);
                }
            }

            if (playing) {
                for (int i = 0; i < aliens.length; i++) {
                    if (aliens[i].isVisible) aliens[i].drawAlien(guiGraphics);
                }
            }

            tank.drawPlayer(guiGraphics).drawLives(guiGraphics);

            if (bullet != null) bullet.drawBullet(guiGraphics);

            for (int i = 0; i < bombs.length; i++) {
                if (bombs[i].isVisible) bombs[i].drawBomb(guiGraphics);
            }

            if (ufo != null) ufo.drawUFO(guiGraphics);

            if (!playing) guiGraphics.drawCenteredString(font, Component.translatable("text.arcademod.ready.spaceinvaders"), boardX + (MAZE_X / 2), boardY + (MAZE_Y / 2) + 13, 0xFFFF00);

            guiGraphics.drawString(font, Component.literal("Score: " + score), boardX + 10, boardY + 6, 0xFFFFFF);
            guiGraphics.drawString(font, Component.literal("Level: " + level), boardX + 170, boardY + 6, 0xFFFFFF);
        }

        guiGraphics.pose().popPose();
    }

    private void drawMaze(GuiGraphics guiGraphics) {
        if (mazeBlink) setGuiColor(guiGraphics, Color.WHITE);
        else setGuiColor(guiGraphics, new Color(33, 33, 222));
        guiGraphics.blit(TEXTURE, boardX, boardY, GUI_X, 0, MAZE_X, MAZE_Y, 512, 512);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void collisionDetection() {
        if (bullet != null) {
            for (int i = 0; i < aliens.length; i++) {
                if (aliens[i].isVisible &&
                        bullet.extendedX >= (aliens[i].extendedX - 7) &&
                        bullet.extendedX <= (aliens[i].extendedX + 7) &&
                        bullet.extendedY >= (aliens[i].extendedY - 7) &&
                        bullet.extendedY <= (aliens[i].extendedY + 7)) {
                    aliens[i].isVisible = false;
                    bullet = null;
                    score += 10;
                    world.playSound(player, player.blockPosition(), superhb.arcademod.init.ModRegistries.SPACEINVADERS_EXPLODE.get(), net.minecraft.sounds.SoundSource.BLOCKS, getVolume() * 0.7f, 1.0f);
                    break;
                }
            }
        }

        if (activeBombs > 0) {
            for (int i = 0; i < bombs.length; i++) {
                if (bombs[i].isVisible &&
                        bombs[i].extendedX >= (tank.extendedX - 7) &&
                        bombs[i].extendedX <= (tank.extendedX + 7) &&
                        bombs[i].extendedY + 7 >= (tank.extendedY - 7) &&
                        bombs[i].extendedY <= (tank.extendedY + 7)) {
                    world.playSound(player, player.blockPosition(), superhb.arcademod.init.ModRegistries.SPACEINVADERS_DESTROYED.get(), net.minecraft.sounds.SoundSource.BLOCKS, getVolume() * 0.7f, 1.0f);
                    endGame();
                }
            }
        }

        if (bullet != null && ufo != null) {
            if (bullet.extendedX >= (ufo.extendedX - 7) &&
                    bullet.extendedX <= (ufo.extendedX + 7) &&
                    bullet.extendedY >= (ufo.extendedY - 7) &&
                    bullet.extendedY <= (ufo.extendedY + 7) &&
                    ufo.isVisible) {
                bullet = null;
                ufo = null;
                score += 50;
            }
        }
    }

    private void endGame() {
        if (!gameOver) {
            gameOver = true;
            tank.canMove = false;
            for (int i = 0; i < aliens.length; i++) aliens[i].canMove = false;
            tank.kill();
        }
    }

    private void setupGame() {
        resetGame();

        if (gameOver) {
            tank.reset();
            tickCounter = deathTick = gameOverTick = 0;
            gameOver = false;
        }
        startTick = tickCounter;
        playing = false;
    }

    private void respawn() {
        if (gameOver) {
            tank.reset();
            tickCounter = deathTick = gameOverTick = 0;
            gameOver = false;
        }
        startTick = tickCounter;
        playing = false;
        activeBombs = 0;
    }

    private void resetGame() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 8; j++) {
                int id = i * 8 + j;
                if (i == 0) aliens[id] = new Alien(id, 7 + (j * 2), 5 + (i * 2), new Color(29, 226, 255), true);
                if (i == 1) aliens[id] = new Alien(id, 7 + (j * 2), 5 + (i * 2), new Color(30, 180, 50), true);
                if (i == 2) aliens[id] = new Alien(id, 7 + (j * 2), 5 + (i * 2), new Color(255, 180, 0), true);
                if (i == 3) aliens[id] = new Alien(id, 7 + (j * 2), 5 + (i * 2), new Color(180, 30, 180), true);
            }
        }
        allDead = false;
        nextLevel = false;
        for (int i = 0; i < bombs.length; i++) bombs[i] = new Bomb(i, 1, 1, new Color(0, 0, 0), false);
        activeBombs = 0;
    }

    private void startGame() {
        score = 0;
        level = 1;
        inMenu = false;
        tank = new ArcadePlayer();
        canGetCoinBack = false;

        setupTiles();
        setupGame();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (inMenu) {
            if (menu == 0) {
                if (keyCode == GLFW.GLFW_KEY_DOWN) {
                    menuOption = (menuOption == 2) ? 0 : menuOption + 1;
                } else if (keyCode == GLFW.GLFW_KEY_UP) {
                    menuOption = (menuOption == 0) ? 2 : menuOption - 1;
                } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    if (menuOption == 0) startGame();
                    else menu = menuOption;
                }
            } else if (menu == 2) {
                if (keyCode == GLFW.GLFW_KEY_LEFT) {
                    if (editVolume) decreaseVolume();
                    else menu = 0;
                } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                    if (editVolume) increaseVolume();
                } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    if (editVolume) {
                        editVolume = false;
                        saveVolume(true);
                    } else editVolume = true;
                }
            } else if (menu == 1 || menu == 3) {
                if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_ESCAPE) menu = 0;
            }
        } else {
            if (keyCode == GLFW.GLFW_KEY_LEFT) tank.current = Direction.LEFT;
            else if (keyCode == GLFW.GLFW_KEY_RIGHT) tank.current = Direction.RIGHT;
            else if (keyCode == GLFW.GLFW_KEY_UP) {
                if (bullet == null) {
                    bullet = new Bullet(0, tank.x, tank.y, new Color(255, 0, 0), true);
                    bullet.canMove = true;
                    world.playSound(player, player.blockPosition(), superhb.arcademod.init.ModRegistries.SPACEINVADERS_SHOOT.get(), net.minecraft.sounds.SoundSource.BLOCKS, getVolume() * 0.7f, 1.0f);
                }
            }
            // ESC key - give tickets when exiting mid-game (matches 1.12.2 behavior)
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                int tickets = score / 200;
                if (tickets > 0) {
                    giveReward(new ItemStack(superhb.arcademod.init.ModRegistries.TICKET.get(), tickets));
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private enum Direction {
        STAND, UP, DOWN, LEFT, RIGHT
    }

    private enum EnumTile {
        PLAY, WALL, TELE, TELE_ZONE, GHOST_ONLY, GHOST_LIMIT
    }

    private class Tile {
        int x, y, extendedX, extendedY;
        EnumTile type;

        Tile(int x, int y, EnumTile type) {
            this.x = x;
            this.y = y;
            this.extendedX = x * 8 + boardX;
            this.extendedY = y * 8 + boardY;
            this.type = type;
        }

        Tile drawTile(GuiGraphics guiGraphics) {
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            return this;
        }

        Tile updatePosition(int x, int y) {
            extendedX = this.x * 8 + x;
            extendedY = this.y * 8 + y;
            return this;
        }
    }

    private class ArcadePlayer extends Mover {
        int lives = 3, deathAnimation;
        boolean playDeathAnimation;
        int STATE = 0;
        int prevX, prevY;

        public ArcadePlayer() {
            super(14, 29);
            prevX = 14;
            prevY = 29;
            current = Direction.LEFT;
        }

        private Mover move() {
            if (canMove) {
                switch (current) {
                    case LEFT:
                        if (!isBlockedLeft()) {
                            if (GLFW.glfwGetKey(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
                                moveX -= getSpeed();
                            }
                        } else current = Direction.STAND;
                        return this;
                    case RIGHT:
                        if (!isBlockedRight()) {
                            if (GLFW.glfwGetKey(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
                                moveX += getSpeed();
                            }
                        } else current = Direction.STAND;
                        return this;
                    default:
                        return this;
                }
            }
            return this;
        }

        @Override
        public void update() {
            if (playDeathAnimation) {
                if (deathAnimation <= 12) {
                    if ((tickCounter - deathTick) == 2) {
                        deathTick = tickCounter;
                        deathAnimation++;
                    }
                }
                if (deathAnimation >= 12 && gameOverTick == 0) {
                    moveX = moveY = 0;
                    setStartPos(14, 29);
                    prevX = 14;
                    prevY = 29;
                    gameOverTick = tickCounter;
                }
                if ((tickCounter - gameOverTick) == 20) {
                    if (lives == 0) {
                        inMenu = true;
                        menu = 3;
                        playDeathAnimation = false;
                        backTick = tickCounter;
                        gameOver = mazeBlink = nextLevel = updatePos = false;
                        int tickets = score / 200;
                        if (tickets > 0) {
                            Arcade.logger.info("GuiSpaceInvaders: awarding tickets=" + tickets);
                            giveReward(new ItemStack(superhb.arcademod.init.ModRegistries.TICKET.get(), tickets));
                        }
                    } else {
                        lives--;
                        respawn();
                        deathAnimation = 0;
                        playDeathAnimation = false;
                        showBonus = false;
                        bonusTick = 0;
                    }
                }
            }
        }

        public void kill() {
            playDeathAnimation = true;
            deathTick = tickCounter;
        }

        private void reset() {
            setStartPos(14, 29);
            prevX = 14;
            prevY = 29;
            moveX = moveY = 0;
            current = Direction.LEFT;
        }

        private float getSpeed() {
            if (level == 0) return 0.8f;
            if (level >= 1 && level <= 3) return 0.9f;
            if (level >= 4 && level <= 19) return 1;
            if (level >= 20) return 0.9f;
            return 0.8f;
        }

        @Override
        public ArcadePlayer updatePosition(int x, int y) {
            super.updatePosition(x, y);
            if ((this.x != prevX) || (this.y != prevY)) {
                prevX = this.x;
                prevY = this.y;
                if (STATE == 0) STATE = 1;
                else STATE = 0;
            }
            return this;
        }

        private ArcadePlayer drawPlayer(GuiGraphics guiGraphics) {
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            if (!gameOver) {
                switch (current) {
                    case STAND:
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        guiGraphics.blit(TEXTURE, extendedX - 4, extendedY - 4, TANK * 0, GUI_Y + (300 - GUI_Y), TANK, TANK, 512, 512);
                        break;
                }
            } else {
                setGuiColor(guiGraphics, Color.WHITE);
                if (playDeathAnimation && deathAnimation <= 12) {
                    guiGraphics.blit(TEXTURE, extendedX - 4, extendedY - 4, TANK * deathAnimation, GUI_Y + (TANK * 2) + (300 - GUI_Y), TANK, TANK, 512, 512);
                }
                guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
            return this;
        }

        private ArcadePlayer drawLives(GuiGraphics guiGraphics) {
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            for (int i = 0; i < lives; i++) {
                guiGraphics.blit(TEXTURE, offsetX + (i * 14), offsetY + 248, TANK * 0, GUI_Y + (300 - GUI_Y), TANK, TANK, 512, 512);
            }
            return this;
        }
    }

    private class Alien extends Mover {
        private int alienid;
        private Color aliencolor;
        private boolean isVisible;
        int BODY_STATE = 0;

        public Alien(int id, int startx, int starty, Color color, boolean isVisible) {
            super(startx, starty);
            alienid = id;
            aliencolor = color;
            this.isVisible = isVisible;
            current = Direction.LEFT;
        }

        private Mover move() {
            if (canMove) {
                switch (current) {
                    case LEFT:
                        if (!isBlockedLeft()) moveX -= getSpeed();
                        return this;
                    case RIGHT:
                        if (!isBlockedRight()) moveX += getSpeed();
                        return this;
                    case UP:
                        if (!isBlocked()) moveY -= getSpeed();
                        return this;
                    case DOWN:
                        if (!isBlockedDown()) moveY += 3;
                        return this;
                    default:
                        return this;
                }
            }
            return this;
        }

        private float getSpeed() {
            if (level == 1 || level == 2) return 0.75f;
            if (level == 3 || level == 4) return 1.0f;
            if (level >= 5) return 2.0f;
            return 0.75f;
        }

        @Override
        public void update() {
            if ((extendedX - offsetX) % 4 == 0) {
                if (BODY_STATE == 0) BODY_STATE = 1;
                else BODY_STATE = 0;
            }
        }

        private Alien ai() {
            if (current == Direction.LEFT) {
                if (isBlockedLeft(x - 1, y)) {
                    desiredDirection = Direction.DOWN;
                }
            }
            if (current == Direction.RIGHT) {
                if (isBlockedRight(x + 1, y)) {
                    desiredDirection = Direction.DOWN;
                }
            }
            if (current == Direction.DOWN) {
                if (isBlockedDown(x, y + 2)) {
                    if (isVisible) {
                        tank.lives = 0;
                        endGame();
                    }
                }
            }
            return this;
        }

        private boolean isBlockedDown(int x, int y) {
            return tiles[Math.min(30, y + 1)][Math.max(0, x)].type == EnumTile.WALL;
        }

        private boolean isBlockedLeft(int x, int y) {
            return tiles[y][Math.max(0, x - 1)].type == EnumTile.WALL;
        }

        private boolean isBlockedRight(int x, int y) {
            return tiles[y][Math.min(27, x + 1)].type == EnumTile.WALL;
        }

        @Override
        public boolean isBlockedDown() {
            return isBlockedDown(x, y);
        }

        private Alien drawAlien(GuiGraphics guiGraphics) {
            if (!isVisible) return this;
            setGuiColor(guiGraphics, aliencolor);
            int startX = GUI_X + 10;

            if (alienid < 32) startX = GUI_X + 10;
            if (alienid < 24) startX = GUI_X + 10 + (ALIEN * 6);
            if (alienid < 16) startX = GUI_X + 10 + (ALIEN * 4);
            if (alienid < 8) startX = GUI_X + 10 + (ALIEN * 2);

            if (BODY_STATE == 0) guiGraphics.blit(TEXTURE, extendedX - 3, extendedY - 3, startX, MAZE_Y, ALIEN, ALIEN, 512, 512);
            else guiGraphics.blit(TEXTURE, extendedX - 3, extendedY - 3, startX + ALIEN, MAZE_Y, ALIEN, ALIEN, 512, 512);

            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            return this;
        }
    }

    private class Bullet extends Mover {
        private int id;
        private Color color;
        private boolean isVisible;
        int BODY_STATE = 0;

        public Bullet(int id, int startx, int starty, Color color, boolean isVisible) {
            super(startx, starty);
            this.id = id;
            this.color = color;
            this.isVisible = isVisible;
        }

        private Mover move() {
            if (canMove) {
                if (!isBlocked(x, y - 4)) {
                    if (isVisible) {
                        moveY -= getSpeed();
                    }
                } else {
                    bullet = null;
                    return this;
                }
            }
            return this;
        }

        private float getSpeed() {
            return 5.0f;
        }

        @Override
        public void update() {
            if ((extendedX - offsetX) % 4 == 0) {
                if (BODY_STATE == 0) BODY_STATE = 1;
                else BODY_STATE = 0;
            }
        }

        private boolean isBlocked(int x, int y) {
            return tiles[Math.max(0, y - 1)][Math.max(0, x)].type == EnumTile.WALL;
        }

        private boolean isBlockedDown(int x, int y) {
            return tiles[Math.min(30, y + 1)][Math.max(0, x)].type == EnumTile.WALL;
        }

        @Override
        public boolean isBlockedDown() {
            return isBlockedDown(x, y);
        }

        private Bullet drawBullet(GuiGraphics guiGraphics) {
            if (!isVisible) return this;
            setGuiColor(guiGraphics, color);
            guiGraphics.blit(TEXTURE, extendedX - 3, extendedY - 3, 30, 301, 14, 4, 512, 512);
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            return this;
        }
    }

    private class Bomb extends Mover {
        private int id;
        private Color color;
        private boolean isVisible;
        int BODY_STATE = 0;

        public Bomb(int id, int startx, int starty, Color color, boolean isVisible) {
            super(startx, starty);
            this.id = id;
            this.color = color;
            this.isVisible = isVisible;
        }

        private Mover move() {
            if (canMove) {
                if (!isBlockedDown(x, y + 1)) {
                    if (isVisible) moveY += getSpeed();
                } else {
                    canMove = false;
                    isVisible = false;
                    return this;
                }
            }
            return this;
        }

        private float getSpeed() {
            return 4.0f;
        }

        @Override
        public void update() {
            if ((extendedY - offsetY) % 4 == 0) {
                if (BODY_STATE == 0) BODY_STATE = 1;
                else BODY_STATE = 0;
            }
        }

        private boolean isBlockedDown(int x, int y) {
            return tiles[Math.min(30, y + 1)][Math.max(0, x)].type == EnumTile.WALL;
        }

        @Override
        public boolean isBlockedDown() {
            return isBlockedDown(x, y);
        }

        private Bomb drawBomb(GuiGraphics guiGraphics) {
            if (!isVisible) return this;
            setGuiColor(guiGraphics, color);
            if (BODY_STATE == 0) guiGraphics.blit(TEXTURE, extendedX - 3, extendedY - 3, 240, 270, 14, 14, 512, 512);
            else guiGraphics.blit(TEXTURE, extendedX - 3, extendedY - 3, 257, 270, 14, 14, 512, 512);
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            return this;
        }
    }

    private class UFO extends Mover {
        private int id;
        private Color color;
        private boolean isVisible;
        int BODY_STATE = 0;

        public UFO(int id, int startx, int starty, Color color, boolean isVisible) {
            super(startx, starty);
            this.id = id;
            this.color = color;
            this.isVisible = isVisible;
        }

        private Mover move() {
            if (canMove) {
                if (!isBlockedDown(x - 1, y)) {
                    if (isVisible) moveX -= getSpeed();
                } else {
                    canMove = false;
                    isVisible = false;
                    ufo = null;
                    return this;
                }
            }
            return this;
        }

        private float getSpeed() {
            return 2.0f;
        }

        @Override
        public void update() {
            if ((extendedY - offsetY) % 4 == 0) {
                if (BODY_STATE == 0) BODY_STATE = 1;
                else BODY_STATE = 0;
            }
        }

        private boolean isBlockedDown(int x, int y) {
            return tiles[Math.min(30, y + 1)][Math.max(0, x)].type == EnumTile.WALL;
        }

        @Override
        public boolean isBlockedDown() {
            return isBlockedDown(x, y);
        }

        private UFO drawUFO(GuiGraphics guiGraphics) {
            if (!isVisible) return this;
            setGuiColor(guiGraphics, color);
            int startX = GUI_X + 10 + (ALIEN * 8);
            if (BODY_STATE == 0) guiGraphics.blit(TEXTURE, extendedX - 3, extendedY - 3, startX, MAZE_Y, ALIEN, ALIEN, 512, 512);
            else {
                setGuiColor(guiGraphics, color.brighter());
                guiGraphics.blit(TEXTURE, extendedX - 3, extendedY - 3, startX, MAZE_Y, ALIEN, ALIEN, 512, 512);
            }
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            return this;
        }
    }

    private class Mover {
        Direction current;
        int x, y;
        int x1, y1;
        float moveX, moveY;
        int extendedX, extendedY;
        int offsetX, offsetY;
        public boolean canMove;

        private Mover(int x, int y) {
            setStartPos(x, y);
        }

        public Mover updatePosition(int x, int y) {
            extendedX = (int)(x1 + x + moveX);
            extendedY = (int)(y1 + y + moveY);

            offsetX = x;
            offsetY = y;

            if ((extendedX - x) % 8 == 0) this.x = (extendedX - x) / 8;
            if ((extendedY - y) % 8 == 0) this.y = (extendedY - y) / 8;
            return this;
        }

        public void setStartPos(int x, int y) {
            this.x1 = (x * 8) - 4;
            this.y1 = y * 8;
            this.x = x;
            this.y = y;
        }

        public boolean isBlocked() {
            return tiles[Math.max(0, y - 1)][x].type == EnumTile.WALL;
        }

        public boolean isBlockedDown() {
            return tiles[y + 1][x].type == EnumTile.WALL;
        }

        boolean isBlockedLeft() {
            return tiles[y][Math.max(0, x - 1)].type == EnumTile.WALL;
        }

        boolean isBlockedRight() {
            return tiles[y][Math.min(27, x + 1)].type == EnumTile.WALL;
        }

        public void update() {}
    }

    private void setupTiles() {
        for (int i = 0; i < 28; i++) tiles[0][i] = new Tile(i, 0, EnumTile.WALL);
        for (int j = 1; j <= 29; j++) {
            for (int i = 0; i < 28; i++) {
                if (i == 0 || i == 27) tiles[j][i] = new Tile(i, j, EnumTile.WALL);
                else tiles[j][i] = new Tile(i, j, EnumTile.PLAY);
            }
        }
        for (int i = 0; i < 28; i++) tiles[30][i] = new Tile(i, 30, EnumTile.WALL);
    }

    @Override
    public void onClose() {
        if (theme != null) {
            minecraft.getSoundManager().stop(theme);
        }
        super.onClose();
    }
}
