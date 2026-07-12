package com.fisch.client.hud;

import com.fisch.client.ClientMoneyStorage;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class CoinHudOverlay implements HudRenderCallback {
    // Твоя текстура
    private static final ResourceLocation COIN_TEXTURE = new ResourceLocation("fisch", "textures/item/monetka.png");

    @Override
    public void onHudRender(GuiGraphics guiGraphics, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) return;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        long balance = ClientMoneyStorage.getBalance();
        String balanceText = String.valueOf(balance);

        // Правый нижний угол (с отступом 20 пикселей)
        int coinX = screenWidth - 20;
        int coinY = screenHeight - 20;

        // МАГИЯ ЗДЕСЬ:
        // Мы говорим игре: "Нарисуй квадрат 16x16 на экране, но впихни в него текстуру размером 24x24"
        guiGraphics.blit(COIN_TEXTURE, coinX, coinY, 16, 16, 0.0f, 0.0f, 24, 24, 24, 24);

        // Рисуем текст левее монетки
        int textWidth = client.font.width(balanceText);
        int textX = coinX - textWidth - 4; // Отступ 4 пикселя от монетки
        int textY = coinY + 4; // Центруем по высоте относительно новой сжатой монетки

        // Выводим золотой баланс
        guiGraphics.drawString(client.font, balanceText, textX, textY, 0xFFD700, true);
    }
}