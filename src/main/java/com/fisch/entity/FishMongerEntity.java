package com.fisch.entity;

import com.fisch.item.ModItems;
import com.fisch.menu.FishMongerMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

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
        ItemStack itemStack = player.getItemInHand(hand);

        // Разрешаем переименовывать жителя биркой или использовать яйцо призыва
        if (itemStack.is(net.minecraft.world.item.Items.NAME_TAG) || itemStack.getItem() instanceof net.minecraft.world.item.SpawnEggItem) {
            return super.mobInteract(player, hand);
        }

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (this.level().isClientSide()) {
            return InteractionResult.sidedSuccess(true);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            Holder<Biome> biome = this.level().getBiome(this.blockPosition());
            Item rodToSell = getRodForBiome(biome);

            // ЕСЛИ УДОЧКА НЕ НАЙДЕНА — ПИШЕМ СООБЩЕНИЕ И БЛОКИРУЕМ ОТКРЫТИЕ МЕНЮ
            if (rodToSell == null) {
                serverPlayer.sendSystemMessage(Component.translatable("message.fisch.monger.no_items"));
                return InteractionResult.SUCCESS;
            }

            this.setTradingPlayer(player);
            final Item finalRod = rodToSell;

            try {
                serverPlayer.openMenu(new ExtendedScreenHandlerFactory() {
                    @Override
                    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                        buf.writeInt(BuiltInRegistries.ITEM.getId(finalRod));
                        buf.writeInt(FishMongerEntity.this.getId());
                    }

                    @Override
                    public Component getDisplayName() {
                        return Component.translatable("gui.fisch.shop_title");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                        return new FishMongerMenu(syncId, inv, finalRod, FishMongerEntity.this);
                    }
                });
            } catch (Exception e) {
                System.out.println("[FischMod] КРИТИЧЕСКАЯ ОШИБКА ОТКРЫТИЯ МЕНЮ:");
                e.printStackTrace();
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    private Item getRodForBiome(Holder<Biome> biome) {
        if (!biome.unwrapKey().isPresent()) {
            return null;
        }

        ResourceKey<Biome> biomeKey = biome.unwrapKey().get();
        String path = biomeKey.location().getPath();

        // 1. ПЕСЧАНАЯ УДОЧКА: Пустыня, Саванна и Badlands
        if (biome.is(Biomes.DESERT) || biome.is(BiomeTags.IS_SAVANNA) || biome.is(BiomeTags.IS_BADLANDS)
                || path.contains("desert") || path.contains("savanna") || path.contains("badlands")) {
            return ModItems.SAND_ROD;
        }

        // 2. ДЖУНГЛЕВАЯ УДОЧКА: Все виды джунглей
        if (biome.is(BiomeTags.IS_JUNGLE) || path.contains("jungle")) {
            return ModItems.JUNGLE_ROD;
        }

        // 3. БОЛОТНАЯ УДОЧКА: Болото и мангровые заросли
        if (biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP) || path.contains("swamp") || path.contains("mangrove")) {
            return ModItems.SWAMP_ROD;
        }

        // 4. ГРИБНАЯ УДОЧКА: Грибные поля
        if (biome.is(Biomes.MUSHROOM_FIELDS) || path.contains("mushroom")) {
            return ModItems.MUSHROOM_ROD;
        }

        // 5. ЛЕДЯНАЯ УДОЧКА: Тайга, горы, замерзшие океаны, снежные биомы
        if (biome.is(BiomeTags.IS_TAIGA) || biome.is(Biomes.SNOWY_PLAINS) || biome.is(Biomes.ICE_SPIKES)
                || biome.is(Biomes.FROZEN_OCEAN) || biome.is(Biomes.SNOWY_TAIGA) || biome.is(Biomes.SNOWY_SLOPES)
                || path.contains("taiga") || path.contains("frozen") || path.contains("snow") || path.contains("ice")) {
            return ModItems.ICE_ROD;
        }

        // Если ни один биом не подошел — возвращаем null
        return null;
    }
}