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

    private static final int PER_PAGE = 45;
    private static final int PREV_SLOT = 45;
    private static final int INFO_SLOT = 49;
    private static final int NEXT_SLOT = 53;

    private static class MenuHolder implements InventoryHolder {
        private Inventory inventory;
        private final int page;

        MenuHolder(int page) {
            this.page = page;
        }

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
        open(player, 0);
    }

    public void open(Player player, int page) {
        Kingdom kingdom = manager.getKingdomData(player.getUniqueId());
        if (kingdom == null) {
            player.sendMessage(Component.text("Join a kingdom first. Use /kingdom"));
            return;
        }

        List<Quest> list = quests.getQuests();
        int pages = Math.max(1, (list.size() + PER_PAGE - 1) / PER_PAGE);
        int current = Math.max(0, Math.min(page, pages - 1));

        MenuHolder holder = new MenuHolder(current);
        Inventory inventory = Bukkit.createInventory(holder, 54, Component.text("Kingdom Quests"));
        holder.inventory = inventory;

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, filler);
        }

        for (int i = 0; i < PER_PAGE; i++) {
            int index = current * PER_PAGE + i;
            if (index >= list.size()) break;
            inventory.setItem(i, createItem(list.get(index), kingdom));
        }

        inventory.setItem(INFO_SLOT, simpleItem(Material.PAPER,
                "Page " + (current + 1) + "/" + pages, NamedTextColor.WHITE));
        if (current > 0) {
            inventory.setItem(PREV_SLOT, simpleItem(Material.ARROW, "Previous page", NamedTextColor.YELLOW));
        }
        if (current < pages - 1) {
            inventory.setItem(NEXT_SLOT, simpleItem(Material.ARROW, "Next page", NamedTextColor.YELLOW));
        }

        player.openInventory(inventory);
    }

    private ItemStack simpleItem(Material material, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(text(name, color));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Quest quest, Kingdom kingdom) {
        Material icon = switch (quest.type()) {
            case KILL_MOB -> Material.IRON_SWORD;
            case MINE_BLOCK -> Material.IRON_PICKAXE;
            default -> Material.IRON_HOE;
        };

        long progress = kingdom.getProgress(quest.id());
        int filled = (int) Math.min(10, progress * 10 / quest.amount());
        Component bar = text("|".repeat(filled), NamedTextColor.GREEN)
                .append(Component.text("|".repeat(10 - filled), NamedTextColor.DARK_GRAY));

        List<Component> lore = new ArrayList<>();
        lore.add(text("Progress: " + progress + "/" + quest.amount(), NamedTextColor.GRAY));
        lore.add(bar);
        if (quest.minContributors() > 1) {
            lore.add(text("Group quest: " + kingdom.getContributorCount(quest.id()) + "/"
                    + quest.minContributors() + " members contributed", NamedTextColor.AQUA));
        }
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
        if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        int slot = event.getSlot();
        if (slot == PREV_SLOT && holder.page > 0) {
            open(player, holder.page - 1);
        } else if (slot == NEXT_SLOT && quests.getQuests().size() > (holder.page + 1) * PER_PAGE) {
            open(player, holder.page + 1);
        }
    }
                                                }
