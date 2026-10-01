package dev.kingdoms;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobKillListener implements Listener {

    private final KingdomManager manager;

    public MobKillListener(KingdomManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead instanceof Enemy)) return;

        Player killer = dead.getKiller();
        if (killer == null) return;

        Kingdom kingdom = manager.getKingdomData(killer.getUniqueId());
        if (kingdom == null) return;

        long kills = kingdom.getMonsterKills() + 1;

        if (kills >= Kingdom.MONSTER_QUEST_GOAL) {
            kingdom.setMonsterKills(0);
            kingdom.addPoints(50);
            kingdom.addCoins(25);
            manager.saveKingdoms();

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (manager.getKingdom(online.getUniqueId()) == kingdom.getType()) {
                    online.sendMessage(Component.text("Quest complete: Monster Hunters! +50 Points, +25 Coins"));
                }
            }
        } else {
            kingdom.setMonsterKills(kills);
            killer.sendActionBar(Component.text("Monster Hunters: " + kills + "/" + Kingdom.MONSTER_QUEST_GOAL));
        }
    }
                }
