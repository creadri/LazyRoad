package com.creadri.lazyroad;

/**
 *
 * @author creadri
 */
public class Road {
    private RoadPart[] parts;
    private int partsSize;
    private int maxGradient;
    private RoadPart stairs;
    private int maxSequence;

    public Road(int partsSize) {
        this.partsSize = partsSize;
        this.parts = new RoadPart[partsSize];
    }

    public int getPartsSize() {
        return partsSize;
    }
    
    public RoadPart getRoadPart(int index) {
        return parts[index];
    }
    
    public void setRoadPart(int index, RoadPart part) {
        parts[index] = part;
    }

    public void setMaxGradient(int maxGradient) {
        this.maxGradient = maxGradient;
    }

    public void setStairs(RoadPart stairs) {
        this.stairs = stairs;
    }

    public int getMaxGradient() {
        return maxGradient;
    }

    public RoadPart getStairs() {
        return stairs;
    }

    public int getMaxSequence() {
        return maxSequence;
    }

    public void setMaxSequence(int maxSequence) {
        this.maxSequence = maxSequence;
    }
}
