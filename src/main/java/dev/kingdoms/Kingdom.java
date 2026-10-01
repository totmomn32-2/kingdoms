package dev.kingdoms;

public class Kingdom {

    private final KingdomType type;
    private int level = 1;
    private long points = 0;
    private long coins = 0;
    private long core = 0;

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

    public void addPoints(long amount) {
        points += amount;
    }

    public void addCoins(long amount) {
        coins += amount;
    }

    public void addCore(long amount) {
        core += amount;
    }
}
