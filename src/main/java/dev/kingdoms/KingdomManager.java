package dev.kingdoms;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KingdomManager {

    private final Map<UUID, KingdomType> members = new HashMap<>();

    public boolean isMember(UUID player) {
        return members.containsKey(player);
    }

    public KingdomType getKingdom(UUID player) {
        return members.get(player);
    }

    public void join(UUID player, KingdomType kingdom) {
        members.put(player, kingdom);
    }
}
