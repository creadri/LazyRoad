/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creadri.lazyroad.commands;

import com.creadri.lazyroad.*;
import com.creadri.util.ColumnChat;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Faye
 */
public class roadCommand extends CommandHandler {

    public roadCommand(LazyRoad plugin) {
        super(plugin);
    }

    @Override
    public boolean perform(CommandSender sender, String label, String[] args) {
        if (anonymousCheck(sender)) {
            return true;
        }
        Player player = (Player) sender;
        String playerName = player.getName();
        boolean tunnel = label.equalsIgnoreCase("tunnel") ? true : false;
        boolean bridge = label.equalsIgnoreCase("bridge") ? true : false;
        LazyMiner lm = plugin.getLazyMiner(playerName);

        switch (args.length) {
            case 1:
                if (getPermissions(sender, "lazyroad.stop") && args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("end")) {
                    /**
                     * SUB-COMMAND STOP
                     */
                    RoadEnabled re = plugin.getPlayerListener().removeBuilder(playerName);
                    if (re != null) {
                        player.sendMessage(plugin.getMessage("messages.buildStop", re.getCount()));
                    }
                    if (lm != null) {
                        if (lm.enabled()) {
                            player.sendMessage(plugin.getMessage("messages.lazyminer.stop", lm.size()));
                            lm.saveMinerData();
                        }
                    }
                    return true;
                } else if (getPermissions(sender, "lazyroad.up") && args[0].equalsIgnoreCase("up")) {
                    /**
                     * SUB-COMMAND up
                     */
                    RoadEnabled re = plugin.getPlayerListener().setForceUp(playerName);
                    if (re != null) {
                        player.sendMessage(plugin.getMessage("messages.forceUp"));
                    }
                    return true;
                } else if (getPermissions(sender, "lazyroad.down") && args[0].equalsIgnoreCase("down")) {
                    /**
                     * SUB-COMMAND down
                     */
                    RoadEnabled re = plugin.getPlayerListener().setForceDown(playerName);
                    if (re != null) {
                        player.sendMessage(plugin.getMessage("messages.forceDown"));
                    }
                    return true;
                } else if (getPermissions(sender, "lazyroad.normal") && args[0].equalsIgnoreCase("normal")) {
                    /**
                     * SUB-COMMAND normal
                     */
                    RoadEnabled re = plugin.getPlayerListener().setNormal(playerName);
                    if (re != null) {
                        player.sendMessage(plugin.getMessage("messages.normal"));
                    }
                    return true;
                } else if (getPermissions(sender, "lazyroad.reload") && args[0].equalsIgnoreCase("reload")) {
                    /**
                     * SUB-COMMAND RELOAD
                     */
                    try {
                        plugin.loadRoads();
                        plugin.loadPillars();
                    } catch (IOException ex) {
                        player.sendMessage(plugin.getMessage("messages.ioError"));
                    }
                    player.sendMessage(plugin.getMessage("messages.reload"));
                    return true;

                } else if (getPermissions(sender, "lazyroad.undo") && args[0].equalsIgnoreCase("undo")) {
                    /**
                     * SUB-COMMAND UNDO
                     */
                    if (plugin.getPlayerListener().undo(playerName)) {
                        //messages.sendPlayerMessage(player, "messages.undo");
                        player.sendMessage(plugin.getMessage("messages.undo"));
                    } else {
                        //messages.sendPlayerMessage(player, "messages.undoError");
                        player.sendMessage(plugin.getMessage("messages.undoError"));
                    }
                    return true;

                } else if (getPermissions(sender, "lazyroad.straight") && args[0].equalsIgnoreCase("straight")) {
                    /**
                     * SUB-COMMAND STRAIGHT
                     */
                    if (plugin.getPlayerPropStraight(playerName)) {
                        plugin.setPlayerPropStraight(playerName, false);
                        player.sendMessage(plugin.getMessage("messages.straightEnabled"));
                    } else {
                        plugin.setPlayerPropStraight(playerName, true);
                        player.sendMessage(plugin.getMessage("messages.straightDisabled"));
                    }
                    return true;
                } else {
                    try {
                        // if the arg is a number show that help page
                        int page = Integer.parseInt(args[0]);
                        sendRoadPillarMessages(player, page);
                        return true;
                    } catch (NumberFormatException ex) {
                        /**
                         * Else it's a Build Road or Tunnel command
                         */
                        if (getPermissions(sender, "lazyroad.build") || getPermissions(sender, "lazyroad.road." + args[0].toLowerCase())) {
                            Road road = plugin.getRoads().get(args[0]);
                            if (road == null) {
                                player.sendMessage(plugin.getMessage("messages.noRoad"));
                                return true;
                            }
                            RoadEnabled re = new RoadEnabled(player, road, plugin);
                            int count = 1;
                            re.setCount(count - 1);
                            /**
                             * Handle if Tunnel
                             */
                            if (tunnel) {

                                re.setTunnel(true);
                            }
                            if (plugin.getPlayerPropStraight(playerName)) {
                                re.setStraight(false);
                            }

                            if (plugin.getPlayerListener().addBuilder(playerName, re)) {
                                player.sendMessage(plugin.getMessage("messages.beginBuilding"));
                            } else {
                                player.sendMessage(plugin.getMessage("messages.alreadyBuilding"));
                            }
                            return true;
                        } else {
                            player.sendMessage(plugin.getMessage("messages.noPermission"));
                            return true;
                        }
                    }
                }
            case 2:
                if (bridge) {
                    if (getPermissions(sender, "lazyroad.build") || (getPermissions(sender, "lazyroad.road." + args[0]) && getPermissions(sender, "lazyroad.pillar." + args[1]))) {
                        Road road = plugin.getRoads().get(args[0]);
                        if (road == null) {
                            player.sendMessage(plugin.getMessage("messages.noRoad"));
                            return true;
                        }

                        Pillar pillar = plugin.getPillars().get(args[1]);
                        if (pillar == null) {
                            player.sendMessage(plugin.getMessage("messages.noPillar"));
                            return true;
                        }

                        RoadEnabled re = new RoadEnabled(player, road, plugin);
                        int count = 1;
                        re.setCount(count - 1);
                        re.setTunnel(true);
                        re.setPillar(pillar);

                        if (plugin.getPlayerPropStraight(playerName)) {
                            re.setStraight(false);
                        }

                        if (plugin.getPlayerListener().addBuilder(playerName, re)) {
                            player.sendMessage(plugin.getMessage("messages.beginBuilding"));
                        } else {
                            player.sendMessage(plugin.getMessage("messages.alreadyBuilding"));
                        }
                        return true;
                    } else {
                        player.sendMessage(plugin.getMessage("messages.noPermission"));
                        return true;
                    }
                } else {
                    /**
                     * Else it's a Build Road or Tunnel command
                     */
                    if (getPermissions(sender, "lazyroad.build") || getPermissions(sender, "lazyroad.road." + args[0])) {
                        Road road = plugin.getRoads().get(args[0]);
                        if (road == null) {
                            player.sendMessage(plugin.getMessage("messages.noRoad"));
                            return true;
                        }
                        RoadEnabled re = new RoadEnabled(player, road, plugin);
                        int count = 1;
                        try {
                            count = Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex) {
                            count = 1;
                        }
                        re.setCount(count - 1);

                        /**
                         * Handle if Tunnel
                         */
                        if (tunnel) {
                            re.setTunnel(true);
                        }
                        if (plugin.getPlayerPropStraight(playerName)) {
                            re.setStraight(false);
                        }

                        if (plugin.getPlayerListener().addBuilder(playerName, re)) {
                            player.sendMessage(plugin.getMessage("messages.beginBuilding"));
                        } else {
                            player.sendMessage(plugin.getMessage("messages.alreadyBuilding"));
                        }
                        return true;
                    } else {
                        player.sendMessage(plugin.getMessage("messages.noPermission"));
                        return true;
                    }
                }
            case 3:
                if (bridge) {
                    if (getPermissions(sender, "lazyroad.build") || (getPermissions(sender, "lazyroad.road." + args[0]) && getPermissions(sender, "lazyroad.pillar." + args[1]))) {
                        Road road = plugin.getRoads().get(args[0]);
                        if (road == null) {
                            player.sendMessage(plugin.getMessage("messages.noRoad"));
                            return true;
                        }

                        Pillar pillar = plugin.getPillars().get(args[1]);
                        if (pillar == null) {
                            player.sendMessage(plugin.getMessage("messages.noPillar"));
                            return true;
                        }

                        RoadEnabled re = new RoadEnabled(player, road, plugin);
                        int count = 1;
                        try {
                            count = Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex) {
                            count = 1;
                        }
                        re.setCount(count - 1);
                        re.setTunnel(true);
                        re.setPillar(pillar);

                        if (plugin.getPlayerPropStraight(playerName)) {
                            re.setStraight(false);
                        }

                        if (plugin.getPlayerListener().addBuilder(playerName, re)) {
                            player.sendMessage(plugin.getMessage("messages.beginBuilding"));
                        } else {
                            player.sendMessage(plugin.getMessage("messages.alreadyBuilding"));
                        }
                        return true;
                    } else {
                        player.sendMessage(plugin.getMessage("messages.noPermission"));
                        return true;
                    }
                }
            case 0:
            default:
                sendRoadPillarMessages(player, 0);
                return true;
        }
    }

    private void sendRoadPillarMessages(Player player, int page) {
        HashMap<String, Road> roads = plugin.getRoads();
        HashMap<String, Pillar> pillars = plugin.getPillars();

        int pages = Math.max(roads.size(), pillars.size()) / 6;

        if (page > pages || page < 0) {

            return;
        }

        String titleColor = ChatColor.GOLD.toString();
        String valueColor = ChatColor.LIGHT_PURPLE.toString();
        String barColor = ChatColor.AQUA.toString();

        player.sendMessage(ColumnChat.getColumn(barColor, titleColor, "Roads", "Pillar"));

        Iterator<String> roadIt = roads.keySet().iterator();
        Iterator<String> pillarIt = pillars.keySet().iterator();
        // advance till the correct page
        int until = page * 6;
        while (until > 0 && roadIt.hasNext()) {
            roadIt.next();
            until--;
        }
        until = page * 6;
        while (until > 0 && pillarIt.hasNext()) {
            pillarIt.next();
            until--;
        }
        // print
        int i = 6;
        while (i > 0 && (roadIt.hasNext() || pillarIt.hasNext())) {
            String pillarName = pillarIt.hasNext() ? pillarIt.next() : "";
            String roadName = roadIt.hasNext() ? roadIt.next() : "";

            player.sendMessage(ColumnChat.getColumn(barColor, valueColor, roadName, pillarName));
            i--;
        }

        player.sendMessage("Page " + page + " of " + pages);
    }
}
