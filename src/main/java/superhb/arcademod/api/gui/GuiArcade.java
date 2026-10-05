package superhb.arcademod.api.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import superhb.arcademod.Arcade;
import superhb.arcademod.Config;
import superhb.arcademod.Reference;
import superhb.arcademod.client.tileentity.BlockEntityArcade;
import superhb.arcademod.network.ArcadePacketHandler;
import superhb.arcademod.network.RewardMessage;
import superhb.arcademod.network.ServerCoinMessage;
import superhb.arcademod.client.ArcadeItems;
import superhb.arcademod.Arcade;

import javax.annotation.Nullable;
import java.awt.Color;

public class GuiArcade extends Screen {
    public int tickCounter = 0;
    private boolean useTick = true;
    
    public boolean inMenu = true;
    public int menuOption = 0;
    public int menu = 0, startMenu = 0;
    public boolean useInternalMenu = true;
    public boolean editVolume;
    private float volume = 1.0f;
    private int iVol = 100;
    
    public int textureWidth = 256, textureHeight = 256;
    protected int guiLeft, guiTop;
    protected int xSize = 0, ySize = 0;
    protected ResourceLocation gui;
    protected Button insertCoin;
    protected int buttonX = 0, buttonY = 0;
    public int buttonWidth, buttonHeight = 20;
    protected int[] offset = { 0, 0 };
    protected float scale = 1;
    public int xScaled, yScaled;
    
    protected int cost = 1;
    protected boolean enoughCoins = true;
    public boolean canGetCoinBack = true;
    
    protected BlockEntityArcade tileEntity;
    protected Player player;
    protected Level world;
    protected BlockPos pos;
    
    private static final ResourceLocation ARROWS = new ResourceLocation(Reference.MODID, "textures/gui/gui_arrows.png");
    
    public GuiArcade(Level world, BlockEntityArcade tileEntity, @Nullable BlockPos pos, Player player, Component title) {
        super(title);
        this.pos = pos;
        this.world = world;
        this.tileEntity = tileEntity;
        this.player = player;
        if (useCoins()) {
            if (player != null && !player.isCreative()) menu = -1;
            else menu = startMenu;
        } else {
            menu = startMenu;
        }
    }
    
    public void setGuiSize(int width, int height) {
        this.xSize = width;
        this.ySize = height;
    }
    
    public void setGuiSize(int width, int height, float scale) {
        this.xSize = width;
        this.ySize = height;
        this.scale = scale;
    }
    
    public void setGuiScale(float scale) {
        this.scale = scale;
    }
    
    public float getGuiScale() {
        return scale;
    }
    
    public void disableInternalMenu(boolean disable) {
        useInternalMenu = !disable;
    }
    
    public void setButtonPos(int x, int y) {
        buttonX = x;
        buttonY = y;
    }
    
    public void setOffset(int x, int y) {
        offset[0] = x;
        offset[1] = y;
    }
    
    public void setStartMenu(int startMenu) {
        this.startMenu = startMenu;
    }
    
    public int getStartMenu() {
        return startMenu;
    }
    
    public void setTexture(ResourceLocation texture) {
        gui = texture;
    }
    
    public void setTexture(ResourceLocation texture, int width, int height) {
        gui = texture;
        textureWidth = width;
        textureHeight = height;
    }
    
    public void setButtonSize(int width, int height) {
        buttonWidth = width;
        buttonHeight = height;
    }
    
    public void isEnoughCoins(boolean enough) {
        enoughCoins = enough;
    }
    
    public boolean useCoins() {
        return !Config.disableCoins.get();
    }
    
    public void setCost(int cost) {
        this.cost = cost;
    }

    public void increaseVolume() {
        if (iVol < 100) iVol += 10;
    }

    public void decreaseVolume() {
        if (iVol > 0) iVol -= 10;
    }

    public void saveVolume(boolean update) {
        if (update) volume = iVol / 100f;
        else iVol = (int)(volume * 100);
    }

    public float getVolume() {
        return volume;
    }
    
    @Override
    public void tick() {
        if (useTick) tickCounter++;
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        xScaled = Math.round((width / 2) / scale);
        yScaled = Math.round((height / 2) / scale);
        
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);
        
        if (gui != null) {
            guiGraphics.blit(gui, xScaled - (xSize / 2), yScaled - (ySize / 2), 0, 0, xSize, ySize, textureWidth, textureHeight);
        }
        
        int scaledMouseX = (int)(mouseX / scale);
        int scaledMouseY = (int)(mouseY / scale);
        super.render(guiGraphics, scaledMouseX, scaledMouseY, partialTicks);
        
        if (useInternalMenu) {
            if (inMenu && menu == -1) {
                if (insertCoin != null) {
                    insertCoin.active = true;
                    insertCoin.visible = true;
                }
                guiGraphics.drawString(font, Component.translatable("button.arcademod.insert"), xScaled + offset[0] - 20, yScaled + offset[1], 0xFFFFFF);
                if (!enoughCoins) {
                    guiGraphics.drawString(font, Component.literal(cost + " needed"), xScaled + offset[0] - 20, yScaled + 10 + offset[1], 0xFF0000);
                }
            }
            if (menu != -1 && useCoins()) {
                if (insertCoin != null) {
                    insertCoin.active = false;
                    insertCoin.visible = false;
                }
            }
        }
        guiGraphics.pose().popPose();
    }
    
    public void drawLeftArrow(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(ARROWS, x, y, 7, 0, 7, 11, 128, 128);
    }
    
    public void drawRightArrow(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(ARROWS, x, y, 0, 0, 7, 11, 128, 128);
    }
    
    public void drawUpArrow(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(ARROWS, x, y, 25, 0, 11, 7, 128, 128);
    }
    
    public void drawDownArrow(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(ARROWS, x, y, 14, 0, 11, 7, 128, 128);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    protected void init() {
        super.init();
        this.font = Minecraft.getInstance().font;
        this.buttonWidth = this.font.width(Component.translatable("button.arcademod.insert")) + 6;
        
        guiLeft = Math.round((width / 2) / scale) - (xSize / 2);
        guiTop = Math.round((height / 2) / scale) - (ySize / 2);
        
        if (useCoins()) {
            insertCoin = Button.builder(Component.translatable("button.arcademod.insert"), button -> {
                Arcade.logger.info("GuiArcade: insertCoin pressed, sending coin packet");
                ArcadePacketHandler.INSTANCE.sendToServer(new ServerCoinMessage(new ItemStack(ArcadeItems.COIN), cost));
            }).bounds(guiLeft + buttonX, guiTop + buttonY, buttonWidth, buttonHeight).build();
            this.addRenderableWidget(insertCoin);
        }
    }

    @Override
    public void onClose() {
        Arcade.logger.info("GuiArcade: onClose() called for " + this.getTitle().getString());
        super.onClose();
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            if (canGetCoinBack && !minecraft.player.isCreative() && menu != -1) {
                ArcadePacketHandler.INSTANCE.sendToServer(new RewardMessage(new ItemStack(ArcadeItems.COIN, cost)));
            }
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    public void giveReward(ItemStack reward) {
        Arcade.logger.info("GuiArcade: sending RewardMessage for " + reward);
        ArcadePacketHandler.INSTANCE.sendToServer(new RewardMessage(reward));
    }
    
    public void checkMenuAfterGameOver() {
        if (useCoins()) {
            if (!minecraft.player.isCreative()) menu = -1;
            else menu = startMenu;
        } else {
            menu = startMenu;
        }
    }
}
