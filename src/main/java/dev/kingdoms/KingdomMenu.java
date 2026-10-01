package dev.kingdoms;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class KingdomMenu implements Listener {

    private static class MenuHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private final KingdomManager manager;

    public KingdomMenu(KingdomManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {
        MenuHolder holder = new MenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, Component.text("Three Kingdoms"));
        holder.inventory = inventory;

        boolean joined = manager.isMember(player.getUniqueId());

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        inventory.setItem(10, createItem(Material.RED_BANNER, KingdomType.RED, NamedTextColor.RED, joined));
        inventory.setItem(13, createItem(Material.BLUE_BANNER, KingdomType.BLUE, NamedTextColor.BLUE, joined));
        inventory.setItem(16, createItem(Material.GREEN_BANNER, KingdomType.GREEN, NamedTextColor.GREEN, joined));

        player.openInventory(inventory);
    }

    private ItemStack createItem(Material material, KingdomType type, NamedTextColor color, boolean joined) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(text(type.getDisplayName(), color));
        meta.lore(List.of(
                text("Members: " + manager.getMemberCount(type), NamedTextColor.GRAY),
                text(joined ? "You are already in a kingdom" : "Click to join", NamedTextColor.YELLOW)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private Component text(String message, NamedTextColor color) {
        return Component.text(message, color).decoration(TextDecoration.ITALIC, false);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        KingdomType type = switch (event.getSlot()) {
            case 10 -> KingdomType.RED;
            case 13 -> KingdomType.BLUE;
            case 16 -> KingdomType.GREEN;
            default -> null;
        };
        if (type == null) return;

        player.closeInventory();
        if (manager.isMember(player.getUniqueId())) {
            player.sendMessage(Component.text("You are already in a kingdom."));
            return;
        }
        manager.join(player.getUniqueId(), type);
        player.sendMessage(Component.text("You joined the " + type.getDisplayName() + "!"));
    }
            }
