package com.fisch.rod;

import com.fisch.fish.NewFish;
import net.minecraft.world.level.Level;
import java.util.Random; // Или net.minecraft.util.math.random.Random

public class RodMechanics {

    private static final Random RANDOM = new Random();

    public static NewFish determineCatch(Level world, NewFish[] bestiary, String bait, float luck) {
        if (bestiary == null || bestiary.length == 0) {
            return null;
        }

        float[] fishPercentages = new float[bestiary.length];
        float totalSum = 0;

        float initialPercentage = 100.0f / bestiary.length;

        for (int i = 0; i < bestiary.length; i++) {
            float percentage = fishDropPercentage(initialPercentage, bestiary[i], world, bait, luck);
            fishPercentages[i] = percentage;
            totalSum += percentage;
        }

        float rolledValue = RANDOM.nextFloat() * totalSum;
        float currentSum = 0;
        for (int i = 0; i < bestiary.length; i++) {
            currentSum += fishPercentages[i];
            if (rolledValue <= currentSum) {
                return bestiary[i];
            }
        }

        return bestiary[bestiary.length - 1];
    }

    public static boolean checkWeather(Level world, String bestWeather) {
        String weather;

        if (world.isRaining()) {
            weather = "raining";
        } else if (world.isThundering()) {
            weather = "thundering";
        } else {
            weather = "clear";
        }

        return bestWeather.equals(weather);
    }

    public static boolean checkTime(Level world, String bestTime) {
        String time;
        long timeOfDay = world.getDayTime() % 24000;

        if (timeOfDay >= 0 && timeOfDay < 12000) {
            time = "day";
        } else if (timeOfDay >= 12000 && timeOfDay < 13000) {
            time = "sunset";
        } else if (timeOfDay >= 13000 && timeOfDay < 23000) {
            time = "night";
        } else {
            time = "sunrise";
        }

        return bestTime.equals(time);
    }

    public static float fishDropPercentage(float initialPercentage, NewFish fish, Level world, String bait, float luck){
        float fishPercentage = initialPercentage;

        if (checkWeather(world, fish.bestWeather)){
            fishPercentage += 1;
        }
        if (checkTime(world, fish.bestTime)){
            fishPercentage += 1;
        }
        if (bait.equals(fish.bestBait)){
            fishPercentage += 1;
        }

        return fishPercentage + fish.rarity + getLuckBonus(luck, fish.rarity);
    }

    public static boolean checkProgress(int x1, int x2, int fishX1, int fishX2){
        return x1 <= fishX1 && x2 >= fishX2 || x1 == fishX2 && x2 == fishX1;
    }

    public static int getFishX(int maxMovement){
        return RANDOM.nextInt(maxMovement - (-maxMovement) + 1) + (-maxMovement);
    }
    public static float getLuckBonus(float luckPercent, int fishRare){
      if (fishRare >= 5){
          return fishRare * -(luckPercent / 1000);
      }
      if (fishRare == 4){
          return fishRare * (luckPercent / 1000);
      }
      if (fishRare == 3){
          return fishRare * (luckPercent / 1000) + 0.5f;
      }
      if (fishRare == 2){
          return fishRare * (luckPercent / 1000) + 1f;
      }
      if (fishRare == 1) {
          return fishRare * (luckPercent / 1000) + 2f;
      }
      return 0;
    }

    public static float getFishSpeedMultiplier(int rarity) {
        return switch (rarity) {
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

        float min = 0.01f;
        float max = 1.0f;

        resilience = Math.max(min, Math.min(max, resilience));

        return 1.0f - resilience * 0.5f;
    }
}