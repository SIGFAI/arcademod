package superhb.arcademod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import superhb.arcademod.Arcade;
import superhb.arcademod.Reference;
import superhb.arcademod.api.gui.GuiArrow;
import superhb.arcademod.client.ArcadeItems;
import superhb.arcademod.client.tileentity.BlockEntityPrize;
import superhb.arcademod.network.ArcadePacketHandler;
import superhb.arcademod.network.ServerBuyMessage;

import java.awt.Color;

public class GuiPrize extends Screen {
    private GuiArrow prizeNext, prizePrev, amountUp, amountDown;
    private Button buy;

    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/prize_box.png");

    private static final int GUI_X = 124;
    private static final int GUI_Y = 74;
    private int guiLeft = 0, guiTop = 0;
    private int amount = 1, curPrize = 0;

    private boolean isEnough = true;

    private final BlockEntityPrize tile;

    public GuiPrize(BlockEntityPrize tile) {
        super(Component.literal("Prize Box"));
        this.tile = tile;
    }

    public void isEnough(boolean enough) {
        isEnough = enough;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(TEXTURE, (width / 2) - (GUI_X / 2), (height / 2) - (GUI_Y / 2), 0, 0, GUI_X, GUI_Y);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        String name = tile.getDisplayName().getString();
        int nameWidth = font.width(name);
        guiGraphics.drawString(font, name, (width / 2) - (nameWidth / 2), guiTop + 4, Color.darkGray.getRGB(), false);

        // Amount
        String amountText = String.valueOf(amount);
        guiGraphics.drawString(font, amountText, (guiLeft + 91) - (font.width(amountText) / 2), (guiTop + 34), Color.white.getRGB(), false);

        // Cost
        int cost = Arcade.prizeList[curPrize].getPrice() * amount;
        guiGraphics.drawString(font, "Cost: " + cost, (guiLeft + 70), (guiTop + 63), isEnough ? Color.darkGray.getRGB() : Color.red.getRGB(), false);

        // Draw Prizes
        guiGraphics.fill(guiLeft + 31, guiTop + 29, guiLeft + 31 + 16, guiTop + 29 + 16, 0x80000000);
        ItemStack stack = Arcade.prizeList[curPrize].getStack();
        guiGraphics.renderFakeItem(stack, guiLeft + 31, guiTop + 29);
        // guiGraphics.renderItemDecorations(font, stack, guiLeft + 31, guiTop + 29);
    }

    @Override
    protected void init() {
        super.init();

        guiLeft = (width / 2) - (GUI_X / 2);
        guiTop = (height / 2) - (GUI_Y / 2);

        this.addRenderableWidget(prizeNext = new GuiArrow((guiLeft + 50), (guiTop + 32), 14, 22, 2, button -> {
            if (curPrize != (Arcade.prizeList.length - 1)) curPrize++;
        }));
        this.addRenderableWidget(prizePrev = new GuiArrow((guiLeft + 20), (guiTop + 32), 14, 22, 3, button -> {
            if (curPrize != 0) curPrize--;
        }));
        this.addRenderableWidget(amountUp = new GuiArrow((guiLeft + 85), (guiTop + 19), 11, 7, 5, button -> {
            if (amount != 64) amount++;
        }));
        this.addRenderableWidget(amountDown = new GuiArrow((guiLeft + 85), (guiTop + 48), 11, 7, 4, button -> {
            if (amount != 1) amount--;
        }));
        this.addRenderableWidget(buy = Button.builder(Component.literal("Buy"), button -> {
            ArcadePacketHandler.INSTANCE.sendToServer(new ServerBuyMessage(Arcade.prizeList[curPrize].getStack(), new ItemStack(ArcadeItems.TICKET), amount, Arcade.prizeList[curPrize].getPrice() * amount));
        }).bounds((guiLeft + 30), (guiTop + GUI_Y - 25), 30, 20).build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
