package com.creadri.lazyroad;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Handle events for all Player related events
 *
 * @author creadri
 */
public class LazyRoadPlayerListener implements Listener {

    private final LazyRoad plugin;
    private HashMap<String, RoadEnabled> builders;
    private HashMap<String, Stack<Undo>> undoers;
    private FilenameFilter filenameFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".ser");
        }
    };

    /**
     *
     * @param instance
     */
    public LazyRoadPlayerListener(LazyRoad instance) {
        plugin = instance;
        builders = new HashMap<String, RoadEnabled>();
        undoers = new HashMap<String, Stack<Undo>>();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {

        if (event.isCancelled()) {
            return;
        }

        RoadEnabled road = builders.get(event.getPlayer().getName());

        if (road == null) {
            return;
        }

        road.drawRoad(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {

        String player = event.getPlayer().getName();

        RoadEnabled road = builders.get(player);

        if (road == null) {
            return;
        }

        if (road.getWorld().equals(event.getPlayer().getWorld())) {
            return;
        }

        removeBuilder(player);

        //LazyRoad.messages.sendPlayerMessage(event.getPlayer(), "messages.teleported");
        event.getPlayer().sendMessage(plugin.getMessage("messages.teleported"));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        File pl = null;
        File MinerFolder = new File(plugin.getDataFolder(), "miners");
        try {
            if (!MinerFolder.mkdir()) {
                File[] pls = MinerFolder.listFiles(filenameFilter);
                if (pls.length > 0) {
                    for (File file : pls) {
                        if (file.getName().equalsIgnoreCase(player.getName().toLowerCase() + ".ser")) {
                            pl = file;
                            break;
                        }
                    }

                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(pl));

                    Object raw = ois.readObject();
                    if (raw instanceof Map) {
                        MinerData minerData = MinerData.deserialize((Map<String, Object>) raw);
                        if (!player.hasPermission("lazyroad.lazyminer")) {
                            minerData.setEnabled(false);
                        }
                        plugin.putLazyMiner(player.getName(), new LazyMiner(plugin, player, minerData));
                    }
                    ois.close();

                }
            }
        } catch (IOException iOException) {
            plugin.log.warning("[LazyRoad] An error occured while opening the Miner file " + event.getPlayer().getName() + ".ser !");
        } catch (ClassNotFoundException ex) {
            plugin.log.warning("[LazyRoad] An error occured while parsing the Miner file " + event.getPlayer().getName() + ".ser !");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent event) {
        plugin.removeMiner(event.getPlayer().getName());
    }

    public boolean addBuilder(String player, RoadEnabled road) {
        if (builders.containsKey(player)) {
            return false;
        }

        builders.put(player, road);
        return true;
    }

    public RoadEnabled setForceUp(String player) {
        RoadEnabled re;
        if (builders.containsKey(player)) {
            re = builders.get(player);
            re.setForceUp(true);
            re.setForceDown(false);
        } else {
            re = null;
        }
        return re;
    }

    public RoadEnabled setForceDown(String player) {
        RoadEnabled re;
        if (builders.containsKey(player)) {
            re = builders.get(player);
            re.setForceUp(false);
            re.setForceDown(true);
        } else {
            re = null;
        }
        return re;
    }

    public RoadEnabled setNormal(String player) {
        RoadEnabled re;
        if (builders.containsKey(player)) {
            re = builders.get(player);
            re.setForceUp(false);
            re.setForceDown(false);
        } else {
            re = null;
        }
        return re;
    }

    public RoadEnabled removeBuilder(String player) {
        RoadEnabled re = builders.remove(player);
        if (re != null) {
            if (!undoers.containsKey(player)) {
                undoers.put(player, new Stack<Undo>());
            }
            undoers.get(player).push(re.getUndo());
        }
        return re;
    }

    public boolean undo(String player) {
        RoadEnabled re = builders.get(player);
        if (re != null) {
            re.undo();
            return true;

        } else {

            //get undo stack
            Stack<Undo> stack = undoers.get(player);
            if (stack == null || stack.empty()) {
                return false;
            }

            stack.pop().undo();
            return true;
        }
    }

    public void serializeRoadsUndos(File file) {

        // saving the builders
        Iterator<String> itPlayers = ((HashMap<String, RoadEnabled>) builders.clone()).keySet().iterator();
        while (itPlayers.hasNext()) {
            removeBuilder(itPlayers.next());
        }

        ObjectOutputStream oos = null;
        try {
            file.createNewFile();

            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(undoers);
        } catch (IOException ex) {
            LazyRoad.log.warning("[LazyRoad] Unable to save undo file.");
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public void unSerializeRoadsUndos(File file) {

        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            undoers = (HashMap<String, Stack<Undo>>) ois.readObject();
            ois.close();

            Iterator<Stack<Undo>> it = undoers.values().iterator();
            while (it.hasNext()) {
                Iterator<Undo> itUndo = it.next().iterator();
                while (itUndo.hasNext()) {
                    Undo undo = itUndo.next();
                    undo.setWorld(plugin.getServer().getWorld(undo.getsWorld()));
                }
            }


        } catch (Exception ex) {
            LazyRoad.log.warning("[LazyRoad] Unable to load undo file. Only worry if this happens after you've build a road.");
            undoers = new HashMap<String, Stack<Undo>>();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException ex) {
                }
            }
        }

    }
}
