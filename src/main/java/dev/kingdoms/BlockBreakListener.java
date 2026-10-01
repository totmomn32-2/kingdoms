package dev.kingdoms;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private final QuestManager quests;

    public BlockBreakListener(QuestManager quests) {
        this.quests = quests;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        String blockName = event.getBlock().getType().name();
        quests.addProgress(player, QuestType.MINE_BLOCK, blockName);
    }
}
