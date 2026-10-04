package dev.kingdoms;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ZoneManager {

    private final File file;
    private final Map<String, Zone> zones = new LinkedHashMap<>();

    public ZoneManager(File dataFolder) {
        dataFolder.mkdirs();
        this.file = new File(dataFolder, "zones.yml");
        load();
    }

    public Collection<Zone> getZones() {
        return zones.values();
    }

    public Zone get(String id) {
        return zones.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean create(String id, String name, Location loc, double radius) {
        String key = id.toLowerCase(Locale.ROOT);
        if (zones.containsKey(key)) return false;
        zones.put(key, new Zone(key, name, loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(), radius));
        save();
        return true;
    }

    public boolean remove(String id) {
        if (zones.remove(id.toLowerCase(Locale.ROOT)) == null) return false;
        save();
        return true;
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("zones");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            ConfigurationSection z = section.getConfigurationSection(id);
            if (z == null) continue;
            Zone zone = new Zone(
                    id,
                    z.getString("name", id),
                    z.getString("world", "world"),
                    z.getDouble("x"),
                    z.getDouble("y"),
                    z.getDouble("z"),
                    Math.max(1, z.getDouble("radius", 10))
            );
            zone.setOwner(KingdomType.fromString(z.getString("owner")));
            zones.put(id, zone);
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Zone zone : zones.values()) {
            String path = "zones." + zone.getId();
            yaml.set(path + ".name", zone.getName());
            yaml.set(path + ".world", zone.getWorld());
            yaml.set(path + ".x", zone.getX());
            yaml.set(path + ".y", zone.getY());
            yaml.set(path + ".z", zone.getZ());
            yaml.set(path + ".radius", zone.getRadius());
            if (zone.getOwner() != null) {
                yaml.set(path + ".owner", zone.getOwner().name());
            }
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
