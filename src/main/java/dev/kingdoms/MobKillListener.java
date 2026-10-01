package dev.kingdoms;

import net.kyori.adventure.text.Component;
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

        kingdom.addPoints(1);
        killer.sendActionBar(Component.text("+1 Kingdom Point"));
    }
}
