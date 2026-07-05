package com.fisch;

import com.fisch.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
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

        ModItems.register();

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