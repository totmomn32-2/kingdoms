package dev.kingdoms;

import org.bukkit.plugin.java.JavaPlugin;

public class KingdomsPlugin extends JavaPlugin {

    private KingdomManager kingdomManager;

    @Override
    public void onEnable() {
        kingdomManager = new KingdomManager(getDataFolder());
        getCommand("kingdom").setExecutor(new KingdomCommand(kingdomManager));
        getServer().getPluginManager().registerEvents(new MobKillListener(kingdomManager), this);

        // Auto-save every 5 minutes (6000 ticks)
        getServer().getScheduler().runTaskTimer(this, () -> kingdomManager.saveKingdoms(), 6000L, 6000L);

        getLogger().info("Kingdoms plugin is ON!");
    }

    @Override
    public void onDisable() {
        kingdomManager.saveKingdoms();
        getLogger().info("Kingdoms plugin is OFF!");
    }
}
