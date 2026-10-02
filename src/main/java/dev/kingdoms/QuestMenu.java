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

public class QuestMenu implements Listener {

    private static class MenuHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private final KingdomManager manager;
    private final QuestManager quests;

    public QuestMenu(KingdomManager manager, QuestManager quests) {
        this.manager = manager;
        this.quests = quests;
    }

    public void open(Player player) {
        Kingdom kingdom = manager.getKingdomData(player.getUniqueId());
        if (kingdom == null) {
            player.sendMessage(Component.text("Join a kingdom first. Use /kingdom"));
            return;
        }

        MenuHolder holder = new MenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, Component.text("Kingdom Quests"));
        holder.inventory = inventory;

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        List<Quest> list = quests.getQuests();
        for (int i = 0; i < list.size() && i < 7; i++) {
            inventory.setItem(10 + i, createItem(list.get(i), kingdom));
        }

        player.openInventory(inventory);
    }

    private ItemStack createItem(Quest quest, Kingdom kingdom) {
        Material icon = switch (quest.type()) {
            case KILL_MOB -> Material.IRON_SWORD;
            case MINE_BLOCK -> Material.IRON_PICKAXE;
        };

        long progress = kingdom.getProgress(quest.id());
        int filled = (int) (progress * 10 / quest.amount());
        Component bar = text("|".repeat(filled), NamedTextColor.GREEN)
                .append(Component.text("|".repeat(10 - filled), NamedTextColor.DARK_GRAY));

        List<Component> lore = new ArrayList<>();
        lore.add(text("Progress: " + progress + "/" + quest.amount(), NamedTextColor.GRAY));
        lore.add(bar);
        lore.add(text("", NamedTextColor.GRAY));
        lore.add(text("Rewards:", NamedTextColor.GRAY));
        lore.add(text("+" + quest.points() + " Points", NamedTextColor.GOLD));
        lore.add(text("+" + quest.coins() + " Coins", NamedTextColor.GOLD));
        if (quest.core() > 0) {
            lore.add(text("+" + quest.core() + " Kingdom Core", NamedTextColor.LIGHT_PURPLE));
        }

        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(text(quest.name(), NamedTextColor.YELLOW));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
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
