package com.fisch.rod;

import com.fisch.fish.NewFish;
import com.fisch.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.Random;

public class RodMechanics {

    private static final Random RANDOM = new Random();

    public static boolean isValidWaterBody(Level world, BlockPos pos) {
        int waterBlocks = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y >= -2; y--) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    if (world.getFluidState(checkPos).is(FluidTags.WATER)) {
                        waterBlocks++;
                    }
                }
            }
        }
        return waterBlocks >= 20;
    }

    public static String getBiomeGroup(Level world, BlockPos pos) {
        Holder<Biome> biomeHolder = world.getBiome(pos);

        if (biomeHolder.unwrapKey().isPresent()) {
            ResourceKey<Biome> key = biomeHolder.unwrapKey().get();
            String path = key.location().getPath().toLowerCase();

            if (path.contains("desert") || path.contains("badlands")) {
                return "desert";
            } else if (path.contains("ice") || path.contains("snow") || path.contains("frozen")
                    || path.contains("cold") || path.contains("slope") || path.contains("peaks")) {
                return "ice";
            } else if (path.contains("jungle") || path.contains("bamboo")) {
                return "jungle";
            }
        }
        return "plain";
    }

    // НОВЫЙ МЕТОД: Проверка на выпадение кейса
    // Вызывай это там, где выдаешь лут! Например: if(RodMechanics.rollCrateDrop(luck)) { дать кейс }
    public static boolean rollCrateDrop(float luck) {
        // Базовый шанс у плохой удочки (luck = 0) равен 5% (1 раз в 20 поклевок).
        // За каждую единицу удачи шанс растет на 3%.
        // Джунглевая удочка (luck = 5) получит шанс 5 + 15 = 20% (1 раз в 5 поклевок).
        float crateChance = 5.0f + (luck * 3.0f);

        // Ограничиваем максимальный шанс на 40%, чтобы даже под бафами кейсы не падали каждый раз
        crateChance = Math.min(crateChance, 40.0f);

        return (RANDOM.nextFloat() * 100.0f) <= crateChance;
    }

    public static NewFish determineCatch(Level world, BlockPos pos, NewFish[] bestiary, String bait, float luck) {
        if (bestiary == null || bestiary.length == 0) return null;

        if (!isValidWaterBody(world, pos)) {
            return ModItems.JUNK_FISH[RANDOM.nextInt(ModItems.JUNK_FISH.length)];
        }

        String currentBiomeGroup = getBiomeGroup(world, pos);
        int count = 0;

        for (NewFish fish : bestiary) {
            if (fish.getBiomeGroup().equals(currentBiomeGroup)) {
                count++;
            }
        }

        NewFish[] filteredBestiary;
        if (count > 0) {
            filteredBestiary = new NewFish[count];
            int index = 0;
            for (NewFish fish : bestiary) {
                if (fish.getBiomeGroup().equals(currentBiomeGroup)) {
                    filteredBestiary[index++] = fish;
                }
            }
        } else {
            filteredBestiary = bestiary;
        }

        float[] fishPercentages = new float[filteredBestiary.length];
        float totalSum = 0;

        for (int i = 0; i < filteredBestiary.length; i++) {
            float percentage = fishDropPercentage(filteredBestiary[i], world, bait, luck);
            fishPercentages[i] = percentage;
            totalSum += percentage;
        }

        if (totalSum <= 0) {
            return filteredBestiary[0];
        }

        float rolledValue = RANDOM.nextFloat() * totalSum;
        float currentSum = 0;

        for (int i = 0; i < filteredBestiary.length; i++) {
            currentSum += fishPercentages[i];
            if (rolledValue <= currentSum) {
                return filteredBestiary[i];
            }
        }

        return filteredBestiary[filteredBestiary.length - 1];
    }

    public static boolean checkWeather(Level world, String bestWeather) {
        String weather = world.isRaining() ? "raining" : (world.isThundering() ? "thundering" : "clear");
        return bestWeather.equals(weather);
    }

    public static boolean checkTime(Level world, String bestTime) {
        String time;
        long timeOfDay = world.getDayTime() % 24000;

        if (timeOfDay < 12000) {
            time = "day";
        } else if (timeOfDay < 13000) {
            time = "sunset";
        } else if (timeOfDay < 23000) {
            time = "night";
        } else {
            time = "sunrise";
        }

        return bestTime.equals(time);
    }

    public static float fishDropPercentage(NewFish fish, Level world, String bait, float luck) {
        float weight;

        // ЖЕСТКИЙ БАЛАНС: Разделяем рыбу по тирам.
        // 1 - Экзотика, 2 - Мифик, 3-4 - Редкие (Леги/Эпики), 5-10 - Обычные
        if (fish.rarity == 1) { // Экзотика
            // Худшая удочка (luck=0) -> вес 0.5 (шанс мизерный). Джунглевая (luck=5) -> вес ~5.5 (шанс ~5%)
            weight = 0.5f + (luck * 1.0f);
        } else if (fish.rarity == 2) { // Мифик
            // Худшая удочка -> вес 3. Джунглевая (luck=5) -> вес 18 (шанс ~20%)
            weight = 3.0f + (luck * 3.0f);
        } else if (fish.rarity <= 4) { // Редкие
            weight = 15.0f + (luck * 2.0f);
        } else { // Обычные рыбы (березовый, золотой и тд)
            // Их всегда много, но хорошая удочка чуть-чуть урезает их количество, чтобы освободить место редким
            weight = 80.0f - (luck * 4.0f);
        }

        // Защита, чтобы вес никогда не уходил в минус при багах
        weight = Math.max(0.1f, weight);

        if (checkWeather(world, fish.bestWeather)) weight *= 1.5f;
        if (checkTime(world, fish.bestTime)) weight *= 1.5f;
        if (bait.equals(fish.bestBait)) weight *= 2.0f;

        return weight;
    }

    public static boolean checkProgress(int x1, int x2, int fishX1, int fishX2) {
        return x1 <= fishX1 && x2 >= fishX2 || x1 == fishX2 && x2 == fishX1;
    }

    public static int getFishX(int maxMovement) {
        return RANDOM.nextInt(maxMovement - (-maxMovement) + 1) + (-maxMovement);
    }

    public static float getFishSpeedMultiplier(int rarity) {
        return switch (rarity) {
            case 10 -> 0.05f;
            case 8 -> 0.7f;
            case 7 -> 0.9f;
            case 6 -> 1.1f;
            case 5 -> 1.4f;
            case 4 -> 1.8f;
            case 3 -> 2.3f;
            case 2 -> 3.0f;
            case 1 -> 3.5f;
            default -> 1.0f;
        };
    }

    public static int getFishMovement(int rarity) {
        return switch (rarity) {
            case 10 -> 2;
            case 8 -> 8;
            case 7 -> 12;
            case 6 -> 16;
            case 5 -> 22;
            case 4 -> 30;
            case 3 -> 40;
            case 2 -> 55;
            case 1 -> 60;
            default -> 15;
        };
    }

    public static float getResilienceMultiplier(float resilience) {
        resilience = Math.max(0.01f, Math.min(1.0f, resilience));
        return 1.0f - resilience * 0.5f;
    }
}