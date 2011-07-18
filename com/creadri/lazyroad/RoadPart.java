package com.creadri.lazyroad;

import java.io.Serializable;

/**
 *
 * @author creadri
 */
public class RoadPart implements Comparable<RoadPart>, Serializable {
    private int[][] ids;
    private byte[][] datas;
    private int height;
    private int width;
    
    private int groundLayer;
    private int repeatEvery;
    
    public RoadPart(int height, int width) {
        ids = new int[height][width];
        datas = new byte[height][width];
        this.height = height;
        this.width = width;
    }
    
    public boolean isToBuild(int count, int maxSequence) {
        return ((count % maxSequence) % repeatEvery) == 0;
    }
    
    public boolean isToBuild(int count) {
        return count % repeatEvery == 0;
    }
    
    public byte[] getData(int height) {
        return datas[height];
    }

    public byte[][] getDatas() {
        return datas;
    }

    public void setDatas(byte[][] datas) {
        this.datas = datas;
    }

    public int getGroundLayer() {
        return groundLayer;
    }

    public void setGroundLayer(int groundLayer) {
        this.groundLayer = groundLayer;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    
    public int[] getIds(int height) {
        return ids[height];
    }

    public int[][] getIds() {
        return ids;
    }

    public void setIds(int[][] ids) {
        this.ids = ids;
    }

    public int getRepeatEvery() {
        return repeatEvery;
    }

    public void setRepeatEvery(int repeatEvery) {
        this.repeatEvery = repeatEvery;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
    
    public void setSize(int newWidth, int newHeight) {
        
        int[][] newids = new int[newHeight][newWidth];
        byte[][] newdatas = new byte[newHeight][newWidth];
        
        int imax = Math.min(height, newHeight);
        int jmax = Math.min(width, newWidth);
        
        for (int i = 0; i < imax; i++) {
            for (int j = 0; j < jmax; j++) {
                newids[i][j] = ids[i][j];
                newdatas[i][j] = datas[i][j];
            }
        }
        
        ids = newids;
        datas = newdatas;
        width = newWidth;
        height = newHeight;
    }

    @Override
    public int compareTo(RoadPart o) {
        return o.repeatEvery - repeatEvery;
    }

    @Override
    public String toString() {
        return "Road Part " + Integer.toString(repeatEvery);
    }
}
