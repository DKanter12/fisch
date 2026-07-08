package com.fisch.client;

import com.fisch.FischMod;
import com.fisch.client.screen.FishCatchScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import static com.fisch.FischMod.MODID;

public class FischModClient implements ClientModInitializer {

    public static final ResourceLocation FINISH_MINIGAME_PACKET =
            new ResourceLocation(MODID, "finish_minigame");
    @Override
    public void onInitializeClient() {

        ClientPlayNetworking.registerGlobalReceiver(
                FischMod.FISH_GUI_PACKET_ID,
                (client, handler, buf, responseSender) -> {

                    String fishName = buf.readUtf();
                    int fishRarity = buf.readInt();
                    float control = buf.readFloat();
                    float resilience = buf.readFloat();
                    int rarity = buf.readInt();

                    client.execute(() ->
                            Minecraft.getInstance().setScreen(
                                    new FishCatchScreen(
                                            fishName,
                                            fishRarity,
                                            control,
                                            resilience
                                    )
                            )
                    );
                }
        );
    }
}