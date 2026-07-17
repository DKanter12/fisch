package com.fisch.client.screen;

import com.fisch.screen.BaitScreenHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BaitScreen extends AbstractContainerScreen<BaitScreenHandler> {

    /*
     * Текстура обычного ванильного инвентаря.
     *
     * Мы НЕ рисуем её целиком.
     * Берём только текстуру отдельного слота.
     */
    private static final ResourceLocation INVENTORY_TEXTURE =
            new ResourceLocation(
                    "minecraft",
                    "textures/gui/container/inventory.png"
            );

    public BaitScreen(
            BaitScreenHandler menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);

        /*
         * Размер интерфейса.
         *
         * 176 — стандартная ширина контейнера Minecraft.
         * 166 — высота.
         */
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {

        super.init();

        /*
         * Центрируем интерфейс.
         */
        this.leftPos =
                (this.width - this.imageWidth) / 2;

        this.topPos =
                (this.height - this.imageHeight) / 2;
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {

        /*
         * ============================================================
         * ОСНОВНОЙ ФОН
         * ============================================================
         *
         * Цвет максимально близкий к фону ванильных контейнеров.
         */
        graphics.fill(
                this.leftPos,
                this.topPos,
                this.leftPos + this.imageWidth,
                this.topPos + this.imageHeight,
                0xFFC6C6C6
        );


        /*
         * ============================================================
         * ВНЕШНЯЯ РАМКА
         * ============================================================
         */

        // Верхняя светлая граница
        graphics.fill(
                this.leftPos,
                this.topPos,
                this.leftPos + this.imageWidth,
                this.topPos + 4,
                0xFFFFFFFF
        );

        // Левая светлая граница
        graphics.fill(
                this.leftPos,
                this.topPos,
                this.leftPos + 4,
                this.topPos + this.imageHeight,
                0xFFFFFFFF
        );

        // Правая тёмная граница
        graphics.fill(
                this.leftPos + this.imageWidth - 4,
                this.topPos,
                this.leftPos + this.imageWidth,
                this.topPos + this.imageHeight,
                0xFF555555
        );

        // Нижняя тёмная граница
        graphics.fill(
                this.leftPos,
                this.topPos + this.imageHeight - 4,
                this.leftPos + this.imageWidth,
                this.topPos + this.imageHeight,
                0xFF555555
        );


        /*
         * ============================================================
         * СЛОТ ПРИМАНКИ
         * ============================================================
         *
         * Должен совпадать с координатами в BaitScreenHandler:
         *
         * new BaitSlot(
         *     baitContainer,
         *     80,
         *     35
         * )
         */
        drawVanillaSlot(
                graphics,
                this.leftPos + 80,
                this.topPos + 35
        );


        /*
         * ============================================================
         * ИНВЕНТАРЬ ИГРОКА
         * ============================================================
         *
         * 3 ряда по 9 слотов.
         */
        for (int row = 0; row < 3; row++) {

            for (int column = 0; column < 9; column++) {

                drawVanillaSlot(
                        graphics,

                        this.leftPos
                                + 8
                                + column * 18,

                        this.topPos
                                + 84
                                + row * 18
                );
            }
        }


        /*
         * ============================================================
         * ХОТБАР
         * ============================================================
         */
        for (int column = 0; column < 9; column++) {

            drawVanillaSlot(
                    graphics,

                    this.leftPos
                            + 8
                            + column * 18,

                    this.topPos + 142
            );
        }
    }


    /*
     * ================================================================
     * ОТРИСОВКА ОДНОГО ВАНИЛЬНОГО СЛОТА
     * ================================================================
     *
     * В inventory.png слот находится примерно в области:
     *
     * u = 7
     * v = 83
     *
     * Размер:
     *
     * 18 x 18
     *
     * Поэтому мы берём ТОЛЬКО слот,
     * а не всю текстуру инвентаря.
     */
    private void drawVanillaSlot(
            GuiGraphics graphics,
            int x,
            int y
    ) {

        graphics.blit(
                INVENTORY_TEXTURE,

                x,
                y,

                7,
                83,

                18,
                18,

                256,
                256
        );
    }


    /*
     * ================================================================
     * РЕНДЕР
     * ================================================================
     */
    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        this.renderBackground(guiGraphics);

        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                delta
        );

        // Заголовок над слотом приманки
        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("screen.fisch.bait"),
                this.width / 2,
                this.topPos + 25,
                0xAAAAAA
        );


        this.renderTooltip(
                guiGraphics,
                mouseX,
                mouseY
        );
    }
}