package com.creadri.lazyroad;

import java.io.Serializable;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 *
 * @author creadri
 */
public class Undo implements Serializable {

    private transient World world;
    private String sWorld;
    private int[] xs;
    private int[] ys;
    private int[] zs;
    private int[] ids;
    private byte[] datas;
    private int current;
    private int size;
    private final int incSize = 2048;

    public Undo(World world) {
        this.sWorld = world.getName();
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
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = current - 1; i >= 0; i--) {
                    Block b = world.getBlockAt(xs[i], ys[i], zs[i]);
                    b.setTypeIdAndData(ids[i], datas[i], false);
                }

                current = 0;
            }
        });
        
        thread.start();
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public byte[] getDatas() {
        return datas;
    }

    public void setDatas(byte[] datas) {
        this.datas = datas;
    }

    public int[] getIds() {
        return ids;
    }

    public void setIds(int[] ids) {
        this.ids = ids;
    }

    public String getsWorld() {
        return sWorld;
    }

    public void setsWorld(String sWorld) {
        this.sWorld = sWorld;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public int[] getXs() {
        return xs;
    }

    public void setXs(int[] xs) {
        this.xs = xs;
    }

    public int[] getYs() {
        return ys;
    }

    public void setYs(int[] ys) {
        this.ys = ys;
    }

    public int[] getZs() {
        return zs;
    }

    public void setZs(int[] zs) {
        this.zs = zs;
    }
}
