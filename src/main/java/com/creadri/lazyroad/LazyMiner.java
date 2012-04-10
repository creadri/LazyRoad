/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.creadri.lazyroad;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faye
 */
public class LazyMiner {
    private int[] checkIds;
    private Player player;

    public LazyMiner(LazyRoad plugin, Player player) {
        checkIds = plugin.getCheckIds();
        this.player = player;
    }
    
    public boolean giveblock(Block b){
        if (checkIfOne(b.getTypeId())) {
            ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);
            b.breakNaturally(tool);
        } else {
            return false;
        }
        
        return false;
    }
    
    private boolean checkIfOne(int id){
        for (int i : checkIds) {
            if (i == id) {
                return true;
            }
        }
        return false;
    }

}
