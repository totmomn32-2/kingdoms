package dev.kingdoms;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ActivityListener implements Listener {

    private record ChunkKey(UUID world, int x, int z) {
    }

    private final QuestManager quests;
    private final KingdomManager manager;
    private final Map<UUID, Set<ChunkKey>> visited = new HashMap<>();

    public ActivityListener(QuestManager quests, KingdomManager manager) {
        this.quests = quests;
        this.manager = manager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        String blockName = event.getBlockPlaced().getType().name();
        quests.addProgress(player, QuestType.PLACE_BLOCK, "ANY", blockName);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;

        Player player = event.getPlayer();
        if (manager.getKingdomData(player.getUniqueId()) == null) return;

        Location to = event.getTo();
        ChunkKey key = new ChunkKey(to.getWorld().getUID(), to.getBlockX() >> 4, to.getBlockZ() >> 4);
        Set<ChunkKey> seen = visited.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        if (!seen.add(key)) return;

        String dimension = to.getWorld().getEnvironment().name();
        quests.addProgress(player, QuestType.EXPLORE, "ANY", dimension);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        visited.remove(event.getPlayer().getUniqueId());
    }
}
