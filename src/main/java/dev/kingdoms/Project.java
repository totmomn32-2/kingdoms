package dev.kingdoms;

import org.bukkit.Material;

public record Project(
        String id,
        String name,
        Material icon,
        int maxLevel,
        long basePoints,
        long baseCoins,
        long baseCore,
        String description
) {
    public long pointsFor(int currentLevel) {
        return basePoints * (currentLevel + 1);
    }

    public long coinsFor(int currentLevel) {
        return baseCoins * (currentLevel + 1);
    }

    public long coreFor(int currentLevel) {
        return baseCore * (currentLevel + 1);
    }
  }
