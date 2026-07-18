package com.fisch;

import com.fisch.command.ModCommands;
import com.fisch.entity.FishMongerEntity;
import com.fisch.entity.ModEntities;
import com.fisch.events.ModEvents;
import com.fisch.item.ModCreativeTabs;
import com.fisch.item.ModItems;
import com.fisch.menu.FishMerchantMenu;
import com.fisch.networking.ModPackets;
import com.fisch.screen.ModScreenHandlers;
import com.fisch.util.CurrencyHolder;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FischMod implements ModInitializer {

    public static final String MODID = "fisch";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final ResourceLocation FISH_GUI_PACKET_ID = new ResourceLocation(MODID, "open_fish_gui");
    public static final ResourceLocation FINISH_MINIGAME_PACKET_ID = new ResourceLocation(MODID, "finish_minigame");

    @Override
    public void onInitialize() {
        ModCommands.register();
        ModItems.register();
        ModCreativeTabs.register();
        ModEvents.register();
        ModScreenHandlers.register();
        ModPackets.register();
        ModEntities.registerModEntities();

        /*
         * ========================================================
         * ENTITY ATTRIBUTES
         * ========================================================
         */
        FabricDefaultAttributeRegistry.register(
                ModEntities.FISH_MONGER,
                FishMongerEntity.createAttributes()
        );

        /*
         * ========================================================
         * OPEN FISH MERCHANT MENU
         * ========================================================
         */
        UseEntityCallback.EVENT.register(
                (player, level, hand, entity, hitResult) -> {

                    // Пропускаем кастомного продавца удочек, чтобы он сам открыл свое меню
                    if (entity instanceof FishMongerEntity) {
                        return InteractionResult.PASS;
                    }

                    // Обычный скупщик рыбы
                    if (entity instanceof Villager villager && villager.getVillagerData().getProfession() == VillagerProfession.FISHERMAN) {
                        if (!level.isClientSide) {
                            villager.setTradingPlayer(player);
                            SimpleContainer merchantInventory = new SimpleContainer(27);

                            player.openMenu(
                                    new SimpleMenuProvider(
                                            (syncId, playerInventory, menuPlayer) ->
                                                    new FishMerchantMenu(syncId, playerInventory, merchantInventory, villager),
                                            Component.translatable("container.fisch.fish_merchant")
                                    )
                            );
                        }
                        return InteractionResult.SUCCESS;
                    }

                    return InteractionResult.PASS;
                }
        );

        /*
         * ========================================================
         * SYNC MONEY & GIVE GUIDE BOOK WHEN PLAYER JOINS
         * ========================================================
         */
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> {
                    ServerPlayer player = handler.getPlayer();
                    ModPackets.syncMoney(player);

                    // Выдача книги ПРИ ПЕРВОМ ЗАХОДЕ
                    if (!player.getTags().contains("fisch.given_guide")) {
                        ItemStack guideBook = new ItemStack(ModItems.FISCH_GUIDE_BOOK);

                        // Если рука пустая - даем прямо в нее
                        if (player.getMainHandItem().isEmpty()) {
                            player.setItemInHand(InteractionHand.MAIN_HAND, guideBook);
                        } else {
                            // Иначе просто кладем в инвентарь
                            player.getInventory().add(guideBook);
                        }

                        // Сохраняем тег, чтобы книга не выдавалась каждый раз при входе
                        player.addTag("fisch.given_guide");
                    }
                }
        );

        /*
         * ========================================================
         * COPY MONEY AFTER RESPAWN / DIMENSION CHANGE
         * ========================================================
         */
        ServerPlayerEvents.COPY_FROM.register(
                (oldPlayer, newPlayer, alive) -> {
                    long currentMoney = ((CurrencyHolder) oldPlayer).getMoney();
                    ((CurrencyHolder) newPlayer).setMoney(currentMoney);
                    ModPackets.syncMoney(newPlayer);
                }
        );

        /*
         * ========================================================
         * FINISH FISHING MINIGAME
         * ========================================================
         */
        ServerPlayNetworking.registerGlobalReceiver(
                FINISH_MINIGAME_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    boolean success = buf.readBoolean();

                    server.execute(() -> {
                        if (player.fishing instanceof com.fisch.FishingHookDuck duck) {
                            duck.finishMiniGame(success);
                        }
                    });
                }
        );

        LOGGER.info("FischMod initialized successfully!");
    }
}