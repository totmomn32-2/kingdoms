package dev.kingdoms;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ZoneMenu implements Listener {

    private static class MenuHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private final ZoneManager zones;

    public ZoneMenu(ZoneManager zones) {
        this.zones = zones;
    }

    public void open(Player player) {
        MenuHolder holder = new MenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 54, Component.text("Zone Map"));
        holder.inventory = inventory;

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, filler);
        }

        if (zones.getZones().isEmpty()) {
            ItemStack empty = new ItemStack(Material.PAPER);
            ItemMeta meta = empty.getItemMeta();
            meta.displayName(text("No zones yet", NamedTextColor.GRAY));
            empty.setItemMeta(meta);
            inventory.setItem(22, empty);
        } else {
            int slot = 0;
            for (Zone zone : zones.getZones()) {
                if (slot >= 45) break;
                inventory.setItem(slot++, createItem(zone, player));
            }
        }

        player.openInventory(inventory);
    }

    private ItemStack createItem(Zone zone, Player player) {
        KingdomType owner = zone.getOwner();
        Material banner = owner == null ? Material.WHITE_BANNER : bannerFor(owner);
        NamedTextColor color = owner == null ? NamedTextColor.GRAY : colorFor(owner);

        List<Component> lore = new ArrayList<>();
        lore.add(text("Owner: " + (owner == null ? "nobody" : owner.getDisplayName()), color));
        lore.add(text("Location: " + (int) zone.getX() + ", " + (int) zone.getY() + ", "
                + (int) zone.getZ() + " (" + zone.getWorld() + ")", NamedTextColor.GRAY));
        lore.add(text("Radius: " + (int) zone.getRadius() + " blocks", NamedTextColor.GRAY));

        Location loc = player.getLocation();
        if (zone.contains(loc)) {
            lore.add(text("You are inside this zone", NamedTextColor.GREEN));
        } else if (loc.getWorld() != null && loc.getWorld().getName().equals(zone.getWorld())) {
            double dx = zone.getX() - loc.getX();
            double dz = zone.getZ() - loc.getZ();
            int distance = (int) Math.sqrt(dx * dx + dz * dz);
            lore.add(text("Distance: " + distance + " blocks (" + direction(dx, dz) + ")", NamedTextColor.AQUA));
        } else {
            lore.add(text("In another world", NamedTextColor.DARK_GRAY));
        }

        for (KingdomType type : KingdomType.values()) {
            int progress = zone.getProgress(type);
            if (progress > 0) {
                lore.add(text("Capturing: " + type.getDisplayName() + " " + progress + "%", colorFor(type)));
            }
        }

        ItemStack item = new ItemStack(banner);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(text(zone.getName(), NamedTextColor.YELLOW));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String direction(double dx, double dz) {
        // In Minecraft: +x is East, +z is South.
        if (Math.abs(dx) > 2 * Math.abs(dz)) {
            return dx > 0 ? "East" : "West";
        }
        if (Math.abs(dz) > 2 * Math.abs(dx)) {
            return dz > 0 ? "South" : "North";
        }
        return (dz > 0 ? "South" : "North") + "-" + (dx > 0 ? "East" : "West");
    }

    private Material bannerFor(KingdomType type) {
        return switch (type) {
            case RED -> Material.RED_BANNER;
            case BLUE -> Material.BLUE_BANNER;
            case GREEN -> Material.GREEN_BANNER;
        };
    }

    private NamedTextColor colorFor(KingdomType type) {
        return switch (type) {
            case RED -> NamedTextColor.RED;
            case BLUE -> NamedTextColor.BLUE;
            case GREEN -> NamedTextColor.GREEN;
        };
    }

    private Component text(String message, NamedTextColor color) {
        return Component.text(message, color).decoration(TextDecoration.ITALIC, false);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }
              }
