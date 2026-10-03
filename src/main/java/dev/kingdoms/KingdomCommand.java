package dev.kingdoms;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

        if (args.length >= 2 && args[1].equalsIgnoreCase("give")) {
            return handleGive(sender, args);
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("kick")) {
            return handleKick(sender, args);
        }

        sender.sendMessage(Component.text("Admin commands:"));
        sender.sendMessage(Component.text("/kingdom admin reload"));
        sender.sendMessage(Component.text("/kingdom admin give <red|blue|green> <points|coins|core> <amount>"));
        sender.sendMessage(Component.text("/kingdom admin kick <player>"));
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(Component.text("Usage: /kingdom admin give <red|blue|green> <points|coins|core> <amount>"));
            return true;
        }

        KingdomType type = KingdomType.fromString(args[2]);
        if (type == null) {
            sender.sendMessage(Component.text("Unknown kingdom. Choose red, blue or green."));
            return true;
        }

        long amount;
        try {
            amount = Long.parseLong(args[4]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("The amount must be a number."));
            return true;
        }

        Kingdom kingdom = manager.getKingdomData(type);
        String what = args[3].toLowerCase(Locale.ROOT);
        switch (what) {
            case "points" -> kingdom.addPoints(amount);
            case "coins" -> kingdom.addCoins(amount);
            case "core" -> kingdom.addCore(amount);
            default -> {
                sender.sendMessage(Component.text("Choose points, coins or core."));
                return true;
            }
        }

        manager.saveKingdoms();
        sender.sendMessage(Component.text("Gave " + amount + " " + what + " to the " + type.getDisplayName() + "."));
        return true;
    }

    private boolean handleKick(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /kingdom admin kick <player>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[2]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found (they must have joined the server before)."));
            return true;
        }

        if (manager.leave(target.getUniqueId())) {
            sender.sendMessage(Component.text(args[2] + " was removed from their kingdom."));
        } else {
            sender.sendMessage(Component.text(args[2] + " is not in a kingdom."));
        }
        return true;
    }
}
