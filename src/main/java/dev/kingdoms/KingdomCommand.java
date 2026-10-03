package dev.kingdoms;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KingdomCommand implements CommandExecutor {

    private final KingdomManager manager;
    private final KingdomMenu menu;
    private final QuestMenu questMenu;
    private final UpgradeMenu upgradeMenu;
    private final QuestManager questManager;

    public KingdomCommand(KingdomManager manager, KingdomMenu menu, QuestMenu questMenu,
                          UpgradeMenu upgradeMenu, QuestManager questManager) {
        this.manager = manager;
        this.menu = menu;
        this.questMenu = questMenu;
        this.upgradeMenu = upgradeMenu;
        this.questManager = questManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            return handleAdmin(sender, args);
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command."));
            return true;
        }

        if (args.length == 0) {
            menu.open(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("join")) {
            if (args.length < 2) {
                player.sendMessage(Component.text("Usage: /kingdom join <red|blue|green>"));
                return true;
            }
            KingdomType type = KingdomType.fromString(args[1]);
            if (type == null) {
                player.sendMessage(Component.text("Unknown kingdom. Choose red, blue or green."));
                return true;
            }
            if (manager.isMember(player.getUniqueId())) {
                player.sendMessage(Component.text("You are already in a kingdom."));
                return true;
            }
            manager.join(player.getUniqueId(), type);
            player.sendMessage(Component.text("You joined the " + type.getDisplayName() + "!"));
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            Kingdom kingdom = manager.getKingdomData(player.getUniqueId());
            if (kingdom == null) {
                player.sendMessage(Component.text("You are not in a kingdom yet. Use /kingdom join <red|blue|green>"));
            } else {
                player.sendMessage(Component.text("=== " + kingdom.getType().getDisplayName() + " ==="));
                player.sendMessage(Component.text("Level: " + kingdom.getLevel()));
                player.sendMessage(Component.text("Points: " + kingdom.getPoints()));
                player.sendMessage(Component.text("Coins: " + kingdom.getCoins()));
                player.sendMessage(Component.text("Core: " + kingdom.getCore()));
                player.sendMessage(Component.text("Members: " + manager.getMemberCount(kingdom.getType())));
                player.sendMessage(Component.text("Next level costs: " + kingdom.getLevelUpCost() + " points"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("quests")) {
            questMenu.open(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("upgrades") || args[0].equalsIgnoreCase("upgrade")) {
            upgradeMenu.open(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("top")) {
            List<Kingdom> ranking = new ArrayList<>(manager.getAllKingdoms());
            ranking.sort(Comparator.comparingInt(Kingdom::getLevel)
                    .thenComparingLong(Kingdom::getPoints)
                    .reversed());

            player.sendMessage(Component.text("=== Kingdom Ranking ==="));
            for (int i = 0; i < ranking.size(); i++) {
                Kingdom k = ranking.get(i);
                player.sendMessage(Component.text("#" + (i + 1) + " " + k.getType().getDisplayName()
                        + " - Level " + k.getLevel() + " (" + k.getPoints() + " points)"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("levelup")) {
            Kingdom kingdom = manager.getKingdomData(player.getUniqueId());
            if (kingdom == null) {
                player.sendMessage(Component.text("Join a kingdom first. Use /kingdom"));
                return true;
            }
            long cost = kingdom.getLevelUpCost();
            if (!kingdom.spendPoints(cost)) {
                player.sendMessage(Component.text("Not enough points. You need " + cost
                        + ", your kingdom has " + kingdom.getPoints() + "."));
                return true;
            }
            kingdom.levelUp();
            manager.saveKingdoms();
            player.sendMessage(Component.text("Your kingdom reached level " + kingdom.getLevel() + "!"));
            return true;
        }

        // TEMPORARY command for testing, we will remove it later.
        if (args[0].equalsIgnoreCase("test")) {
            if (!player.isOp()) {
                player.sendMessage(Component.text("Only operators can use this."));
                return true;
            }
            Kingdom kingdom = manager.getKingdomData(player.getUniqueId());
            if (kingdom == null) {
                player.sendMessage(Component.text("Join a kingdom first."));
                return true;
            }
            kingdom.addPoints(100);
            kingdom.addCoins(50);
            player.sendMessage(Component.text("Added 100 points and 50 coins."));
            return true;
        }

        return true;
    }

    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Component.text("Only operators can use this."));
            return true;
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("reload")) {
            questManager.load();
            sender.sendMessage(Component.text("Quests reloaded: " + questManager.getQuests().size() + " quests."));
            return true;
        }

        sender.sendMessage(Component.text("Usage: /kingdom admin reload"));
        return true;
    }
}
