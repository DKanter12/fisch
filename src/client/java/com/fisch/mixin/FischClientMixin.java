package com.fisch.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class FischClientMixin {

	// Пример: этот код сработает, когда клиент закончит инициализацию
	@Inject(at = @At("TAIL"), method = "run")
	private void onClientRun(CallbackInfo info) {
		// Здесь можно вывести что-то в консоль для теста
		// System.out.println("Клиент Fisch запущен!");
	}
}