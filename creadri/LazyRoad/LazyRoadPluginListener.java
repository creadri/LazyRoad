package creadri.LazyRoad;

import com.nijikokun.bukkit.Permissions.Permissions;
import java.util.logging.Level;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author creadri
 */
public class LazyRoadPluginListener extends ServerListener {

    LazyRoad plugin;
    
    /**
     * 
     * @param plugin
     */
    public LazyRoadPluginListener(LazyRoad plugin) {
        this.plugin = plugin;
    }

    /**
     * 
     * @param event
     */
    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if (!plugin.isPermissionsSet()) {
            Plugin permissions = plugin.getServer().getPluginManager().getPlugin("Permissions");
            if (permissions != null) {
                if (permissions.isEnabled()) {
                    plugin.setPermissions(((Permissions)permissions).getHandler());
                    LazyRoad.log.log(Level.INFO, "[NearestSpawn] Successfully linked with Permissions.");
                }
            }
        }
    }
}
