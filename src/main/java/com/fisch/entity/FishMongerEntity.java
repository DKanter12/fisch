package com.fisch.entity;

import com.fisch.item.ModItems;
import com.fisch.menu.FishMongerMenu;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;

public class FishMongerEntity extends Villager {

    public FishMongerEntity(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
        this.setVillagerData(this.getVillagerData().setProfession(VillagerProfession.FISHERMAN));
    }

    // Без аннотации @Override, если ругается
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zombie.class, 8.0F, 1.2D, 1.35D));
        // Используем правильный класс задачи:
        this.goalSelector.addGoal(2, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    // Остальной код (mobInteract и getRodForBiome) оставляем без изменений
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            Holder<Biome> biome = this.level().getBiome(this.blockPosition());
            Item rodToSell = getRodForBiome(biome);

            if (rodToSell == null) {
                serverPlayer.sendSystemMessage(Component.literal("§c[Продавец] Тут для тебя ничего нет..."));
                return InteractionResult.SUCCESS;
            }

            this.setTradingPlayer(player);
            final Item finalRod = rodToSell;

            serverPlayer.openMenu(new ExtendedScreenHandlerFactory() {
                public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                    buf.writeInt(BuiltInRegistries.ITEM.getId(finalRod));
                    buf.writeInt(FishMongerEntity.this.getId());
                }
                public Component getDisplayName() { return Component.literal("Магазин"); }
                public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                    return new FishMongerMenu(syncId, inv, finalRod, FishMongerEntity.this);
                }
            });
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private Item getRodForBiome(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_JUNGLE)) return ModItems.JUNGLE_ROD;
        if (biome.is(BiomeTags.IS_BADLANDS) || biome.is(BiomeTags.IS_SAVANNA) || biome.is(BiomeTags.HAS_DESERT_PYRAMID)) return ModItems.SAND_ROD;
        if (biome.is(BiomeTags.IS_TAIGA) || biome.is(BiomeTags.IS_MOUNTAIN) || biome.value().getBaseTemperature() < 0.15f) return ModItems.ICE_ROD;
        return null;
    }
}