package dev.kingdoms;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
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

        Block block = event.getBlock();
        String blockName = block.getType().name();

        if (block.getBlockData() instanceof Ageable crop) {
            if (crop.getAge() >= crop.getMaximumAge()) {
                quests.addProgress(player, QuestType.HARVEST_CROP, blockName);
            }
            return;
        }

        if (PlacedBlocks.contains(block)) return;

        quests.addProgress(player, QuestType.MINE_BLOCK, blockName);
    }
}
