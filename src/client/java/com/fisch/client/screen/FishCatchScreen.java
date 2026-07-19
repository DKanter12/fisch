package com.fisch.client.screen;

import com.fisch.FischMod;
import com.fisch.rod.RodMechanics;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class FishCatchScreen extends Screen {

    private static final ResourceLocation FISH_CATCH_BAR =
            new ResourceLocation("fisch", "textures/screen/fish_catch_bar.png");

    // Идентификатор нашего кастомного звука
    private static final ResourceLocation REEL_SOUND_ID =
            new ResourceLocation("fisch", "reel_sound");

    private final String fishName;
    private final int rarity;
    private final float control;
    private float progress = 50f;
    private final float resilience;

    int PLAYER_BAR_WIDTH;

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

    private float fishX = 0f;
    private float fishTargetX = 0f;
    private float fishVelocity = 0f;

    private boolean leftMouseHeld = false;
    private int tickCounter = 0;

    private int gameTicks = 0;

    // Переменная для хранения проигрываемого звука
    private SimpleSoundInstance currentSound;

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
        gameTicks = 0;

        // Запускаем звук при открытии экрана
        playReelSound();
    }

    // Метод для запуска звука
    private void playReelSound() {
        // Если звук уже играет, останавливаем его перед новым запуском
        stopReelSound();

        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(REEL_SOUND_ID);
        // Создаем звук для интерфейса (Громкость 1.0, Высота 1.0)
        currentSound = SimpleSoundInstance.forUI(soundEvent, 1.0F, 1.0F);

        Minecraft.getInstance().getSoundManager().play(currentSound);
    }

    // Метод для остановки звука
    private void stopReelSound() {
        if (currentSound != null) {
            Minecraft.getInstance().getSoundManager().stop(currentSound);
            currentSound = null;
        }
    }

    // Этот метод вызывается игрой автоматически, когда экран закрывается
    @Override
    public void removed() {
        super.removed();
        // Обрубаем звук, когда выходим из мини-игры
        stopReelSound();
    }

    @Override
    public void tick() {
        gameTicks++;

        // Перезапускаем звук каждые 10 секунд (200 тиков)
        if (gameTicks > 0 && gameTicks % 200 == 0) {
            playReelSound();
        }

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

        g.fill(
                (int)(barX + marker),
                barY,
                (int)(barX + marker + PLAYER_BAR_WIDTH),
                barY + 10,
                0xFFFFFFFF
        );

        g.fill(
                (int)(barX + fishX),
                barY,
                (int)(barX + fishX + FISH_WIDTH),
                barY + 10,
                0xFF808080
        );

        int progressWidth = 100;
        int progressHeight = 4;
        int progressX = barX + (barWidth - progressWidth) / 2;
        int progressY = barY - 20;

        g.blit(FISH_CATCH_BAR,
                progressX,
                progressY, 0, 0,
                progressWidth,
                progressHeight,
                progressWidth,
                progressHeight
        );

        g.fill(
                progressX ,
                progressY ,
                progressX + (int)(progressWidth * (progress / 100f)),
                progressY + progressHeight,
                0xFFFFFFFF
        );

        if (gameTicks < 80) {
            ResourceLocation markTexture = getExclamationTexture(this.rarity);

            // Увеличили ширину знака, чтобы он не был худым
            int markW = 18;
            int markH = 26;

            // Отодвинули чуть левее (с 25 на 30), чтобы из-за ширины он не задел белую полоску
            int markX = progressX - 30;
            int markY = progressY - (markH / 2) + (progressHeight / 2);

            g.blit(markTexture, markX, markY, 0, 0, markW, markH, markW, markH);
        }
    }

    private ResourceLocation getExclamationTexture(int rarity) {
        return switch (rarity) {
            case 10 -> new ResourceLocation("fisch", "textures/screen/gray_exclamation_mark.png");
            case 8 -> new ResourceLocation("fisch", "textures/screen/white_exclamation_mark.png");
            case 7 -> new ResourceLocation("fisch", "textures/screen/green_exclamation_mark.png");
            case 6 -> new ResourceLocation("fisch", "textures/screen/turquoise_exclamation_mark.png");
            case 5 -> new ResourceLocation("fisch", "textures/screen/purple_exclamation_mark.png");
            case 4 -> new ResourceLocation("fisch", "textures/screen/yellow_exclamation_mark.png");
            case 3 -> new ResourceLocation("fisch", "textures/screen/red_exclamation_mark.png");
            case 2, 1 -> new ResourceLocation("fisch", "textures/screen/burgundy_exclamation_mark.png");
            default -> new ResourceLocation("fisch", "textures/screen/white_exclamation_mark.png");
        };
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
}