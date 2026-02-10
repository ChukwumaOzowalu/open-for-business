package com.hiipower.openforbusiness.client.screen;

import com.hiipower.openforbusiness.menu.LedgerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LedgerScreen extends AbstractContainerScreen<LedgerMenu> {

    private static final ResourceLocation HOPPER_TEX =
        new ResourceLocation("minecraft", "textures/gui/container/hopper.png");

    public LedgerScreen(LedgerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 133;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos;
        int y = this.topPos;

        // Minus button (id 0)
        this.addRenderableWidget(Button.builder(Component.literal("-"), btn -> {
            if (this.minecraft != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
            }
        }).bounds(x + 44, y + 18, 20, 20).build());

        // Plus button (id 1)
        this.addRenderableWidget(Button.builder(Component.literal("+"), btn -> {
            if (this.minecraft != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
            }
        }).bounds(x + 112, y + 18, 20, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        gfx.blit(HOPPER_TEX, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Price display
        int price = this.menu.getSelectedItemPrice();
        gfx.drawString(this.font, "Price: " + price, x + 70, y + 8, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderTooltip(gfx, mouseX, mouseY);
    }
}
