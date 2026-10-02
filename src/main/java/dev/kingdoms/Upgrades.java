package dev.kingdoms;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;

import java.util.List;

public final class Upgrades {

    public static final List<Upgrade> ALL = List.of(
            new Upgrade("health", "Health", Material.GOLDEN_APPLE,
                    Attribute.MAX_HEALTH, 20.0, 2.0, 5, 50,
                    "+1 heart per level for every member"),
            new Upgrade("speed", "Speed", Material.SUGAR,
                    Attribute.MOVEMENT_SPEED, 0.1, 0.005, 5, 60,
                    "+5% movement speed per level"),
            new Upgrade("strength", "Strength", Material.BLAZE_POWDER,
                    Attribute.ATTACK_DAMAGE, 1.0, 0.5, 5, 80,
                    "+0.5 attack damage per level")
    );

    private Upgrades() {
    }
}
