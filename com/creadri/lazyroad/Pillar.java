package com.creadri.lazyroad;

/**
 *
 * @author creadri
 */
public class Pillar {
    private PillarPart[] parts;
    private int partsSize;
    private int maxSequence;

    public Pillar(int partsSize) {
        this.partsSize = partsSize;
        this.parts = new PillarPart[partsSize];
    }

    public void setMaxSequence(int maxSequence) {
        this.maxSequence = maxSequence;
    }

    public int getMaxSequence() {
        return maxSequence;
    }

    public int getPartsSize() {
        return partsSize;
    }
    
    public PillarPart getPillarPart(int index) {
        return parts[index];
    }
    
    public void setPillarPart(int index, PillarPart part) {
        parts[index] = part;
    }
}
