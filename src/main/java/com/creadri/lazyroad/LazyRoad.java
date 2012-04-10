package com.creadri.lazyroad;

import com.creadri.util.ColumnChat;
import com.creadri.util.FileManager;
import com.creadri.util.Messages;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

/**
 * @author creadri some updates buy VeraLapsa
 */
public class LazyRoad extends JavaPlugin {

    private final LazyRoadPlayerListener playerListener = new LazyRoadPlayerListener(this);
    private boolean eventRegistered = false;
    // player properties
    private HashSet<String> playerPropStraight = new HashSet<String>();
    private HashMap<String, LazyMiner> lazyMiners = new HashMap<String, LazyMiner>();
    // roads and pillars
    private HashMap<String, Road> roads = new HashMap<String, Road>();
    private HashMap<String, Pillar> pillars = new HashMap<String, Pillar>();
    // files related
    private File roadsDirectory;
    private File pillarsDirectory;
    private File undoSave;
    private FilenameFilter filenameFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".ser");
        }
    };
    // LazyMiner
    private int checkIds[] = null;
    // config and message related
    public static Messages messages;
    public static final Logger log = Logger.getLogger("Minecraft");

    /**
     * method called by bukkit when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        // configuration files
        try {

            roadsDirectory = new File(getDataFolder(), "roads");
            pillarsDirectory = new File(getDataFolder(), "pillars");

            if (!roadsDirectory.exists() || !pillarsDirectory.exists()) {
                FileManager.copyDefaultRessources(getDataFolder(), "/", "defaultRoads.zip", "defaultPillars.zip");
            }

            // load roads and pillars
            loadRoads();
            loadPillars();

            undoSave = new File(getDataFolder(), "undo.dat");

            // load undo
            playerListener.unSerializeRoadsUndos(undoSave);

            if (!getConfig().contains("version")) {
                this.saveDefaultConfig();
                this.getConfig().set("version", "" + getDescription().getVersion());
            } else if (!getConfig().getString("version").equalsIgnoreCase(getDescription().getVersion())) {
                this.saveDefaultConfig();
                this.getConfig().set("version", "" + getDescription().getVersion());
            }
            this.getConfig().options().copyDefaults(true);
            this.saveConfig();

        } catch (IOException ex) {

            log.log(Level.SEVERE, "[LazyRoad] : Errors on files, stopping");
            return;
        }

        setupLazyMinerIds();

        // Register events
        if (!eventRegistered) {
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvents(this.playerListener, this);

            eventRegistered = true;

            log.log(Level.INFO, "[LazyRoad] : Version " + getDescription().getVersion() + " is enabled!");
        }


    }

    /**
     * Bukkit method that is called when the plugin is disabled
     */
    @Override
    public void onDisable() {
        playerListener.serializeRoadsUndos(undoSave);
        log.info("[LazyRoad] : Plugin disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;
            String splayer = player.getName();


            if (!player.hasPermission("lazyroad.build")) {
                //messages.sendPlayerMessage(player, "messages.noPermission");
                String message = getMessage("messages.noPermission");
                player.sendMessage(message);
                return true;
            }

            if (args.length == 0) {
                /**
                 * SUB-COMMAND LISTING
                 */
                sendRoadPillarMessages(player, 0);

            } else if (args.length == 1 && (args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("end"))) {
                /**
                 * SUB-COMMAND STOP
                 */
                RoadEnabled re = playerListener.removeBuilder(splayer);
                if (re != null) {
                    //messages.sendPlayerMessage(player, "messages.buildStop", re.getCount());
                    player.sendMessage(getMessage("messages.buildStop", re.getCount()));
                }

                return true;

            } else if (args.length == 1 && args[0].equalsIgnoreCase("up")) {
                /**
                 * SUB-COMMAND up
                 */
                RoadEnabled re = playerListener.setForceUp(splayer);
                if (re != null) {
                    //messages.sendPlayerMessage(player, "messages.forceUp");
                    player.sendMessage(getMessage("messages.forceUp"));
                }
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("down")) {
                /**
                 * SUB-COMMAND down
                 */
                RoadEnabled re = playerListener.setForceDown(splayer);
                if (re != null) {
                    //messages.sendPlayerMessage(player, "messages.forceDown");
                    player.sendMessage(getMessage("messages.forceDown"));
                }
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("normal")) {
                /**
                 * SUB-COMMAND normal
                 */
                RoadEnabled re = playerListener.setNormal(splayer);
                if (re != null) {
                    //messages.sendPlayerMessage(player, "messages.normal");
                    player.sendMessage(getMessage("messages.normal"));
                }
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("lazyminer")) {
                /**
                 * SUB-COMMAND normal
                 */
                if (lazyMiners.containsKey(player.getName())) {
                    lazyMiners.remove(player.getName());
                    player.sendMessage(getMessage("messages.lazyminer.disable"));
                } else {
                    setLazyMiners(player.getName(), new LazyMiner(this, player));
                    player.sendMessage(getMessage("messages.lazyminer.enable"));
                }
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                /**
                 * SUB-COMMAND RELOAD
                 */
                try {
                    loadRoads();
                    loadPillars();
                } catch (IOException ex) {
                    //messages.sendPlayerMessage(player, "messages.ioError");
                    player.sendMessage(getMessage("messages.ioError"));
                }
                //messages.sendPlayerMessage(player, "messages.reload");
                player.sendMessage(getMessage("messages.reload"));
                return true;

            } else if (args.length == 1 && args[0].equalsIgnoreCase("undo")) {
                /**
                 * SUB-COMMAND UNDO
                 */
                if (playerListener.undo(splayer)) {
                    //messages.sendPlayerMessage(player, "messages.undo");
                    player.sendMessage(getMessage("messages.undo"));
                } else {
                    //messages.sendPlayerMessage(player, "messages.undoError");
                    player.sendMessage(getMessage("messages.undoError"));
                }
                return true;

            } else if (args.length == 1 && args[0].equalsIgnoreCase("straight")) {
                /**
                 * SUB-COMMAND STRAIGHT
                 */
                if (playerPropStraight.contains(splayer)) {
                    playerPropStraight.remove(splayer);
                    //messages.sendPlayerMessage(player, "messages.straightEnabled");
                    player.sendMessage(getMessage("messages.straightEnabled"));
                } else {
                    playerPropStraight.add(splayer);
                    //messages.sendPlayerMessage(player, "messages.straightDisabled");
                    player.sendMessage(getMessage("messages.straightDisabled"));
                }

                return true;

            } else if (args.length == 1) {
                try {
                    int page = Integer.parseInt(args[0]);

                    sendRoadPillarMessages(player, page);

                    return true;
                } catch (NumberFormatException ex) {
                    //
                }
            }


            if (label.equals("road")) {
                /**
                 * ROAD COMMAND
                 */
                if (args.length > 0) {
                    Road road = roads.get(args[0]);
                    if (road == null) {
                        //messages.sendPlayerMessage(player, "messages.noRoad");
                        player.sendMessage(getMessage("messages.noRoad"));
                        return true;
                    }

                    RoadEnabled re = new RoadEnabled(player, road, this);
                    int count = 1;
                    if (args.length > 1) {
                        try {
                            count = Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex) {
                            count = 1;
                        }
                    }
                    re.setCount(count - 1);

                    if (playerPropStraight.contains(splayer)) {
                        re.setStraight(false);
                    }

                    if (playerListener.addBuilder(splayer, re)) {
                        //messages.sendPlayerMessage(player, "messages.beginBuilding");
                        player.sendMessage(getMessage("messages.beginBuilding"));
                    } else {
                        //messages.sendPlayerMessage(player, "messages.alreadyBuilding");
                        player.sendMessage(getMessage("messages.alreadyBuilding"));
                    }
                }

            } else if (label.equals("tunnel")) {
                /**
                 * TUNNEL COMMAND
                 */
                if (args.length > 0) {
                    Road road = roads.get(args[0]);
                    if (road == null) {
                        //messages.sendPlayerMessage(player, "messages.noRoad");
                        player.sendMessage(getMessage("messages.noRoad"));
                        return true;
                    }

                    RoadEnabled re = new RoadEnabled(player, road, this);
                    int count = 1;
                    if (args.length > 1) {
                        try {
                            count = Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex) {
                            count = 1;
                        }
                    }
                    re.setCount(count - 1);
                    re.setTunnel(true);

                    if (playerPropStraight.contains(splayer)) {
                        re.setStraight(false);
                    }

                    if (playerListener.addBuilder(splayer, re)) {
                        //messages.sendPlayerMessage(player, "messages.beginBuilding");
                        player.sendMessage(getMessage("messages.beginBuilding"));
                    } else {
                        //messages.sendPlayerMessage(player, "messages.alreadyBuilding");
                        player.sendMessage(getMessage("messages.alreadyBuilding"));
                    }
                }

            } else if (label.equals("bridge")) {
                /**
                 * BRIDGE COMMAND
                 */
                if (args.length > 1) {
                    Road road = roads.get(args[0]);
                    if (road == null) {
                        //messages.sendPlayerMessage(player, "messages.noRoad");
                        player.sendMessage(getMessage("messages.noRoad"));
                        return true;
                    }

                    Pillar pillar = pillars.get(args[1]);
                    if (pillars == null) {
                        //messages.sendPlayerMessage(player, "messages.noPillar");
                        player.sendMessage(getMessage("messages.noPillar"));
                        return true;
                    }

                    RoadEnabled re = new RoadEnabled(player, road, this);
                    int count = 1;
                    if (args.length > 2) {
                        try {
                            count = Integer.parseInt(args[2]);
                        } catch (NumberFormatException ex) {
                            count = 1;
                        }
                    }
                    re.setCount(count - 1);
                    re.setTunnel(true);
                    re.setPillar(pillar);

                    if (playerPropStraight.contains(splayer)) {
                        re.setStraight(false);
                    }

                    if (playerListener.addBuilder(splayer, re)) {
                        //messages.sendPlayerMessage(player, "messages.beginBuilding");
                        player.sendMessage(getMessage("messages.beginBuilding"));
                    } else {
                        //messages.sendPlayerMessage(player, "messages.alreadyBuilding");
                        player.sendMessage(getMessage("messages.alreadyBuilding"));
                    }
                }
            }
        }

        return true;
    }

    private void loadRoads() throws IOException {
        roads.clear();

        File[] files = roadsDirectory.listFiles(filenameFilter);

        int imax = files.length;
        for (int i = 0; i < imax; i++) {
            File f = files[i];

            String name = "noRoad";

            try {
                name = f.getName();
                name = name.substring(0, name.length() - 4);

                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));

                Road openRoad = (Road) ois.readObject();

                roads.put(name, openRoad);

                ois.close();

            } catch (ClassNotFoundException ex) {
                log.warning("[LazyRoad] An error occured while parsing the road " + name + " !");
            }
        }
    }

    private void loadPillars() throws IOException {
        pillars.clear();

        File[] files = pillarsDirectory.listFiles(filenameFilter);

        int imax = files.length;
        for (int i = 0; i < imax; i++) {
            File f = files[i];

            String name = "noPillar";

            try {
                name = f.getName();
                name = name.substring(0, name.length() - 4);

                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));

                Pillar openPillar = (Pillar) ois.readObject();

                pillars.put(name, openPillar);

                ois.close();

            } catch (ClassNotFoundException ex) {
                log.warning("[LazyRoad] An error occured while parsing the Pillar " + name + " !");
            }
        }
    }

    private void sendRoadPillarMessages(Player player, int page) {

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

    /**
     * Takes a string and replaces &# color codes with ChatColors
     *
     * @param message
     * @return
     */
    protected String replaceColors(String message) {
        return message.replaceAll("(?i)&([a-f0-9])", "\u00A7$1");
    }

    protected String getMessage(String node, Object... values) {
        String msg = getConfig().getString(node);
        msg = replaceColors(msg);
        if (values != null) {

            for (int j = 0; j < values.length; j++) {
                String fieldName = "%" + j;

                msg = msg.replaceFirst(fieldName, values[j].toString());
            }
        }
        return msg;
    }

    private void setupLazyMinerIds() {
        String ids = getConfig().getString("lazyminer.ids");
        ids = ids.replace('[', ' ').replace(']', ' ');
        String[] parsedIds = ids.split(",");

        if (parsedIds.length > 0) {
            checkIds = new int[parsedIds.length];
            for (int i = 0; i < parsedIds.length; i++) {
                parsedIds[i] = parsedIds[i].trim();
                try {
                    checkIds[i] = Integer.parseInt(parsedIds[i]);
                } catch (NumberFormatException numberFormatException) {
                    log.warning("Error Parsing " + parsedIds[i] + " as a number.");
                }
            }
        } else {
            log.warning("No Id's set for the LazyMiner Feature.");
        }
    }

    public LazyMiner getLazyMiners(String name) {
        return lazyMiners.get(name);
    }

    public void setLazyMiners(String name, LazyMiner lm) {
        lazyMiners.put(name, lm);
    }

    public int[] getCheckIds() {
        return checkIds;
    }
}
