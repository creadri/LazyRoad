package com.creadri.lazyroad;

import org.bukkit.World;
import org.bukkit.block.Block;

/**
 *
 * @author creadri
 */
public class Undo {

    private World world;
    private int[] xs;
    private int[] ys;
    private int[] zs;
    private int[] ids;
    private byte[] datas;
    private int current;
    private int size;
    private final int incSize = 2048;

    public Undo(World world) {
        this.world = world;
        this.size = incSize;
        this.xs = new int[size];
        this.ys = new int[size];
        this.zs = new int[size];
        this.ids = new int[size];
        this.datas = new byte[size];
        this.current = 0;
    }

    public void putBlock(Block b) {
        xs[current] = b.getX();
        ys[current] = b.getY();
        zs[current] = b.getZ();

        ids[current] = b.getTypeId();
        datas[current] = b.getData();

        current++;
        if (current >= size) {

            int newsize = size + incSize;

            int[] newxs = new int[newsize];
            int[] newys = new int[newsize];
            int[] newzs = new int[newsize];
            int[] newids = new int[newsize];
            byte[] newdatas = new byte[newsize];
            
            System.arraycopy(xs, 0, newxs, 0, size);
            System.arraycopy(ys, 0, newys, 0, size);
            System.arraycopy(zs, 0, newzs, 0, size);
            System.arraycopy(ids, 0, newids, 0, size);
            System.arraycopy(datas, 0, newdatas, 0, size);
            
            xs = newxs;
            ys = newys;
            zs = newzs;
            ids = newids;
            datas = newdatas;
            size = newsize;
        }
    }
    
    public void undo() {
        
        for (int i = 0; i < current; i++) {
            Block b = world.getBlockAt(xs[i], ys[i], zs[i]);
            b.setTypeIdAndData(ids[i], datas[i], false);
        }
        
        current = 0;
    }
}
