package dev.kingdoms;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class QuestManager {

    private final KingdomManager manager;

    private final List<Quest> quests = List.of(
            new Quest("monster_hunters", "Monster Hunters", QuestType.KILL_MOB, "ANY_HOSTILE", 5, 50, 25, 0),
            new Quest("zombie_slayers", "Zombie Slayers", QuestType.KILL_MOB, "ZOMBIE", 10, 80, 40, 0),
            new Quest("stone_masons", "Stone Masons", QuestType.MINE_BLOCK, "STONE", 20, 40, 20, 0),
            new Quest("wither_hunters", "Wither Hunters", QuestType.KILL_MOB, "WITHER_SKELETON", 3, 200, 100, 1),
            new Quest("wheat_farmers", "Wheat Farmers", QuestType.HARVEST_CROP, "WHEAT", 10, 60, 30, 0),
            new Quest("animal_hunters", "Animal Hunters", QuestType.KILL_MOB, "ANY_ANIMAL", 5, 40, 20, 0)
    );

    public QuestManager(KingdomManager manager) {
        this.manager = manager;
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

            if (progress >= quest.amount()) {
                // Project bonuses: +10% per level
                long points = quest.points() + quest.points() * kingdom.getProjectLevel("academy") * 10 / 100;
                long coins = quest.coins() + quest.coins() * kingdom.getProjectLevel("market") * 10 / 100;

                kingdom.setProgress(quest.id(), 0);
                kingdom.addPoints(points);
                kingdom.addCoins(coins);
                kingdom.addCore(quest.core());
                manager.saveKingdoms();

                String reward = "+" + points + " Points, +" + coins + " Coins";
                if (quest.core() > 0) {
                    reward += ", +" + quest.core() + " Core";
                }

                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (manager.getKingdom(online.getUniqueId()) == kingdom.getType()) {
                        online.sendMessage(Component.text("Quest complete: " + quest.name() + "! " + reward));
                    }
                }
            } else {
                kingdom.setProgress(quest.id(), progress);
                player.sendActionBar(Component.text(quest.name() + ": " + progress + "/" + quest.amount()));
            }
        }
    }
}
