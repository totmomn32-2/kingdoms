package dev.kingdoms;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Locale;

public class MobKillListener implements Listener {

    private final QuestManager quests;

    public MobKillListener(QuestManager quests) {
        this.quests = quests;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        boolean hostile = dead instanceof Enemy;
        boolean animal = dead instanceof Animals;
        if (!hostile && !animal) return;

        Player killer = dead.getKiller();
        if (killer == null) return;

        String mobName = dead.getType().getKey().getKey().toUpperCase(Locale.ROOT);
        String category = hostile ? "ANY_HOSTILE" : "ANY_ANIMAL";
        quests.addProgress(killer, QuestType.KILL_MOB, category, mobName);
    }
}
