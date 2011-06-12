package creadri.LazyRoad;

/**
 *
 * @author creadri
 */
public class RoadPart {
    private RoadLayer[] layers;
    private int layerSize;
    private int groundLayer;
    private int repeatEvery;
    
    public RoadPart(int layerSize, int repeatEvery) {
        this.layerSize = layerSize;
        this.repeatEvery = repeatEvery;
        this.layers = new RoadLayer[layerSize];
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

    public RoadLayer[] getLayers() {
        return layers;
    }
    
    public RoadLayer getLayer(int index) {
        return layers[index];
    }
    
    public void setLayer(int index, RoadLayer layer) {
        layers[index] = layer;
    }

    public int getGroundLayer() {
        return groundLayer;
    }

    public void setGroundLayer(int groundLayer) {
        this.groundLayer = groundLayer;
    }
}
