package com.fisch.client.screen;

import com.fisch.menu.FishMerchantMenu;
import com.fisch.networking.ModPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FishMerchantScreen extends AbstractContainerScreen<FishMerchantMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("textures/gui/container/shulker_box.png");
    private String priceEstimationText = "";
    private Button sellAllButton;
    private Button checkPriceButton;

    public FishMerchantScreen(FishMerchantMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int btnWidth = 54;
        int btnHeight = 20;
        int buttonsY = this.imageHeight + 4; // Координата Y для первого ряда кнопок

        // Кнопка "Продать"
        this.sellAllButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.fisch.merchant.sell"), button -> {
            ClientPlayNetworking.send(ModPackets.SELL_ITEMS_C2S, PacketByteBufs.create());
            this.priceEstimationText = Component.translatable("gui.fisch.merchant.sold").getString();
        }).bounds(this.leftPos + 6, this.topPos + buttonsY, btnWidth, btnHeight).build());

        // Кнопка "Закрыть"
        this.addRenderableWidget(Button.builder(Component.translatable("gui.fisch.merchant.close"), button -> {
            this.onClose();
        }).bounds(this.leftPos + 61, this.topPos + buttonsY, btnWidth, btnHeight).build());

        // Кнопка "Цена"
        this.checkPriceButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.fisch.merchant.price"), button -> {
            checkFishPrice(); // Вызываем проверку цены по клику
        }).bounds(this.leftPos + 116, this.topPos + buttonsY, btnWidth, btnHeight).build());
    }

    private void checkFishPrice() {
        // Забираем готовую сумму, которую для нас посчитал сервер в DataSlot
        int totalValue = this.menu.getTotalPrice();
        this.priceEstimationText = totalValue + " C$";
    }

    @Override
    public void containerTick() {
        super.containerTick();
        boolean hasItems = false;
        for (int i = 0; i < 27; i++) {
            if (!this.menu.getMerchantInventory().getItem(i).isEmpty()) {
                hasItems = true;
                break;
            }
        }
        this.sellAllButton.active = hasItems;
        this.checkPriceButton.active = hasItems;
        if (!hasItems && !this.priceEstimationText.equals("gui.fisch.merchant.sold")) this.priceEstimationText = "";
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Отрисовка названий «Рыботорговец» и «Инвентарь» стандартным тёмно-серым цветом
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

        // Отрисовка цены (срабатывает только после нажатия на кнопку "Цена", когда строка не пустая)
        if (!priceEstimationText.isEmpty()) {
            int titleWidth = this.font.width(this.title);
            int priceX = this.titleLabelX + titleWidth + 5;

            // Зелёный цвет для отображения цены: 0x00AA00
            guiGraphics.drawString(this.font, "➔ " + this.priceEstimationText, priceX, this.titleLabelY, 0x00AA00, false);
        }
    }
}