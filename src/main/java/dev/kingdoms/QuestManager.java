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
    private EventManager events;

    public QuestManager(KingdomManager manager, JavaPlugin plugin) {
        this.manager = manager;
        this.plugin = plugin;
        load();
    }

    public void setEventManager(EventManager events) {
        this.events = events;
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
                    Math.max(1, q.getInt("min-contributors", 1)),
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

            long progress = Math.min(kingdom.getProgress(quest.id()) + 1, quest.amount());
            kingdom.addContributor(quest.id(), player.getUniqueId());
            int contributors = kingdom.getContributorCount(quest.id());

            if (progress >= quest.amount() && contributors >= quest.minContributors()) {
                // Project bonuses: +10% per level
                long points = quest.points() + quest.points() * kingdom.getProjectLevel("academy") * 10 / 100;
                long coins = quest.coins() + quest.coins() * kingdom.getProjectLevel("market") * 10 / 100;

                // Event multiplier (core is never multiplied)
                double multiplier = (events == null) ? 1.0 : events.getMultiplier();
                points = Math.round(points * multiplier);
                coins = Math.round(coins * multiplier);

                kingdom.setProgress(quest.id(), 0);
                kingdom.clearContributors(quest.id());
                kingdom.addPoints(points);
                kingdom.addCoins(coins);
                kingdom.addCore(quest.core());
                manager.saveKingdoms();

                String reward = "+" + points + " Points, +" + coins + " Coins";
                if (quest.core() > 0) {
                    reward += ", +" + quest.core() + " Core";
                }
                if (multiplier > 1.0) {
                    reward += " (event x" + multiplier + ")";
                }

                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (manager.getKingdom(online.getUniqueId()) == kingdom.getType()) {
                        online.sendMessage(Component.text("Quest complete: " + quest.name() + "! " + reward));
                    }
                }
            } else {
                kingdom.setProgress(quest.id(), progress);
                if (progress >= quest.amount()) {
                    int missing = quest.minContributors() - contributors;
                    player.sendActionBar(Component.text(quest.name() + ": waiting for " + missing
                            + " more member(s) to contribute"));
                } else if (quest.minContributors() > 1) {
                    player.sendActionBar(Component.text(quest.name() + ": " + progress + "/" + quest.amount()
                            + " (members " + contributors + "/" + quest.minContributors() + ")"));
                } else {
                    player.sendActionBar(Component.text(quest.name() + ": " + progress + "/" + quest.amount()));
                }
            }
        }
    }
}
