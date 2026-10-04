package dev.kingdoms;

import org.bukkit.block.Block;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PlacedBlocks {

    private record BlockKey(UUID world, int x, int y, int z) {
        static BlockKey of(Block block) {
            return new BlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }
    }

    private static final int MAX_SIZE = 50_000;

    private static final Set<BlockKey> PLACED = Collections.newSetFromMap(
            new LinkedHashMap<BlockKey, Boolean>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<BlockKey, Boolean> eldest) {
                    return size() > MAX_SIZE;
                }
            });

    private PlacedBlocks() {
    }

    /** Marks the block's position. Returns true only the first time. */
    public static boolean mark(Block block) {
        return PLACED.add(BlockKey.of(block));
    }

    public static boolean contains(Block block) {
        return PLACED.contains(BlockKey.of(block));
    }
                               }
