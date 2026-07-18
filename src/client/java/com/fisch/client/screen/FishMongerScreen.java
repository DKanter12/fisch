package com.fisch.client.screen;

import com.fisch.client.ClientMoneyStorage;
import com.fisch.menu.FishMongerMenu;
import com.fisch.networking.ModPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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

        // Добавляем нашу кастомную тёмную кнопку вместо стандартной
        this.addRenderableWidget(new DarkButton(
                this.width / 2 - 60,
                this.topPos + 82,
                120, 20,
                Component.translatable("gui.fisch.buy_rod"),
                () -> ClientPlayNetworking.send(ModPackets.BUY_ROD_C2S, PacketByteBufs.empty())
        ));
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

        // Главный фон с двойной рамкой
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF000000); // Чёрная окантовка
        guiGraphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + this.imageHeight - 1, 0xFF2D2D2D); // Тёмно-серый фон

        // Разделительная линия (вдавленная)
        int lineY = y + 32;
        guiGraphics.fill(x + 10, lineY, x + this.imageWidth - 10, lineY + 1, 0xFF111111); // Тень
        guiGraphics.fill(x + 10, lineY + 1, x + this.imageWidth - 10, lineY + 2, 0xFF444444); // Блик

        int centerX = this.width / 2;

        // Рисуем тёмные вдавленные слоты
        drawCustomSlot(guiGraphics, centerX - 45, y + 42);
        drawCustomSlot(guiGraphics, centerX + 19, y + 42);

        // Иконка монеты
        guiGraphics.blit(COIN_ICON, centerX - 44, y + 43, 0, 0, 16, 16, 16, 16);

        // Стрелка
        try {
            guiGraphics.blit(ARROW_ICON, centerX - 12, y + 43, 0, 0, 24, 16, 24, 16);
        } catch (Exception e) {
            guiGraphics.drawString(this.font, "➔", centerX - 5, y + 47, 0xFFFFFF, false);
        }

        // Рендер удочки
        ItemStack rodStack = new ItemStack(this.menu.rodItem);
        guiGraphics.renderItem(rodStack, centerX + 20, y + 43);

        // Тултип для удочки при наведении
        if (mouseX >= centerX + 19 && mouseX <= centerX + 37 && mouseY >= y + 42 && mouseY <= y + 60) {
            guiGraphics.renderTooltip(this.font, rodStack, mouseX, mouseY);
        }
    }

    private void drawCustomSlot(GuiGraphics guiGraphics, int x, int y) {
        // Темный, почти черный фон слота
        guiGraphics.fill(x, y, x + 18, y + 18, 0xFF151515);

        // Внутренняя тень (верхняя и левая грани)
        guiGraphics.fill(x, y, x + 18, y + 1, 0xFF000000);
        guiGraphics.fill(x, y, x + 1, y + 18, 0xFF000000);

        // Внутренний блик (нижняя и правая грани)
        guiGraphics.fill(x, y + 17, x + 18, y + 18, 0xFF404040);
        guiGraphics.fill(x + 17, y, x + 18, y + 18, 0xFF404040);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Заголовок
        Component shopTitle = Component.translatable("gui.fisch.shop_title");
        int titleWidth = this.font.width(shopTitle);
        guiGraphics.drawString(this.font, shopTitle, (this.imageWidth - titleWidth) / 2, 8, 0xFFD700, true);

        // Баланс
        Component balanceText = Component.translatable("gui.fisch.balance", ClientMoneyStorage.getBalance());
        int balanceWidth = this.font.width(balanceText);
        guiGraphics.drawString(this.font, balanceText, (this.imageWidth - balanceWidth) / 2, 20, 0x55FF55, true);

        // Цена товара (Идеально отцентрирована под монетой с добавлением тени)
        long price = this.menu.getPriceForItem(this.menu.rodItem);
        String priceText = price + " C$";
        int priceWidth = this.font.width(priceText);
        int slotCenterX = (this.imageWidth / 2) - 45 + 9; // Центр слота монеты

        // true в конце включает красивую тень для текста
        guiGraphics.drawString(this.font, priceText, slotCenterX - (priceWidth / 2), 64, 0xFFD700, true);
    }

    // ==========================================
    // ВНУТРЕННИЙ КЛАСС КАСТОМНОЙ ТЁМНОЙ КНОПКИ
    // ==========================================
    private class DarkButton extends AbstractButton {
        private final Runnable onPressAction;

        public DarkButton(int x, int y, int width, int height, Component message, Runnable onPressAction) {
            super(x, y, width, height, message);
            this.onPressAction = onPressAction;
        }

        @Override
        public void onPress() {
            this.onPressAction.run();
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // Цвет фона меняется при наведении
            int bgColor = this.isHoveredOrFocused() ? 0xFF3D3D3D : 0xFF222222;

            // Черная рамка кнопки
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF000000);

            // Заливка фона кнопки
            guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, bgColor);

            // Текст (белый если активна, серый если недоступна)
            int textColor = this.active ? 0xFFFFFF : 0xA0A0A0;
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColor);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}