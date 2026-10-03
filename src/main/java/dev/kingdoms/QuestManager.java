package dev.kingdoms;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class QuestManager {

    private final KingdomManager manager;
    private final JavaPlugin plugin;
    private final List<Quest> quests = new ArrayList<>();

    public QuestManager(KingdomManager manager, JavaPlugin plugin) {
        this.manager = manager;
        this.plugin = plugin;
        load();
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "quests.yml");
        if (!file.exists()) {
            plugin.saveResource("quests.yml", false);
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("quests");
        quests.clear();
        if (section == null) {
            plugin.getLogger().warning("quests.yml has no 'quests:' section.");
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection q = section.getConfigurationSection(id);
            if (q == null) continue;

            QuestType type;
            try {
                type = QuestType.valueOf(q.getString("type", "").toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Quest '" + id + "' has an invalid type, skipping it.");
                continue;
            }

            quests.add(new Quest(
                    id,
                    q.getString("name", id),
                    type,
                    q.getString("target", "ANY").toUpperCase(Locale.ROOT),
                    Math.max(1, q.getLong("amount", 1)),
                    q.getLong("points"),
                    q.getLong("coins"),
                    q.getLong("core")
            ));
        }
        plugin.getLogger().info("Loaded " + quests.size() + " quests.");
    }

    public List<Quest> getQuests() {
        return quests;
    }

    public void addProgress(Player player, QuestType type, String... keys) {
        Kingdom kingdom = manager.getKingdomData(player.getUniqueId());
        if (kingdom == null) return;

        for (Quest quest : quests) {
            if (quest.type() != type) continue;
            if (!Arrays.asList(keys).contains(quest.target())) continue;

            long progress = kingdom.getProgress(quest.id()) + 1;

            if (progres
