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

public class UpgradeMenu implements Listener {

    private static final int[] SLOTS = {11, 12, 13, 14, 15};

    private static class MenuHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private final KingdomManager manager;

    public UpgradeMenu(KingdomManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {
        Kingdom kingdom = manager.getKingdomData(player.getUniqueId());
        if (kingdom == null) {
            player.sendMessage(Component.text("Join a kingdom first. Use /kingdom"));
            return;
        }

        MenuHolder holder = new MenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, Component.text("Kingdom Upgrades"));
        holder.inventory = inventory;

        refresh(inventory, kingdom);
        player.openInventory(inventory);
    }

    private void refresh(Inventory inventory, Kingdom kingdom) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        for (int i = 0; i < Upgrades.ALL.size() && i < SLOTS.length; i++) {
            inventory.setItem(SLOTS[i], createItem(Upgrades.ALL.get(i), kingdom));
        }
    }

    private ItemStack createItem(Upgrade upgrade, Kingdom kingdom) {
        int level = kingdom.getUpgradeLevel(upgrade.id());
        boolean max = level >= upgrade.maxLevel();

        ItemStack item = new ItemStack(upgrade.icon());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(text(upgrade.name(), NamedTextColor.YELLOW));
        meta.lore(List.of(
                text(upgrade.description(), NamedTextColor.GRAY),
                text("Level: " + level + "/" + upgrade.maxLevel(), NamedTextColor.WHITE),
                text("", NamedTextColor.GRAY),
                max
                        ? text("MAX LEVEL", NamedTextColor.GREEN)
                        : text("Cost: " + upgrade.costForNext(level) + " coins", NamedTextColor.GOLD),
                text("Kingdom coins: " + kingdom.getCoins(), NamedTextColor.GRAY),
                max ? text("", NamedTextColor.GRAY) : text("Click to buy", NamedTextColor.YELLOW)
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

        int index = -1;
        for (int i = 0; i < SLOTS.length; i++) {
            if (SLOTS[i] == event.getSlot()) index = i;
        }
        if (index < 0 || index >= Upgrades.ALL.size()) return;

        Upgrade upgrade = Upgrades.ALL.get(index);
        Kingdom kingdom = manager.getKingdomData(player.getUniqueId());
        if (kingdom == null) return;

        int level = kingdom.getUpgradeLevel(upgrade.id());
        if (level >= upgrade.maxLevel()) {
            player.sendMessage(Component.text(upgrade.name() + " is already at max level."));
            return;
        }

        long cost = upgrade.costForNext(level);
        if (!kingdom.spendCoins(cost)) {
            player.sendMessage(Component.text("Not enough coins. You need " + cost
                    + ", your kingdom has " + kingdom.getCoins() + "."));
            return;
        }

        kingdom.setUpgradeLevel(upgrade.id(), level + 1);
        manager.saveKingdoms();
        manager.applyUpgradesToKingdom(kingdom.getType());
        player.sendMessage(Component.text(upgrade.name() + " upgraded to level " + (level + 1) + "!"));
        refresh(event.getInventory(), kingdom);
    }
}
