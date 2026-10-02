package dev.kingdoms;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;

public record Upgrade(
        String id,
        String name,
        Material icon,
        Attribute attribute,
        double baseValue,
        double perLevel,
        int maxLevel,
        long baseCost,
        String description
) {
    public long costForNext(int currentLevel) {
        return baseCost * (currentLevel + 1);
    }
  }
