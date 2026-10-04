package dev.kingdoms;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Projects {

    public static final List<Project> ALL = new ArrayList<>();

    private Projects() {
    }

    public static void load(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "projects.yml");
        if (!file.exists()) {
            plugin.saveResource("projects.yml", false);
        }

        ALL.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("projects");
        if (section == null) {
            plugin.getLogger().warning("projects.yml has no 'projects:' section.");
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection p = section.getConfigurationSection(id);
            if (p == null) continue;

            Material icon = Material.matchMaterial(p.getString("icon", "PAPER"));
            if (icon == null) {
                icon = Material.PAPER;
            }

            ALL.add(new Project(
                    id,
                    p.getString("name", id),
                    icon,
                    Math.max(1, p.getInt("max-level", 3)),
                    Math.max(0, p.getLong("points")),
                    Math.max(0, p.getLong("coins")),
                    Math.max(0, p.getLong("core")),
                    p.getString("description", "")
            ));
        }
        plugin.getLogger().info("Loaded " + ALL.size() + " projects.");
    }
}
