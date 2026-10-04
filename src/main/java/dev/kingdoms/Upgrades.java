package dev.kingdoms;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Upgrades {

    public static final List<Upgrade> ALL = new ArrayList<>();

    private Upgrades() {
    }

    public static void load(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "upgrades.yml");
        if (!file.exists()) {
            plugin.saveResource("upgrades.yml", false);
        }

        ALL.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("upgrades");
        if (section == null) {
            plugin.getLogger().warning("upgrades.yml has no 'upgrades:' section.");
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection u = section.getConfigurationSection(id);
            if (u == null) continue;

            Attribute attribute = null;
            try {
                String key = u.getString("attribute", "").toLowerCase(Locale.ROOT);
                attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(key));
            } catch (IllegalArgumentException ignored) {
                // invalid key text
            }
            if (attribute == null) {
                plugin.getLogger().warning("Upgrade '" + id + "' has an unknown attribute, skipping it.");
                continue;
            }

            Material icon = Material.matchMaterial(u.getString("icon", "PAPER"));
            if (icon == null) {
                icon = Material.PAPER;
            }

            ALL.add(new Upgrade(
                    id,
                    u.getString("name", id),
                    icon,
                    attribute,
                    u.getDouble("base"),
                    u.getDouble("per-level"),
                    Math.max(1, u.getInt("max-level", 5)),
                    Math.max(0, u.getLong("cost", 50)),
                    u.getString("description", "")
            ));
        }
        plugin.getLogger().info("Loaded " + ALL.size() + " upgrades.");
    }
}
