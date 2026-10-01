package dev.kingdoms;

import org.bukkit.plugin.java.JavaPlugin;

public class KingdomsPlugin extends JavaPlugin {

    private KingdomManager kingdomManager;

    @Override
    public void onEnable() {
        kingdomManager = new KingdomManager(getDataFolder());
        getCommand("kingdom").setExecutor(new KingdomCommand(kingdomManager));

        // Auto-save every 5 minutes (20 ticks = 1 second, so 6000 ticks = 5 minutes)
        getServer().getScheduler().runTaskTimer(this, () -> kingdomManager.saveKingdoms(), 6000L, 6000L);

        getLogger().info("Kingdoms plugin is ON!");
    }

    @Override
    public void onDisable() {
        kingdomManager.saveKingdoms();
        getLogger().info("Kingdoms plugin is OFF!");
    }
}
