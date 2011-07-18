package com.creadri.lazyroad;

import creadri.util.Messages;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Iterator;
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

/**
 * @author creadri
 */
public class LazyRoad extends JavaPlugin {
    
    private final LazyRoadPlayerListener playerListener = new LazyRoadPlayerListener(this);

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
            if (!player.hasPermission("LazyRoad.build")) {
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

    private void loadRoads() {

        roads.clear();

        File[] files = roadsDirectory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".ser");
            }
        });


        int imax = files.length;
        for (int i = 0; i < imax; i++) {
            File f = files[i];
            
            String name = "noRoad";

            try {
                name = f.getName();
                name = name.substring(0, name.length() - 4);

                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
                
                Road openRoad = (Road)ois.readObject();
                
                roads.put(name, openRoad);
                
                ois.close();

            } catch (Exception ex) {
                log.warning("[LazyRoad] An error occured while parsing the road " + name + " !");
            }
        }
    }


    private void loadPillars() {

        pillars.clear();

        File[] files = pillarsDirectory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".ser");
            }
        });

        int imax = files.length;
        for (int i = 0; i < imax; i++) {
            File f = files[i];

            String name = "noPillar";

            try {
                name = f.getName();
                name = name.substring(0, name.length() - 4);

                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
                
                Pillar openPillar = (Pillar)ois.readObject();
                
                pillars.put(name, openPillar);
                
                ois.close();

            } catch (Exception ex) {
                log.warning("[LazyRoad] An error occured while parsing the Pillar " + name + " !");
            }
        }
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
