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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FischMod implements ModInitializer {

    public static final String MODID = "fisch";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MODID);


    /*
     * ============================================================
     * NETWORK PACKETS
     * ============================================================
     */

    public static final ResourceLocation FISH_GUI_PACKET_ID =
            new ResourceLocation(MODID, "open_fish_gui");

    public static final ResourceLocation FINISH_MINIGAME_PACKET_ID =
            new ResourceLocation(MODID, "finish_minigame");


    @Override
    public void onInitialize() {

        /*
         * ========================================================
         * BASIC REGISTRATION
         * ========================================================
         */

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
         * MENUS
         * ========================================================
         */

        // Если у тебя есть отдельный класс ModMenuTypes
        // и он действительно нужен — регистрируй его только один раз.
        //
        // ModMenuTypes.registerMenus();


        /*
         * ========================================================
         * OPEN FISH MERCHANT MENU
         * ========================================================
         */

        UseEntityCallback.EVENT.register(
                (player, level, hand, entity, hitResult) -> {

                    /*
                     * Проверяем, что игрок взаимодействует
                     * именно с рыбаком
                     */
                    if (
                            entity instanceof Villager villager
                                    &&
                                    villager.getVillagerData().getProfession()
                                            == VillagerProfession.FISHERMAN
                    ) {

                        /*
                         * На клиенте ничего не открываем.
                         * Меню открывается только на сервере.
                         */
                        if (!level.isClientSide) {

                            /*
                             * Запоминаем игрока, который торгует
                             * с жителем
                             */
                            villager.setTradingPlayer(player);


                            /*
                             * Временный контейнер торговца
                             */
                            SimpleContainer merchantInventory =
                                    new SimpleContainer(27);


                            /*
                             * Открываем меню
                             */
                            player.openMenu(
                                    new SimpleMenuProvider(

                                            /*
                                             * Создание меню
                                             */
                                            (syncId, playerInventory, menuPlayer) ->
                                                    new FishMerchantMenu(
                                                            syncId,
                                                            playerInventory,
                                                            merchantInventory,
                                                            villager
                                                    ),

                                            /*
                                             * Заголовок меню
                                             */
                                            Component.translatable(
                                                    "container.fisch.fish_merchant"
                                            )
                                    )
                            );
                        }


                        /*
                         * Говорим Minecraft, что взаимодействие
                         * обработано нашим модом
                         */
                        return InteractionResult.SUCCESS;
                    }


                    return InteractionResult.PASS;
                }
        );


        /*
         * ========================================================
         * SYNC MONEY WHEN PLAYER JOINS
         * ========================================================
         */

        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> {

                    ServerPlayer player =
                            handler.getPlayer();

                    ModPackets.syncMoney(player);
                }
        );


        /*
         * ========================================================
         * COPY MONEY AFTER RESPAWN / DIMENSION CHANGE
         * ========================================================
         */

        ServerPlayerEvents.COPY_FROM.register(
                (oldPlayer, newPlayer, alive) -> {

                    long currentMoney =
                            ((CurrencyHolder) oldPlayer).getMoney();


                    ((CurrencyHolder) newPlayer)
                            .setMoney(currentMoney);


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

                    boolean success =
                            buf.readBoolean();


                    server.execute(() -> {

                        if (
                                player.fishing
                                        instanceof com.fisch.FishingHookDuck duck
                        ) {

                            duck.finishMiniGame(success);
                        }
                    });
                }
        );


        /*
         * ========================================================
         * LOG
         * ========================================================
         */

        LOGGER.info(
                "FischMod initialized successfully!"
        );
    }
}