package dev.kingdoms;

import org.bukkit.Material;

import java.util.List;

public final class Projects {

    public static final List<Project> ALL = List.of(
            new Project("castle", "Castle", Material.STONE_BRICKS, 3, 200, 300, 1,
                    "The heart of the kingdom"),
            new Project("market", "Market", Material.EMERALD, 3, 100, 200, 0,
                    "Where the kingdom trades"),
            new Project("academy", "Academy", Material.ENCHANTING_TABLE, 3, 150, 250, 1,
                    "Where members learn new skills"),
            new Project("storage", "Storage", Material.CHEST, 3, 100, 150, 0,
                    "Shared kingdom storage")
    );

    private Projects() {
    }
}
