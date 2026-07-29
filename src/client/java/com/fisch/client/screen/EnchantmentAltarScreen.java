package com.fisch.client.screen;

import com.fisch.FischMod;
import com.fisch.screen.EnchantmentAltarScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class EnchantmentAltarScreen extends AbstractContainerScreen<EnchantmentAltarScreenHandler> {
    private static final ResourceLocation INVENTORY_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/inventory.png");

    private static final int BG_W = 176;
    private static final int BG_H = 166;
    private static final int BTN_X = 80;
    private static final int BTN_Y = 33;
    private static final int BTN_W = 18;
    private static final int BTN_H = 18;

    public EnchantmentAltarScreen(EnchantmentAltarScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = BG_W;
        this.imageHeight = BG_H;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        graphics.fill(x, y, x + BG_W, y + BG_H, 0xFFC6C6C6);

        graphics.fill(x, y, x + BG_W, y + 1, 0xFF555555);
        graphics.fill(x, y, x + 1, y + BG_H, 0xFF555555);
        graphics.fill(x + BG_W - 1, y, x + BG_W, y + BG_H, 0xFF373737);
        graphics.fill(x, y + BG_H - 1, x + BG_W, y + BG_H, 0xFF373737);

        drawSlot(graphics, x + 44, y + 35);
        drawSlot(graphics, x + 116, y + 35);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(graphics, x + 8 + col * 18, y + 84 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlot(graphics, x + 8 + col * 18, y + 142);
        }

        drawEnchantButton(graphics, x + BTN_X, y + BTN_Y);

        graphics.drawCenteredString(this.font, Component.translatable("gui.fisch.crate.rod"), x + 53, y + 55, 0xFFCE93D8);
        graphics.drawCenteredString(this.font, Component.translatable("gui.fisch.crate.relic"), x + 125, y + 55, 0xFFCE93D8);
    }

    private void drawEnchantButton(GuiGraphics graphics, int x, int y) {
        int color = isEnchantButtonHovered() ? 0xFFE1BEE7 : 0xFFCE93D8;
        graphics.drawCenteredString(this.font, Component.literal("\u2697"), x + BTN_W / 2, y + 5, color);
    }

    private boolean isMouseOverButton(double mouseX, double mouseY) {
        int bx = this.leftPos + BTN_X;
        int by = this.topPos + BTN_Y;
        return mouseX >= bx && mouseX < bx + BTN_W && mouseY >= by && mouseY < by + BTN_H;
    }

    private boolean isEnchantButtonHovered() {
        return isMouseOverButton(this.minecraft.mouseHandler.xpos(), this.minecraft.mouseHandler.ypos());
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.blit(INVENTORY_TEXTURE, x, y, 7, 83, 18, 18, 256, 256);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);

        if (isMouseOverButton(mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("message.fisch.enchant.tooltip"), mouseX, mouseY);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverButton(mouseX, mouseY)) {
            ClientPlayNetworking.send(FischMod.ENCHANT_ROD_PACKET_ID, PacketByteBufs.create());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
