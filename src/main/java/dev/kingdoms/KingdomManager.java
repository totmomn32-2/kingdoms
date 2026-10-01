package dev.kingdoms;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KingdomManager {

    private final Map<UUID, KingdomType> members = new HashMap<>();
    private final Map<KingdomType, Kingdom> kingdoms = new EnumMap<>(KingdomType.class);
    private final File file;

    public KingdomManager(File dataFolder) {
        dataFolder.mkdirs();
        this.file = new File(dataFolder, "members.yml");
        for (KingdomType type : KingdomType.values()) {
            kingdoms.put(type, new Kingdom(type));
        }
        load();
    }

    public boolean isMember(UUID player) {
        return members.containsKey(player);
    }

    public KingdomType getKingdom(UUID player) {
        return members.get(player);
    }

    public Kingdom getKingdomData(UUID player) {
        KingdomType type = members.get(player);
        if (type == null) return null;
        return kingdoms.get(type);
    }

    public int getMemberCount(KingdomType type) {
        int count = 0;
        for (KingdomType t : members.values()) {
            if (t == type) count++;
        }
        return count;
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
