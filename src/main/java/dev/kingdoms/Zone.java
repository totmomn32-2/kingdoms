package dev.kingdoms;

import org.bukkit.Location;

import java.util.EnumMap;
import java.util.Map;

public class Zone {

    private final String id;
    private final String name;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final double radius;
    private KingdomType owner;
    private final Map<KingdomType, Integer> progress = new EnumMap<>(KingdomType.class);

    public Zone(String id, String name, String world, double x, double y, double z, double radius) {
        this.id = id;
        this.name = name;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getRadius() {
        return radius;
    }

    public KingdomType getOwner() {
        return owner;
    }

    public void setOwner(KingdomType owner) {
        this.owner = owner;
    }

    public int getProgress(KingdomType type) {
        return progress.getOrDefault(type, 0);
    }

    public void setProgress(KingdomType type, int value) {
        progress.put(type, Math.max(0, Math.min(100, value)));
    }

    public void clearProgress() {
        progress.clear();
    }

    public boolean contains(Location loc) {
        if (loc.getWorld() == null || !loc.getWorld().getName().equals(world)) return false;
        double dx = loc.getX() - x;
        double dz = loc.getZ() - z;
        return dx * dx + dz * dz <= radius * radius;
    }
  }
