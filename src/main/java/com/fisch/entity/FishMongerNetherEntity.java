package com.fisch.entity;

import com.fisch.item.ModItems;
import com.fisch.menu.FishMongerMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FishMongerNetherEntity extends PathfinderMob {
    private Player tradingPlayer;

    public FishMongerNetherEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    // --- ДОБАВЛЯЕМ ПОВЕДЕНИЕ (ИИ) ---
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this)); // Плавает, если упадет в воду/лаву
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0D)); // Гуляет по местности
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F)); // Смотрит на игрока
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this)); // Просто крутит головой
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D); // Скорость как у обычного жителя
    }

    // --- ДОБАВЛЯЕМ ЗВУКИ ЖИТЕЛЯ ---
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.VILLAGER_AMBIENT; }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) { return SoundEvents.VILLAGER_HURT; }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.VILLAGER_DEATH; }

    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {

            // ПРОВЕРКА: Измерение Незер И высота ниже бедрока (127)
            if (this.level().dimension().equals(Level.NETHER) && this.getY() < 127) {
                this.setTradingPlayer(player);
                Item rodToSell = ModItems.HELL_ROD;

                // Звук успешного открытия торгов
                this.playSound(SoundEvents.VILLAGER_TRADE, 1.0F, 1.0F);

                player.openMenu(new ExtendedScreenHandlerFactory() {
                    @Override
                    public void writeScreenOpeningData(ServerPlayer serverPlayer, FriendlyByteBuf buf) {
                        buf.writeInt(BuiltInRegistries.ITEM.getId(rodToSell));
                    }

                    @Override
                    public Component getDisplayName() {
                        return Component.translatable("container.fisch.fish_monger");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                        return new FishMongerMenu(syncId, playerInventory, rodToSell, FishMongerNetherEntity.this);
                    }
                });
                return InteractionResult.SUCCESS;
            } else {
                // Звук отказа
                this.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                player.displayClientMessage(Component.translatable("Житель отказывается торговать вне своей среды!").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
        }
        return super.mobInteract(player, hand);
    }
}