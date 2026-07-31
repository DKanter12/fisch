package com.fisch.entity;

import com.fisch.menu.WizardMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FishermanWizardEntity extends Villager {
    private Player tradingPlayer;

    // Переменная для хранения игрока
// 2. БЛОКИРУЕМ ИИ, ПОКА МЕНЮ ОТКРЫТО
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (this.tradingPlayer != null) {
            // Если игрок отошел слишком далеко или закрыл инвентарь (открыл обычный)
            if (this.tradingPlayer.distanceToSqr(this) > 16.0D || this.tradingPlayer.containerMenu == this.tradingPlayer.inventoryMenu) {
                // Отпускаем моба, пусть идет по своим делам
                this.tradingPlayer = null;
            } else {
                // Если игрок в меню - стоим на месте и смотрим прямо на него!
                this.getNavigation().stop();
                this.getLookControl().setLookAt(this.tradingPlayer, 30.0F, 30.0F);
                this.setYRot(this.yHeadRot); // Поворачиваем тело за головой
            }
        }
    }

    public void setTradingPlayer(Player player) {
        this.tradingPlayer = player;
    }

    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    public FishermanWizardEntity(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
    }
    @Override
    public void tick() {
        super.tick();

        if (this.tradingPlayer != null) {
            // Заставляем моба смотреть прямо на торгующего игрока
            this.getLookControl().setLookAt(this.tradingPlayer, 30.0F, 30.0F);
            // Останавливаем любые попытки пойти гулять
            this.getNavigation().stop();
        }
    }
    @Override
    protected void registerGoals() {
        // Увеличили скорость с 0.5D до 0.7D, чтобы он не ходил как черепаха
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            if (!this.level().isClientSide) {
                this.setTradingPlayer(player);

                // Используем ключ перевода вместо русского текста
                player.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, playerEntity) -> new WizardMenu(containerId, playerInventory),
                        Component.translatable("container.fisch.wizard")
                ));
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return InteractionResult.PASS;
    }
}