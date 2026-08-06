package com.fisch.client.hud;

import com.fisch.client.ClientMoneyStorage;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class CoinHudOverlay implements HudRenderCallback {
    // Твоя текстура
    private static final ResourceLocation COIN_TEXTURE = new ResourceLocation("fisch", "textures/item/monetka.png");

    // Переменная, которая хранит состояние (скрыты монетки или нет)
    public static boolean isHidden = false;

    @Override
    public void onHudRender(GuiGraphics guiGraphics, float tickDelta) {
        Minecraft client = Minecraft.getInstance();

        // Если игрока нет или нажат F1 (скрытие интерфейса) - ничего не рисуем
        if (client == null || client.player == null || client.options.hideGui) return;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        // Правый нижний угол
        int coinX = screenWidth - 20;
        int coinY = screenHeight - 20;

        if (isHidden) {
            // Если баланс СКРЫТ: рисуем только серенькую стрелочку
            guiGraphics.drawString(client.font, ">", coinX + 4, coinY + 4, 0xAAAAAA, true);
        } else {
            // Если баланс ОТКРЫТ: рисуем всё как обычно
            long balance = ClientMoneyStorage.getBalance();
            String balanceText = String.valueOf(balance);

            // Монетка
            guiGraphics.blit(COIN_TEXTURE, coinX, coinY, 16, 16, 0.0f, 0.0f, 24, 24, 24, 24);

            // Текст левее монетки
            int textWidth = client.font.width(balanceText);
            int textX = coinX - textWidth - 4;
            int textY = coinY + 4;

            // Выводим золотой баланс
            guiGraphics.drawString(client.font, balanceText, textX, textY, 0xFFD700, true);

            // Маленькая серая стрелочка сверху монетки, показывающая, что ее можно скрыть
            guiGraphics.drawString(client.font, "v", coinX + 5, coinY - 8, 0xAAAAAA, true);
        }
    }
}