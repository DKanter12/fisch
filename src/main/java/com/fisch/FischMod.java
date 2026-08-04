package com.fisch;

import com.fisch.command.ModCommands;
import com.fisch.entity.FishMongerEntity;
import com.fisch.entity.FishermanWizardEntity;
import com.fisch.entity.ModEntities;
import com.fisch.events.ModEvents;
import com.fisch.item.ModCreativeTabs;
import com.fisch.item.ModItems;
import com.fisch.menu.FishMerchantMenu;
import com.fisch.menu.FishMongerMenu;
import com.fisch.networking.ModPackets;
import com.fisch.screen.ModScreenHandlers;
import com.fisch.util.CurrencyHolder;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FischMod implements ModInitializer {

    public static final String MODID = "fisch";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final ResourceLocation FISH_GUI_PACKET_ID = new ResourceLocation(MODID, "open_fish_gui");
    public static final ResourceLocation FINISH_MINIGAME_PACKET_ID = new ResourceLocation(MODID, "finish_minigame");
    public static final ResourceLocation ENCHANT_ROD_PACKET_ID = new ResourceLocation(MODID, "enchant_rod");

    @Override
    public void onInitialize() {
        ModCommands.register();
        ModItems.register();
        com.fisch.block.ModBlocks.register();
        com.fisch.block.entity.ModBlockEntities.register();
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

        FabricDefaultAttributeRegistry.register(
                ModEntities.FISHERMAN_WIZARD,
                FishermanWizardEntity.createAttributes()
        );

        /*
         * ========================================================
         * OPEN MENUS ON ENTITY CLICK (ВКЛЮЧАЯ БИОМНУЮ ЛОГИКУ!)
         * ========================================================
         */
        UseEntityCallback.EVENT.register(
                (player, level, hand, entity, hitResult) -> {
                    // ПРОДАВЕЦ УДОЧЕК (FISH MONGER)
                    if (entity instanceof FishMongerEntity monger) {
                        if (!level.isClientSide) {
                            // 1. Получаем биом в реальном времени под ногами жителя
                            Holder<Biome> biomeHolder = level.getBiome(monger.blockPosition());
                            Item rodToSell = ModItems.ICE_ROD; // Удочка по умолчанию

                            // 2. Проверяем биом и назначаем нужную удочку
                            if (biomeHolder.is(Biomes.SWAMP) || biomeHolder.is(Biomes.MANGROVE_SWAMP)) {
                                rodToSell = ModItems.SWAMP_ROD;
                            } else if (biomeHolder.is(Biomes.MUSHROOM_FIELDS)) {
                                rodToSell = ModItems.MUSHROOM_ROD;
                            } else if (biomeHolder.is(BiomeTags.IS_JUNGLE)) {
                                rodToSell = ModItems.JUNGLE_ROD;
                            } else if (biomeHolder.is(Biomes.DESERT) || biomeHolder.is(BiomeTags.HAS_DESERT_PYRAMID)) {
                                rodToSell = ModItems.SAND_ROD;
                            }

                            // 3. Открываем меню и передаем туда нашу удочку
                            monger.setTradingPlayer(player);
                            Item finalRod = rodToSell;

                            player.openMenu(new ExtendedScreenHandlerFactory() {
                                @Override
                                public void writeScreenOpeningData(ServerPlayer serverPlayer, FriendlyByteBuf buf) {
                                    buf.writeInt(BuiltInRegistries.ITEM.getId(finalRod));
                                }

                                @Override
                                public Component getDisplayName() {
                                    return Component.translatable("container.fisch.fish_monger");
                                }

                                @Override
                                public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                                    return new FishMongerMenu(syncId, playerInventory, finalRod, monger);
                                }
                            });
                        }
                        return InteractionResult.SUCCESS;
                    }

                    if (entity instanceof FishermanWizardEntity) {
                        return InteractionResult.PASS;
                    }

                    // СКУПЩИК РЫБЫ
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

                    if (!player.getTags().contains("fisch.given_guide")) {
                        ItemStack guideBook = new ItemStack(ModItems.FISCH_GUIDE_BOOK);

                        if (player.getMainHandItem().isEmpty()) {
                            player.setItemInHand(InteractionHand.MAIN_HAND, guideBook);
                        } else {
                            if (!player.getInventory().add(guideBook)) {
                                player.drop(guideBook, false);
                            }
                        }

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
         * ПОКУПКА УДОЧКИ (ИСПРАВЛЕНО!)
         * ========================================================
         */
        ServerPlayNetworking.registerGlobalReceiver(new ResourceLocation(FischMod.MODID, "buy_rod"), (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                // Теперь мы не хардкодим выдачу ICE_ROD за 50 монет!
                // Мы берем открытое меню игрока и вызываем наш умный метод buyRod()
                if (player.containerMenu instanceof FishMongerMenu mongerMenu) {
                    mongerMenu.buyRod(player);
                }
            });
        });

        /*
         * ========================================================
         * ENCHANT ROD
         * ========================================================
         */
        ServerPlayNetworking.registerGlobalReceiver(
                ENCHANT_ROD_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    server.execute(() -> {
                        if (player.containerMenu instanceof com.fisch.screen.EnchantmentAltarScreenHandler altar) {
                            altar.enchantRod(player);
                        }
                    });
                }
        );

        LOGGER.info("FischMod initialized successfully!");
    }
}