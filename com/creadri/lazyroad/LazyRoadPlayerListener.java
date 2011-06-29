package com.creadri.lazyroad;

import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handle events for all Player related events
 * @author creadri
 */
public class LazyRoadPlayerListener extends PlayerListener {

    private final LazyRoad plugin;
    private HashMap<Player,RoadEnabled> builders;
    private HashMap<Player,RoadEnabled> undoers;

    /**
     * 
     * @param instance
     */
    public LazyRoadPlayerListener(LazyRoad instance) {
        plugin = instance;
        builders = new HashMap<Player, RoadEnabled>();
        undoers = new HashMap<Player, RoadEnabled>();
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        
        if (event.isCancelled()) {
            return;
        }
        
        RoadEnabled road = builders.get(event.getPlayer());
        
        if (road == null) {
            return;
        }
        
        road.drawRoad(event.getPlayer());
    }
    
    public void addBuilder(Player player, RoadEnabled road) {
        undoers.remove(player);
        builders.remove(player);
        builders.put(player, road);
    }
    
    public RoadEnabled removeBuilder(Player player) {
        RoadEnabled re = builders.remove(player);
        if (re != null) {
            undoers.put(player, re);
        }
        return re;
    }
    
    public void undo(Player player) {
        RoadEnabled re = builders.get(player);
        if (re != null) {
            re.undo();
        } else {
            re = undoers.get(player);
            if (re != null) {
                re.undo();
            }
        }
    }
}
