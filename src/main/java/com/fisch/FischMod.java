package com.fisch;

import com.fisch.entity.FishMongerEntity;
import com.fisch.entity.ModEntities;
import com.fisch.events.ModEvents;
import com.fisch.item.ModItems;
import com.fisch.menu.FishMerchantMenu;
import com.fisch.networking.ModPackets;
import com.fisch.registry.ModMenuTypes;
import com.fisch.util.CurrencyHolder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FischMod implements ModInitializer {
    public static final String MODID = "fisch";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final ResourceLocation FISH_GUI_PACKET_ID = new ResourceLocation(MODID, "open_fish_gui");
    public static final ResourceLocation FINISH_MINIGAME_PACKET_ID = new ResourceLocation(MODID, "finish_minigame");

    @Override
    public void onInitialize() {
        ModMenuTypes.registerMenus();
        FabricDefaultAttributeRegistry.register(ModEntities.FISH_MONGER, FishMongerEntity.createAttributes());
        ModEntities.registerModEntities();
        ModItems.register();
        ModEvents.register();

        try { Class.forName("com.fisch.registry.ModMenuTypes"); } catch (ClassNotFoundException e) { e.printStackTrace(); }

        ModPackets.registerServerPackets();

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (entity instanceof Villager villager && !(entity instanceof FishMongerEntity) && villager.getVillagerData().getProfession() == VillagerProfession.FISHERMAN) {
                if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                    SimpleContainer tempMerchantInventory = new SimpleContainer(27);

                    // ОСТАНАВЛИВАЕМ ЖИТЕЛЯ ПРИ КЛИКЕ
                    villager.setTradingPlayer(player);

                    serverPlayer.openMenu(new ExtendedScreenHandlerFactory() {
                        @Override
                        public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {}

                        @Override
                        public Component getDisplayName() {
                            return Component.literal("Продажа рыбы");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int syncId, Inventory playerInv, Player p) {
                            return new FishMerchantMenu(syncId, playerInv, tempMerchantInventory, villager);
                        }
                    });
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> { ModPackets.syncMoney(handler.getPlayer()); });
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            long currentMoney = ((CurrencyHolder) oldPlayer).getMoney();
            ((CurrencyHolder) newPlayer).setMoney(currentMoney);
            ModPackets.syncMoney(newPlayer);
        });

        LOGGER.info("Hello Fabric world!");
    }
}