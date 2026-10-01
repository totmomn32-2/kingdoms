package dev.kingdoms;

import org.bukkit.plugin.java.JavaPlugin;

public class KingdomsPlugin extends JavaPlugin {

    private KingdomManager kingdomManager;

    @Override
    public void onEnable() {
        kingdomManager = new KingdomManager(getDataFolder());
        getCommand("kingdom").setExecutor(new KingdomCommand(kingdomManager));
        getLogger().info("Kingdoms plugin is ON!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Kingdoms plugin is OFF!");
    }
}
