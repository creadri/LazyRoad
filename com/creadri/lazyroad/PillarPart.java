package com.creadri.lazyroad;

/**
 *
 * @author creadri
 */
public class PillarPart {
    private Layer[] layers;
    private int layerSize;
    private int repeatEvery;
    private int buildUntil;

    public PillarPart(int layerSize, int repeatEvery) {
        this.layerSize = layerSize;
        this.repeatEvery = repeatEvery;
        this.layers = new Layer[layerSize];
    }

    public int getLayerSize() {
        return layerSize;
    }

    public int getRepeatEvery() {
        return repeatEvery;
    }
    
    public Layer getLayer(int index) {
        return layers[index];
    }
    
    public void setLayer(int index, Layer layer) {
        layers[index] = layer;
    }
    
    public boolean isToBuild(int count, int maxSequence) {
        return ((count % maxSequence) % repeatEvery) == 0;
    }

    public int getBuildUntil() {
        return buildUntil;
    }

    public void setBuildUntil(int buildUntil) {
        this.buildUntil = buildUntil;
    }
}