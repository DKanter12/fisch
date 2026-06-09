package com.fisch.mixin;

import com.fisch.FishingHookDuck;
import com.fisch.RodMechanics;
import com.fisch.fish.NewFish;
import com.fisch.fish.ModFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin implements FishingHookDuck {

    @Unique
    private static final Logger fisch$LOGGER = LoggerFactory.getLogger("FischMod");

    @Shadow public abstract Player getPlayerOwner();
    @Shadow private int nibble; // Ванильный таймер поклёвки (сколько тиков рыба будет на крючке)

    // Наше кастомное поле для хранения определенной рыбы
    @Unique
    private NewFish fisch$customCatch = null;

    // Флаг, гарантирующий, что поклёвка началась и рыба все еще считается активной для подсечки
    @Unique
    private boolean fisch$isBiting = false;

    @Override
    public NewFish fisch$getCustomCatch() {
        return this.fisch$customCatch;
    }

    @Override
    public void fisch$setCustomCatch(NewFish fish) {
        this.fisch$customCatch = fish;
    }

    /**
     * Инъекция в метод tick(). Срабатывает в момент, когда сервер рассчитывает поклёвку.
     * Ванильный Майнкрафт устанавливает nibble > 0, когда рыба «кусает» крючок.
     */
    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/projectile/FishingHook;nibble:I",
                    shift = At.Shift.AFTER
            )
    )
    private void onBiteStart(CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;
        Level level = hook.level();

        if (!level.isClientSide()) {
            // Если игра установила таймер поклёвки (> 0) и мы еще не перешли в режим поклёвки
            if (this.nibble > 0 && !this.fisch$isBiting) {
                Player player = this.getPlayerOwner();
                if (player != null) {
                    this.fisch$isBiting = true;
                    fisch$LOGGER.info("[Fisch] Зафиксирован старт поклёвки! nibble={}", this.nibble);

                    // 1. Получаем наживку из NBT удочки в руке игрока
                    String bait = getBaitFromPlayer(player);

                    // 2. Получаем ваш список рыб (бестиарий) напрямую из класса ModFish
                    NewFish[] bestiary = getActiveBestiary();

                    // 3. Вызываем ваш кастомный метод для генерации улова
                    this.fisch$customCatch = RodMechanics.determineCatch(level, bestiary, bait);

                    if (this.fisch$customCatch != null) {
                        fisch$LOGGER.info("[Fisch] Рыба успешно выбрана в tick(): {}", this.fisch$customCatch.name);
                    } else {
                        fisch$LOGGER.warn("[Fisch] Не удалось выбрать рыбу в tick() (метод вернул null)! Проверьте наполнение бестиария.");
                    }
                }
            }

            // Если поклёвка упущена естественным образом (время вышло, а игрок не нажал ПКМ)
            if (this.nibble <= 0 && this.fisch$isBiting) {
                if (this.fisch$customCatch != null) {
                    fisch$LOGGER.info("[Fisch] Игрок упустил рыбу {}", this.fisch$customCatch.name);
                }
                this.fisch$isBiting = false;
                this.fisch$customCatch = null;
            }
        }
    }

    /**
     * Перехват вылавливания рыбы (нажатие ПКМ игроком).
     * Мы перехватываем HEAD метода retrieve.
     */
    @Inject(method = "retrieve", at = @At("HEAD"), cancellable = true)
    private void onHookRetrieve(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        FishingHook hook = (FishingHook) (Object) this;
        Level level = hook.level();

        if (!level.isClientSide()) {
            Player player = this.getPlayerOwner();
            fisch$LOGGER.info("[Fisch] Игрок пытается смотать удочку. Состояние: fisch$isBiting={}, nibble={}", this.fisch$isBiting, this.nibble);

            if (player != null) {
                // ПРОВЕРКА: Была ли зафиксирована поклёвка
                if (this.fisch$isBiting) {

                    // Если вдруг по какой-то причине объект рыбы не сгенерировался ранее, делаем экстренную генерацию
                    if (this.fisch$customCatch == null) {
                        fisch$LOGGER.warn("[Fisch] Обнаружена поклёвка без улова в retrieve. Запуск экстренной генерации.");
                        String bait = getBaitFromPlayer(player);
                        NewFish[] bestiary = getActiveBestiary();
                        this.fisch$customCatch = RodMechanics.determineCatch(level, bestiary, bait);
                    }

                    if (this.fisch$customCatch != null) {
                        // Выдаем игроку кастомный улов
                        giveCustomFishToPlayer(player, this.fisch$customCatch);

                        // Сбрасываем состояния
                        this.fisch$isBiting = false;
                        this.fisch$customCatch = null;

                        // Заставляем поплавок исчезнуть
                        hook.discard();

                        // Возвращаем 1 (урон удочке) и полностью отменяем ванильный retrieve
                        cir.setReturnValue(1);
                        return;
                    } else {
                        fisch$LOGGER.error("[Fisch] Ошибка: улов равен null даже после экстренной генерации. Возвращаем стандартное поведение.");
                    }
                } else {
                    fisch$LOGGER.info("[Fisch] Удочка смотана без улова (поклёвки не было).");
                }
            }
        }
    }

    @Unique
    private String getBaitFromPlayer(Player player) {
        ItemStack rod = player.getMainHandItem();
        if (rod.getItem() instanceof net.minecraft.world.item.FishingRodItem) {
            if (rod.hasTag() && rod.getTag().contains("Bait")) {
                return rod.getTag().getString("Bait");
            }
        }
        return "none";
    }

    @Unique
    private NewFish[] getActiveBestiary() {
        // Теперь здесь возвращается ваш реальный список всех рыб из класса ModFish
        return ModFish.ALL_FISH;
    }

    @Unique
    private void giveCustomFishToPlayer(Player player, NewFish fish) {
        // Используем стандартный SLF4J LOGGER для вывода информации в консоль сервера
        fisch$LOGGER.info("Игрок " + player.getName().getString() + " выловил кастомную рыбу: " + fish.name);

        // Тут вы можете выдать предмет игроку, например:
        // ItemStack stack = new ItemStack(YourItems.CUSTOM_FISH_ITEM);
        // player.getInventory().add(stack);
    }
}