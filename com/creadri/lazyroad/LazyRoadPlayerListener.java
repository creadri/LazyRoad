package com.creadri.lazyroad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Handle events for all Player related events
 * @author creadri
 */
public class LazyRoadPlayerListener extends PlayerListener {

    private final LazyRoad plugin;
    private HashMap<String, RoadEnabled> builders;
    private HashMap<String, Stack<Undo>> undoers;

    /**
     * 
     * @param instance
     */
    public LazyRoadPlayerListener(LazyRoad instance) {
        plugin = instance;
        builders = new HashMap<String, RoadEnabled>();
        undoers = new HashMap<String, Stack<Undo>>();
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {

        if (event.isCancelled()) {
            return;
        }

        RoadEnabled road = builders.get(event.getPlayer().getName());

        if (road == null) {
            return;
        }

        road.drawRoad(event.getPlayer(),event.getPlayer().getLocation());
    }

    @Override
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
        
        LazyRoad.messages.sendPlayerMessage(event.getPlayer(), "messages.teleported");
    }
    

    public boolean addBuilder(String player, RoadEnabled road) {
        if (builders.containsKey(player)) {
            return false;
        }

        builders.put(player, road);
        return true;
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
            LazyRoad.log.warning("[LazyRoad] Unable to save undo file");
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
            
            Iterator <Stack<Undo>> it = undoers.values().iterator();
            while (it.hasNext()) {
                Iterator<Undo> itUndo = it.next().iterator();
                while (itUndo.hasNext()) {
                    Undo undo = itUndo.next();
                    undo.setWorld(plugin.getServer().getWorld(undo.getsWorld()));
                }
            }
            
            
        } catch (Exception ex) {
            LazyRoad.log.warning("[LazyRoad] Unable to load undo file");
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