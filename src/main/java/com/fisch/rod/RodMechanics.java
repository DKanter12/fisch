package com.fisch.rod;

import com.fisch.fish.NewFish;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import java.util.Random;

public class RodMechanics {

    private static final Random RANDOM = new Random();

    // Проверка размера водоема: ищем минимум 20 блоков воды в зоне 3x3 и 3 блока в глубину
    public static boolean isValidWaterBody(Level world, BlockPos pos) {
        int waterBlocks = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y >= -2; y--) { // Проверяем на 3 блока вниз
                    BlockPos checkPos = pos.offset(x, y, z);
                    if (world.getFluidState(checkPos).is(FluidTags.WATER)) {
                        waterBlocks++;
                    }
                }
            }
        }
        return waterBlocks >= 20; // Если меньше 20 блоков воды из 27 возможных, это "лужа"
    }

    public static String getBiomeGroup(Level world, BlockPos pos) {
        Holder<Biome> biomeHolder = world.getBiome(pos);
        if (biomeHolder.unwrapKey().isPresent()) {
            ResourceKey<Biome> key = biomeHolder.unwrapKey().get();
            String path = key.location().getPath().toLowerCase();
            if (path.contains("desert") || path.contains("badlands")) {
                return "desert";
            } else if (path.contains("ice") || path.contains("snow") || path.contains("frozen") || path.contains("cold") || path.contains("slope") || path.contains("peaks")) {
                return "ice";
            } else if (path.contains("jungle") || path.contains("bamboo")) {
                return "jungle";
            }
        }
        return "plain";
    }

    public static NewFish determineCatch(Level world, BlockPos pos, NewFish[] bestiary, String bait, float luck) {
        if (bestiary == null || bestiary.length == 0) return null;

        String currentBiomeGroup = getBiomeGroup(world, pos);

        // Если водоем слишком мелкий (самодельный), принудительно меняем улов на мусор (с шансом 85%)
        if (!isValidWaterBody(world, pos)) {
            if (RANDOM.nextFloat() < 0.85f) {
                currentBiomeGroup = "junk";
            }
        }

        int count = 0;
        for (NewFish fish : bestiary) {
            if (fish.getBiomeGroup().equals(currentBiomeGroup)) count++;
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
        float initialPercentage = 100.0f / filteredBestiary.length;

        for (int i = 0; i < filteredBestiary.length; i++) {
            float percentage = fishDropPercentage(initialPercentage, filteredBestiary[i], world, bait, luck);
            fishPercentages[i] = percentage;
            totalSum += percentage;
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
        if (timeOfDay < 12000) time = "day";
        else if (timeOfDay < 13000) time = "sunset";
        else if (timeOfDay < 23000) time = "night";
        else time = "sunrise";
        return bestTime.equals(time);
    }

    public static float fishDropPercentage(float initialPercentage, NewFish fish, Level world, String bait, float luck){
        float fishPercentage = initialPercentage;
        if (checkWeather(world, fish.bestWeather)) fishPercentage += 1;
        if (checkTime(world, fish.bestTime)) fishPercentage += 1;
        if (bait.equals(fish.bestBait)) fishPercentage += 1;
        return fishPercentage + fish.rarity + getLuckBonus(luck, fish.rarity);
    }

    public static boolean checkProgress(int x1, int x2, int fishX1, int fishX2){
        return x1 <= fishX1 && x2 >= fishX2 || x1 == fishX2 && x2 == fishX1;
    }

    public static int getFishX(int maxMovement){
        return RANDOM.nextInt(maxMovement - (-maxMovement) + 1) + (-maxMovement);
    }

    public static float getLuckBonus(float luckPercent, int fishRare){
        if (fishRare >= 5) return fishRare * -(luckPercent / 1000);
        if (fishRare == 4) return fishRare * (luckPercent / 1000);
        if (fishRare == 3) return fishRare * (luckPercent / 1000) + 0.5f;
        if (fishRare == 2) return fishRare * (luckPercent / 1000) + 1f;
        if (fishRare == 1) return fishRare * (luckPercent / 1000) + 2f;
        return 0;
    }

    public static float getFishSpeedMultiplier(int rarity) {
        return switch (rarity) {
            case 10 -> 0.05f; // Мусор почти не двигается
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
            case 10 -> 2; // Мусор почти не двигается
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