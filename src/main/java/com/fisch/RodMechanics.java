package com.fisch;

import com.fisch.fish.NewFish;
import net.minecraft.world.level.Level;
import java.util.Random; // Или net.minecraft.util.math.random.Random

public class RodMechanics {

    private static final Random RANDOM = new Random();

    public static NewFish determineCatch(Level world, NewFish[] bestiary, String bait) {
        if (bestiary == null || bestiary.length == 0) {
            return null;
        }

        float[] fishPercentages = new float[bestiary.length];
        float totalSum = 0;

        float initialPercentage = 100.0f / bestiary.length;

        for (int i = 0; i < bestiary.length; i++) {
            float percentage = fishDropPercentage(initialPercentage, bestiary[i], world, bait);
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

    public static float fishDropPercentage(float initialPercentage, NewFish fish, Level world, String bait){
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

        return fishPercentage + fish.rarity;
    }
}