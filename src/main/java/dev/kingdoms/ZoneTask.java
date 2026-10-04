package dev.kingdoms;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ZoneTask implements Runnable {

    private static final int CAPTURE_PER_SECOND = 2;
    private static final int DECAY_PER_SECOND = 2;
    private static final int REWARD_INTERVAL_SECONDS = 60;
    private static final long REWARD_POINTS = 10;
    private static final long REWARD_COINS = 10;

    private final KingdomManager manager;
    private final ZoneManager zones;
    private int seconds = 0;

    public ZoneTask(KingdomManager manager, ZoneManager zones) {
        this.manager = manager;
        this.zones = zones;
    }

    @Override
    public void run() {
        seconds++;
        boolean rewardTime = seconds % REWARD_INTERVAL_SECONDS == 0;
        boolean ownerChanged = false;
        boolean rewarded = false;

        for (Zone zone : zones.getZones()) {
            Map<KingdomType, List<Player>> inside = new EnumMap<>(KingdomType.class);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getGameMode() == GameMode.SPECTATOR) continue;
                if (!zone.contains(player.getLocation())) continue;
                KingdomType type = manager.getKingdom(player.getUniqueId());
                if (type == null) continue;
                inside.computeIfAbsent(type, k -> new ArrayList<>()).add(player);
            }

            if (updateCapture(zone, inside)) {
                ownerChanged = true;
            }

            if (rewardTime && zone.getOwner() != null) {
                Kingdom kingdom = manager.getKingdomData(zone.getOwner());
                kingdom.addPoints(REWARD_POINTS);
                kingdom.addCoins(REWARD_COINS);
                rewarded = true;
            }
        }

        if (ownerChanged) {
            zones.save();
        }
        if (rewarded) {
            manager.saveKingdoms();
        }
    }

    /** Returns true if the zone changed hands. */
    private boolean updateCapture(Zone zone, Map<KingdomType, List<Player>> inside) {
        if (inside.size() > 1) {
            for (List<Player> list : inside.values()) {
                for (Player player : list) {
                    player.sendActionBar(Component.text(zone.getName() + ": CONTESTED!"));
                }
            }
            return false;
        }

        if (inside.isEmpty()) {
            decay(zone, null);
            return false;
        }

        KingdomType attacker = inside.keySet().iterator().next();
        List<Player> players = inside.get(attacker);

        if (attacker == zone.getOwner()) {
            decay(zone, attacker);
            for (Player player : players) {
                player.sendActionBar(Component.text("Your kingdom holds " + zone.getName()));
            }
            return false;
        }

        decay(zone, attacker);
        zone.setProgress(attacker, zone.getProgress(attacker) + CAPTURE_PER_SECOND);

        if (zone.getProgress(attacker) >= 100) {
            zone.setOwner(attacker);
            zone.clearProgress();
            Bukkit.broadcast(Component.text(attacker.getDisplayName() + " captured " + zone.getName() + "!"));
            return true;
        }

        for (Player player : players) {
            player.sendActionBar(Component.text(zone.getName() + ": capturing "
                    + zone.getProgress(attacker) + "%"));
        }
        return false;
    }

    private void decay(Zone zone, KingdomType except) {
        for (KingdomType type : KingdomType.values()) {
            if (type == except) continue;
            int value = zone.getProgress(type);
            if (value > 0) {
                zone.setProgress(type, value - DECAY_PER_SECOND);
            }
        }
    }
        }
