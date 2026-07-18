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
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
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

        if (this.getNavigation() instanceof GroundPathNavigation navigation) {
            navigation.setCanOpenDoors(false);
            navigation.setCanPassDoors(false);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Обрабатываем клик только один раз (для главной руки), чтобы не было двойного срабатывания
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (this.level().isClientSide()) {
            return InteractionResult.sidedSuccess(true);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            Holder<Biome> biome = this.level().getBiome(this.blockPosition());
            Item rodToSell = getRodForBiome(biome);

            this.setTradingPlayer(player);
            final Item finalRod = rodToSell;

            System.out.println("[FischMod] Попытка открыть меню продавца удочек...");

            try {
                serverPlayer.openMenu(new ExtendedScreenHandlerFactory() {
                    @Override
                    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                        buf.writeInt(BuiltInRegistries.ITEM.getId(finalRod));
                        buf.writeInt(FishMongerEntity.this.getId());
                    }

                    @Override
                    public Component getDisplayName() {
                        return Component.literal("Магазин Удочек");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                        return new FishMongerMenu(syncId, inv, finalRod, FishMongerEntity.this);
                    }
                });
                System.out.println("[FischMod] Меню успешно отправлено игроку!");
            } catch (Exception e) {
                System.out.println("[FischMod] КРИТИЧЕСКАЯ ОШИБКА ОТКРЫТИЯ МЕНЮ:");
                e.printStackTrace();
            }

            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    private Item getRodForBiome(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_JUNGLE)) return ModItems.JUNGLE_ROD;
        if (biome.is(BiomeTags.IS_BADLANDS) || biome.is(BiomeTags.IS_SAVANNA) || biome.is(BiomeTags.HAS_DESERT_PYRAMID)) return ModItems.SAND_ROD;
        if (biome.is(BiomeTags.IS_TAIGA) || biome.is(BiomeTags.IS_MOUNTAIN) || biome.value().getBaseTemperature() < 0.15f) return ModItems.ICE_ROD;

        // ВМЕСТО NULL ТЕПЕРЬ ВЫДАЕТСЯ ОБЫЧНАЯ УДОЧКА, ЧТОБЫ МЕНЮ ОТКРЫЛОСЬ В ЛЮБОМ БИОМЕ!
        return net.minecraft.world.item.Items.FISHING_ROD;
    }
}