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
        private final boolean hub;

        MenuHolder(boolean hub) {
            this.hub = hub;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private final KingdomManager manager;
    private final QuestMenu questMenu;
    private final UpgradeMenu upgradeMenu;
    private final ProjectMenu projectMenu;

    public KingdomMenu(KingdomManager manager, QuestMenu questMenu, UpgradeMenu upgradeMenu, ProjectMenu projectMenu) {
        this.manager = manager;
        this.questMenu = questMenu;
        this.upgradeMenu = upgradeMenu;
        this.projectMenu = projectMenu;
    }

    public void open(Player player) {
        Kingdom kingdom = manager.getKingdomData(player.getUniqueId());
        boolean member = kingdom != null;

        MenuHolder holder = new MenuHolder(member);
        String title = member ? kingdom.getType().getDisplayName() : "Three Kingdoms";
        Inventory inventory = Bukkit.createInventory(holder, 27, Component.text(title));
        holder.inventory = inventory;

        fill(inventory);
        if (member) {
            showHub(inventory, kingdom);
        } else {
            inventory.setItem(10, joinItem(KingdomType.RED));
            inventory.setItem(13, joinItem(KingdomType.BLUE));
            inventory.setItem(16, joinItem(KingdomType.GREEN));
        }

        player.openInventory(inventory);
    }

    private void showHub(Inventory inventory, Kingdom kingdom) {
        KingdomType type = kingdom.getType();

        inventory.setItem(10, item(bannerFor(type), text(type.getDisplayName(), colorFor(type)),
                text("Level: " + kingdom.getLevel(), NamedTextColor.GRAY),
                text("Points: " + kingdom.getPoints(), NamedTextColor.GRAY),
                text("Coins: " + kingdom.getCoins(), NamedTextColor.GRAY),
                text("Core: " + kingdom.getCore(), NamedTextColor.GRAY),
                text("Members: " + manager.getMemberCount(type), NamedTextColor.GRAY)));

        inventory.setItem(12, item(Material.IRON_SWORD, text("Quests", NamedTextColor.YELLOW),
                text("See your kingdom's quests", NamedTextColor.GRAY),
                text("Click to open", NamedTextColor.YELLOW)));

        inventory.setItem(14, item(Material.GOLDEN_APPLE, text("Upgrades", NamedTextColor.YELLOW),
                text("Spend coins to make everyone stronger", NamedTextColor.GRAY),
                text("Click to open", NamedTextColor.YELLOW)));

        inventory.setItem(16, item(Material.EXPERIENCE_BOTTLE, text("Level Up", NamedTextColor.YELLOW),
                text("Cost: " + kingdom.getLevelUpCost() + " points", NamedTextColor.GOLD),
                text("Kingdom points: " + kingdom.getPoints(), NamedTextColor.GRAY),
                text("Click to level up", NamedTextColor.YELLOW)));

        inventory.setItem(22, item(Material.BEACON, text("Projects", NamedTextColor.YELLOW),
                text("Build the castle, market and more", NamedTextColor.GRAY),
                text("Click to open", NamedTextColor.YELLOW)));
    }

    private ItemStack joinItem(KingdomType type) {
        return item(bannerFor(type), text(type.getDisplayName(), colorFor(type)),
                text("Members: " + manager.getMemberCount(type), NamedTextColor.GRAY),
                text("Click to join", NamedTextColor.YELLOW));
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

    private void fill(Inventory inventory) {
        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
    }

    private ItemStack item(Material material, Component name, Component... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(List.of(lore));
        item.setItemMeta(meta);
        return item;
    }

    private Component text(String message, NamedTextColor color) {
        return Component.text(message, color).decoration(TextDecoration.ITALIC, false);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        if (holder.hub) {
            handleHubClick(player, event.getSlot(), event.getInventory());
        } else {
            handleJoinClick(player, event.getSlot());
        }
    }

    private void handleHubClick(Player player, int slot, Inventory inventory) {
        Kingdom kingdom = manager.getKingdomData(player.getUniqueId());
        if (kingdom == null) return;

        switch (slot) {
            case 12 -> questMenu.open(player);
            case 14 -> upgradeMenu.open(player);
            case 16 -> levelUp(player, kingdom, inventory);
            case 22 -> projectMenu.open(player);
            default -> {
            }
        }
    }

    private void levelUp(Player player, Kingdom kingdom, Inventory inventory) {
        long cost = kingdom.getLevelUpCost();
        if (!kingdom.spendPoints(cost)) {
            player.sendMessage(Component.text("Not enough points. You need " + cost
                    + ", your kingdom has " + kingdom.getPoints() + "."));
            return;
        }
        kingdom.levelUp();
        manager.saveKingdoms();
        player.sendMessage(Component.text("Your kingdom reached level " + kingdom.getLevel() + "!"));
        fill(inventory);
        showHub(inventory, kingdom);
    }

    private void handleJoinClick(Player player, int slot) {
        KingdomType type = switch (slot) {
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
