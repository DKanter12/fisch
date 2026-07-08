package com.fisch.client.screen;

import com.fisch.FischMod;
import com.fisch.rod.RodMechanics;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.logging.Logger;

public class  FishCatchScreen extends Screen {

    private static final ResourceLocation FISH_CATCH_BAR =
            new ResourceLocation("fisch", "textures/screen/fish_catch_bar.png");

    private final String fishName;
    private final int rarity;
    private final float control;
    private float progress = 50f;
    private final float resilience;

    int PLAYER_BAR_WIDTH;

    // Скорость изменения прогресса
    private static final float PROGRESS_GAIN = 0.5f;
    private static final float PROGRESS_LOSS = 0.5f;

    private static final float PLAYER_MOVE_SPEED = 4.0f;
    private static final float PLAYER_FALL_SPEED = 3.0f;

    private static final int FISH_WIDTH = 4;
    private static final float FISH_ACCELERATION = 0.03f;
    private static final float FISH_FRICTION = 0.90f;

    private float marker = 0;

    private final int barWidth = 225;
    private final int barX = 125;
    private final int barY = 200;

    // Рыба
    private float fishX = 0f;
    private float fishTargetX = 0f;
    private float fishVelocity = 0f;

    private boolean leftMouseHeld = false;

    private int tickCounter = 0;


    public FishCatchScreen(String fishName, int rarity, float control, float resilience) {
        super(Component.literal("Улов!"));
        this.fishName = fishName;
        this.rarity = rarity;
        this.control = control;
        this.resilience = resilience;

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

        float speedMultiplier =
                RodMechanics.getFishSpeedMultiplier(rarity) *
                        RodMechanics.getResilienceMultiplier(resilience);

        fishTargetX += RodMechanics.getFishX(
                RodMechanics.getFishMovement(rarity)
        ) * speedMultiplier;

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

        int playerX1 = (int) marker;
        int playerX2 = (int) marker + PLAYER_BAR_WIDTH;

        int fishX1 = (int) fishX;
        int fishX2 = (int) fishX + FISH_WIDTH;

        if (RodMechanics.checkProgress(playerX1, playerX2, fishX1, fishX2)) {
            progress += PROGRESS_GAIN;
        } else {
            progress -= PROGRESS_LOSS;
        }

        if (progress >= 100) {

            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(true);

            ClientPlayNetworking.send(
                    FischMod.FINISH_MINIGAME_PACKET_ID,
                    buf
            );

            onClose();
            return;
        }

        if (progress <= 0) {

            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(false);

            ClientPlayNetworking.send(
                    FischMod.FINISH_MINIGAME_PACKET_ID,
                    buf
            );

            onClose();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        PLAYER_BAR_WIDTH = getPlayerBarWidth();
        g.blit(FISH_CATCH_BAR, barX, barY, 1f, 0, barWidth - 2, 10, barWidth , 10);

        //игрок
        g.fill(
                (int)(barX + marker),
                barY,
                (int)(barX + marker + PLAYER_BAR_WIDTH),
                barY + 10,
                0xFFFFFFFF
        );

        // рыба
        g.fill(
                (int)(barX + fishX),
                barY,
                (int)(barX + fishX + FISH_WIDTH),
                barY + 10,
                0xFF808080
        );



        g.drawString(
                this.font,
                "Catch: " + fishName.replace("_",  " "),
                barX,
                barY - 15,
                getRarityColor(rarity)
        );

        int progressWidth = 100;
        int progressHeight = 4;

        int progressX = barX + (barWidth - progressWidth) / 2;
        int progressY = barY - 20;

// Фон
        g.blit(FISH_CATCH_BAR,
                progressX,
                progressY, 0, 0,
                 progressWidth,
                 progressHeight,
                 progressWidth,
                progressHeight
                );

// Заполнение
        g.fill(
                progressX ,
                progressY ,
                progressX + (int)(progressWidth * (progress / 100f)),
                progressY + progressHeight,
                0xFFFFFFFF
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

    private int getPlayerBarWidth() {

        float minControl = 0.01f;
        float maxControl = 1f;

        float clamped = Math.max(minControl,
                Math.min(control, maxControl));

        float normalized =
                (float)(
                        Math.log(clamped / minControl)
                                / Math.log(maxControl / minControl)
                );

        return (int)(25 + normalized * 150);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private int getRarityColor(int rarity) {
        return switch (rarity) {
            case 8 -> 0xFFFFFFFF; // Common
            case 7 -> 0xFFA8E61D; // Uncommon
            case 6 -> 0xFF7D3FA6; // Unusual
            case 5 -> 0xFF2B0047; // Rare
            case 4 -> 0xFFFCCA00; // Legendary
            case 3 -> 0xFFFA0C1C; // Mythical
            case 2 -> 0xFF2200FF; // Exotic
            case 1 -> 0x080808; // Secret
            default -> 0xFFFFFFFF;
        };
    }

}