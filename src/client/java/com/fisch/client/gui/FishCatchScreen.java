package com.fisch.client.gui;

import com.fisch.RodMechanics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FishCatchScreen extends Screen {

    private static final ResourceLocation BG =
            new ResourceLocation("fisch", "textures/gui/fish_catch_bg.png");

    private final String fishName;

    // Позиция ползунка игрока
    private int marker = 0;


    // Ширина полоски
    private final int barWidth = 275;
    int barX = 125;
    int barY = 200;
    private int xFish = barX;
    // Зажата ли ЛКМ
    private boolean leftMouseHeld = false;

    // Счётчик тиков для движения влево
    private int tickCounter = 0;

    public FishCatchScreen(String fishName) {
        super(Component.literal("Улов!"));
        this.fishName = fishName;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void tick() {
         xFish += RodMechanics.getFishX(4);

         if (xFish >= barWidth) {
             xFish = barWidth + 1 + 4;
        }
        if (xFish <= barX) {
            xFish = barX + 1 + 4;
        }



        if (leftMouseHeld) {
        marker += 2;
        } else {
            // Не зажата -> каждые 2 тика влево
            tickCounter++;

            if (tickCounter >= 1) {
                marker -= 2;
                tickCounter = 0;
            }
        }

        // Ограничение границ
        if (marker < 0) {
            marker = 0;
        }

        if (marker + 22 > barWidth) {
            marker = barWidth - 22;
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);


        // Фон полоски
        g.blit(BG, barX, barY, 0, 0, barWidth, 10, barWidth, 10);

        // Ползунок игрока
        g.fill(
                barX + marker,
                barY ,
                barX + marker + 20,
                barY + 10,
                0xFFFF0000
        );
        g.fill(
                barX + xFish,
                barY ,
                barX + xFish + 4,
                barY + 10,
                0xFFFF0000
        );

        g.drawString(
                this.font,
                "Fish: " + fishName,
                barX,
                barY - 15,
                0xFFFFFF
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        // ЛКМ
        if (button == 0) {
            leftMouseHeld = true;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {

        // Отпустили ЛКМ
        if (button == 0) {
            leftMouseHeld = false;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}