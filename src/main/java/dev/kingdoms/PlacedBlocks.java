package dev.kingdoms;

import org.bukkit.block.Block;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    private static boolean dirty = false;

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
        boolean added = PLACED.add(BlockKey.of(block));
        if (added) {
            dirty = true;
        }
        return added;
    }

    public static boolean contains(Block block) {
        return PLACED.contains(BlockKey.of(block));
    }

    public static void load(File file) {
        if (!file.exists()) return;
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 4) continue;
                try {
                    PLACED.add(new BlockKey(
                            UUID.fromString(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3])));
                } catch (IllegalArgumentException ignored) {
                    // skip bad lines
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(File file) {
        if (!dirty) return;
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            for (BlockKey key : PLACED) {
                writer.write(key.world() + "," + key.x() + "," + key.y() + "," + key.z());
                writer.newLine();
            }
            dirty = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
