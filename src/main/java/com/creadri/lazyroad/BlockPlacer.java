package com.creadri.lazyroad;

import org.bukkit.World;
import org.bukkit.block.Block;

/**
 *
 * @author creadri
 */
public class BlockPlacer {
    
    protected World world;
    protected Undo undo;
    
    
    public void putBlock(int x, int y, int z, int id, byte data, Direction dir) {

        if (id == -1) {
            return;
        }

        Block b = world.getBlockAt(x, y, z);

        if (b.getTypeId() == id && b.getData() == data) {
            return;
        }

        undo.putBlock(b);

        // directional items
        if (dir != Direction.NORTH) { // north is the default direction for items

            // torches normal and redstone
            if ((id == 50 || id == 75 || id == 76) && data != (byte) 5) {
                if (dir == Direction.SOUTH && data == (byte) 1) {
                    // south vs south
                    data = (byte) 2;
                } else if (dir == Direction.SOUTH && data == (byte) 2) {
                    // south vs north
                    data = (byte) 1;
                } else if (dir == Direction.SOUTH && data == (byte) 3) {
                    // south vs west
                    data = (byte) 4;
                } else if (dir == Direction.SOUTH && data == (byte) 4) {
                    // south vs east
                    data = (byte) 3;
                } else if (dir == Direction.WEST && data == (byte) 1) {
                    // west vs south
                    data = (byte) 3;
                } else if (dir == Direction.WEST && data == (byte) 2) {
                    // west vs north
                    data = (byte) 4;
                } else if (dir == Direction.WEST && data == (byte) 3) {
                    // west vs west
                    data = (byte) 2;
                } else if (dir == Direction.WEST && data == (byte) 4) {
                    // west vs east
                    data = (byte) 1;
                } else if (dir == Direction.EAST && data == (byte) 1) {
                    // east vs south
                    data = (byte) 4;
                } else if (dir == Direction.EAST && data == (byte) 2) {
                    // east vs north
                    data = (byte) 3;
                } else if (dir == Direction.EAST && data == (byte) 3) {
                    // east vs west
                    data = (byte) 1;
                } else if (dir == Direction.EAST && data == (byte) 4) {
                    // east vs east
                    data = (byte) 2;
                }
            }

            // torches normal and redstone
            if ((id == 53 || id == 67 || id == 108 || id == 109)) {               
                
                if (dir == Direction.SOUTH && data == (byte) 0) {
                    // south vs south
                    data = (byte) 1;
                } else if (dir == Direction.SOUTH && data == (byte) 1) {
                    // south vs north
                    data = (byte) 0;
                } else if (dir == Direction.SOUTH && data == (byte) 2) {
                    // south vs west
                    data = (byte) 3;
                } else if (dir == Direction.SOUTH && data == (byte) 3) {
                    // south vs east
                    data = (byte) 2;
                    
                    
                } else if (dir == Direction.WEST && data == (byte) 0) {
                    // west vs south
                    data = (byte) 3;
                } else if (dir == Direction.WEST && data == (byte) 1) {
                    // west vs north
                    data = (byte) 2;
                } else if (dir == Direction.WEST && data == (byte) 2) {
                    // west vs west
                    data = (byte) 0;
                } else if (dir == Direction.WEST && data == (byte) 3) {
                    // west vs east
                    data = (byte) 1;
                    
                    
                } else if (dir == Direction.EAST && data == (byte) 0) {
                    // east vs south
                    data = (byte) 2;
                } else if (dir == Direction.EAST && data == (byte) 1) {
                    // east vs north
                    data = (byte) 3;
                } else if (dir == Direction.EAST && data == (byte) 2) {
                    // east vs west
                    data = (byte) 0;
                } else if (dir == Direction.EAST && data == (byte) 3) {
                    // east vs east
                    data = (byte) 1;
                }
            }
        }

        b.setTypeIdAndData(id, data, false);
    }
}
