package dev.kingdoms;

import org.bukkit.plugin.java.JavaPlugin;

public class KingdomsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("kingdom").setExecutor(new KingdomCommand());
        getLogger().info("Kingdoms plugin is ON!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Kingdoms plugin is OFF!");
    }
}
