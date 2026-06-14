package com.fisch.client.screen;

import com.fisch.rod.RodMechanics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class  FishCatchScreen extends Screen {

    private static final ResourceLocation BG =
            new ResourceLocation("fisch", "textures/gui/fish_catch_bg.png");

    private final String fishName;
    private final int rarity;
    // Прогресс поимки
    private float progress = 50f;

    // Скорость изменения прогресса
    private static final float PROGRESS_GAIN = 0.5f;
    private static final float PROGRESS_LOSS = 0.5f;

    private static final int PLAYER_BAR_WIDTH = 25;

    private static final float PLAYER_MOVE_SPEED = 4.0f;
    private static final float PLAYER_FALL_SPEED = 3.0f;

    private static final int FISH_WIDTH = 4;

    private static final float FISH_SPEED = 0.8f;
    private static final float FISH_ACCELERATION = 0.03f;
    private static final float FISH_FRICTION = 0.90f;

    private float marker = 0;

    private final int barWidth = 275;
    private final int barX = 125;
    private final int barY = 200;

    // Рыба
    private float fishX = 0f;
    private float fishTargetX = 0f;
    private float fishVelocity = 0f;

    private boolean leftMouseHeld = false;

    private int tickCounter = 0;

    public FishCatchScreen(String fishName, int rarity) {
        super(Component.literal("Улов!"));
        this.fishName = fishName;
        this.rarity = rarity;
    }

    @Override
    protected void init() {
        super.init();
        progress = 50f;
        marker = 0;

        fishX = 0;
        fishTargetX = 0;
        fishVelocity = 0;
    }

    @Override
    public void tick() {

        // =========================
        // РЫБА
        // =========================

        fishTargetX += RodMechanics.getFishX(
                RodMechanics.getFishMovement(rarity)
        ) * RodMechanics.getFishSpeedMultiplier(rarity);

        if (fishTargetX < 0) {
            fishTargetX = 0;
        }

        if (fishTargetX > barWidth - FISH_WIDTH) {
            fishTargetX = barWidth - FISH_WIDTH;
        }

        fishVelocity += (fishTargetX - fishX) * FISH_ACCELERATION;
        fishVelocity *= FISH_FRICTION;
        fishX += fishVelocity;

        if (fishX < 0) {
            fishX = 0;
            fishVelocity = 0;
        }

        if (fishX > barWidth - FISH_WIDTH) {
            fishX = barWidth - FISH_WIDTH;
            fishVelocity = 0;
        }

        // =========================
        // ИГРОК
        // =========================

        if (leftMouseHeld) {
            marker += PLAYER_MOVE_SPEED;
        } else {
            tickCounter++;

            if (tickCounter >= 1) {
                marker -= PLAYER_FALL_SPEED;
                tickCounter = 0;
            }
        }

        if (marker < 0) {
            marker = 0;
        }

        if (marker + PLAYER_BAR_WIDTH > barWidth) {
            marker = barWidth - PLAYER_BAR_WIDTH;
        }
        // =========================
// ПРОГРЕСС ПОИМКИ
// =========================

        int playerX1 = (int) marker;
        int playerX2 = (int) marker + PLAYER_BAR_WIDTH;

        int fishX1 = (int) fishX;
        int fishX2 = (int) fishX + FISH_WIDTH;

        if (RodMechanics.checkProgress(
                playerX1,
                playerX2,
                fishX1,
                fishX2
        )) {
            progress += PROGRESS_GAIN;
        } else {
            progress -= PROGRESS_LOSS;
        }

        if (progress > 100) {
            progress = 100;

            // Поймал рыбу
            onClose();
        }

        if (progress < 0) {
            progress = 0;

            // Упустил рыбу
            onClose();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        // Фон
        g.blit(BG, barX, barY, 0, 0, barWidth, 10, barWidth, 10);

        // Игрок
        g.fill(
                (int)(barX + marker),
                barY,
                (int)(barX + marker + PLAYER_BAR_WIDTH),
                barY + 10,
                0xFFFF0000
        );

        // Рыба
        g.fill(
                (int)(barX + fishX),
                barY,
                (int)(barX + fishX + FISH_WIDTH),
                barY + 10,
                0xFF00FF00
        );

        g.drawString(
                this.font,
                "Fish: " + fishName,
                barX,
                barY - 15,
                0xFFFFFF
        );
        // =========================
// ПОЛОСА ПРОГРЕССА
// =========================

        int progressWidth = 200;
        int progressHeight = 8;

        int progressX = barX + (barWidth - progressWidth) / 2;
        int progressY = barY - 20;

// Фон
        g.fill(
                progressX,
                progressY,
                progressX + progressWidth,
                progressY + progressHeight,
                0xFF444444
        );

// Заполнение
        g.fill(
                progressX,
                progressY,
                progressX + (int)(progressWidth * (progress / 100f)),
                progressY + progressHeight,
                0xFF00FF00
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            leftMouseHeld = true;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
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