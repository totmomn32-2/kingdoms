package dev.kingdoms;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public class EventManager {

    private double multiplier = 1.0;
    private long endsAt = 0;

    public void start(double multiplier, int minutes) {
        this.multiplier = multiplier;
        this.endsAt = System.currentTimeMillis() + minutes * 60_000L;
        Bukkit.broadcast(Component.text("EVENT! Quest rewards x" + multiplier
                + " for " + minutes + " minutes!"));
    }

    public void stop() {
        this.multiplier = 1.0;
        this.endsAt = 0;
        Bukkit.broadcast(Component.text("The reward event has ended."));
    }

    public boolean isActive() {
        return multiplier > 1.0 && System.currentTimeMillis() < endsAt;
    }

    public double getMultiplier() {
        return isActive() ? multiplier : 1.0;
    }

    public long getMinutesLeft() {
        if (!isActive()) return 0;
        return Math.max(1, (endsAt - System.currentTimeMillis()) / 60_000L);
    }
}
