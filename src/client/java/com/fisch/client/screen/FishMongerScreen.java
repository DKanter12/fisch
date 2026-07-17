package com.fisch.client.screen;

import com.fisch.client.ClientMoneyStorage;
import com.fisch.menu.FishMongerMenu;
import com.fisch.networking.ModPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class FishMongerScreen extends AbstractContainerScreen<FishMongerMenu> {

    private static final ResourceLocation COIN_ICON = new ResourceLocation("fisch", "textures/item/monetka.png");
    private static final ResourceLocation ARROW_ICON = new ResourceLocation("fisch", "textures/gui/arrow.png");

    public FishMongerScreen(FishMongerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 114;
    }

    @Override
    protected void init() {
        super.init();

        // ИСПОЛЬЗУЕМ КЛЮЧ ПЕРЕВОДА ДЛЯ КНОПКИ
        this.addRenderableWidget(Button.builder(Component.translatable("gui.fisch.buy_rod"), button -> {
            ClientPlayNetworking.send(ModPackets.BUY_ROD_C2S, PacketByteBufs.empty());
        }).bounds(this.width / 2 - 60, this.topPos + 80, 120, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF101010);
        guiGraphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, 0xFF2D2D2D);

        int centerX = this.width / 2;

        drawCustomSlot(guiGraphics, centerX - 45, y + 37);
        drawCustomSlot(guiGraphics, centerX + 19, y + 37);

        guiGraphics.blit(COIN_ICON, centerX - 44, y + 38, 0, 0, 16, 16, 16, 16);

        try {
            guiGraphics.blit(ARROW_ICON, centerX - 12, y + 38, 0, 0, 24, 16, 24, 16);
        } catch (Exception e) {
            guiGraphics.drawString(this.font, "➔", centerX - 5, y + 42, 0xFFFFFF, false);
        }

        ItemStack rodStack = new ItemStack(this.menu.rodItem);
        guiGraphics.renderItem(rodStack, centerX + 20, y + 38);

        if (mouseX >= centerX + 20 && mouseX <= centerX + 36 && mouseY >= y + 38 && mouseY <= y + 54) {
            guiGraphics.renderTooltip(this.font, rodStack, mouseX, mouseY);
        }
    }

    private void drawCustomSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x - 1, y - 1, x + 17, y, 0xFF101010);
        guiGraphics.fill(x - 1, y, x, y + 17, 0xFF101010);
        guiGraphics.fill(x + 17, y - 1, x + 18, y + 18, 0xFFFFFFFF);
        guiGraphics.fill(x - 1, y + 17, x + 17, y + 18, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 17, y + 17, 0xFF8B8B8B);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // ИСПОЛЬЗУЕМ КЛЮЧ ПЕРЕВОДА ДЛЯ ЗАГОЛОВКА
        Component shopTitle = Component.translatable("gui.fisch.shop_title");
        int titleWidth = this.font.width(shopTitle);
        guiGraphics.drawString(this.font, shopTitle, (this.imageWidth - titleWidth) / 2, 8, 0xFFD700, true);

        // ИСПОЛЬЗУЕМ КЛЮЧ ПЕРЕВОДА ДЛЯ БАЛАНСА (передаем переменную внутрь)
        Component balanceText = Component.translatable("gui.fisch.balance", ClientMoneyStorage.getBalance());
        int balanceWidth = this.font.width(balanceText);
        guiGraphics.drawString(this.font, balanceText, (this.imageWidth - balanceWidth) / 2, 20, 0x55FF55, true);

        long price = this.menu.getPriceForItem(this.menu.rodItem);
        guiGraphics.drawString(this.font, price + " C$", (this.imageWidth / 2) - 52, 58, 0xFFD700, false);
    }
}