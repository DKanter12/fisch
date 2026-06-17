package com.fisch.client;

import com.fisch.FischMod;
import com.fisch.client.screen.FishCatchScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class FischModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(FischMod.FISH_GUI_PACKET_ID, (client, handler, buf, responseSender) -> {
			String fishName = buf.readUtf();
            int fishRarity = buf.readInt();
			float control = buf.readFloat();
			client.execute(() -> {
				Minecraft.getInstance().setScreen(new FishCatchScreen(fishName, fishRarity, control));
			});
		});
	}
}