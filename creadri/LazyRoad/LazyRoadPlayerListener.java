package creadri.LazyRoad;

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

    /**
     * 
     * @param instance
     */
    public LazyRoadPlayerListener(LazyRoad instance) {
        plugin = instance;
        builders = new HashMap<Player, RoadEnabled>();
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        
        RoadEnabled road = builders.get(event.getPlayer());
        
        if (road == null) {
            return;
        }
        
        road.drawRoad(event.getPlayer());
    }
    
    public void addBuilder(Player player, RoadEnabled road) {
        builders.remove(player);
        builders.put(player, road);
    }
    
    public RoadEnabled removeBuilder(Player player) {
        return builders.remove(player);
    }
}
