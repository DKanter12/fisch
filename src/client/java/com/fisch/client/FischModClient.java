package com.fisch.client;

import com.fisch.FischMod;
import com.fisch.client.gui.FishCatchScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class FischModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(FischMod.FISH_GUI_PACKET_ID, (client, handler, buf, responseSender) -> {
			String fishName = buf.readUtf();
			client.execute(() -> {
				Minecraft.getInstance().setScreen(new FishCatchScreen(fishName));
			});
		});
	}
}