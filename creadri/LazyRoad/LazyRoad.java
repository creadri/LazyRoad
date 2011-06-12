package creadri.LazyRoad;

import com.nijiko.permissions.PermissionHandler;
import creadri.util.Messages;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.yaml.snakeyaml.Yaml;

/**
 * @author creadri
 */
public class LazyRoad extends JavaPlugin {

    private final LazyRoadPluginListener pluginListener = new LazyRoadPluginListener(this);
    private final LazyRoadPlayerListener playerListener = new LazyRoadPlayerListener(this);
    /**
     * The Permission plugin to handle permissions.
     */
    public static PermissionHandler permissions;
    private boolean eventRegistered = false;
    private File roadsDirectory;
    private HashMap<String, Road> roads;
    /**
     * A message class that is designed to load messages to have custom
     * output.
     */
    public static Messages messages = new Messages();
    /**
     * The log of bukkit
     */
    public static final Logger log = Logger.getLogger("Minecraft");

    /**
     * method called by bukkit when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        // configuration files
        try {

            roadsDirectory = new File("./plugins/LazyRoad/roads");
            if (!roadsDirectory.exists()) {
                roadsDirectory.mkdirs();
            }

            // messages file
            File msgFile = new File("./plugins/LazyRoad/messages.properties");
            if (!msgFile.exists() || !msgFile.isFile()) {
                msgFile.createNewFile();
            }
            messages.loadMessages(msgFile);

        } catch (IOException ex) {
            log.log(Level.SEVERE, "[LazyRoad] : Error on Config File:" + ex.getMessage());
            return;
        }

        // get the roads
        roads = new HashMap<String, Road>();
        loadRoads();

        // Register events
        if (!eventRegistered) {
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Priority.Monitor, this);
        }
    }

    /**
     * Bukkit method that is called when the plugin is disabled
     */
    @Override
    public void onDisable() {
        log.info("[LazyRoad] : Plugin disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (label.equals("road") && sender instanceof Player) {
            Player player = (Player) sender;

            if (!permissions.has(player, "LazyRoad.build")) {
                player.sendMessage(messages.getMessage("noPermissions"));
                return true;
            }

            if (args.length > 0) {

                if (args[0].equals("stop")) {
                    playerListener.removeBuilder(player);
                    player.sendMessage(messages.getMessage("buildStop"));
                    return true;
                } else if (args[0].equals("reload")) {
                    loadRoads();
                    player.sendMessage(messages.getMessage("roadLoaded"));
                    return true;
                }

                // get the road
                Road road = roads.get(args[0]);

                if (road == null) {
                    player.sendMessage(messages.getMessage("noRoad"));
                    return true;
                }

                boolean tunnel = args.length > 1;

                playerListener.addBuilder(player, new RoadEnabled(road, tunnel));

                player.sendMessage(messages.getMessage("canBuild"));
            } else {
                // list the roads
                Iterator<String> it = roads.keySet().iterator();
                while (it.hasNext()) {
                    String msg = messages.getMessage("roadList");
                    msg = Messages.setField(msg, "%name%", it.next());
                    player.sendMessage(msg);
                }
            }
        }

        return true;
    }

    /**
     * Gets the permission class to handle permissions. Not used.
     * @return
     */
    public static PermissionHandler getPermissions() {
        return permissions;
    }

    /**
     * Sets the permission class to handle permissions.
     * @param perm
     */
    public void setPermissions(PermissionHandler perm) {
        permissions = perm;

        registerEvents();
    }

    /**
     * 
     * @return
     */
    public boolean isPermissionsSet() {
        return permissions != null;
    }

    private void registerEvents() {
        if (!eventRegistered) {
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
            eventRegistered = true;

            log.log(Level.INFO, "[LazyRoad] : Version 0.2 is enabled!");
        }
    }

    private void loadRoads() {
        
        roads.clear();

        File[] files = roadsDirectory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".yml");
            }
        });

        Yaml yaml = new Yaml();

        int imax = files.length;
        for (int i = 0; i < imax; i++) {
            File f = files[i];

            try {
                String roadName = f.getName();
                roadName = roadName.substring(0, roadName.length() - 4);

                Map<String, Object> map = (Map<String, Object>) yaml.load(new FileInputStream(f));



                int maxRepeatStairs = ((Integer) map.get("maxRepeatStairs")).intValue();
                RoadPart stairs = getRoadPart((List<List<String>>) map.get("stairs"), 1, 0);

                Map<String, Map<String, Object>> parts = (Map<String, Map<String, Object>>) map.get("parts");
                int maxSequence = 0;
                Road road = new Road(parts.size());
                road.setStairs(stairs);
                road.setMaxGradient(maxRepeatStairs);

                Iterator<Map<String, Object>> it = parts.values().iterator();
                int partIndex = 0;
                while (it.hasNext()) {
                    Map<String, Object> layer = it.next();

                    int groundLayer = ((Integer) layer.get("ground")).intValue();
                    int repeatEvery = ((Integer) layer.get("repeatEvery")).intValue();
                    RoadPart rp = getRoadPart((List<List<String>>) layer.get("layers"), repeatEvery, groundLayer);
                    if (rp.getRepeatEvery() > maxSequence) {
                        maxSequence = rp.getRepeatEvery();
                    }
                    road.setRoadPart(partIndex, rp);
                    
                    partIndex++;
                }
                
                road.setMaxSequence(maxSequence);

                roads.put(roadName, road);

            } catch (Exception ex) {
                log.warning("[LazyRoad] An error occured while parsing the roads !");
            }
        }


    }

    private RoadPart getRoadPart(List<List<String>> materials, int repeatEvery, int groundLayer) {

        RoadPart rp = new RoadPart(materials.size(), repeatEvery);
        rp.setGroundLayer(groundLayer);

        Iterator<List<String>> itmat = materials.iterator();
        int layerIndex = 0;
        while (itmat.hasNext()) {
            List<String> matlist = itmat.next();

            RoadLayer rl = new RoadLayer(matlist.size());

            Iterator<String> itmatlist = matlist.iterator();
            int insideLayerIndex = 0;
            while (itmatlist.hasNext()) {
                String[] ressource = itmatlist.next().split(":");

                rl.setMaterial(insideLayerIndex, Integer.parseInt(ressource[0]), Short.parseShort(ressource[1]));

                insideLayerIndex++;
            }

            rp.setLayer(layerIndex, rl);

            layerIndex++;
        }

        return rp;
    }
}
