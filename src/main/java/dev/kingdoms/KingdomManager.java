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
    private final File membersFile;
    private final File kingdomsFile;

    public KingdomManager(File dataFolder) {
        dataFolder.mkdirs();
        this.membersFile = new File(dataFolder, "members.yml");
        this.kingdomsFile = new File(dataFolder, "kingdoms.yml");
        for (KingdomType type : KingdomType.values()) {
            kingdoms.put(type, new Kingdom(type));
        }
        loadMembers();
        loadKingdoms();
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
        saveMembers();
    }

    private void loadMembers() {
        if (!membersFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(membersFile);
        for (String key : yaml.getKeys(false)) {
            KingdomType type = KingdomType.fromString(yaml.getString(key));
            if (type != null) {
                members.put(UUID.fromString(key), type);
            }
        }
    }

    private void saveMembers() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, KingdomType> entry : members.entrySet()) {
            yaml.set(entry.getKey().toString(), entry.getValue().name());
        }
        try {
            yaml.save(membersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadKingdoms() {
        if (!kingdomsFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(kingdomsFile);
        for (Kingdom kingdom : kingdoms.values()) {
            String path = kingdom.getType().name();
            kingdom.restore(
                    yaml.getInt(path + ".level", 1),
                    yaml.getLong(path + ".points", 0),
                    yaml.getLong(path + ".coins", 0),
                    yaml.getLong(path + ".core", 0)
            );
        }
    }

    public void saveKingdoms() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Kingdom kingdom : kingdoms.values()) {
            String path = kingdom.getType().name();
            yaml.set(path + ".level", kingdom.getLevel());
            yaml.set(path + ".points", kingdom.getPoints());
            yaml.set(path + ".coins", kingdom.getCoins());
            yaml.set(path + ".core", kingdom.getCore());
        }
        try {
            yaml.save(kingdomsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
