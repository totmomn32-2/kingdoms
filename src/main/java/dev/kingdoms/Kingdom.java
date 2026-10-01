package dev.kingdoms;

import java.util.HashMap;
import java.util.Map;

public class Kingdom {

    public static final long MONSTER_QUEST_GOAL = 5;

    private final KingdomType type;
    private int level = 1;
    private long points = 0;
    private long coins = 0;
    private long core = 0;
    private long monsterKills = 0;
    private final Map<String, Long> questProgress = new HashMap<>();

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

    public long getMonsterKills() {
        return monsterKills;
    }

    public void setMonsterKills(long monsterKills) {
        this.monsterKills = monsterKills;
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

    public void addPoints(long amount) {
        points += amount;
    }

    public void addCoins(long amount) {
        coins += amount;
    }

    public void addCore(long amount) {
        core += amount;
    }

    public void restore(int level, long points, long coins, long core) {
        this.level = level;
        this.points = points;
        this.coins = coins;
        this.core = core;
    }
}
