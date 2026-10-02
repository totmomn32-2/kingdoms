package dev.kingdoms;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
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

    public Collection<Kingdom> getAllKingdoms() {
        return kingdoms.values();
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
        Player online = Bukkit.getPlayer(player);
        if (online != null) {
            applyUpgrades(online);
        }
    }

    public void applyUpgrades(Player player) {
        AttributeInstance health = player.getAttribute(Attribute.MAX_HEALTH);
        if (health == null) return;
        Kingdom kingdom = getKingdomData(player.getUniqueId());
        int level = (kingdom == null) ? 0 : kingdom.getUpgradeLevel("health");
        health.setBaseValue(20.0 + level * 2.0);
    }

    public void applyUpgradesToKingdom(KingdomType type) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (members.get(online.getUniqueId()) == type) {
                applyUpgrades(online);
            }
        }
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
            ConfigurationSection quests = yaml.getConfigurationSection(path + ".quests");
            if (quests != null) {
                for (String questId : quests.getKeys(false)) {
                    kingdom.setProgress(questId, quests.getLong(questId));
                }
            }
            ConfigurationSection upgrades = yaml.getConfigurationSection(path + ".upgrades");
            if (upgrades != null) {
                for (String upgradeId : upgrades.getKeys(false)) {
                    kingdom.setUpgradeLevel(upgradeId, upgrades.getInt(upgradeId));
                }
            }
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
            for (Map.Entry<String, Long> entry : kingdom.getQuestProgress().entrySet()) {
                yaml.set(path + ".quests." + entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, Integer> entry : kingdom.getUpgrades().entrySet()) {
                yaml.set(path + ".upgrades." + entry.getKey(), entry.getValue());
            }
        }
        try {
            yaml.save(kingdomsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
