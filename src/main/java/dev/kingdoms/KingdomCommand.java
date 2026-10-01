package dev.kingdoms;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KingdomCommand implements CommandExecutor {

    private final KingdomManager manager;
    private final KingdomMenu menu;

    public KingdomCommand(KingdomManager manager, KingdomMenu menu) {
        this.manager = manager;
        this.menu = menu;
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
            }
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
