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

import java.util.ArrayList;
import java.util.List;

public class ProjectMenu implements Listener {

    private static final int[] SLOTS = {10, 12, 14, 16};

    private static class MenuHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private final KingdomManager manager;

    public ProjectMenu(KingdomManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {
        Kingdom kingdom = manager.getKingdomData(player.getUniqueId());
        if (kingdom == null) {
            player.sendMessage(Component.text("Join a kingdom first. Use /kingdom"));
            return;
        }

        MenuHolder holder = new MenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, Component.text("Kingdom Projects"));
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

        for (int i = 0; i < Projects.ALL.size() && i < SLOTS.length; i++) {
            inventory.setItem(SLOTS[i], createItem(Projects.ALL.get(i), kingdom));
        }
    }

    private ItemStack createItem(Project project, Kingdom kingdom) {
        int level = kingdom.getProjectLevel(project.id());
        boolean max = level >= project.maxLevel();

        List<Component> lore = new ArrayList<>();
        lore.add(text(project.description(), NamedTextColor.GRAY));
        lore.add(text("Level: " + level + "/" + project.maxLevel(), NamedTextColor.WHITE));
        lore.add(text("", NamedTextColor.GRAY));
        if (max) {
            lore.add(text("COMPLETED", NamedTextColor.GREEN));
        } else {
            lore.add(text("Next level costs:", NamedTextColor.GRAY));
            lore.add(text(project.pointsFor(level) + " Points", NamedTextColor.GOLD));
            lore.add(text(project.coinsFor(level) + " Coins", NamedTextColor.GOLD));
            if (project.coreFor(level) > 0) {
                lore.add(text(project.coreFor(level) + " Kingdom Core", NamedTextColor.LIGHT_PURPLE));
            }
            lore.add(text("", NamedTextColor.GRAY));
            lore.add(text("Click to build", NamedTextColor.YELLOW));
        }

        ItemStack item = new ItemStack(project.icon());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(text(project.name(), NamedTextColor.YELLOW));
        meta.lore(lore);
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
        if (index < 0 || index >= Projects.ALL.size()) return;

        Project project = Projects.ALL.get(index);
        Kingdom kingdom = manager.getKingdomData(player.getUniqueId());
        if (kingdom == null) return;

        int level = kingdom.getProjectLevel(project.id());
        if (level >= project.maxLevel()) {
            player.sendMessage(Component.text(project.name() + " is already completed."));
            return;
        }

        long points = project.pointsFor(level);
        long coins = project.coinsFor(level);
        long core = project.coreFor(level);

        if (kingdom.getPoints() < points || kingdom.getCoins() < coins || kingdom.getCore() < core) {
            player.sendMessage(Component.text("Not enough resources. Needs " + points + " Points, "
                    + coins + " Coins, " + core + " Core."));
            return;
        }

        kingdom.spendPoints(points);
        kingdom.spendCoins(coins);
        kingdom.spendCore(core);
        kingdom.setProjectLevel(project.id(), level + 1);
        manager.saveKingdoms();

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (manager.getKingdom(online.getUniqueId()) == kingdom.getType()) {
                online.sendMessage(Component.text(player.getName() + " upgraded " + project.name()
                        + " to level " + (level + 1) + "!"));
            }
        }
        refresh(event.getInventory(), kingdom);
    }
          }
