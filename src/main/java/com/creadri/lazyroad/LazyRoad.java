package com.creadri.lazyroad;

import com.creadri.lazyroad.commands.LazyMinerCommand;
import com.creadri.lazyroad.commands.roadCommand;
import com.creadri.util.ColumnChat;
import com.creadri.util.FileManager;
import com.creadri.util.Messages;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

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
    private Map<String, CommandHandler> commands = new HashMap<String, CommandHandler>();
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
                FileManager.copyDefaultRessources(getDataFolder(), "", "defaultRoads.zip", "defaultPillars.zip");
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

        commands.put("road", new roadCommand(this));
        commands.put("tunnel", new roadCommand(this));
        commands.put("bridge", new roadCommand(this));
        commands.put("lazyminer", new LazyMinerCommand(this));
    }

    /**
     * Bukkit method that is called when the plugin is disabled
     */
    @Override
    public void onDisable() {
        playerListener.serializeRoadsUndos(undoSave);
        for (Map.Entry<String, LazyMiner> entry : lazyMiners.entrySet()) {
            entry.getValue().saveMinerData();
        }
        log.info("[LazyRoad] : Plugin disabled");
    }
    /**
     * Handles the commands for the plugin.
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandHandler handler = commands.get(command.getName().toLowerCase());

        if (handler != null) {
            return handler.perform(sender, label, args);
        } else {
            return false;
        }
    }
    /**
     * Loads the roads in the roads folder into the plugin.
     * @throws IOException
     */
    public void loadRoads() throws IOException {
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
    /**
     * Loads the pillars in the pillars folder into the plugin.
     * @throws IOException
     */
    public void loadPillars() throws IOException {
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

    /**
     * Takes a string and replaces &# color codes with ChatColors
     *
     * @param message
     * @return
     */
    protected String replaceColors(String message) {
        return message.replaceAll("(?i)&([a-f0-9])", "\u00A7$1");
    }
    /**
     * Get a message from the config.
     * @param node The node in the config.
     * @param values 0 or more values that are to be inserted into the message.
     * @return The parsed message.
     */
    public String getMessage(String node, Object... values) {
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
    /**
     * Generates the int[] checkIds from the config
     */
    private void setupLazyMinerIds() {
        String ids = getConfig().getString("lazyminer.ids", "");
        if (ids.equalsIgnoreCase("")) {
            return;
        }
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
    /**
     *
     * @param name
     * @return If the Miner if the plugin has the miner else it returns null
     */
    public LazyMiner getLazyMiner(String name) {
        if (lazyMiners.containsKey(name)) {
            return lazyMiners.get(name);
        } else {
            return null;
        }

    }
    /**
     * Removes a miner from the plugin.
     * @param name
     */
    public void removeMiner(String name) {
        if (lazyMiners.containsKey(name)) {
            lazyMiners.remove(name);
        }
    }
    /**
     * Adds a new Miner to the plugin.
     * @param name Name of the miner
     * @param lm The new Miner
     */
    public void putLazyMiner(String name, LazyMiner lm) {
        lazyMiners.put(name, lm);
    }
    /**
     *
     * @return Returns the array of checkids.
     */
    public int[] getCheckIds() {
        return checkIds;
    }
    /**
     * Gets the plugins loaded Pillars
     * @return
     */
    public HashMap<String, Pillar> getPillars() {
        return pillars;
    }
    /**
     * Set the plugins Pillars
     * @param pillars
     */
    public void setPillars(HashMap<String, Pillar> pillars) {
        this.pillars = pillars;
    }
    /**
     * Gets the plugins loaded Roads
     * @return
     */
    public HashMap<String, Road> getRoads() {
        return roads;
    }
    /**
     * Set the plugins Roads
     * @param roads
     */
    public void setRoads(HashMap<String, Road> roads) {
        this.roads = roads;
    }
    /**
     * Gets if the player should not lay the road straight
     * @param name
     * @return
     */
    public boolean getPlayerPropStraight(String name) {
        return playerPropStraight.contains(name);
    }
    /**
     * Sets if the player should not lay the road straight
     * @param name
     * @param state
     */
    public void setPlayerPropStraight(String name, boolean state) {
        if (state) {
            playerPropStraight.add(name);
        } else {
            playerPropStraight.remove(name);
        }
    }
    /**
     * Returns the plugins playerListiner
     * @return
     */
    public LazyRoadPlayerListener getPlayerListener() {
        return playerListener;
    }
    
}
