package dev.kingdoms;

import org.bukkit.plugin.java.JavaPlugin;

public class KingdomsPlugin extends JavaPlugin {

    private KingdomManager kingdomManager;

    @Override
    public void onEnable() {
        kingdomManager = new KingdomManager(getDataFolder());
        EventManager eventManager = new EventManager();
        ZoneManager zoneManager = new ZoneManager(getDataFolder());
        QuestManager questManager = new QuestManager(kingdomManager, this);
        questManager.setEventManager(eventManager);
        QuestMenu questMenu = new QuestMenu(kingdomManager, questManager);
        UpgradeMenu upgradeMenu = new UpgradeMenu(kingdomManager);
        ProjectMenu projectMenu = new ProjectMenu(kingdomManager);
        KingdomMenu menu = new KingdomMenu(kingdomManager, questMenu, upgradeMenu, projectMenu);

        getCommand("kingdom").setExecutor(new KingdomCommand(
                kingdomManager, menu, questMenu, upgradeMenu, questManager, eventManager, zoneManager));
        getServer().getPluginManager().registerEvents(menu, this);
        getServer().getPluginManager().registerEvents(questMenu, this);
        getServer().getPluginManager().registerEvents(upgradeMenu, this);
        getServer().getPluginManager().registerEvents(projectMenu, this);
        getServer().getPluginManager().registerEvents(new MobKillListener(questManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(questManager), this);
        getServer().getPluginManager().registerEvents(new ActivityListener(questManager, kingdomManager), this);
        getServer().getPluginManager().registerEvents(new JoinListener(kingdomManager), this);

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
