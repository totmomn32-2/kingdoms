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

    public KingdomCommand(KingdomManager manager, KingdomMenu menu, QuestMenu questMenu) {
        this.manager = manager;
        this.menu = menu;
        this.questMenu = questMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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

        if (args[0].equalsIgnoreCase("upgrades") || args[0].equalsIgnoreCase("upgrade")) {
            Kingdom kingdom = manager.getKingdomData(player.getUniqueId());
            if (kingdom == null) {
                player.sendMessage(Component.text("Join a kingdom first. Use /kingdom"));
                return true;
            }

            if (args.length < 2) {
                int level = kingdom.getUpgradeLevel("health");
                player.sendMessage(Component.text("=== Upgrades ==="));
                if (level >= Kingdom.HEALTH_MAX_LEVEL) {
                    player.sendMessage(Component.text("Health: " + level + "/" + Kingdom.HEALTH_MAX_LEVEL + " (MAX)"));
                } else {
                    player.sendMessage(Component.text("Health: " + level + "/" + Kingdom.HEALTH_MAX_LEVEL
                            + " - next costs " + kingdom.getHealthUpgradeCost() + " coins"));
                }
                player.sendMessage(Component.text("Buy with: /kingdom upgrade health"));
                return true;
            }

            if (args[1].equalsIgnoreCase("health")) {
                int level = kingdom.getUpgradeLevel("health");
                if (level >= Kingdom.HEALTH_MAX_LEVEL) {
                    player.sendMessage(Component.text("Health upgrade is already at max level."));
                    return true;
                }
                long cost = kingdom.getHealthUpgradeCost();
                if (!kingdom.spendCoins(cost)) {
                    player.sendMessage(Component.text("Not enough coins. You need " + cost
                            + ", your kingdom has " + kingdom.getCoins() + "."));
                    return true;
                }
                kingdom.setUpgradeLevel("health", level + 1);
                manager.saveKingdoms();
                manager.applyUpgradesToKingdom(kingdom.getType());
                player.sendMessage(Component.text("Health upgraded to level " + (level + 1) + "! Everyone gets +1 heart."));
                return true;
            }

            player.sendMessage(Component.text("Unknown upgrade. Try: /kingdom upgrade health"));
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
                                                    }
