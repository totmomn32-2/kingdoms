package dev.kingdoms;

import java.util.HashMap;
import java.util.Map;

public class Kingdom {

    public static final int HEALTH_MAX_LEVEL = 5;

    private final KingdomType type;
    private int level = 1;
    private long points = 0;
    private long coins = 0;
    private long core = 0;
    private final Map<String, Long> questProgress = new HashMap<>();
    private final Map<String, Integer> upgrades = new HashMap<>();

    public Kingdom(KingdomType type) {
        this.type = type;
    }

    public KingdomType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public long getPoints() {
        return points;
    }

    public long getCoins() {
        return coins;
    }

    public long getCore() {
        return core;
    }

    public long getProgress(String questId) {
        return questProgress.getOrDefault(questId, 0L);
    }

    public void setProgress(String questId, long value) {
        questProgress.put(questId, value);
    }

    public Map<String, Long> getQuestProgress() {
        return questProgress;
    }

    public int getUpgradeLevel(String id) {
        return upgrades.getOrDefault(id, 0);
    }

    public void setUpgradeLevel(String id, int value) {
        upgrades.put(id, value);
    }

    public Map<String, Integer> getUpgrades() {
        return upgrades;
    }

    public long getHealthUpgradeCost() {
        return 50L * (getUpgradeLevel("health") + 1);
    }

    public void addPoints(long amount) {
        points += amount;
    }

    public void addCoins(long amount) {
        coins += amount;
    }

    public void addCore(long amount) {
        core += amount;
    }

    public boolean spendPoints(long amount) {
        if (points < amount) return false;
        points -= amount;
        return true;
    }

    public boolean spendCoins(long amount) {
        if (coins < amount) return false;
        coins -= amount;
        return true;
    }

    public long getLevelUpCost() {
        return 100L * level;
    }

    public void levelUp() {
        level++;
    }

    public void restore(int level, long points, long coins, long core) {
        this.level = level;
        this.points = points;
        this.coins = coins;
        this.core = core;
    }
}
