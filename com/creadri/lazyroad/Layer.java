package com.creadri.lazyroad;

/**
 *
 * @author creadri
 */
public class Layer {

    private int[] layerIds;
    private short[] layerDurabilities;
    private int size;

    public Layer(int size) {
        this.size = size;
        this.layerIds = new int[size];
        this.layerDurabilities = new short[size];
    }

    public short[] getLayerDurabilities() {
        return layerDurabilities;
    }

    public int[] getLayerIds() {
        return layerIds;
    }

    public int getSize() {
        return size;
    }

    public void setMaterial(int index, int typeId, short durability) {
        layerDurabilities[index] = durability;
        layerIds[index] = typeId;
    }

    public int getTypeId(int index) {
        return layerIds[index];
    }

    public short getDurability(int index) {
        return layerDurabilities[index];
    }
}
