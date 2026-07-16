package com.fisch;

import com.fisch.command.ModCommands;
import com.fisch.events.ModEvents;
import com.fisch.item.ModItems;
import com.fisch.menu.FishMerchantMenu;
import com.fisch.networking.ModPackets;
import com.fisch.util.CurrencyHolder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FischMod implements ModInitializer {

    public static final String MODID = "fisch";

    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final ResourceLocation FISH_GUI_PACKET_ID =
            new ResourceLocation(MODID, "open_fish_gui");

    public static final ResourceLocation FINISH_MINIGAME_PACKET_ID =
            new ResourceLocation(MODID, "finish_minigame");

    @Override
    public void onInitialize() {
        ModCommands.register();
        ModItems.register();
        ModEvents.register();

        try {
            Class.forName("com.fisch.registry.ModMenuTypes");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        ModPackets.registerServerPackets();

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (entity instanceof Villager villager && villager.getVillagerData().getProfession() == VillagerProfession.FISHERMAN) {
                if (!level.isClientSide) {

                    // 1. МАГИЯ ЗДЕСЬ: Говорим жителю, что с ним торгуют.
                    // Его ИИ сразу остановит его и заставит смотреть на игрока.
                    villager.setTradingPlayer(player);

                    SimpleContainer tempMerchantInventory = new SimpleContainer(27);
                    player.openMenu(new SimpleMenuProvider(
                            // 2. Обязательно передаём самого жителя в меню, чтобы знать, кого потом отпускать
                            (syncId, playerInv, p) -> new FishMerchantMenu(syncId, playerInv, tempMerchantInventory, villager),
                            // Замени Component.literal("Рыботорговец") на:
                            Component.translatable("container.fisch.fish_merchant")
                    ));
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ModPackets.syncMoney(handler.getPlayer());
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            long currentMoney = ((CurrencyHolder) oldPlayer).getMoney();
            ((CurrencyHolder) newPlayer).setMoney(currentMoney);
            ModPackets.syncMoney(newPlayer);
        });
    
        ServerPlayNetworking.registerGlobalReceiver(
                FINISH_MINIGAME_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {

                    boolean success = buf.readBoolean();

                    server.execute(() -> {

                        if (player.fishing instanceof FishingHookDuck duck) {
                            duck.finishMiniGame(success);
                        }

                    });
                }
        );

        LOGGER.info("Hello Fabric world!");
    }
}