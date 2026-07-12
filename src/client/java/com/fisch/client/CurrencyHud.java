package com.fisch.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class CurrencyHud implements HudRenderCallback {

    // Переменная для синхронизации баланса из ClientNetwork
    public static long clientMoney = 0;

    // Путь к текстуре монетки
    private static final ResourceLocation COIN_TEXTURE = new ResourceLocation("fisch", "textures/gui/monetka.png");

    @Override
    public void onHudRender(GuiGraphics guiGraphics, float tickDelta) {
        Minecraft client = Minecraft.getInstance();

        // Не рисуем HUD, если игрок не в мире или скрыл интерфейс на F1
        if (client.player == null || client.options.hideGui) {
            return;
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        // 1. ПОДГОТОВКА И РАСЧЕТ РАЗМЕРОВ
        String amountText = String.valueOf(clientMoney);

        // Узнаем точную ширину текста в пикселях (зависит от количества цифр)
        int textWidth = client.font.width(amountText);

        int coinSize = 16; // Твой новый уменьшенный размер монетки на экране (было 24)
        int gap = 4;       // Расстояние в пикселях между монеткой и цифрами

        // Общая ширина всей нашей конструкции (монетка + отступ + цифры)
        int totalWidth = coinSize + gap + textWidth;

        // Расчет координат: отнимаем totalWidth от правой границы экрана.
        // Теперь HUD намертво привязан к правому краю (с отступом в 10 пикселей) и увеличивается влево!
        int x = width - totalWidth - 10;
        int y = height - coinSize - 5; // 10 пикселей от нижнего края экрана

        // 2. ОТРИСОВКА УМЕНЬШЕННОЙ МОНЕТКИ С МАСШТАБИРОВАНИЕМ
        // Параметры по порядку:
        // текстура, x, y,
        // ширина на экране (16), высота на экране (16),
        // u-координата (0), v-координата (0),
        // ширина области в файле (24), высота области в файле (24),
        // разрешение самого файла текстуры (24, 24)
        guiGraphics.blit(COIN_TEXTURE, x, y, coinSize, coinSize, 0f, 0f, 24, 24, 24, 24);

        // 3. ОТРИСОВКА БАЛАНСА РЯДОМ
        // Текст начинается сразу после монетки и небольшого зазора
        int textX = x + coinSize + gap;

        // Автоматически центрируем текст по вертикали относительно монетки
        // (высота стандартного шрифта Майнкрафта в среднем 8 пикселей)
        int textY = y + (coinSize - 8) / 2;

        guiGraphics.drawString(client.font, amountText, textX, textY, 0xFFFFFFFF, true);
    }
}