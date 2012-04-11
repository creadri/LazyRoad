/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creadri.lazyroad.commands;

import com.creadri.lazyroad.CommandHandler;
import com.creadri.lazyroad.LazyMiner;
import com.creadri.lazyroad.LazyRoad;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Faye
 */
public class LazyMinerCommand extends CommandHandler {

    public LazyMinerCommand(LazyRoad plugin) {
        super(plugin);
    }

    @Override
    public boolean perform(CommandSender sender, String label, String[] args) {
        if (anonymousCheck(sender)) {
            return true;
        }
        Player player = (Player) sender;
        if (!getPermissions(sender, "lazyroad.lazyminer")) {
            player.sendMessage(plugin.getMessage("messages.noPermission"));
        }
        String playerName = player.getName();
        LazyMiner lm = plugin.getLazyMiner(playerName);

        switch (args.length) {
            case 0:
                if (lm != null) {
                    if (lm.enabled()) {
                        lm.disable();
                        player.sendMessage(plugin.getMessage("messages.lazyminer.disable"));
                    } else {
                        lm.enable();
                        player.sendMessage(plugin.getMessage("messages.lazyminer.enable"));
                    }
                } else {
                    lm = new LazyMiner(plugin, player);
                    lm.enable();
                    plugin.putLazyMiner(player.getName(), lm);
                    lm.saveMinerData();
                    player.sendMessage(plugin.getMessage("messages.lazyminer.enable"));
                }
                return true;
            case 1:
                if (args[0].equalsIgnoreCase("store")) {
                    if (lm != null) {
                        lm.putBlocks();
                        return true;
                    } else {
                        player.sendMessage(plugin.getMessage("messages.lazyminer.drops"));
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("ids")) {
                    if (lm != null) {
                        player.sendMessage(plugin.getMessage("messages.lazyminer.ids", lm.checkIdsToString()));
                        return true;
                    } else {
                        player.sendMessage(plugin.getMessage("messages.lazyminer.drops"));
                        return true;
                    }
                }
            case 2:
                if (args[0].equalsIgnoreCase("addid")) {
                    if (lm != null) {
                        int id;
                        try {
                            id = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "That not a number I can use.");
                            return true;
                        }
                        lm.addCheckID(id);
                        lm.saveMinerData();
                        player.sendMessage(plugin.getMessage("messages.lazyminer.addid", id));
                        return true;
                    } else {
                        player.sendMessage(plugin.getMessage("messages.lazyminer.drops"));
                        return true;
                    }

                } else if (args[0].equalsIgnoreCase("removeid")) {
                    if (lm != null) {
                        int id;
                        try {
                            id = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "That not a number I can use.");
                            return true;
                        }
                        if (lm.removeCheckId(id)) {
                            lm.saveMinerData();
                            player.sendMessage(plugin.getMessage("messages.lazyminer.removeid", id));
                            return true;
                        } else {
                            player.sendMessage(plugin.getMessage("messages.lazyminer.cantremove", id));
                            return true;
                        }
                    } else {
                        player.sendMessage(plugin.getMessage("messages.lazyminer.drops"));
                        return true;
                    }
                }
            default:
                return false;
        }
    }
}
