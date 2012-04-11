package com.creadri.lazyroad;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author VeraLapsa
 */
public class LazyMiner {

    private LazyRoad plugin;
    private Player player;
    private MinerData data;

    public LazyMiner(LazyRoad plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.data = new MinerData(plugin.getCheckIds());
    }

    public LazyMiner(LazyRoad plugin, Player player, MinerData data) {
        this.plugin = plugin;
        this.player = player;
        this.data = data;
    }

    public boolean SaveBlock(Block b) {
        if (checkIfOne(b.getTypeId())) {
            ItemStack drop = getDrop(b.getDrops(new ItemStack(Material.DIAMOND_PICKAXE)));
            if (!data.getDrops().isEmpty()) {
                for (int i = 0; i < data.getDrops().size(); i++) {
                    ItemStack item = data.get(i);
                    if ((drop.getTypeId() == item.getTypeId()) && (drop.getDurability() == item.getDurability()) && (item.getMaxStackSize() != item.getAmount())) {
                        item.setAmount(item.getAmount() + 1);
                        return true;
                    }
                }
                data.put(data.size(), drop);
                return true;
            } else {
                data.put(0, drop);
                return true;
            }
        } else {
            return false;
        }
    }

    private boolean checkIfOne(int id) {
        for (int i : data.getCheckIds()) {
            if (i == id) {
                return true;
            }
        }
        return false;
    }

    private ItemStack getDrop(Collection<ItemStack> d) {
        Random randomePicker = new Random();
        int choice = randomePicker.nextInt(d.size());
        ItemStack[] tmp = new ItemStack[d.size()];
        tmp = d.toArray(tmp);
        return tmp[choice];
    }

    public void putBlocks() {
        Block t = player.getTargetBlock(null, 10);
        BlockState b = t.getState();
        if (b instanceof DoubleChest || b instanceof Chest) {
            ItemStack[] d = new ItemStack[data.getDrops().size()];
            for (int i = 0; i < data.getDrops().size(); i++) {
                d[i] = data.getDrops().get(i);
            }
            if (b instanceof DoubleChest) {
                DoubleChest chest = (DoubleChest) b;
                Inventory chestInv = chest.getInventory();
                data.setDrops(chestInv.addItem(d));
            } else {
                Chest chest = (Chest) b;
                Inventory chestInv = chest.getInventory();
                data.setDrops(chestInv.addItem(d));
            }
            if (data.size() > 0) {
                player.sendMessage(plugin.getMessage("messages.lazyminer.notempty", data.size()));
            } else {
                player.sendMessage(plugin.getMessage("messages.lazyminer.empty"));
            }
            saveMinerData();
        } else {
            player.sendMessage("Must be looking at a chest for LazyRoad to put drops in.");
        }
    }

    public int size() {
        return data.getDrops().size();
    }

    public boolean enabled() {
        return data.isEnabled();
    }

    public void disable() {
        data.setEnabled(false);
    }

    public void enable() {
        data.setEnabled(true);
    }

    public void addCheckID(int id){
        data.addACheckID(id);
    }

    public boolean removeCheckId(int id){
        return data.removeACheckId(id);
    }

    public void saveMinerData(){
        File folder = new File(plugin.getDataFolder(), "miners");
        File saveFile = new File(folder, player.getName().concat(".ser"));
        try {

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile));

            oos.writeObject(data.serialize());

            oos.close();

        } catch (Exception ex) {
            player.sendMessage(ChatColor.DARK_RED + "An error occured when trying to save " + saveFile.getName());
            LazyRoad.log.severe(ChatColor.DARK_RED + "An error occured when trying to save " + saveFile.getName());
            LazyRoad.log.severe(ex.toString());
        }
    }

    public String checkIdsToString(){
        return data.checkIdsToString();
    }
}
