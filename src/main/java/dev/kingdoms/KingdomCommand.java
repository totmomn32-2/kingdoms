package dev.kingdoms;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class KingdomCommand implements CommandExecutor {

    private final KingdomManager manager;
    private final KingdomMenu menu;
    private final QuestMenu questMenu;
    private final UpgradeMenu upgradeMenu;
    private final QuestManager questManager;
    private final EventManager eventManager;
    private final ZoneManager zoneManager;

    public KingdomCommand(KingdomManager manager, KingdomMenu menu, QuestMenu questMenu,
                          UpgradeMenu upgradeMenu, QuestManager questManager,
                          EventManager eventManager, ZoneManager zoneManager) {
        this.manager = manager;
        this.menu = menu;
        this.questMenu = questMenu;
        this.upgradeMenu = upgradeMenu;
        this.questManager = questManager;
        this.eventManager = eventManager;
        this.zoneManager = zoneManager;
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
                if (eventManager.isActive()) {
                    player.sendMessage(Component.text("EVENT: rewards x" + eventManager.getMultiplier()
                            + " (" + eventManager.getMinutesLeft() + " min left)"));
                }
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

        if (args.length >= 2 && args[1].equalsIgnoreCase("event")) {
            return handleEvent(sender, args);
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("zone")) {
            return handleZone(sender, args);
        }

        sender.sendMessage(Component.text("Admin commands:"));
        sender.sendMessage(Component.text("/kingdom admin reload"));
        sender.sendMessage(Component.text("/kingdom admin give <red|blue|green> <points|coins|core> <amount>"));
        sender.sendMessage(Component.text("/kingdom admin kick <player>"));
        sender.sendMessage(Component.text("/kingdom admin event <multiplier> <minutes>"));
        sender.sendMessage(Component.text("/kingdom admin event stop"));
        sender.sendMessage(Component.text("/kingdom admin zone create <id> <radius> <name>"));
        sender.sendMessage(Component.text("/kingdom admin zone remove <id>"));
        sender.sendMessage(Component.text("/kingdom admin zone list"));
        return true;
    }

    private boolean handleZone(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /kingdom admin zone <create|remove|list>"));
            return true;
        }

        String sub = args[2].toLowerCase(Locale.ROOT);

        if (sub.equals("list")) {
            if (zoneManager.getZones().isEmpty()) {
                sender.sendMessage(Component.text("There are no zones yet."));
                return true;
            }
            sender.sendMessage(Component.text("=== Zones ==="));
            for (Zone zone : zoneManager.getZones()) {
                String owner = zone.getOwner() == null ? "nobody" : zone.getOwner().getDisplayName();
                sender.sendMessage(Component.text(zone.getId() + " - " + zone.getName()
                        + " (radius " + (int) zone.getRadius() + ", owner: " + owner + ")"));
            }
            return true;
        }

        if (sub.equals("remove")) {
            if (args.length < 4) {
                sender.sendMessage(Component.text("Usage: /kingdom admin zone remove <id>"));
                return true;
            }
            if (zoneManager.remove(args[3])) {
                sender.sendMessage(Component.text("Zone removed."));
            } else {
                sender.sendMessage(Component.text("No zone with that id."));
            }
            return true;
        }

        if (sub.equals("create")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Only players can create zones (it uses your location)."));
                return true;
            }
            if (args.length < 6) {
                sender.sendMessage(Component.text("Usage: /kingdom admin zone create <id> <radius> <name>"));
                return true;
            }

            double radius;
            try {
                radius = Double.parseDouble(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("The radius must be a number."));
                return true;
            }
            if (radius < 3 || radius > 100) {
                sender.sendMessage(Component.text("The radius must be between 3 and 100."));
                return true;
            }

            String name = String.join(" ", Arrays.copyOfRange(args, 5, args.length));
            if (zoneManager.create(args[3], name, player.getLocation(), radius)) {
                sender.sendMessage(Component.text("Zone '" + name + "' created here with radius " + (int) radius + "."));
            } else {
                sender.sendMessage(Component.text("A zone with that id already exists."));
            }
            return true;
        }

        sender.sendMessage(Component.text("Usage: /kingdom admin zone <create|remove|list>"));
        return true;
    }

    private boolean handleEvent(CommandSender sender, String[] args) {
        if (args.length >= 3 && args[2].equalsIgnoreCase("stop")) {
            eventManager.stop();
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(Component.text("Usage: /kingdom admin event <multiplier> <minutes>"));
            return true;
        }

        double multiplier;
        int minutes;
        try {
            multiplier = Double.parseDouble(args[2]);
            minutes = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Multiplier and minutes must be numbers."));
            return true;
        }

        if (multiplier <= 1.0 || multiplier > 10.0 || minutes < 1 || minutes > 1440) {
            sender.sendMessage(Component.text("Multiplier must be between 1 and 10, minutes between 1 and 1440."));
            return true;
        }

        eventManager.start(multiplier, minutes);
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
