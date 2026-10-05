package superhb.arcademod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import org.lwjgl.glfw.GLFW;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArcade;
import superhb.arcademod.init.ModRegistries;
import superhb.arcademod.client.tileentity.BlockEntityArcade;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

public class GuiKong extends GuiArcade {
    
    private static final ResourceLocation TEX_A = new ResourceLocation(Reference.MODID, "textures/gui/kong/a.png");
    private static final ResourceLocation TEX_PLATFORM = new ResourceLocation(Reference.MODID, "textures/gui/kong/platform.png");
    private static final ResourceLocation TEX_LADDER = new ResourceLocation(Reference.MODID, "textures/gui/kong/ladder.png");
    private static final ResourceLocation TEX_HEALTH = new ResourceLocation(Reference.MODID, "textures/gui/kong/health.png");
    private static final ResourceLocation TEX_BOOST = new ResourceLocation(Reference.MODID, "textures/gui/kong/boost.png");
    private static final ResourceLocation TEX_BAR = new ResourceLocation(Reference.MODID, "textures/gui/kong/bar.png");
    private static final ResourceLocation TEX_LOGO = new ResourceLocation(Reference.MODID, "textures/gui/kong/peach.png"); // Princess
    
    private static final int GAME_WIDTH = 600;
    private static final int GAME_HEIGHT = 800;

    private int boardX, boardY;
    private boolean playing, gameOver, isWin;
    private int score;
    private int startTick = 0;
    
    private int currentStage = 1; // 1: 25m, 2: 50m, 3: 75m, 4: 100m
    private int totalRivets = 0;
    
    private ArrayList<LevelObj> Levels = new ArrayList<>();
    private ArrayList<ElevatorObj> Elevators = new ArrayList<>();
    private ArrayList<Ladder> Ladders = new ArrayList<>();
    private ArrayList<Hazard> Hazards = new ArrayList<>();
    private ArrayList<Rivet> Rivets = new ArrayList<>();
    
    private Princess princess;
    private Health health;
    private Boost boost;
    private int hazardTimer = 60;
    
    private Mario mario;
    private Kong kong;
    
    private Random random = new Random();

    public GuiKong(Level world, BlockEntityArcade tile, @Nullable BlockPos pos, Player player) {
        super(world, tile, pos, player, Component.literal("Girder Climber"));
        setGuiSize(GAME_WIDTH, GAME_HEIGHT, 0.28F);
        setTexture(null);
        setCost(4);
        setOffset(0, 0);
        setButtonPos((GAME_WIDTH / 2) - (buttonWidth / 2), GAME_HEIGHT - 30);
        setStartMenu(0);
        initLevel();
    }

    private void initLevel() {
        Levels.clear();
        Elevators.clear();
        Ladders.clear();
        Hazards.clear();
        Rivets.clear();
        hazardTimer = 60;
        totalRivets = 0;

        princess = new Princess(260, 25);
        health = new Health();
        boost = new Boost();
        kong = new Kong(60, 5);
        mario = new Mario(50, 775 - 40);

        if (currentStage == 1) { 
            Levels.add(new LevelObj(0, 600, 775, 775));
            Levels.add(new LevelObj(0, 565, 625, 650));
            Levels.add(new LevelObj(35, 565, 490, 465));
            Levels.add(new LevelObj(0, 565, 325, 350));
            Levels.add(new LevelObj(35, 565, 210, 185));
            Levels.add(new LevelObj(0, 565, 55, 80));
            
            Ladders.add(new Ladder(500, 625, 15, 150));
            Ladders.add(new Ladder(150, 490, 15, 140));
            Ladders.add(new Ladder(250, 475, 15, 160));
            Ladders.add(new Ladder(400, 350, 15, 125));
            Ladders.add(new Ladder(150, 210, 15, 125));
            Ladders.add(new Ladder(450, 80, 15, 125));
            
            kong.x = 60; kong.y = 5;
            princess.x = 260; princess.y = 25;
        } 
        else if (currentStage == 2) { // 50m: Pie Factory (Accurate to image)
            Levels.add(new LevelObj(0, 600, 775, 775));
            Levels.add(new LevelObj(0, 600, 625, 625).setConveyor(-1.5f));
            Levels.add(new LevelObj(0, 600, 475, 475).setConveyor(1.5f));
            Levels.add(new LevelObj(0, 600, 325, 325).setConveyor(-1.5f));
            Levels.add(new LevelObj(100, 400, 175, 175)); // Top platform for Princess
            
            // Staggered ladders to prevent straight climbing
            Ladders.add(new Ladder(50, 625, 15, 150));   // L0 to L1 Left
            Ladders.add(new Ladder(300, 625, 15, 150));  // L0 to L1 Middle
            Ladders.add(new Ladder(550, 625, 15, 150));  // L0 to L1 Right
            
            Ladders.add(new Ladder(150, 475, 15, 150));  // L1 to L2 Left-Mid
            Ladders.add(new Ladder(450, 475, 15, 150));  // L1 to L2 Right-Mid
            
            Ladders.add(new Ladder(50, 325, 15, 150));   // L2 to L3 Left
            Ladders.add(new Ladder(300, 325, 15, 150));  // L2 to L3 Middle
            Ladders.add(new Ladder(550, 325, 15, 150));  // L2 to L3 Right
            
            Ladders.add(new Ladder(150, 175, 15, 150));  // L3 to L4 Left-Mid
            Ladders.add(new Ladder(450, 175, 15, 150));  // L3 to L4 Right-Mid
            
            kong.x = 260; kong.y = 325 - 75; // Kong stands on L3
            princess.x = 260; princess.y = 175 - 52; // Princess stands on L4
            
            // 3 fireballs per floor, spread left / mid / right
            // Floor 0 (y=775)
            Hazards.add(new Fireball(80,  775 - 30));
            Hazards.add(new Fireball(300, 775 - 30));
            Hazards.add(new Fireball(520, 775 - 30));
            // Floor 1 (y=625)
            Hazards.add(new Fireball(100, 625 - 30));
            Hazards.add(new Fireball(300, 625 - 30));
            Hazards.add(new Fireball(500, 625 - 30));
            // Floor 2 (y=475)
            Hazards.add(new Fireball(80,  475 - 30));
            Hazards.add(new Fireball(280, 475 - 30));
            Hazards.add(new Fireball(490, 475 - 30));
            // Floor 3 (y=325)
            Hazards.add(new Fireball(100, 325 - 30));
            Hazards.add(new Fireball(300, 325 - 30));
            Hazards.add(new Fireball(500, 325 - 30));
        }
        else if (currentStage == 3) { // 75m: Elevators
            Levels.add(new LevelObj(0, 200, 775, 775));
            Levels.add(new LevelObj(0, 200, 600, 600));
            Levels.add(new LevelObj(0, 200, 425, 425));
            Levels.add(new LevelObj(0, 200, 250, 250));
            Levels.add(new LevelObj(400, 200, 775, 775));
            Levels.add(new LevelObj(400, 200, 600, 600));
            Levels.add(new LevelObj(400, 200, 425, 425));
            Levels.add(new LevelObj(400, 200, 250, 250));
            Levels.add(new LevelObj(200, 200, 100, 100)); // Top Kong plat
            
            Ladders.add(new Ladder(50, 600, 15, 175));
            Ladders.add(new Ladder(150, 425, 15, 175));
            Ladders.add(new Ladder(50, 250, 15, 175));
            Ladders.add(new Ladder(550, 600, 15, 175));
            Ladders.add(new Ladder(450, 425, 15, 175));
            Ladders.add(new Ladder(550, 250, 15, 175));
            Ladders.add(new Ladder(280, 100, 15, 150));
            
            Elevators.add(new ElevatorObj(200, 50, 100, 2.0f));
            Elevators.add(new ElevatorObj(200, 50, 400, 2.0f));
            Elevators.add(new ElevatorObj(200, 50, 700, 2.0f));
            Elevators.add(new ElevatorObj(350, 50, 100, -2.0f));
            Elevators.add(new ElevatorObj(350, 50, 400, -2.0f));
            Elevators.add(new ElevatorObj(350, 50, 700, -2.0f));
            
            kong.x = 260; kong.y = 100 - 75;
            princess.x = 260; princess.y = 20; // Needs to be above kong visually
        }
        else if (currentStage == 4) { // 100m: Rivets
            Levels.add(new LevelObj(0, 600, 775, 775));
            Levels.add(new LevelObj(0, 600, 625, 625));
            Levels.add(new LevelObj(0, 600, 475, 475));
            Levels.add(new LevelObj(0, 600, 325, 325));
            Levels.add(new LevelObj(200, 200, 175, 175));
            
            addRivetAndCut(1, 200, 625); addRivetAndCut(1, 400, 625);
            addRivetAndCut(2, 200, 475); addRivetAndCut(2, 400, 475);
            addRivetAndCut(3, 200, 325); addRivetAndCut(3, 400, 325);
            
            Ladders.add(new Ladder(50, 625, 15, 150));
            Ladders.add(new Ladder(550, 625, 15, 150));
            Ladders.add(new Ladder(100, 475, 15, 150));
            Ladders.add(new Ladder(500, 475, 15, 150));
            Ladders.add(new Ladder(50, 325, 15, 150));
            Ladders.add(new Ladder(550, 325, 15, 150));
            Ladders.add(new Ladder(250, 175, 15, 150));
            Ladders.add(new Ladder(350, 175, 15, 150));
            
            // Stage 4: 3 fireballs per active floor
            Hazards.add(new Fireball(80,  775 - 20));
            Hazards.add(new Fireball(300, 775 - 20));
            Hazards.add(new Fireball(520, 775 - 20));
            Hazards.add(new Fireball(100, 625 - 20));
            Hazards.add(new Fireball(300, 625 - 20));
            Hazards.add(new Fireball(500, 625 - 20));
            Hazards.add(new Fireball(100, 475 - 20));
            Hazards.add(new Fireball(300, 475 - 20));
            Hazards.add(new Fireball(500, 475 - 20));
            
            kong.x = 260; kong.y = 175 - 75;
            princess.x = 260; princess.y = 50;
        }
    }
    
    private void addRivetAndCut(int levelIdx, int rx, int ry) {
        Rivets.add(new Rivet(rx, ry));
        totalRivets++;
        LevelObj l = Levels.get(levelIdx);
        LevelObj newRight = new LevelObj(rx + 20, (l.x + l.width) - (rx + 20), ry, ry);
        l.width = rx - l.x;
        Levels.add(newRight);
    }

    private void startGame() {
        score = 0;
        currentStage = 1;
        inMenu = false;
        gameOver = false;
        isWin = false;
        canGetCoinBack = false;
        initLevel();
        startTick = tickCounter;
        playing = true;
    }

    private void endGame(boolean win) {
        gameOver = true;
        isWin = win;
        playing = false;
        inMenu = true;
        menu = 3;
        int tickets = (score / 100) + (win ? 10 : 0);
        if (tickets > 0) giveReward(new ItemStack(ModRegistries.TICKET.get(), tickets));
    }

    private void nextStage() {
        score += 1000;
        currentStage++;
        if (currentStage > 4) {
            endGame(true);
            return;
        }
        initLevel();
    }

    @Override
    public void tick() {
        super.tick();

        if (inMenu) {
            if (menu == 3 && (tickCounter - startTick) > 100) {
                menu = 0;
                gameOver = false;
            }
            return;
        }

        if (playing) {
            mario.tick();
            kong.tick();
            
            for (ElevatorObj e : Elevators) e.tick();
            
            hazardTimer--;
            if (hazardTimer <= 0) {
                if (currentStage == 1) Hazards.add(new Barrel());
                if (currentStage == 2) Hazards.add(new Barrel());
                if (currentStage == 3) Hazards.add(new Spring());
                if (currentStage == 4) Hazards.add(new Barrel());
                hazardTimer = 100 - Math.min(60, (score / 100));
            }
            
            for (int i = Hazards.size() - 1; i >= 0; i--) {
                Hazard h = Hazards.get(i);
                h.tick();
                if (!h.isAlive) Hazards.remove(i);
            }
            
            if (currentStage == 4 && Rivets.isEmpty() && totalRivets > 0) {
                nextStage(); // Removed all rivets
            }
            
            if (mario.health <= 0) endGame(false);
            if (mario.isWin) nextStage();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        boardX = xScaled - (GAME_WIDTH / 2);
        boardY = yScaled - (GAME_HEIGHT / 2);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);
        guiGraphics.fill(boardX, boardY, boardX + GAME_WIDTH, boardY + GAME_HEIGHT, 0xFF000000);

        if (inMenu) {
            renderMenu(guiGraphics);
        } else {
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            
            for (LevelObj l : Levels) drawPlatform(guiGraphics, l, false);
            for (ElevatorObj e : Elevators) drawPlatform(guiGraphics, e, true);

            for (Ladder l : Ladders) {
                guiGraphics.blit(TEX_LADDER, boardX + l.x + l.width / 2 - 25, boardY + l.y, 50, l.height, 0.0f, 0.0f, 469, 532, 469, 532);
            }
            
            for (Rivet r : Rivets) {
                guiGraphics.fill(boardX + r.x, boardY + r.y, boardX + r.x + 20, boardY + r.y + 10, 0xFFFFFF00);
            }

            if (mario.health == 2) guiGraphics.blit(TEX_BAR, boardX + 528, boardY + 5, 629/9, 148/9, 0.0f, 926.0f, 630, 148, 630, 1074);
            else if (mario.health == 1) guiGraphics.blit(TEX_BAR, boardX + 528, boardY + 5, 629/9, 148/9, 0.0f, 371.0f, 630, 148, 630, 1074);

            if (health.isActive) guiGraphics.blit(TEX_HEALTH, boardX + health.x, boardY + health.y, health.width, health.height, 0.0f, 0.0f, 1200, 1200, 1200, 1200);
            if (boost.isActive) guiGraphics.blit(TEX_BOOST, boardX + boost.x, boardY + boost.y, boost.width, boost.height, 0.0f, 0.0f, 512, 512, 512, 512);

            guiGraphics.blit(TEX_LOGO, boardX + princess.x - 10, boardY + princess.y - 5, 65, 60, 0.0f, 0.0f, 284, 256, 284, 256);

            kong.render(guiGraphics);
            for (Hazard h : Hazards) h.render(guiGraphics);
            mario.render(guiGraphics);
            
            guiGraphics.pose().scale(3.0f, 3.0f, 3.0f);
            guiGraphics.drawString(minecraft.font, Component.literal("STAGE: " + currentStage), (boardX + 350)/3, (boardY + 5)/3, 0xFFFF00);
            guiGraphics.drawString(minecraft.font, Component.literal("SCORE: " + score), (boardX + 350)/3, (boardY + 22)/3, 0xFFFFFF);
            guiGraphics.pose().scale(1/3.0f, 1/3.0f, 1/3.0f);
        }

        guiGraphics.pose().popPose();
    }
    
    private void drawPlatform(GuiGraphics guiGraphics, LevelObj l, boolean isElevator) {
        if (l.width <= 0) return;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(boardX + l.x, boardY + l.leftY, 0);
        float angle = (float) Math.toDegrees(Math.atan2(l.rightY - l.leftY, l.width));
        guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
        int length = (int) Math.sqrt(Math.pow(l.width, 2) + Math.pow(l.rightY - l.leftY, 2));
        
        if (isElevator) {
            guiGraphics.fill(0, 0, length, 15, 0xFF00FFFF);
        } else if (l.pushSpeed != 0) {
            guiGraphics.fill(0, 0, length, 15, 0xFF0000FF);
            int offset = (int)(tickCounter * Math.abs(l.pushSpeed) * 2) % 20;
            if (l.pushSpeed < 0) offset = 20 - offset; // Animate left
            guiGraphics.fill(offset, 0, offset+5, 15, 0xFFFFFFFF);
            guiGraphics.fill(offset+20, 0, offset+25, 15, 0xFFFFFFFF);
            guiGraphics.fill(offset-20, 0, offset-15, 15, 0xFFFFFFFF);
        } else {
            guiGraphics.blit(TEX_PLATFORM, 0, 0, length, 15, 0.0f, 0.0f, 150, 8, 150, 8);
        }
        guiGraphics.pose().popPose();
    }

    private void renderMenu(GuiGraphics guiGraphics) {
        guiGraphics.pose().scale(3.0f, 3.0f, 3.0f);
        int mX = boardX / 3; int mY = boardY / 3; int w = GAME_WIDTH / 3; int h = GAME_HEIGHT / 3;

        switch (menu) {
            case 0:
                guiGraphics.drawCenteredString(minecraft.font, Component.literal("GIRDER CLIMBER"), mX + (w / 2), mY + 20, 0xFF0000);
                // MC splash screen style BETA text
                guiGraphics.pose().pushPose();
                float betaCenterX = (mX + (w / 2)) + 48;
                float betaCenterY = mY + 28;
                guiGraphics.pose().translate(betaCenterX, betaCenterY, 0);
                guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-20));
                float betaScale = 0.85f + 0.1f * (float)Math.abs(Math.sin(tickCounter * 0.08));
                guiGraphics.pose().scale(betaScale, betaScale, 1.0f);
                int betaColor = (tickCounter % 10 < 5) ? 0xFFFF00 : 0xFFAA00;
                guiGraphics.drawCenteredString(minecraft.font, Component.literal("BETA"), 0, -4, betaColor);
                guiGraphics.pose().popPose();
                guiGraphics.drawCenteredString(minecraft.font, Component.translatable("option.arcademod.start"), mX + (w / 2), mY + (h / 2) - 30, 0xFFFFFF);
                guiGraphics.drawCenteredString(minecraft.font, Component.translatable("option.arcademod.control"), mX + (w / 2), mY + (h / 2) - 10, 0xFFFFFF);
                if (menuOption == 0) guiGraphics.drawString(minecraft.font, ">", mX + (w / 2) - 40, mY + (h / 2) - 30, 0xFFFF00);
                else if (menuOption == 1) guiGraphics.drawString(minecraft.font, ">", mX + (w / 2) - 40, mY + (h / 2) - 10, 0xFFFF00);
                break;
            case 1:
                guiGraphics.drawCenteredString(minecraft.font, Component.translatable("option.arcademod.control"), mX + (w / 2), mY + 20, 0xFF0000);
                guiGraphics.drawString(minecraft.font, Component.literal("[W] Up"), mX + 20, mY + (h / 2) - 20, 0xFFFFFF);
                guiGraphics.drawString(minecraft.font, Component.literal("[S] Down"), mX + 20, mY + (h / 2) - 10, 0xFFFFFF);
                guiGraphics.drawString(minecraft.font, Component.literal("[A] Left"), mX + 20, mY + (h / 2), 0xFFFFFF);
                guiGraphics.drawString(minecraft.font, Component.literal("[D] Right"), mX + 20, mY + (h / 2) + 10, 0xFFFFFF);
                guiGraphics.drawString(minecraft.font, Component.literal("[SPACE] Jump"), mX + 20, mY + (h / 2) + 20, 0xFFFFFF);
                guiGraphics.drawString(minecraft.font, Component.literal("[ESC] Back"), mX + 2, mY + h - 10, 0xFFFFFF);
                break;
            case 3:
                guiGraphics.drawCenteredString(minecraft.font, Component.literal(isWin ? "ALL STAGES CLEARED!" : "GAME OVER"), mX + (w / 2), mY + (h / 2) - 20, isWin ? 0x00FF00 : 0xFF0000);
                guiGraphics.drawCenteredString(minecraft.font, Component.literal("Score: " + score), mX + (w / 2), mY + (h / 2), 0xFFFFFF);
                break;
        }
        guiGraphics.pose().scale(1/3.0f, 1/3.0f, 1/3.0f);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (inMenu) {
            if (menu == 0) {
                if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_S) menuOption = (menuOption == 1) ? 0 : 1;
                else if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_W) menuOption = (menuOption == 0) ? 1 : 0;
                else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
                    if (menuOption == 0) startGame();
                    else menu = 1;
                }
            } else if (menu == 1 || menu == 3) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) menu = 0;
            }
        } else {
            if (keyCode == GLFW.GLFW_KEY_A) mario.keyLeft = true;
            if (keyCode == GLFW.GLFW_KEY_D) mario.keyRight = true;
            if (keyCode == GLFW.GLFW_KEY_W) mario.keyUp = true;
            if (keyCode == GLFW.GLFW_KEY_S) mario.keyDown = true;
            if (keyCode == GLFW.GLFW_KEY_SPACE) mario.keyJump = true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!inMenu) {
            if (keyCode == GLFW.GLFW_KEY_A) mario.keyLeft = false;
            if (keyCode == GLFW.GLFW_KEY_D) mario.keyRight = false;
            if (keyCode == GLFW.GLFW_KEY_W) mario.keyUp = false;
            if (keyCode == GLFW.GLFW_KEY_S) mario.keyDown = false;
            if (keyCode == GLFW.GLFW_KEY_SPACE) mario.keyJump = false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    // -------------------------------------------------------------------------
    // Objects & Physics
    // -------------------------------------------------------------------------

    private class LevelObj {
        int x, width, leftY, rightY;
        float pushSpeed = 0;
        public LevelObj(int x, int width, int leftY, int rightY) {
            this.x = x; this.width = width; this.leftY = leftY; this.rightY = rightY;
        }
        public LevelObj setConveyor(float speed) { this.pushSpeed = speed; return this; }
        public int getYAt(float px) {
            if (px <= x) return leftY;
            if (px >= x + width) return rightY;
            float t = (px - x) / width;
            return (int) (leftY + t * (rightY - leftY));
        }
        public Rectangle getBounds() { return new Rectangle(x, Math.min(leftY, rightY) - 5, width, Math.abs(rightY - leftY) + 15); }
    }

    private class ElevatorObj extends LevelObj {
        float yPos;
        float speed;
        public ElevatorObj(int x, int width, float startY, float speed) {
            super(x, width, (int)startY, (int)startY);
            this.yPos = startY;
            this.speed = speed;
        }
        public void tick() {
            yPos += speed;
            if (yPos > 850) yPos = -50;
            if (yPos < -50) yPos = 850;
            this.leftY = (int)yPos;
            this.rightY = (int)yPos;
        }
    }

    private class Ladder {
        int x, y, width, height;
        public Ladder(int x, int y, int width, int height) {
            this.x = x; this.y = y; this.width = width; this.height = height;
        }
        public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    }
    
    private class Rivet {
        int x, y;
        public Rivet(int x, int y) { this.x = x; this.y = y; }
        public Rectangle getBounds() { return new Rectangle(x, y-10, 20, 20); }
    }

    private class Princess {
        int x, y, width = 38, height = 52;
        public Princess(int x, int y) { this.x = x; this.y = y; }
        public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    }
    
    private class Health {
        int x = 100, y = 50, width = 50, height = 50;
        boolean isActive = true;
        public Health() {
            int a = random.nextInt(3);
            if (a == 2) { y = 25; x = 140 + random.nextInt(385); }
            else if (a == 1) { y = 305; x = random.nextInt(525); }
            else if (a == 0) { y = 585; x = random.nextInt(525); }
        }
        public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    }

    private class Boost {
        int x = 100, y = 50, width = 50, height = 50;
        boolean isActive = true;
        int a;
        public Boost() {
            a = random.nextInt(3);
            if (a == 2) { y = 160; x = 35 + random.nextInt(515); }
            else if (a == 1) { y = 440; x = 35 + random.nextInt(515); }
            else if (a == 0) { y = 725; x = 140 + random.nextInt(400); }
        }
        public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    }

    // -------------------------------------------------------------------------
    // Player
    // -------------------------------------------------------------------------

    private class Mario {
        float x, y, vx = 0, vy = 0;
        int width = 40, height = 40;
        float speed = 6.55f;
        boolean isWin = false, climbing = false, jumping = false;
        int health = 2;
        boolean keyLeft, keyRight, keyUp, keyDown, keyJump;
        int animState = 0, animTick = 0;
        boolean facingRight = true;
        int iframes = 0;

        public Mario(float x, float y) { this.x = x; this.y = y; }
        public Rectangle getBounds() { return new Rectangle((int)x + 10, (int)y + 5, 20, 35); }

        public void tick() {
            if (iframes > 0) iframes--;

            Ladder intersectingLadder = null;
            Rectangle bounds = getBounds();
            for (Ladder l : Ladders) {
                if (bounds.intersects(l.getBounds())) {
                    intersectingLadder = l; break;
                }
            }

            ArrayList<LevelObj> allPlats = new ArrayList<>(Levels);
            allPlats.addAll(Elevators);

            if (intersectingLadder != null) {
                if (keyUp) {
                    climbing = true; vy = -4.0f;
                    x += ((intersectingLadder.x + intersectingLadder.width / 2) - (x + width / 2)) * 0.2f;
                } else if (keyDown) {
                    climbing = true; vy = 4.0f;
                    x += ((intersectingLadder.x + intersectingLadder.width / 2) - (x + width / 2)) * 0.2f;
                } else if (climbing) {
                    vy = 0; // Hold position
                }
                
                // Disengage from ladder explicitly when pressing left or right while touching a floor
                if (climbing && (keyLeft || keyRight)) {
                    for (LevelObj l : allPlats) {
                        if (x + 20 >= l.x && x + 20 <= l.x + l.width) {
                            int platY = l.getYAt(x + 20);
                            if (Math.abs((y + height) - platY) <= 8) {
                                climbing = false;
                                y = platY - height; // Snap exactly to floor
                                break;
                            }
                        }
                    }
                }
            } else { 
                climbing = false; 
            }

            if (!climbing) {
                if (keyLeft) { vx = -speed; facingRight = false; animTick++; }
                else if (keyRight) { vx = speed; facingRight = true; animTick++; }
                else vx = 0;

                if (keyJump && !jumping) { vy = -12.0f; jumping = true; }
                vy += 1.5f;
                if (vy > 15.0f) vy = 15.0f;
            } else { vx = 0; }

            x += vx;
            if (x < 0) x = 0;
            if (x > GAME_WIDTH - width) x = GAME_WIDTH - width;
            y += vy;

            boolean onPlatform = false;
            
            if (vy >= 0 && !climbing) {
                for (LevelObj l : allPlats) {
                    if (x + 20 >= l.x && x + 20 <= l.x + l.width) {
                        int platY = l.getYAt(x + 20);
                        if (y + height >= platY - 8 && y + height <= platY + 15 && vy >= 0) {
                            y = platY - height;
                            vy = 0;
                            jumping = false;
                            onPlatform = true;

                            // Conveyor belt pushes Mario horizontally
                            if (l.pushSpeed != 0) {
                                x += l.pushSpeed;
                                if (x < 0) x = 0;
                                if (x > GAME_WIDTH - width) x = GAME_WIDTH - width;
                            }

                            if (l instanceof ElevatorObj) {
                                y += ((ElevatorObj)l).speed; // Move with elevator
                            }
                            break;
                        }
                    }
                }
                if (y > GAME_HEIGHT) health = 0;
            }

            if (currentStage == 4 && onPlatform) {
                for (int i = 0; i < Rivets.size(); i++) {
                    if (getBounds().intersects(Rivets.get(i).getBounds())) {
                        Rivets.remove(i);
                        score += 100;
                        break;
                    }
                }
            }

            if (animTick > 3) { animState = (animState + 1) % 3; animTick = 0; }

            if (GuiKong.this.health.isActive && getBounds().intersects(GuiKong.this.health.getBounds())) {
                if (GuiKong.this.mario.health < 2) GuiKong.this.mario.health++;
                GuiKong.this.health.isActive = false; score += 500;
            }

            if (boost.isActive && getBounds().intersects(boost.getBounds())) {
                speed = 11.05f; boost.isActive = false; score += 200;
            }

            if (getBounds().intersects(princess.getBounds())) isWin = true;

            if (iframes == 0) {
                for (Hazard h : Hazards) {
                    if (getBounds().intersects(h.getBounds())) {
                        GuiKong.this.mario.health--;
                        iframes = 30;
                        break;
                    }
                }
            }
        }

        public void render(GuiGraphics guiGraphics) {
            if (iframes > 0 && iframes % 4 < 2) return;
            int[] rightMap = {158, 3, 176, 4, 197, 3};
            int[] leftMap = {136, 3, 115, 4, 94, 3};
            int texX, texY, texW = 15, texH = 16;
            
            if (vx == 0 && vy == 0 && !climbing) {
                if (facingRight) { texX = rightMap[0]; texY = rightMap[1]; } 
                else { texX = leftMap[0]; texY = leftMap[1]; }
            } else if (climbing) {
                texX = 145; texY = 24; texW = 16; texH = 15;
            } else if (facingRight) {
                texX = rightMap[animState*2]; texY = rightMap[animState*2+1];
            } else {
                texX = leftMap[animState*2]; texY = leftMap[animState*2+1];
            }
            guiGraphics.blit(TEX_A, boardX + (int)x, boardY + (int)y, 40, 40, texX, texY, texW, texH, 308, 287);
        }
    }

    // -------------------------------------------------------------------------
    // Enemies & Hazards
    // -------------------------------------------------------------------------

    private abstract class Hazard {
        float x, y;
        boolean isAlive = true;
        abstract void tick();
        abstract void render(GuiGraphics g);
        Rectangle getBounds() { return new Rectangle((int)x + 5, (int)y + 5, 20, 20); }
    }

    private class Barrel extends Hazard {
        float vx = 6.0f, vy = 0.0f;
        int animState = 0, animTick = 0;
        boolean dropping = false;
        int dropTargetY = 0;
        int[] animX = {66, 81, 66, 81}, animY = {258, 258, 270, 270};

        public Barrel() { x = 120; y = 50; }

        public void tick() {
            if (!dropping) { vy += 1.2f; if (vy > 12.0f) vy = 12.0f; x += vx; } 
            else { vy = 4.0f; x += 0; }
            y += vy;
            
            if (vy >= 0 && !dropping) {
                for (LevelObj l : Levels) {
                    if (x + 15 >= l.x && x + 15 <= l.x + l.width) {
                        int platY = l.getYAt(x + 15);
                        
                        if (y + 30 >= platY - 8 && y + 30 <= platY + 15 && vy >= 0) {
                            y = platY - 30; vy = 0;
                            if (l.leftY < l.rightY) vx = Math.abs(vx == 0 ? 6.0f : vx);
                            else if (l.leftY > l.rightY) vx = -Math.abs(vx == 0 ? 6.0f : vx);
                            else if (vx == 0) vx = random.nextBoolean() ? 6.0f : -6.0f;
                            
                            for (Ladder ladder : Ladders) {
                                if (ladder.y >= platY - 10 && ladder.y <= platY + 10) {
                                    if (Math.abs((x + 15) - (ladder.x + 7)) < 6 && random.nextInt(100) < 30) {
                                        dropping = true; 
                                        x = ladder.x + 7 - 15; 
                                        vx = 0; 
                                        dropTargetY = ladder.y + ladder.height;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            } else if (dropping) {
                if (y + 30 >= dropTargetY) {
                    dropping = false;
                    vy = 0;
                    y = dropTargetY - 30;
                }
            }
            
            if (x >= GAME_WIDTH - 30 && !dropping) { x = GAME_WIDTH - 30; vx = -Math.abs(vx); }
            if (x <= 0 && !dropping) { x = 0; vx = Math.abs(vx); }
            if (y > GAME_HEIGHT) isAlive = false;

            animTick++;
            if (animTick > 2) { animState = (animState + 1) % 4; animTick = 0; }
        }

        public void render(GuiGraphics guiGraphics) {
            guiGraphics.blit(TEX_A, boardX + (int)x, boardY + (int)y, 30, 30, animX[animState], animY[animState], 12, 10, 308, 287);
        }
    }

    private class Fireball extends Hazard {
        float vx = 2.0f, vy = 0.0f;
        // Size constants – small so Mario can jump over them
        private static final int SIZE = 16;
        public Fireball(int x, int y) { this.x = x; this.y = y; }

        @Override
        Rectangle getBounds() { return new Rectangle((int)x + 2, (int)y + 2, SIZE - 4, SIZE - 4); }

        public void tick() {
            vy += 1.2f; if (vy > 12.0f) vy = 12.0f;
            x += vx; y += vy;

            for (LevelObj l : Levels) {
                if (x + SIZE / 2 >= l.x && x + SIZE / 2 <= l.x + l.width) {
                    int platY = l.getYAt(x + SIZE / 2);
                    if (y + SIZE >= platY - 8 && y + SIZE <= platY + 15 && vy >= 0) {
                        y = platY - SIZE; vy = 0;
                        if (random.nextInt(100) < 2) vx = -vx; // Rare direction flip
                        break;
                    }
                }
            }
            if (x >= GAME_WIDTH - SIZE) { x = GAME_WIDTH - SIZE; vx = -Math.abs(vx); }
            if (x <= 0) { x = 0; vx = Math.abs(vx); }
            if (y > GAME_HEIGHT) isAlive = false;
        }

        public void render(GuiGraphics g) {
            int flicker = (int)(System.currentTimeMillis() / 150) % 2;
            // Body (bottom half)
            g.fill(boardX + (int)x,              boardY + (int)y + 8,  boardX + (int)x + SIZE,     boardY + (int)y + SIZE, flicker == 0 ? 0xFFFF6600 : 0xFFFF2200);
            // Tip (top half, narrower)
            g.fill(boardX + (int)x + 4,          boardY + (int)y,       boardX + (int)x + SIZE - 4, boardY + (int)y + 9,   flicker == 0 ? 0xFFFFAA00 : 0xFFFF6600);
        }
    }

    private class Spring extends Hazard {
        float vx = -4.0f, vy = 0.0f;
        public Spring() { x = 500; y = 50; }
        public void tick() {
            vy += 0.5f;
            x += vx; y += vy;
            for (LevelObj l : Levels) {
                if (x + 10 >= l.x && x + 10 <= l.x + l.width) {
                    int platY = l.getYAt(x + 10);
                    if (y + 20 >= platY - 8 && y + 20 <= platY + 15 && vy >= 0) {
                        y = platY - 20; vy = -8.0f; // Bounce
                        break;
                    }
                }
            }
            if (x < 0 || y > GAME_HEIGHT) isAlive = false;
        }
        public void render(GuiGraphics g) {
            g.fill(boardX + (int)x, boardY + (int)y, boardX + (int)x + 20, boardY + (int)y + 20, 0xFF00FF00);
        }
    }

    private class Kong {
        float x, y;
        int animState = 0, animTick = 0;
        public Kong(float x, float y) { this.x = x; this.y = y; }
        public void tick() {
            animTick++;
            if (animTick > 20) { animState = (animState == 0) ? 1 : 0; animTick = 0; }
        }
        public void render(GuiGraphics guiGraphics) {
            int texX = (animState == 0) ? 58 : 202;
            guiGraphics.blit(TEX_A, boardX + (int)x, boardY + (int)y, 75, 75, texX, 152, 46, 32, 308, 287);
        }
    }
}
