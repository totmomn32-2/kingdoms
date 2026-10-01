package dev.kingdoms;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KingdomManager {

    private final Map<UUID, KingdomType> members = new HashMap<>();
    private final File file;

    public KingdomManager(File dataFolder) {
        dataFolder.mkdirs();
        this.file = new File(dataFolder, "members.yml");
        load();
    }

    public boolean isMember(UUID player) {
        return members.containsKey(player);
    }

    public KingdomType getKingdom(UUID player) {
        return members.get(player);
    }

    public void join(UUID player, KingdomType kingdom) {
        members.put(player, kingdom);
        save();
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getKeys(false)) {
            KingdomType type = KingdomType.fromString(yaml.getString(key));
            if (type != null) {
                members.put(UUID.fromString(key), type);
            }
        }
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, KingdomType> entry : members.entrySet()) {
            yaml.set(entry.getKey().toString(), entry.getValue().name());
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
