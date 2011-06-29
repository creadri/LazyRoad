package com.creadri.lazyroad;

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
import org.bukkit.ChatColor;
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
    private File pillarsDirectory;
    private HashMap<String, Road> roads;
    private HashMap<String, Pillar> pillars;
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

            pillarsDirectory = new File("./plugins/LazyRoad/pillars");
            if (!pillarsDirectory.exists()) {
                pillarsDirectory.mkdirs();
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

        // Register events
        if (!eventRegistered) {
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Priority.Monitor, this);
            pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);

            roads = new HashMap<String, Road>();
            pillars = new HashMap<String, Pillar>();
            
            eventRegistered = true;

            log.log(Level.INFO, "[LazyRoad] : Version " + getDescription().getVersion() + " is enabled!");
        }

        // load roads and pillars
        loadRoads();
        loadPillars();
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

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if ((permissions == null && !player.isOp()) || (permissions != null) && !permissions.has(player, "LazyRoad.build")) {
                player.sendMessage(messages.getMessage("noPermissions"));
                return true;
            }

            if (args.length == 0) {
                /**
                 * SUB-COMMAND LISTING
                 */
                sendRoadPillarMessages(player);
            } else if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
                /**
                 * SUB-COMMAND STOP
                 */
                RoadEnabled re = playerListener.removeBuilder(player);
                if (re != null) {
                    String msg = messages.getMessage("buildStop");
                    msg = Messages.setField(msg, "%blocks%", Integer.toString(re.getCount()));
                    player.sendMessage(msg);
                }

                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                /**
                 * SUB-COMMAND RELOAD
                 */
                loadRoads();
                loadPillars();
                player.sendMessage(messages.getMessage("roadLoaded"));
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("undo")) {
                /**
                 * SUB-COMMAND UNDO
                 */
                playerListener.undo(player);
                
                player.sendMessage(messages.getMessage("undo"));
                return true;
            }


            if (label.equals("road")) {
                /**
                 * ROAD COMMAND
                 */
                if (args.length > 0) {
                    Road road = roads.get(args[0]);
                    if (road == null) {
                        player.sendMessage(messages.getMessage("noRoad"));
                        return true;
                    }

                    RoadEnabled re = new RoadEnabled(road, player.getWorld());
                    int count = 1;
                    if (args.length > 1) {
                        try {
                            count = Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex) {
                            count = 1;
                        }
                    }
                    re.setCount(count);

                    playerListener.addBuilder(player, re);
                }

            } else if (label.equals("tunnel")) {
                /**
                 * TUNNEL COMMAND
                 */
                if (args.length > 0) {
                    Road road = roads.get(args[0]);
                    if (road == null) {
                        player.sendMessage(messages.getMessage("noRoad"));
                        return true;
                    }

                    RoadEnabled re = new RoadEnabled(road, player.getWorld());
                    int count = 1;
                    if (args.length > 1) {
                        try {
                            count = Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex) {
                            count = 1;
                        }
                    }
                    re.setCount(count);
                    re.setTunnel(true);

                    playerListener.addBuilder(player, re);
                }

            } else if (label.equals("bridge")) {
                /**
                 * BRIDGE COMMAND
                 */
                if (args.length > 1) {
                    Road road = roads.get(args[0]);
                    if (road == null) {
                        player.sendMessage(messages.getMessage("noRoad"));
                        return true;
                    }

                    Pillar pillar = pillars.get(args[1]);
                    if (pillars == null) {
                        player.sendMessage(messages.getMessage("noPillar"));
                        return true;
                    }

                    RoadEnabled re = new RoadEnabled(road, player.getWorld());
                    int count = 1;
                    if (args.length > 2) {
                        try {
                            count = Integer.parseInt(args[2]);
                        } catch (NumberFormatException ex) {
                            count = 1;
                        }
                    }
                    re.setCount(count);
                    re.setTunnel(true);
                    re.setPillar(pillar);

                    playerListener.addBuilder(player, re);
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
    }

    /**
     * 
     * @return
     */
    public boolean isPermissionsSet() {
        return permissions != null;
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

            Layer rl = new Layer(matlist.size());

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

    private void loadPillars() {

        pillars.clear();

        File[] files = pillarsDirectory.listFiles(new FilenameFilter() {

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
                String pillarName = f.getName();
                pillarName = pillarName.substring(0, pillarName.length() - 4);

                Map<String, Object> map = (Map<String, Object>) yaml.load(new FileInputStream(f));

                Map<String, Map<String, Object>> parts = (Map<String, Map<String, Object>>) map.get("parts");
                int maxSequence = 0;
                Pillar pillar = new Pillar(parts.size());

                Iterator<Map<String, Object>> it = parts.values().iterator();
                int partIndex = 0;
                while (it.hasNext()) {
                    Map<String, Object> layer = it.next();

                    int maxBuild = ((Integer) layer.get("maxBuild")).intValue();
                    int repeatEvery = ((Integer) layer.get("repeatEvery")).intValue();
                    PillarPart pp = getPillarPart((List<List<String>>) layer.get("layers"), repeatEvery, maxBuild);
                    if (pp.getRepeatEvery() > maxSequence) {
                        maxSequence = pp.getRepeatEvery();
                    }
                    pillar.setPillarPart(partIndex, pp);

                    partIndex++;
                }

                pillar.setMaxSequence(maxSequence);

                pillars.put(pillarName, pillar);

            } catch (Exception ex) {
                log.warning("[LazyRoad] An error occured while parsing the pillars !");
            }
        }
    }

    private PillarPart getPillarPart(List<List<String>> materials, int repeatEvery, int maxBuild) {

        PillarPart pp = new PillarPart(materials.size(), repeatEvery);
        pp.setBuildUntil(maxBuild > 0 ? maxBuild : Integer.MAX_VALUE);

        Iterator<List<String>> itmat = materials.iterator();
        int layerIndex = 0;
        while (itmat.hasNext()) {
            List<String> matlist = itmat.next();

            Layer rl = new Layer(matlist.size());

            Iterator<String> itmatlist = matlist.iterator();
            int insideLayerIndex = 0;
            while (itmatlist.hasNext()) {
                String[] ressource = itmatlist.next().split(":");

                rl.setMaterial(insideLayerIndex, Integer.parseInt(ressource[0]), Short.parseShort(ressource[1]));

                insideLayerIndex++;
            }

            pp.setLayer(layerIndex, rl);

            layerIndex++;
        }

        return pp;
    }

    private void sendRoadPillarMessages(Player player) {

        Iterator<String> roadIt = roads.keySet().iterator();
        Iterator<String> pillarIt = pillars.keySet().iterator();

        player.sendMessage(ChatColor.AQUA + String.format("%15s | %15s", "Roads", "Pillars"));
        int imax = Math.max(roads.size(), pillars.size());

        for (int i = 0; i < imax; i++) {
            String pillarName = pillarIt.hasNext() ? pillarIt.next() : "";
            String roadName = roadIt.hasNext() ? roadIt.next() : "";

            player.sendMessage(String.format("%15s   %15s", roadName, pillarName));
        }
    }
}
