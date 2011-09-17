package com.creadri.lazyroad;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author creadri
 */
public class Pillar implements Serializable {

    private ArrayList<PillarPart> parts;
    private int maxSequence;

    public Pillar() {
        this.parts = new ArrayList<PillarPart>();
    }

    public Pillar(int partsSize) {
        this.parts = new ArrayList<PillarPart>(partsSize);
    }

    public int size() {
        return parts.size();
    }

    public PillarPart getPillarPart(int index) {
        return parts.get(index);
    }

    public void setPillarPart(int index, PillarPart part) {
        parts.set(index, part);
        Collections.sort(parts);
        maxSequence = parts.get(0).getRepeatEvery();
    }

    public boolean addPillarPart(PillarPart part) {
        int index = Collections.binarySearch(parts, part);
        
        if (index >= 0) {
            return false;
        }
        
        parts.add(part);
        Collections.sort(parts);
        maxSequence = parts.get(0).getRepeatEvery();
        
        return true;
    }
    
    public void removePillarPart(PillarPart part) {
        parts.remove(part);
    }

    public int getMaxSequence() {
        return maxSequence;
    }

    public void setMaxSequence(int maxSequence) {
        this.maxSequence = maxSequence;
    }

    public ArrayList<PillarPart> getParts() {
        return parts;
    }

    public void setParts(ArrayList<PillarPart> parts) {
        this.parts = parts;
    }

    public PillarPart getRoadPartToBuild(int count) {
        count = (count % maxSequence) + 1;

        for (PillarPart part : parts) {
            int re = part.getRepeatEvery();
            if (re == count) {
                return part;
            } else if (re < count) {
                part = parts.get(parts.size() - 1);
                if (part.getRepeatEvery() == 1) {
                    return part;
                } else {
                    return null;
                }
            }
        }
        
        return null;
    }
}
