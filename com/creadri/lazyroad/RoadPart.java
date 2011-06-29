package com.creadri.lazyroad;

/**
 *
 * @author creadri
 */
public class RoadPart {
    private Layer[] layers;
    private int layerSize;
    private int groundLayer;
    private int repeatEvery;
    
    public RoadPart(int layerSize, int repeatEvery) {
        this.layerSize = layerSize;
        this.repeatEvery = repeatEvery;
        this.layers = new Layer[layerSize];
    }
    
    public int getRepeatEvery() {
        return repeatEvery;
    }

    public boolean isToBuild(int count, int maxSequence) {
        return ((count % maxSequence) % repeatEvery) == 0;
    }

    public int getLayerSize() {
        return layerSize;
    }

    public Layer[] getLayers() {
        return layers;
    }
    
    public Layer getLayer(int index) {
        return layers[index];
    }
    
    public void setLayer(int index, Layer layer) {
        layers[index] = layer;
    }

    public int getGroundLayer() {
        return groundLayer;
    }

    public void setGroundLayer(int groundLayer) {
        this.groundLayer = groundLayer;
    }
}
