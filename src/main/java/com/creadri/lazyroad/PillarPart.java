package com.creadri.lazyroad;

import java.io.Serializable;

/**
 *
 * @author creadri
 */
public class PillarPart implements Comparable<PillarPart>, Serializable {

    private int[][] ids;
    private byte[][] datas;
    private int height;
    private int width;
    private int repeatEvery;
    private int buildUntil;

    public PillarPart(int height, int width) {
        ids = new int[height][width];
        datas = new byte[height][width];
        this.width = width;
        this.height = height;
    }

    public boolean isToBuild(int count, int maxSequence) {
        return ((count % maxSequence) % repeatEvery) == 0;
    }

    public boolean isToBuild(int count) {
        return count % repeatEvery == 0;
    }

    public int getBuildUntil() {
        return buildUntil;
    }

    public void setBuildUntil(int buildUntil) {
        this.buildUntil = buildUntil;
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

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int[] getId(int height) {
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
    public int compareTo(PillarPart o) {
        return o.repeatEvery - repeatEvery;
    }

    @Override
    public String toString() {
        return "Pillar Part " + Integer.toString(repeatEvery);
    }
}