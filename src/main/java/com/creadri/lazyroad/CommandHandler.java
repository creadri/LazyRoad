package com.creadri.lazyroad;

import java.util.List;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @version 2.0
 * @author Faye Bickerton AKA VeraLapsa
 */
public abstract class CommandHandler {

    protected final LazyRoad plugin;
    protected static Logger log;

    public CommandHandler(LazyRoad plugin) {
        this.plugin = plugin;
        log = plugin.log;
    }

    public abstract boolean perform(CommandSender sender, String label, String[] args);

    /**
     * 
     * @param sender
     * @return returns true if sender is not a player
     */
    protected static boolean anonymousCheck(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        } else {
            return false;
        }
    }

    protected static Player getPlayer(CommandSender sender, String[] args, int index) {
        if (args.length > index) {
            List<Player> players = sender.getServer().matchPlayer(args[index]);

            if (players.isEmpty()) {
                sender.sendMessage("I don't know who '" + args[index] + "' is!");
                return null;
            } else {
                return players.get(0);
            }
        } else {
            if (anonymousCheck(sender)) {
                return null;
            } else {
                return (Player) sender;
            }
        }
    }

    protected static boolean getPermissions(CommandSender sender, String node) {
        Player player = (Player) sender;
        return player.hasPermission(node);
    }
}
