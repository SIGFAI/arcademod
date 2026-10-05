package superhb.arcademod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import org.lwjgl.glfw.GLFW;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArcade;
import superhb.arcademod.Arcade;
import superhb.arcademod.client.ArcadeItems;
import superhb.arcademod.client.audio.LoopingSound;
import superhb.arcademod.client.tileentity.BlockEntityArcade;

import javax.annotation.Nullable;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

public class GuiPacMan extends GuiArcade {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/pacman.png");

    private static final int GUI_X = 234, GUI_Y = 284;
    private static final int MAZE_X = 224, MAZE_Y = 248;

    private static final int GHOST = 14;
    private static final int PUPIL = 2;
    private static final int EYE_X = 4, EYE_Y = 5;
    private static final int MOUTH_X = 12, MOUTH_Y = 2;
    private static final int BONUS = 14;
    private static final int PACMAN = 15;
    private static final int DOT = 2;
    private static final int ENERGIZER = 8;

    private int waka;

    private int boardX, boardY;
    private int score;
    private final Tile[][] tiles = new Tile[31][28];
    private int ENERGIZER_STATE = 0;
    private byte level;
    private boolean gameOver, mazeBlink, nextLevel;
    private int mazeBlinkTick = 0;
    private int mazeBlinks = 0;
    private boolean playing, updatePos;
    private int startTick = 0;
    private EnumBonus bonus;
    private int bonusTick, bonusTime;
    private boolean showBonus;
    private int backTick;

    private ArcadePlayer pacman;
    private final Ghost[] ghosts = new Ghost[4];
    private int deathTick = 0, gameOverTick = 0;
    private int energizerTick = 0, scatterTick = 0;
    private int scaredTime = 0, scaredFlash = 0;
    private final ArrayList<Integer> houseQueue = new ArrayList<>();
    private boolean useGlobalCounter;
    private int globalCounter;
    private int dotTick, dotTimeLimit = 80;

    public GuiPacMan(Level world, BlockEntityArcade tile, @Nullable BlockPos pos, Player player) {
        super(world, tile, pos, player, Component.literal("Pellet Muncher"));
        setGuiSize(GUI_X, GUI_Y, 0.8F);
        setTexture(TEXTURE, 512, 512);
        setCost(4);
        setOffset(0, 0);
        setButtonPos((GUI_X / 2) - (buttonWidth / 2), GUI_Y - 30);
        setStartMenu(0);
    }

    private LoopingSound theme;
    private LoopingSound siren, fright, ghostEaten;

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
            // Stop all gameplay sounds when in menu
            if (siren != null) {
                if (minecraft.getSoundManager().isActive(siren)) minecraft.getSoundManager().stop(siren);
                siren = null;
            }
            if (fright != null) {
                if (minecraft.getSoundManager().isActive(fright)) minecraft.getSoundManager().stop(fright);
                fright = null;
            }
            if (ghostEaten != null) {
                if (minecraft.getSoundManager().isActive(ghostEaten)) minecraft.getSoundManager().stop(ghostEaten);
                ghostEaten = null;
            }
            if (menu == 3) {
                if ((tickCounter - backTick) == 60) {
                    tickCounter = score = ENERGIZER_STATE = mazeBlinks = mazeBlinkTick = deathTick = gameOverTick = energizerTick = scatterTick = scaredTime = scaredFlash = level = 0;
                    checkMenuAfterGameOver();
                }
            }
        } else {
            if (theme == null) {
                theme = new LoopingSound(tileEntity, superhb.arcademod.init.ModRegistries.PACMAN_INTRO.get(), SoundSource.BLOCKS, getVolume());
            }
            if (!playing && !minecraft.getSoundManager().isActive(theme)) {
                minecraft.getSoundManager().play(theme);
            } else if (playing && minecraft.getSoundManager().isActive(theme)) {
                minecraft.getSoundManager().stop(theme);
            }

            if (playing) {
                // Siren/Fright sound logic (ported from 1.12.2)
                if (pacman.canEatGhost) {
                    // Ghosts are scared - play fright music, stop siren
                    if (siren != null && minecraft.getSoundManager().isActive(siren)) minecraft.getSoundManager().stop(siren);
                    if (fright == null) fright = new LoopingSound(tileEntity, superhb.arcademod.init.ModRegistries.PACMAN_FRIGHT.get(), SoundSource.BLOCKS, getVolume());
                    if (!minecraft.getSoundManager().isActive(fright) && !gameOver) minecraft.getSoundManager().play(fright);
                } else {
                    // Normal mode - play siren, stop fright
                    if (fright != null && minecraft.getSoundManager().isActive(fright)) minecraft.getSoundManager().stop(fright);
                    if (siren == null) siren = new LoopingSound(tileEntity, superhb.arcademod.init.ModRegistries.PACMAN_SIREN.get(), SoundSource.BLOCKS, getVolume());
                    if (!minecraft.getSoundManager().isActive(siren) && !gameOver) minecraft.getSoundManager().play(siren);
                }

                for (int i = 0; i < 3; i++) pacman.move().updatePosition(boardX, boardY);
                pacman.update();

                for (int i = 0; i < ghosts.length; i++) {
                    for (int j = 0; j < (ghosts[i].eaten ? 6 : 3); j++) ghosts[i].ai().move().updatePosition(boardX, boardY);
                    ghosts[i].update();
                }

                collisionDetection();

                if ((tickCounter - energizerTick) >= 10) {
                    energizerTick = tickCounter;
                    ENERGIZER_STATE = (ENERGIZER_STATE == 0) ? 1 : 0;
                }

                if (pacman.foodEaten == (244 + (244 * level))) {
                    if (!nextLevel) {
                        pacman.canMove = false;
                        for (Ghost ghost : ghosts) ghost.canMove = false;
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
                        waka = mazeBlinks = 0;
                        tickCounter = deathTick = energizerTick = scatterTick = gameOverTick = mazeBlinkTick = 0;
                        pacman.reset();
                        setupTiles();
                        setupGame();
                    }
                }

                if (pacman.foodEaten == 70 || pacman.foodEaten == 170) {
                    bonusTime = (int)((world.random.nextFloat() + 9) * 20);
                    bonusTick = tickCounter;
                    showBonus = true;
                }

                if (showBonus && (tickCounter - bonusTick) == bonusTime) showBonus = false;
            } else {
                if ((tickCounter - startTick) == 80) {
                    playing = true;
                    pacman.canMove = true;
                    for (Ghost ghost : ghosts) ghost.canMove = true;
                    dotTick = tickCounter;
                }
                if (!updatePos) {
                    updatePos = true;
                    pacman.updatePosition(boardX, boardY);
                    for (Ghost ghost : ghosts) ghost.updatePosition(boardX, boardY);
                }
            }
        }
    }

    private void setGuiColor(GuiGraphics guiGraphics, Color color) {
        guiGraphics.setColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 1.0f);
    }

    private void playWaka() {
        net.minecraft.sounds.SoundEvent sound = null;
        switch (waka) {
            case 0: sound = superhb.arcademod.init.ModRegistries.PACMAN_WAKA_1.get(); break;
            case 1: sound = superhb.arcademod.init.ModRegistries.PACMAN_WAKA_2.get(); break;
            case 2: sound = superhb.arcademod.init.ModRegistries.PACMAN_WAKA_3.get(); break;
            case 3: sound = superhb.arcademod.init.ModRegistries.PACMAN_WAKA_4.get(); break;
            case 4: sound = superhb.arcademod.init.ModRegistries.PACMAN_WAKA_5.get(); break;
            case 5: sound = superhb.arcademod.init.ModRegistries.PACMAN_WAKA_6.get(); break;
        }
        if (sound != null) {
            world.playSound(player, pos != null ? pos : player.blockPosition(), sound, SoundSource.BLOCKS, getVolume(), 1.0f);
        }
        waka++;
        if (waka > 5) waka = 0;
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
                    guiGraphics.drawCenteredString(font, Component.translatable("game.arcademod.pacman"), boardX + (GUI_X / 2), boardY + 2, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.start"), boardX + (GUI_X / 2), boardY + (GUI_Y / 2) - 30, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.control"), boardX + (GUI_X / 2), boardY + (GUI_Y / 2) - 20, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.setting"), boardX + (GUI_X / 2), boardY + (GUI_Y / 2) - 10, 0xFFFFFF);

                    if (menuOption == 0) drawRightArrow(guiGraphics, boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 32);
                    else if (menuOption == 1) drawRightArrow(guiGraphics, boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 22);
                    else if (menuOption == 2) drawRightArrow(guiGraphics, boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 12);
                    break;
                case 1:
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.control"), boardX + (GUI_X / 2), boardY + 2, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[UP] Up"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 20, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[DOWN] Down"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) - 10, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[LEFT] Left"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2), 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[RIGHT] Right"), boardX + (GUI_X / 2) - 30, boardY + (GUI_Y / 2) + 10, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[LEFT] Back"), boardX + 2, boardY + GUI_Y - 30, 0xFFFFFF);
                    break;
                case 2:
                    guiGraphics.drawCenteredString(font, Component.translatable("option.arcademod.setting"), boardX + (GUI_X / 2), boardY + 2, 0xFFFFFF);
                    guiGraphics.drawCenteredString(font, Component.translatable("text.arcademod.volume"), boardX + (GUI_X / 2), boardY + (GUI_Y / 2) - 30, 0xFFFFFF);
                    // drawVolumeBar(boardX + (GUI_X / 2), boardY + (GUI_Y / 2) - 20);
                    guiGraphics.drawString(font, Component.literal("[LEFT] Back"), boardX + 2, boardY + GUI_Y - 30, 0xFFFFFF);
                    guiGraphics.drawString(font, Component.literal("[ENTER] " + (editVolume ? "Save" : "Edit")), boardX + 2, boardY + GUI_Y - 40, 0xFFFFFF);
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

            for (Ghost ghost : ghosts) ghost.drawGhost(guiGraphics);

            pacman.drawPlayer(guiGraphics).drawLives(guiGraphics);

            drawBonus(guiGraphics);

            if (!playing) guiGraphics.drawCenteredString(font, Component.translatable("text.arcademod.ready.pacman"), boardX + (MAZE_X / 2), boardY + (MAZE_Y / 2) + 13, 0xFFFF00);

            guiGraphics.drawString(font, Component.literal(String.valueOf(score)), boardX + 30, boardY - 8, 0xFFFFFF);
        }

        guiGraphics.pose().popPose();
    }

    private void drawBonus(GuiGraphics guiGraphics) {
        if (showBonus) {
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            guiGraphics.blit(TEXTURE, boardX + (13 * 8) + 2, boardY + (17 * 8) - 3, GUI_X + ENERGIZER + DOT + (GHOST * 2) + (BONUS * bonus.getId()), MAZE_Y, BONUS, BONUS, 512, 512);
        }
    }

    private void drawMaze(GuiGraphics guiGraphics) {
        if (mazeBlink) setGuiColor(guiGraphics, Color.WHITE);
        else setGuiColor(guiGraphics, new Color(33, 33, 222));
        guiGraphics.blit(TEXTURE, boardX, boardY, GUI_X, 0, MAZE_X, MAZE_Y, 512, 512);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.blit(TEXTURE, boardX + 104, boardY + 101, GUI_X + 8, 264, 16, 2, 512, 512);
    }

    private void collisionDetection() {
        for (Ghost ghost : ghosts) {
            if ((pacman.getTile() == ghost.getTile()) && !ghost.scared && !ghost.eaten) endGame();
        }
    }

    private void pause() {
        pacman.canMove = false;
        for (Ghost ghost : ghosts) ghost.canMove = false;
    }

    private void unpause() {
        pacman.canMove = true;
        for (Ghost ghost : ghosts) ghost.canMove = true;
    }

    private void endGame() {
        if (!gameOver) {
            gameOver = true;
            pacman.canMove = false;
            for (Ghost ghost : ghosts) ghost.canMove = false;
            // Stop siren/fright on death
            if (siren != null && minecraft.getSoundManager().isActive(siren)) minecraft.getSoundManager().stop(siren);
            if (fright != null && minecraft.getSoundManager().isActive(fright)) minecraft.getSoundManager().stop(fright);
            pacman.kill();
            // Match 1.12.2: 1 ticket per 200 points
            int tickets = score / 200;
            if (tickets > 0) {
                Arcade.logger.info("GuiPacMan: awarding tickets=" + tickets);
                giveReward(new ItemStack(superhb.arcademod.init.ModRegistries.TICKET.get(), tickets));
            }
        }
    }

    private void setupGame() {
        getLevelData();
        resetGame();

        if (gameOver) {
            pacman.reset();
            waka = 0;
            tickCounter = deathTick = energizerTick = scatterTick = gameOverTick = 0;
            gameOver = false;
        }
        startTick = tickCounter;
        playing = false;
    }

    private void resetGame() {
        ghosts[0] = new Ghost(EnumGhost.BLINKY);
        ghosts[1] = new Ghost(EnumGhost.PINKY);
        ghosts[2] = new Ghost(EnumGhost.INKY);
        ghosts[3] = new Ghost(EnumGhost.CLYDE);
    }

    private void startGame() {
        score = 0;
        level = 0;
        inMenu = false;
        pacman = new ArcadePlayer();
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
                        saveVolume(true);
                        editVolume = false;
                    } else editVolume = true;
                }
            } else if (menu == 1 || menu == 3) {
                if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_ESCAPE) menu = 0;
            }
        } else {
            if (keyCode == GLFW.GLFW_KEY_LEFT) pacman.desired = Direction.LEFT;
            else if (keyCode == GLFW.GLFW_KEY_RIGHT) pacman.desired = Direction.RIGHT;
            else if (keyCode == GLFW.GLFW_KEY_DOWN) pacman.desired = Direction.DOWN;
            else if (keyCode == GLFW.GLFW_KEY_UP) pacman.desired = Direction.UP;

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
        STAND(0, 0, 0),
        UP(1, 2, 1),
        DOWN(2, 1, 1),
        LEFT(3, 4, 2),
        RIGHT(4, 3, 2);

        private final int direction;
        private final int opposite;
        private final int axis;

        Direction(int direction, int opposite, int axis) {
            this.direction = direction;
            this.opposite = opposite;
            this.axis = axis;
        }

        public int getAxis() {
            return axis;
        }
    }

    private enum EnumGhost {
        BLINKY(0, "Blinky", 14, 11, new Color(255, 7, 7), 0),
        PINKY(1, "Pinky", 14, 14, new Color(255, 184, 222), 7),
        INKY(2, "Inky", 12, 14, new Color(7, 255, 255), 17, new int[]{30, 0, 0}),
        CLYDE(3, "Clyde", 16, 14, new Color(255, 159, 7), 32, new int[]{60, 50, 0});

        private final int id;
        private final String name;
        private final int x, y;
        private final Color color;
        private final int globalLimit;
        private final int[] dotLimit;

        EnumGhost(int id, String name, int x, int y, Color color, int globalLimit) {
            this(id, name, x, y, color, globalLimit, new int[]{0, 0, 0});
        }

        EnumGhost(int id, String name, int x, int y, Color color, int globalLimit, int[] dotLimit) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
            this.color = color;
            this.globalLimit = globalLimit;
            this.dotLimit = dotLimit;
        }

        public int getId() { return id; }
        public int getX() { return x; }
        public int getY() { return y; }
        public Color getColor() { return color; }
        public int getGlobalLimit() { return globalLimit; }
        public int getDotLimit(byte level) {
            if (level > 2) return dotLimit[2];
            return dotLimit[level];
        }
    }

    private enum EnumTile {
        PLAY, WALL, TELE, TELE_ZONE, GHOST_ONLY, GHOST_LIMIT
    }

    private enum EnumBonus {
        CHERRY(0, 100), STRAWBERRY(1, 300), PEACH(2, 500), APPLE(3, 700), GRAPES(4, 1000), GALAXIAN(5, 2000), BELL(6, 3000), KEY(8, 5000);

        private final int id;
        private final int points;

        EnumBonus(int id, int points) {
            this.id = id;
            this.points = points;
        }

        public int getId() { return id; }
        public int getPoints() { return points; }
    }

    private class Tile {
        int x, y, extendedX, extendedY;
        int edible = 0;
        EnumTile type;

        Tile(int x, int y) { this(x, y, EnumTile.PLAY, 1); }
        Tile(int x, int y, int edible) { this(x, y, EnumTile.PLAY, edible); }
        Tile(int x, int y, EnumTile type) { this(x, y, type, 0); }
        Tile(int x, int y, EnumTile type, int edible) {
            this.x = x; this.y = y; this.type = type; this.edible = edible;
        }

        Tile updatePosition(int x, int y) {
            extendedX = this.x * 8 + x;
            extendedY = this.y * 8 + y;
            return this;
        }

        void drawTile(GuiGraphics guiGraphics) {
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            if (edible == 1) guiGraphics.blit(TEXTURE, extendedX + 3, extendedY + 3, GUI_X, MAZE_Y, DOT, DOT, 512, 512);
            else if (edible == 2 && ENERGIZER_STATE == 0) guiGraphics.blit(TEXTURE, extendedX, extendedY, GUI_X + DOT, MAZE_Y, ENERGIZER, ENERGIZER, 512, 512);
        }

        boolean hasEdible() { return edible != 0; }
        Point getPosition() { return new Point(x, y); }
    }

    private class ArcadePlayer extends Mover {
        int lives = 3, foodEaten, ghostsEaten, deathAnimation;
        boolean energizerMode, canEatGhost, playDeathAnimation;
        int STATE = 0, prevX, prevY;

        public ArcadePlayer() {
            super(14, 23);
            prevX = 14; prevY = 23;
            current = desired = Direction.LEFT;
        }

        Mover move() {
            if (desired != current) changeDirection(desired);
            if (checkTile() == EnumTile.TELE) {
                if (extendedX <= (offsetX + 2)) moveX = 103;
                if (extendedX >= (offsetX + (27 * 8))) moveX = -103;
            }
            if (canMove) {
                switch (current) {
                    case LEFT: if (!isBlockedLeft()) moveX -= getSpeed(); else current = Direction.STAND; break;
                    case RIGHT: if (!isBlockedRight()) moveX += getSpeed(); else current = Direction.STAND; break;
                    case UP: if (!isBlocked()) moveY -= getSpeed(); else current = Direction.STAND; break;
                    case DOWN: if (!isBlockedDown()) moveY += getSpeed(); else current = Direction.STAND; break;
                    default: break;
                }
            }
            return this;
        }

        @Override
        public void update() {
            if (tiles[y][x].hasEdible()) {
                if (useGlobalCounter) globalCounter++;
                else if (!houseQueue.isEmpty()) ghosts[houseQueue.get(0)].dotCounter++;
                dotTick = tickCounter;
            }
            if (tiles[y][x].edible == 1) {
                tiles[y][x].edible = 0;
                foodEaten++;
                score += 10;
                playWaka();
            }
            if (tiles[y][x].edible == 2) {
                tiles[y][x].edible = 0;
                foodEaten++;
                score += 50;
                energizerMode = true;
                for (Ghost ghost : ghosts) ghost.beScared = true;
                canEatGhost = true;
                playWaka();
            }
            if (playDeathAnimation) {
                if (deathAnimation <= 12) { if ((tickCounter - deathTick) == 2) { deathTick = tickCounter; deathAnimation++; } }
                if (deathAnimation >= 12 && gameOverTick == 0) { moveX = moveY = 0; setStartPos(14, 23); prevX = 14; prevY = 23; gameOverTick = tickCounter; }
                if ((tickCounter - gameOverTick) == 20) {
                    if (lives == 0) { inMenu = true; menu = 3; playDeathAnimation = false; backTick = tickCounter; houseQueue.clear(); gameOver = mazeBlink = nextLevel = updatePos = false; int tickets = score / 200; if (tickets > 0) giveReward(new ItemStack(superhb.arcademod.init.ModRegistries.TICKET.get(), tickets)); }
                    else { lives--; setupGame(); deathAnimation = 0; useGlobalCounter = true; globalCounter = 0; playDeathAnimation = false; showBonus = false; bonusTick = 0; }
                }
            }
            if (showBonus && (extendedX >= offsetX + (13 * 8) - 2 && (extendedX <= offsetX + (13 * 8) + 2)) && extendedY == offsetY + (17 * 8)) {
                showBonus = false; bonusTick = 0; score += bonus.getPoints();
            }
        }

        public void kill() {
            playDeathAnimation = true;
            deathTick = tickCounter;
            world.playSound(player, pos != null ? pos : player.blockPosition(), superhb.arcademod.init.ModRegistries.PACMAN_DEATH.get(), SoundSource.BLOCKS, getVolume(), 1.0f);
        }

        void reset() { setStartPos(14, 23); prevX = 14; prevY = 23; moveX = moveY = 0; useGlobalCounter = false; current = desired = Direction.LEFT; }

        float getSpeed() {
            if (level == 0) return canEatGhost ? (tiles[y][x].edible != 0 ? 0.79f : 0.9f) : (tiles[y][x].edible != 0 ? 0.71f : 0.8f);
            if (level >= 1 && level <= 3) return canEatGhost ? (tiles[y][x].edible != 0 ? 0.83f : 0.95f) : (tiles[y][x].edible != 0 ? 0.79f : 0.9f);
            if (level >= 4 && level <= 19) return canEatGhost ? (tiles[y][x].edible != 0 ? 0.87f : 1.0f) : (tiles[y][x].edible != 0 ? 0.87f : 1.0f);
            if (level >= 20) return tiles[y][x].edible != 0 ? 0.79f : 0.9f;
            return 0.8f;
        }

        Mover changeDirection(Direction newDirection) {
            if (current.getAxis() != newDirection.getAxis()) { if (onTile()) {
                switch (newDirection) {
                    case LEFT: if (!isBlockedLeft()) current = newDirection; break;
                    case RIGHT: if (!isBlockedRight()) current = newDirection; break;
                    case UP: if (!isBlocked()) current = newDirection; break;
                    case DOWN: if (!isBlockedDown()) current = newDirection; break;
                    default: break;
                }
            } } else {
                switch (newDirection) {
                    case LEFT: if (!isBlockedLeft()) current = newDirection; break;
                    case RIGHT: if (!isBlockedRight()) current = newDirection; break;
                    case UP: if (!isBlocked()) current = newDirection; break;
                    case DOWN: if (!isBlockedDown()) current = newDirection; break;
                    default: break;
                }
            }
            return this;
        }

        @Override
        public ArcadePlayer updatePosition(int x, int y) {
            super.updatePosition(x, y);
            if ((this.x != prevX) || (this.y != prevY)) { prevX = this.x; prevY = this.y; STATE = (STATE == 0) ? 1 : 0; }
            return this;
        }

        ArcadePlayer drawPlayer(GuiGraphics guiGraphics) {
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            if (!gameOver) {
                switch (current) {
                    case STAND: guiGraphics.blit(TEXTURE, extendedX - 4, extendedY - 4, 0, GUI_Y + (300 - GUI_Y), PACMAN, PACMAN, 512, 512); break;
                    case LEFT: guiGraphics.blit(TEXTURE, extendedX - 4, extendedY - 4, PACMAN * STATE, GUI_Y + PACMAN + (300 - GUI_Y), PACMAN, PACMAN, 512, 512); break;
                    case RIGHT: guiGraphics.blit(TEXTURE, extendedX - 4, extendedY - 4, PACMAN * STATE, GUI_Y + (300 - GUI_Y), PACMAN, PACMAN, 512, 512); break;
                    case DOWN: guiGraphics.blit(TEXTURE, extendedX - 4, extendedY - 4, PACMAN * STATE, GUI_Y + (PACMAN * 3) + (300 - GUI_Y), PACMAN, PACMAN, 512, 512); break;
                    case UP: guiGraphics.blit(TEXTURE, extendedX - 4, extendedY - 4, PACMAN * STATE, GUI_Y + (PACMAN * 2) + (300 - GUI_Y), PACMAN, PACMAN, 512, 512); break;
                }
            } else if (playDeathAnimation && deathAnimation <= 12) {
                setGuiColor(guiGraphics, Color.WHITE);
                guiGraphics.blit(TEXTURE, extendedX - 4, extendedY - 4, PACMAN * deathAnimation, GUI_Y + (PACMAN * 2) + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
                guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
            return this;
        }

        void drawLives(GuiGraphics guiGraphics) {
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            for (int i = 0; i < lives; i++) guiGraphics.blit(TEXTURE, offsetX + (i * 14), offsetY + 248, PACMAN, GUI_Y + PACMAN + (300 - GUI_Y), PACMAN, PACMAN, 512, 512);
        }
    }

    private class Ghost extends Mover {
        EnumGhost info;
        boolean scared = false, eaten = false, scatter = true, inHouse, blink, beScared, isScared;
        int SCARED_STATE = 0, BODY_STATE = 0, modeTime, mode, blinks, scaredTick, pauseTick, scaredDuration, dotCounter = 0;
        boolean remove, beRemoved;

        public Ghost(EnumGhost ghost) {
            super(ghost.getX(), ghost.getY());
            info = ghost;
            if (ghost != EnumGhost.BLINKY) { inHouse = true; current = desired = Direction.UP; houseQueue.add(info.id); }
            else current = desired = Direction.LEFT;
        }

        Mover move() {
            if (inHouse) {
                if (current == Direction.UP) moveY -= 0.5f;
                if (current == Direction.DOWN) moveY += 0.5f;
                if (extendedY == (offsetY + (13 * 8) + 4)) current = Direction.DOWN;
                if (extendedY == (offsetY + (15 * 8) - 4)) current = Direction.UP;
            } else if (beRemoved) {
                if (current == Direction.RIGHT) moveX += 0.5f;
                if (current == Direction.LEFT) moveX -= 0.5f;
                if (current == Direction.UP) moveY -= 0.5f;
                if (extendedX == (offsetX + (11 * 8) + 4)) current = Direction.RIGHT;
                if (extendedX == (offsetX + (15 * 8) + 4)) current = Direction.LEFT;
                if (extendedX == (offsetX + 14 * 8) - 4) current = Direction.UP;
                if (extendedY == (offsetY + 11 * 8)) { beRemoved = false; remove = true; current = (info == EnumGhost.BLINKY || info == EnumGhost.INKY) ? Direction.RIGHT : Direction.LEFT; }
            } else {
                if (desired != current) changeDirection(desired);
                if (checkTile() == EnumTile.TELE_ZONE || checkTile() == EnumTile.TELE) {
                    if (info == EnumGhost.BLINKY || info == EnumGhost.PINKY) { if ((extendedX <= (offsetX + 2)) && y == 14) moveX = 100; if ((extendedX >= (offsetX + (26 * 8))) && y == 14) moveX = -103; }
                    else if (info == EnumGhost.INKY) { if (extendedX <= (offsetX + 2) && y == 14) moveX = 116; if (extendedX >= (offsetX + (26 * 8)) && y == 14) moveX = -85; }
                    else if (info == EnumGhost.CLYDE) { if (extendedX <= (offsetX + 2) && y == 14) moveX = 84; if (extendedX >= (offsetX + (26 * 8)) && y == 14) moveX = -120; }
                }
                if (beScared) { beScared = false; isScared = true; if (!eaten) {
                    switch (current) {
                        case LEFT: if (!isBlockedRight()) desired = current = Direction.RIGHT; else { if (!isBlocked()) desired = current = Direction.UP; if (!isBlockedDown()) desired = current = Direction.DOWN; if (!isBlockedLeft()) desired = current = Direction.LEFT; } break;
                        case RIGHT: if (!isBlockedLeft()) desired = current = Direction.LEFT; else { if (!isBlocked()) desired = current = Direction.UP; if (!isBlockedDown()) desired = current = Direction.DOWN; if (!isBlockedRight()) desired = current = Direction.RIGHT; } break;
                        case UP: if (!isBlockedDown()) desired = current = Direction.DOWN; else { if (!isBlockedLeft()) desired = current = Direction.LEFT; if (!isBlockedRight()) desired = current = Direction.RIGHT; if (!isBlocked()) desired = current = Direction.UP; } break;
                        case DOWN: if (!isBlocked()) desired = current = Direction.UP; else { if (!isBlockedLeft()) desired = current = Direction.LEFT; if (!isBlockedRight()) desired = current = Direction.RIGHT; if (!isBlockedDown()) desired = current = Direction.DOWN; } break;
                        default: break;
                    }
                } }
                if (canMove) {
                    switch (current) {
                        case LEFT: if (!isBlockedLeft()) moveX -= getSpeed(); break;
                        case RIGHT: if (!isBlockedRight()) moveX += getSpeed(); break;
                        case UP: if (!isBlocked()) moveY -= getSpeed(); break;
                        case DOWN: if (!isBlockedDown()) moveY += getSpeed(); break;
                        default: break;
                    }
                }
            }
            return this;
        }

        float getSpeed() {
            if (eaten) return 1.0f;
            float tunnelMultiplier = (tiles[y][x].type == EnumTile.TELE_ZONE) ? 0.8f : 1.0f;
            if (level == 0) return (scared ? 0.5f : 0.75f) * tunnelMultiplier;
            if (level >= 1 && level <= 3) return (scared ? 0.55f : 0.85f) * tunnelMultiplier;
            return (scared ? 0.6f : 0.95f) * tunnelMultiplier;
        }

        @Override
        public void update() {
            if ((((extendedX - offsetX) / 8) == ((pacman.extendedX - offsetX) / 8)) && (((extendedY - offsetY) / 8) == ((pacman.extendedY - offsetY) / 8))) {
                if (isScared) {
                    isScared = false;
                    world.playSound(player, pos != null ? pos : player.blockPosition(), superhb.arcademod.init.ModRegistries.PACMAN_GHOST.get(), SoundSource.BLOCKS, getVolume(), 1.0f);
                    pauseTick = tickCounter;
                    scaredDuration = tickCounter - scaredTick;
                    eaten = true;
                    pause();
                }
                // Always unpause after the "ghost eaten" freeze.
                // Previous logic gated this on `scared`, which can flip false before the timer elapses,
                // leaving the player/ghosts permanently paused (soft-lock).
                if (pauseTick != 0 && (tickCounter - pauseTick) == 18) {
                    unpause();
                    pauseTick = 0;
                    scaredTick = tickCounter + scaredDuration;
                    scared = false;
                    score += (int)(200 * Math.pow(2, pacman.ghostsEaten));
                    pacman.ghostsEaten++;
                }
            }
            if (eaten && x == 13 && y == 15) { eaten = scared = isScared = false; remove = true; }
            if (inHouse) {
                if (useGlobalCounter) { if (globalCounter == info.getGlobalLimit()) { inHouse = false; beRemoved = true; if (houseQueue.get(0) == 3) useGlobalCounter = false; houseQueue.remove(0); } }
                else if (dotCounter == info.getDotLimit(level)) { inHouse = false; beRemoved = true; houseQueue.remove(0); }
                if ((tickCounter - dotTick) == dotTimeLimit) { dotTick = tickCounter; inHouse = false; beRemoved = true; houseQueue.remove(0); }
            }
            if (remove && ((x == 13 || x == 14) && y == 11)) remove = false;
            if (level == 0) {
                if (mode == 0) modeTime = 7; else if (mode == 1) modeTime = 20; else if (mode == 2) modeTime = 7; else if (mode == 3) modeTime = 20; else if (mode == 4) modeTime = 5; else if (mode == 5) modeTime = 20; else if (mode == 6) modeTime = 5; else if (mode >= 7) modeTime = 0;
            }
            if (modeTime == 0) scatter = false;
            else if ((tickCounter - scatterTick) == (modeTime * 20)) { scatterTick = tickCounter; mode++; scatter = !scatter; }
            if (!eaten) {
                if ((pacman.energizerMode && !scared) || (pacman.energizerMode && isScared)) { scared = true; if (info == EnumGhost.CLYDE) pacman.energizerMode = false; scaredTick = tickCounter; }
                if (scared && (tickCounter - scaredTick) == Math.max((scaredTime - 1), 0) * 20) { scaredTick = tickCounter; blink = true; }
                if (blink) {
                    if ((tickCounter - scaredTick) == (20 / (scaredFlash == 0 ? 1 : scaredFlash))) {
                        scaredTick = tickCounter; SCARED_STATE = (SCARED_STATE == 0) ? 1 : 0; blinks++;
                    }
                    if (blinks == scaredFlash) { SCARED_STATE = blinks = pacman.ghostsEaten = scaredTick = 0; scared = pacman.canEatGhost = blink = isScared = false; }
                }
            }
            if ((extendedX - offsetX) % 4 == 0 && (extendedY - offsetY) % 4 == 0) BODY_STATE = (BODY_STATE == 0) ? 1 : 0;
        }

        Mover changeDirection(Direction newDirection) {
            if (current.getAxis() != newDirection.getAxis()) { if (onTile()) {
                switch (newDirection) {
                    case LEFT: if (!isBlockedLeft()) current = newDirection; break;
                    case RIGHT: if (!isBlockedRight()) current = newDirection; break;
                    case UP: if (!isBlocked()) current = newDirection; break;
                    case DOWN: if (!isBlockedDown()) current = newDirection; break;
                    default: break;
                }
            } }
            return this;
        }

        double calculateDistance(int x, int y) {
            int targetX = (pacman != null) ? pacman.extendedX : offsetX;
            int targetY = (pacman != null) ? pacman.extendedY : offsetY;
            double dx = x - targetX;
            double dy = y - targetY;
            return Math.sqrt(dx * dx + dy * dy);
        }

        private Ghost ai() {
            if (current == Direction.LEFT) {
                if (isBlockedLeft(x - 1, y)) {
                    if (!isBlockedDown(x - 1, y) && !isBlocked(x - 1, y)) desired = calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX - 8, extendedY + 8) ? Direction.UP : Direction.DOWN;
                    else if (!isBlocked(x - 1, y)) desired = Direction.UP; else if (!isBlockedDown(x - 1, y)) desired = Direction.DOWN;
                } else if (!isBlockedDown(x - 1, y) && !isBlocked(x - 1, y)) {
                    double dL = calculateDistance(extendedX - 16, extendedY), dU = calculateDistance(extendedX - 8, extendedY - 8), dD = calculateDistance(extendedX - 8, extendedY + 8);
                    if (dL <= dU && dL <= dD) desired = Direction.LEFT; else if (dU <= dD) desired = Direction.UP; else desired = Direction.DOWN;
                } else if (!isBlocked(x - 1, y)) { if (calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX - 16, extendedY)) desired = Direction.UP; }
                else if (!isBlockedDown(x - 1, y)) { if (calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX - 16, extendedY)) desired = Direction.DOWN; }
            } else if (current == Direction.RIGHT) {
                if (isBlockedRight(x + 1, y)) {
                    if (!isBlockedDown(x + 1, y) && !isBlocked(x + 1, y)) desired = calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX + 8, extendedY + 8) ? Direction.UP : Direction.DOWN;
                    else if (!isBlocked(x + 1, y)) desired = Direction.UP; else if (!isBlockedDown(x + 1, y)) desired = Direction.DOWN;
                } else if (!isBlockedDown(x + 1, y) && !isBlocked(x + 1, y)) {
                    double dR = calculateDistance(extendedX + 16, extendedY), dU = calculateDistance(extendedX + 8, extendedY - 8), dD = calculateDistance(extendedX + 8, extendedY + 8);
                    if (dR <= dU && dR <= dD) desired = Direction.RIGHT; else if (dU <= dD) desired = Direction.UP; else desired = Direction.DOWN;
                } else if (!isBlocked(x + 1, y)) { if (calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX + 16, extendedY)) desired = Direction.UP; }
                else if (!isBlockedDown(x + 1, y)) { if (calculateDistance(extendedX + 8, extendedY + 8) < calculateDistance(extendedX + 16, extendedY)) desired = Direction.DOWN; }
            } else if (current == Direction.UP) {
                if (isBlocked(x, y - 1)) {
                    if (!isBlockedLeft(x, y - 1) && !isBlockedRight(x, y - 1)) {
                        if (tiles[y - 1][x - 1].type == EnumTile.GHOST_LIMIT) desired = Direction.RIGHT;
                        else if (tiles[y - 1][x + 1].type == EnumTile.GHOST_LIMIT) desired = Direction.LEFT;
                        else desired = calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX + 8, extendedY - 8) ? Direction.LEFT : Direction.RIGHT;
                    } else if (!isBlockedLeft(x, y - 1)) desired = Direction.LEFT; else if (!isBlockedRight(x, y - 1)) desired = Direction.RIGHT;
                } else if (!isBlockedRight(x, y - 1) && !isBlockedLeft(x, y - 1)) {
                    double dU = calculateDistance(extendedX, extendedY - 16), dL = calculateDistance(extendedX - 8, extendedY - 8), dR = calculateDistance(extendedX + 8, extendedY - 8);
                    if (dU <= dL && dU <= dR) desired = Direction.UP; else if (dL <= dR) desired = Direction.LEFT; else desired = Direction.RIGHT;
                } else if (!isBlockedLeft(x, y - 1)) { if (calculateDistance(extendedX - 8, extendedY - 8) < calculateDistance(extendedX, extendedY - 16)) desired = Direction.LEFT; }
                else if (!isBlockedRight(x, y - 1)) { if (calculateDistance(extendedX + 8, extendedY - 8) < calculateDistance(extendedX, extendedY - 16)) desired = Direction.RIGHT; }
            } else if (current == Direction.DOWN) {
                if (isBlockedDown(x, y + 1)) {
                    if (!isBlockedLeft(x, y + 1) && !isBlockedRight(x, y + 1)) desired = calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX + 8, extendedY + 8) ? Direction.LEFT : Direction.RIGHT;
                    else if (!isBlockedRight(x, y + 1)) desired = Direction.RIGHT; else if (!isBlockedLeft(x, y + 1)) desired = Direction.LEFT;
                } else if (!isBlockedLeft(x, y + 1) && !isBlockedRight(x, y + 1)) {
                    double dD = calculateDistance(extendedX, extendedY + 16), dL = calculateDistance(extendedX - 8, extendedY + 8), dR = calculateDistance(extendedX + 8, extendedY + 8);
                    if (dD <= dL && dD <= dR) desired = Direction.DOWN; else if (dL <= dR) desired = Direction.LEFT; else desired = Direction.RIGHT;
                } else if (!isBlockedLeft(x, y + 1)) { if (calculateDistance(extendedX - 8, extendedY + 8) < calculateDistance(extendedX, extendedY + 16)) desired = Direction.LEFT; }
                else if (!isBlockedRight(x, y + 1)) { if (calculateDistance(extendedX + 8, extendedY + 8) < calculateDistance(extendedX, extendedY + 16)) desired = Direction.RIGHT; }
            }
            return this;
        }

        boolean isBlocked(int x, int y) { return tiles[Math.max(0, y - 1)][Math.max(0, x)].type == EnumTile.WALL; }
        boolean isBlockedDown(int x, int y) { return (eaten || inHouse) ? tiles[Math.min(30, y + 1)][Math.max(0, x)].type == EnumTile.WALL : tiles[Math.min(30, y + 1)][Math.max(0, x)].type == EnumTile.WALL || tiles[Math.min(30, y + 1)][x].type == EnumTile.GHOST_ONLY; }
        boolean isBlockedLeft(int x, int y) { return tiles[y][Math.max(0, x - 1)].type == EnumTile.WALL; }
        boolean isBlockedRight(int x, int y) { return tiles[y][Math.min(27, x + 1)].type == EnumTile.WALL; }
        @Override boolean isBlockedDown() { return isBlockedDown(x, y); }

        Point getTarget() {
            if (scared) return inHouse ? new Point((13 * 8) + offsetX, (15 * 8) + offsetY) : new Point((int)(Math.random() * 28 * 8) + offsetX, (int)(Math.random() * 30 * 8) + offsetY);
            if (eaten || inHouse) return new Point((13 * 8) + offsetX, (15 * 8) + offsetY);
            if (remove) return new Point((13 * 8) + offsetX, (11 * 8) + offsetY);
            switch (info) {
                case BLINKY: return scatter ? new Point((23 * 8) + offsetX, offsetY) : new Point((pacman.x * 8) + offsetX, (pacman.y * 8) + offsetY);
                case INKY: return scatter ? new Point((27 * 8) + offsetX, (30 * 8) + offsetY) : new Point((pacman.x * 8) + offsetX, (pacman.y * 8) + offsetY);
                case PINKY: return scatter ? new Point((4 * 8) + offsetX, offsetY) : new Point((pacman.x * 8) + offsetX, (pacman.y * 8) + offsetY);
                case CLYDE: if (scatter) return new Point(offsetX, (30 * 8) + offsetY); else {
                    // Avoid depending on calculateDistance() here; some builds compute distance via getTarget(),
                    // which would recurse back into this method and crash with StackOverflowError.
                    int targetX = (pacman != null) ? pacman.extendedX : offsetX;
                    int targetY = (pacman != null) ? pacman.extendedY : offsetY;
                    double dx = extendedX - targetX;
                    double dy = extendedY - targetY;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < 64) return new Point(offsetX, (30 * 8) + offsetY);
                    return new Point((pacman.x * 8) + offsetX, (pacman.y * 8) + offsetY);
                }
                default: return new Point(offsetX, offsetY);
            }
        }

        Ghost drawGhost(GuiGraphics guiGraphics) {
            if (scared) { setGuiColor(guiGraphics, SCARED_STATE == 0 ? new Color(33, 33, 222) : new Color(245, 245, 255)); }
            else setGuiColor(guiGraphics, info.getColor());
            if (!eaten) { if (BODY_STATE == 0) guiGraphics.blit(TEXTURE, extendedX - 3, extendedY - 3, GUI_X + 10, MAZE_Y, GHOST, GHOST, 512, 512); else guiGraphics.blit(TEXTURE, extendedX - 3, extendedY - 3, GUI_X + 10 + GHOST, MAZE_Y, GHOST, GHOST, 512, 512); }
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            if (!scared) {
                int px = 0, py = 0;
                switch (current) {
                    case LEFT: px = -2; break; case RIGHT: px = 2; break; case UP: py = -2; break; case DOWN: py = 2; break;
                    default: break;
                }
                guiGraphics.blit(TEXTURE, extendedX + px, extendedY + py, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                guiGraphics.blit(TEXTURE, extendedX + px + 6, extendedY + py, GUI_X + 6, MAZE_Y + ENERGIZER, EYE_X, EYE_Y, 512, 512);
                setGuiColor(guiGraphics, new Color(33, 33, 222));
                guiGraphics.blit(TEXTURE, extendedX + px + 2, extendedY + py + 2, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                guiGraphics.blit(TEXTURE, extendedX + px + 8, extendedY + py + 2, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
            } else {
                setGuiColor(guiGraphics, SCARED_STATE == 0 ? new Color(245, 245, 255) : new Color(255, 15, 15));
                guiGraphics.blit(TEXTURE, extendedX + 1, extendedY + 1, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                guiGraphics.blit(TEXTURE, extendedX + 7, extendedY + 1, GUI_X, MAZE_Y + DOT, PUPIL, PUPIL, 512, 512);
                guiGraphics.blit(TEXTURE, extendedX - 2, extendedY + 5, GUI_X, MAZE_Y + GHOST, MOUTH_X, MOUTH_Y, 512, 512);
            }
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            return this;
        }
    }

    private class Mover {
        Direction current, desired;
        int x, y, x1, y1;
        float moveX, moveY;
        int extendedX, extendedY, offsetX, offsetY;
        public boolean canMove;

        Mover(int x, int y) { setStartPos(x, y); }

        Mover updatePosition(int x, int y) {
            extendedX = (int)(x1 + x + moveX); extendedY = (int)(y1 + y + moveY);
            offsetX = x; offsetY = y;
            if ((extendedX - x) % 8 == 0) this.x = (extendedX - x) / 8;
            if ((extendedY - y) % 8 == 0) this.y = (extendedY - y) / 8;
            return this;
        }

        void setStartPos(int x, int y) { this.x1 = (x * 8) - 4; this.y1 = y * 8; this.x = x; this.y = y; }
        boolean isBlocked() { return tiles[Math.max(0, y - 1)][x].type == EnumTile.WALL; }
        boolean isBlockedDown() { return tiles[Math.min(30, y + 1)][x].type == EnumTile.WALL || tiles[Math.min(30, y + 1)][x].type == EnumTile.GHOST_ONLY; }
        boolean isBlockedLeft() { return tiles[y][Math.max(0, x - 1)].type == EnumTile.WALL; }
        boolean isBlockedRight() { return tiles[y][Math.min(27, x + 1)].type == EnumTile.WALL; }
        boolean onTile() { return (extendedX - offsetX) % 8 == 0 && (extendedY - offsetY) % 8 == 0; }
        EnumTile checkTile() { return tiles[y][x].type; }
        Point getPosition() { return new Point(x, y); }
        Tile getTile() { return tiles[y][x]; }
        public void update() {}
    }

    private void getLevelData() {
        dotTimeLimit = (level < 4) ? 80 : 60;
        if (level == 0) bonus = EnumBonus.CHERRY; else if (level == 1) bonus = EnumBonus.STRAWBERRY; else if (level <= 3) bonus = EnumBonus.PEACH; else if (level <= 5) bonus = EnumBonus.APPLE; else if (level <= 7) bonus = EnumBonus.GRAPES; else if (level <= 9) bonus = EnumBonus.GALAXIAN; else if (level <= 11) bonus = EnumBonus.BELL; else bonus = EnumBonus.KEY;
        if (level == 0) { scaredTime = 6; scaredFlash = 5; } else if (level == 1) { scaredTime = 5; scaredFlash = 5; } else if (level == 2) { scaredTime = 4; scaredFlash = 5; } else if (level == 3) { scaredTime = 3; scaredFlash = 5; } else if (level == 4) { scaredTime = 2; scaredFlash = 5; } else if (level == 5) { scaredTime = 5; scaredFlash = 5; } else if (level <= 7) { scaredTime = 2; scaredFlash = 5; } else if (level == 8) { scaredTime = 1; scaredFlash = 3; } else if (level == 9) { scaredTime = 5; scaredFlash = 5; } else if (level == 10) { scaredTime = 2; scaredFlash = 5; } else if (level <= 12) { scaredTime = 1; scaredFlash = 3; } else if (level == 13) { scaredTime = 3; scaredFlash = 5; } else if (level <= 15) { scaredTime = 1; scaredFlash = 3; } else if (level == 16) { scaredTime = 0; scaredFlash = 0; } else if (level == 17) { scaredTime = 1; scaredFlash = 3; } else { scaredTime = 0; scaredFlash = 0; }
    }

    private void setupTiles() {
        for (int i = 0; i < 28; i++) tiles[0][i] = new Tile(i, 0, EnumTile.WALL);
        for (int i = 0; i < 28; i++) { if (i == 0 || (i > 12 && i < 15) || i == 27) tiles[1][i] = new Tile(i, 1, EnumTile.WALL); else tiles[1][i] = new Tile(i, 1); }
        for (int i = 0; i < 28; i++) { if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 26) || i == 27) tiles[2][i] = new Tile(i, 2, EnumTile.WALL); else tiles[2][i] = new Tile(i, 2); }
        for (int i = 0; i < 28; i++) { if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 26) || i == 27) tiles[3][i] = new Tile(i, 3, EnumTile.WALL); else if (i == 1 || i == 26) tiles[3][i] = new Tile(i, 3, 2); else tiles[3][i] = new Tile(i, 3); }
        for (int i = 0; i < 28; i++) { if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 26) || i == 27) tiles[4][i] = new Tile(i, 4, EnumTile.WALL); else tiles[4][i] = new Tile(i, 4); }
        for (int i = 0; i < 28; i++) { if (i == 0 || i == 27) tiles[5][i] = new Tile(i, 5, EnumTile.WALL); else tiles[5][i] = new Tile(i, 5); }
        for (int i = 0; i < 28; i++) { if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 26) || i == 27) tiles[6][i] = new Tile(i, 6, EnumTile.WALL); else tiles[6][i] = new Tile(i, 6); }
        for (int i = 0; i < 28; i++) { if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 26) || i == 27) tiles[7][i] = new Tile(i, 7, EnumTile.WALL); else tiles[7][i] = new Tile(i, 7); }
        for (int i = 0; i < 28; i++) { if (i == 0 || (i > 6 && i < 9) || (i > 12 && i < 15) || (i > 18 && i < 21) || i == 27) tiles[8][i] = new Tile(i, 8, EnumTile.WALL); else tiles[8][i] = new Tile(i, 8); }
        // Match the original 1.12.2 edible layout so the dot total reaches 244 and level transitions trigger.
        for (int i = 0; i < 28; i++) { if ((i >= 0 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 28)) tiles[9][i] = new Tile(i, 9, EnumTile.WALL); else if (i == 12 || i == 15) tiles[9][i] = new Tile(i, 9, 0); else tiles[9][i] = new Tile(i, 9); }
        for (int i = 0; i < 28; i++) { if ((i >= 0 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 28)) tiles[10][i] = new Tile(i, 10, EnumTile.WALL); else if (i == 12 || i == 15) tiles[10][i] = new Tile(i, 10, 0); else tiles[10][i] = new Tile(i, 10); }
        for (int i = 0; i < 28; i++) { if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[11][i] = new Tile(i, 11, EnumTile.WALL); else if (i == 9 || i == 18) tiles[11][i] = new Tile(i, 11, 0); else if (i > 9 && i < 18) tiles[11][i] = new Tile(i, 11, EnumTile.GHOST_LIMIT); else tiles[11][i] = new Tile(i, 11); }
        for (int i = 0; i < 28; i++) { if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 13) || (i > 14 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[12][i] = new Tile(i, 12, EnumTile.WALL); else if (i == 9 || i == 18) tiles[12][i] = new Tile(i, 12, 0); else if (i == 13 || i == 14) tiles[12][i] = new Tile(i, 12, EnumTile.GHOST_ONLY); else tiles[12][i] = new Tile(i, 12); }
        for (int i = 0; i < 28; i++) { if ((i >= 0 && i < 6) || (i > 6 && i < 9) || i == 10 || i == 17 || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[13][i] = new Tile(i, 13, EnumTile.WALL); else if (i == 9 || i == 18) tiles[13][i] = new Tile(i, 13, 0); else if (i > 10 && i < 17) tiles[13][i] = new Tile(i, 13, EnumTile.GHOST_ONLY); else tiles[13][i] = new Tile(i, 13); }
        for (int i = 0; i < 28; i++) { if (i == 0 || i == 27) tiles[14][i] = new Tile(i, 14, EnumTile.TELE); else if ((i > 0 && i < 6) || (i > 21 && i < 27)) tiles[14][i] = new Tile(i, 14, EnumTile.TELE_ZONE); else if (i == 10 || i == 17) tiles[14][i] = new Tile(i, 14, EnumTile.WALL); else if (i > 10 && i < 17) tiles[14][i] = new Tile(i, 14, EnumTile.GHOST_ONLY); else if (i == 5 || (i > 6 && i < 10) || (i > 17 && i < 21) || i == 22) tiles[14][i] = new Tile(i, 14, 0); else tiles[14][i] = new Tile(i, 14); }
        for (int i = 0; i < 28; i++) { if ((i >= 0 && i < 6) || (i > 6 && i < 9) || i == 10 || i == 17 || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[15][i] = new Tile(i, 15, EnumTile.WALL); else if (i == 9 || i == 18) tiles[15][i] = new Tile(i, 15, 0); else if (i > 10 && i < 17) tiles[15][i] = new Tile(i, 15, EnumTile.GHOST_ONLY); else tiles[15][i] = new Tile(i, 15); }
        for (int i = 0; i < 28; i++) { if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[16][i] = new Tile(i, 16, EnumTile.WALL); else if (i == 9 || i == 18) tiles[16][i] = new Tile(i, 16, 0); else tiles[16][i] = new Tile(i, 16); }
        for (int i = 0; i < 28; i++) { if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[17][i] = new Tile(i, 17, EnumTile.WALL); else if (i > 8 && i < 19) tiles[17][i] = new Tile(i, 17, 0); else tiles[17][i] = new Tile(i, 17); }
        for (int i = 0; i < 28; i++) { if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[18][i] = new Tile(i, 18, EnumTile.WALL); else if (i == 9 || i == 18) tiles[18][i] = new Tile(i, 18, 0); else tiles[18][i] = new Tile(i, 18); }
        for (int i = 0; i < 28; i++) { if ((i >= 0 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 28)) tiles[19][i] = new Tile(i, 19, EnumTile.WALL); else if (i == 9 || i == 18) tiles[19][i] = new Tile(i, 19, 0); else tiles[19][i] = new Tile(i, 19); }
        for (int i = 0; i < 28; i++) { if (i == 0 || (i > 12 && i < 15) || i == 27) tiles[20][i] = new Tile(i, 20, EnumTile.WALL); else tiles[20][i] = new Tile(i, 20); }
        for (int i = 0; i < 28; i++) { if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 26) || i == 27) tiles[21][i] = new Tile(i, 21, EnumTile.WALL); else tiles[21][i] = new Tile(i, 21); }
        for (int i = 0; i < 28; i++) { if (i == 0 || (i > 1 && i < 6) || (i > 6 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 21) || (i > 21 && i < 26) || i == 27) tiles[22][i] = new Tile(i, 22, EnumTile.WALL); else tiles[22][i] = new Tile(i, 22); }
        for (int i = 0; i < 28; i++) { if (i == 0 || (i > 3 && i < 6) || (i > 21 && i < 24) || i == 27) tiles[23][i] = new Tile(i, 23, EnumTile.WALL); else if (i == 1 || i == 26) tiles[23][i] = new Tile(i, 23, 2); else if ((i > 9 && i < 13) || (i > 14 && i < 18)) tiles[23][i] = new Tile(i, 23, EnumTile.GHOST_LIMIT, 1); else if (i > 12 && i < 15) tiles[23][i] = new Tile(i, 23, EnumTile.GHOST_LIMIT); else if (i == 13 || i == 14) tiles[23][i] = new Tile(i, 23, 0); else tiles[23][i] = new Tile(i, 23); }
        for (int i = 0; i < 28; i++) { if ((i >= 0 && i < 3) || (i > 3 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 24) || i > 24) tiles[24][i] = new Tile(i, 24, EnumTile.WALL); else tiles[24][i] = new Tile(i, 24); }
        for (int i = 0; i < 28; i++) { if ((i >= 0 && i < 3) || (i > 3 && i < 6) || (i > 6 && i < 9) || (i > 9 && i < 18) || (i > 18 && i < 21) || (i > 21 && i < 24) || i > 24) tiles[25][i] = new Tile(i, 25, EnumTile.WALL); else tiles[25][i] = new Tile(i, 25); }
        for (int i = 0; i < 28; i++) { if (i == 0 || (i > 6 && i < 9) || (i > 12 && i < 15) || (i > 18 && i < 21) || i == 27) tiles[26][i] = new Tile(i, 26, EnumTile.WALL); else tiles[26][i] = new Tile(i, 26); }
        for (int i = 0; i < 28; i++) { if (i == 0 || (i > 1 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 26) || i == 27) tiles[27][i] = new Tile(i, 27, EnumTile.WALL); else tiles[27][i] = new Tile(i, 27); }
        for (int i = 0; i < 28; i++) { if (i == 0 || (i > 1 && i < 12) || (i > 12 && i < 15) || (i > 15 && i < 26) || i == 27) tiles[28][i] = new Tile(i, 28, EnumTile.WALL); else tiles[28][i] = new Tile(i, 28); }
        for (int i = 0; i < 28; i++) { if (i == 0 || i == 27) tiles[29][i] = new Tile(i, 29, EnumTile.WALL); else tiles[29][i] = new Tile(i, 29); }
        for (int i = 0; i < 28; i++) tiles[30][i] = new Tile(i, 30, EnumTile.WALL);
    }

    @Override
    public void onClose() {
        if (theme != null) {
            minecraft.getSoundManager().stop(theme);
        }
        if (siren != null) {
            minecraft.getSoundManager().stop(siren);
        }
        if (fright != null) {
            minecraft.getSoundManager().stop(fright);
        }
        if (ghostEaten != null) {
            minecraft.getSoundManager().stop(ghostEaten);
        }
        super.onClose();
    }
}
