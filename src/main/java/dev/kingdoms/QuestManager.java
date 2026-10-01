package dev.kingdoms;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class QuestManager {

    private final KingdomManager manager;

    private final List<Quest> quests = List.of(
            new Quest("monster_hunters", "Monster Hunters", QuestType.KILL_MOB, "ANY_HOSTILE", 5, 50, 25),
            new Quest("zombie_slayers", "Zombie Slayers", QuestType.KILL_MOB, "ZOMBIE", 10, 80, 40),
            new Quest("stone_masons", "Stone Masons", QuestType.MINE_BLOCK, "STONE", 20, 40, 20)
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
                kingdom.setProgress(quest.id(), 0);
                kingdom.addPoints(quest.points());
                kingdom.addCoins(quest.coins());
                manager.saveKingdoms();

                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (manager.getKingdom(online.getUniqueId()) == kingdom.getType()) {
                        online.sendMessage(Component.text("Quest complete: " + quest.name()
                                + "! +" + quest.points() + " Points, +" + quest.coins() + " Coins"));
                    }
                }
            } else {
                kingdom.setProgress(quest.id(), progress);
                player.sendActionBar(Component.text(quest.name() + ": " + progress + "/" + quest.amount()));
            }
        }
    }
}
