package com.creadri.lazyroad;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author creadri
 */
public class RoadEnabled {

    private Road road;
    private Pillar pillar = null;
    private int count;
    private int lastBuiltStairs = -1;
    private boolean hasBuilt = false;
    private boolean tunnel = false;
    private int oldX = -1;
    private int oldY = -1;
    private int oldZ = -1;
    private Undo undo;

    public RoadEnabled(Road road, World world) {
        this.road = road;
        this.count = 1;
        this.undo = new Undo(world);
    }

    public void setPillar(Pillar pillar) {
        this.pillar = pillar;
    }

    public void undo() {
        undo.undo();
    }

    public void drawRoad(Player player) {

        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();


        World world = player.getWorld();

        // get the first block underneath the player

        while (world.getBlockAt(x, y, z).getTypeId() == 0) {
            y--;
        }

        // to stop stair bugs
        if (hasBuilt && (oldY - y) != 0) {
            if ((count - lastBuiltStairs) < road.getMaxGradient()) {
                y = oldY;
            } else if (oldY - y > 1) {
                y = oldY - 1;
            } else if (y - oldY > 1) {
                y = oldY + 1;
            }
        }

        // get the direction of the player N, S, W, E
        float rot = player.getLocation().getYaw() % 360;
        if (rot < 0) {
            rot += 360;
        }

        if ((rot >= 0 && rot < 45) || (rot >= 315 && rot <= 360)) {
            // West
            if (hasBuilt && (z - oldZ) <= 0) {
                return;
            }

            drawWest(world, x, y, z, tunnel);

        } else if (rot >= 45 && rot < 135) {
            // North
            if (hasBuilt && (oldX - x) <= 0) {
                return;
            }
            drawNorth(world, x, y, z, tunnel);

        } else if (rot >= 135 && rot < 225) {
            // East
            if (hasBuilt && (oldZ - z) <= 0) {
                return;
            }

            drawEast(world, x, y, z, tunnel);

        } else if (rot >= 225 && rot < 315) {
            // South
            if (hasBuilt && (x - oldX) <= 0) {
                return;
            }
            drawSouth(world, x, y, z, tunnel);

        } else {
            // Error !
            return;
        }

        oldX = x;
        oldZ = z;
        oldY = y;

        hasBuilt = true;

        count++;
    }

    private int drawNorth(World world, int x, int y, int z, boolean tunnel) {

        int groundLayer = -1;
        int pmax = road.getPartsSize();
        for (int p = 0; p < pmax; p++) {
            RoadPart part = road.getRoadPart(p);
            if (part.isToBuild(count, road.getMaxSequence())) {
                groundLayer = part.getGroundLayer();
                int newX = tunnel ? x - 1 : x;
                int newY = y - part.getGroundLayer();
                int newZ = z;

                // build all layers
                int imax = part.getLayerSize();
                for (int i = 0; i < imax; i++) {
                    Layer layer = part.getLayer(i);

                    int jmax = layer.getSize();
                    //left
                    newZ = z + (jmax / 2);

                    for (int j = 0; j < jmax; j++) {
                        int typeId = layer.getTypeId(j);
                        if (typeId != -1) {
                            Block b = world.getBlockAt(newX, newY, newZ);
                            undo.putBlock(b);
                            b.setTypeIdAndData(typeId, (byte) layer.getDurability(j), false);
                        }

                        //to right
                        newZ--;
                    }

                    newY++;
                }
                break;
            }
        }

        // build pillars
        if (pillar != null && groundLayer != -1) {
            pmax = pillar.getPartsSize();
            for (int p = 0; p < pmax; p++) {
                PillarPart part = pillar.getPillarPart(p);

                if (part.isToBuild(count, pillar.getMaxSequence())) {
                    int newX = tunnel ? x - 1 : x;
                    int newY = y - groundLayer - 1;
                    int newZ = z;

                    int buildUntil = part.getBuildUntil();

                    // build the pillar
                    int imax = part.getLayerSize();
                    int i = 0;
                    boolean hasBuild = false;
                    do {
                        Layer layer = part.getLayer(i % imax);

                        int jmax = layer.getSize();
                        //left
                        newZ = z + (jmax / 2);
                        hasBuild = false;

                        for (int j = 0; j < jmax; j++) {
                            int typeId = layer.getTypeId(j);
                            if (typeId != -1) {
                                Block b = world.getBlockAt(newX, newY, newZ);
                                int bType = b.getTypeId();
                                if (bType == 0 || (bType >= 8 && bType <= 11) || bType == 31 || bType == 32) {
                                    undo.putBlock(b);
                                    b.setTypeIdAndData(typeId, (byte) layer.getDurability(j), false);
                                    hasBuild = true;
                                }
                            }

                            //to right
                            newZ--;
                        }
                        buildUntil--;
                        newY--;
                    } while (hasBuild && buildUntil > 0 && newY > 0);

                    break;
                }
            }
        }

        // build stairs
        if (hasBuilt && y - oldY != 0) {
            RoadPart stairs = road.getStairs();
            int newX = tunnel ? x - 1 : x;
            int newY = (y - oldY) > 0 ? y : y + 1;
            int newZ = z;

            // build all layers
            int imax = stairs.getLayerSize();
            for (int i = 0; i < imax; i++) {
                Layer layer = stairs.getLayer(i);

                int jmax = layer.getSize();
                //left
                newZ = z + (jmax / 2);

                for (int j = 0; j < jmax; j++) {
                    int typeId = layer.getTypeId(j);
                    if (typeId != -1) {
                        Block b = world.getBlockAt(newX, newY, newZ);
                        undo.putBlock(b);
                        b.setTypeIdAndData(typeId, (byte) layer.getDurability(j), false);
                    }

                    //to right
                    newZ--;
                }

                newY++;
            }

            lastBuiltStairs = count;
        }

        return groundLayer;
    }

    private int drawSouth(World world, int x, int y, int z, boolean tunnel) {
        int groundLayer = -1;
        int pmax = road.getPartsSize();
        for (int p = 0; p < pmax; p++) {
            RoadPart part = road.getRoadPart(p);
            if (part.isToBuild(count, road.getMaxSequence())) {
                groundLayer = part.getGroundLayer();
                int newX = tunnel ? x + 1 : x;
                int newY = y - part.getGroundLayer();
                int newZ = z;

                // build all layers
                int imax = part.getLayerSize();
                for (int i = 0; i < imax; i++) {
                    Layer layer = part.getLayer(i);

                    int jmax = layer.getSize();
                    //left
                    newZ = z - (jmax / 2);

                    for (int j = 0; j < jmax; j++) {
                        int typeId = layer.getTypeId(j);
                        if (typeId != -1) {
                            Block b = world.getBlockAt(newX, newY, newZ);
                            undo.putBlock(b);
                            b.setTypeIdAndData(typeId, (byte) layer.getDurability(j), false);
                        }

                        //to right
                        newZ++;
                    }

                    newY++;
                }
                break;
            }
        }

        // build pillars
        if (pillar != null && groundLayer != -1) {
            pmax = pillar.getPartsSize();
            for (int p = 0; p < pmax; p++) {
                PillarPart part = pillar.getPillarPart(p);

                if (part.isToBuild(count, pillar.getMaxSequence())) {
                    int newX = tunnel ? x - 1 : x;
                    int newY = y - groundLayer - 1;
                    int newZ = z;

                    int buildUntil = part.getBuildUntil();

                    // build the pillar
                    int imax = part.getLayerSize();
                    int i = 0;
                    boolean hasBuild = false;
                    do {
                        Layer layer = part.getLayer(i % imax);

                        int jmax = layer.getSize();
                        //left
                        newZ = z - (jmax / 2);
                        hasBuild = false;

                        for (int j = 0; j < jmax; j++) {
                            int typeId = layer.getTypeId(j);
                            if (typeId != -1) {
                                Block b = world.getBlockAt(newX, newY, newZ);
                                int bType = b.getTypeId();
                                if (bType == 0 || (bType >= 8 && bType <= 11) || bType == 31 || bType == 32) {
                                    undo.putBlock(b);
                                    b.setTypeIdAndData(typeId, (byte) layer.getDurability(j), false);
                                    hasBuild = true;
                                }
                            }

                            //to right
                            newZ++;
                        }
                        buildUntil--;
                        newY--;
                    } while (hasBuild && buildUntil > 0 && newY > 0);

                    break;
                }
            }
        }

        // build stairs
        if (hasBuilt && y - oldY != 0) {
            RoadPart stairs = road.getStairs();
            int newX = tunnel ? x - 1 : x;
            int newY = (y - oldY) > 0 ? y : y + 1;
            int newZ = z;

            // build all layers
            int imax = stairs.getLayerSize();
            for (int i = 0; i < imax; i++) {
                Layer layer = stairs.getLayer(i);

                int jmax = layer.getSize();
                //left
                newZ = z - (jmax / 2);

                for (int j = 0; j < jmax; j++) {
                    int typeId = layer.getTypeId(j);
                    if (typeId != -1) {
                        Block b = world.getBlockAt(newX, newY, newZ);
                        undo.putBlock(b);
                        b.setTypeIdAndData(typeId, (byte) layer.getDurability(j), false);
                    }

                    //to right
                    newZ++;
                }

                newY++;
            }

            lastBuiltStairs = count;
        }

        return groundLayer;
    }

    private int drawEast(World world, int x, int y, int z, boolean tunnel) {
        int groundLayer = -1;
        int pmax = road.getPartsSize();
        for (int p = 0; p < pmax; p++) {
            RoadPart part = road.getRoadPart(p);
            if (part.isToBuild(count, road.getMaxSequence())) {
                groundLayer = part.getGroundLayer();
                int newX = x;
                int newY = y - part.getGroundLayer();
                int newZ = tunnel ? z - 1 : z;

                // build all layers
                int imax = part.getLayerSize();
                for (int i = 0; i < imax; i++) {
                    Layer layer = part.getLayer(i);

                    int jmax = layer.getSize();
                    //left
                    newX = x - (jmax / 2);

                    for (int j = 0; j < jmax; j++) {
                        int typeId = layer.getTypeId(j);
                        if (typeId != -1) {
                            Block b = world.getBlockAt(newX, newY, newZ);
                            undo.putBlock(b);
                            b.setTypeIdAndData(typeId, (byte) layer.getDurability(j), false);
                        }

                        //to right
                        newX++;
                    }

                    newY++;
                }
                break;
            }
        }
        
        // build pillars
        if (pillar != null && groundLayer != -1) {
            pmax = pillar.getPartsSize();
            for (int p = 0; p < pmax; p++) {
                PillarPart part = pillar.getPillarPart(p);

                if (part.isToBuild(count, pillar.getMaxSequence())) {
                    int newX = x;
                    int newY = y - groundLayer - 1;
                    int newZ = tunnel ? z - 1 : z;

                    int buildUntil = part.getBuildUntil();

                    // build the pillar
                    int imax = part.getLayerSize();
                    int i = 0;
                    boolean hasBuild = false;
                    do {
                        Layer layer = part.getLayer(i % imax);

                        int jmax = layer.getSize();
                        //left
                        newX = x - (jmax / 2);
                        hasBuild = false;

                        for (int j = 0; j < jmax; j++) {
                            int typeId = layer.getTypeId(j);
                            if (typeId != -1) {
                                Block b = world.getBlockAt(newX, newY, newZ);
                                int bType = b.getTypeId();
                                if (bType == 0 || (bType >= 8 && bType <= 11) || bType == 31 || bType == 32) {
                                    undo.putBlock(b);
                                    b.setTypeIdAndData(typeId, (byte) layer.getDurability(j), false);
                                    hasBuild = true;
                                }
                            }

                            //to right
                            newX++;
                        }
                        buildUntil--;
                        newY--;
                    } while (hasBuild && buildUntil > 0 && newY > 0);

                    break;
                }
            }
        }
        
        // build stairs
        if (hasBuilt && y - oldY != 0) {
            RoadPart stairs = road.getStairs();
            int newX = x;
            int newY = (y - oldY) > 0 ? y : y + 1;
            int newZ = tunnel ? z - 1 : z;

            // build all layers
            int imax = stairs.getLayerSize();
            for (int i = 0; i < imax; i++) {
                Layer layer = stairs.getLayer(i);

                int jmax = layer.getSize();
                //left
                newX = x - (jmax / 2);

                for (int j = 0; j < jmax; j++) {
                    int typeId = layer.getTypeId(j);
                    if (typeId != -1) {
                        Block b = world.getBlockAt(newX, newY, newZ);
                        undo.putBlock(b);
                        b.setTypeIdAndData(typeId, (byte) layer.getDurability(j), false);
                    }

                    //to right
                    newX++;
                }

                newY++;
            }

            lastBuiltStairs = count;
        }
        return groundLayer;
    }

    private int drawWest(World world, int x, int y, int z, boolean tunnel) {
        int groundLayer = -1;
        int pmax = road.getPartsSize();
        for (int p = 0; p < pmax; p++) {
            RoadPart part = road.getRoadPart(p);
            if (part.isToBuild(count, road.getMaxSequence())) {
                groundLayer = part.getGroundLayer();
                int newX = x;
                int newY = y - part.getGroundLayer();
                int newZ = tunnel ? z + 1 : z;

                // build all layers
                int imax = part.getLayerSize();
                for (int i = 0; i < imax; i++) {
                    Layer layer = part.getLayer(i);

                    int jmax = layer.getSize();
                    //left
                    newX = x + (jmax / 2);

                    for (int j = 0; j < jmax; j++) {
                        int typeId = layer.getTypeId(j);
                        if (typeId != -1) {
                            Block b = world.getBlockAt(newX, newY, newZ);
                            undo.putBlock(b);
                            b.setTypeIdAndData(typeId, (byte) layer.getDurability(j), false);
                        }

                        //to right
                        newX--;
                    }

                    newY++;
                }
                break;
            }
        }
        
        // build pillars
        if (pillar != null && groundLayer != -1) {
            pmax = pillar.getPartsSize();
            for (int p = 0; p < pmax; p++) {
                PillarPart part = pillar.getPillarPart(p);

                if (part.isToBuild(count, pillar.getMaxSequence())) {
                    int newX = x;
                    int newY = y - groundLayer - 1;
                    int newZ = tunnel ? z + 1 : z;

                    int buildUntil = part.getBuildUntil();

                    // build the pillar
                    int imax = part.getLayerSize();
                    int i = 0;
                    boolean hasBuild = false;
                    do {
                        Layer layer = part.getLayer(i % imax);

                        int jmax = layer.getSize();
                        //left
                        newX = x + (jmax / 2);
                        hasBuild = false;

                        for (int j = 0; j < jmax; j++) {
                            int typeId = layer.getTypeId(j);
                            if (typeId != -1) {
                                Block b = world.getBlockAt(newX, newY, newZ);
                                int bType = b.getTypeId();
                                if (bType == 0 || (bType >= 8 && bType <= 11) || bType == 31 || bType == 32) {
                                    undo.putBlock(b);
                                    b.setTypeIdAndData(typeId, (byte) layer.getDurability(j), false);
                                    hasBuild = true;
                                }
                            }

                            //to right
                            newX--;
                        }
                        buildUntil--;
                        newY--;
                    } while (hasBuild && buildUntil > 0 && newY > 0);

                    break;
                }
            }
        }
        
        // build stairs
        if (hasBuilt && y - oldY != 0) {
            RoadPart stairs = road.getStairs();
            int newX = x;
            int newY = (y - oldY) > 0 ? y : y + 1;
            int newZ = tunnel ? z + 1 : z;

            // build all layers
            int imax = stairs.getLayerSize();
            for (int i = 0; i < imax; i++) {
                Layer layer = stairs.getLayer(i);

                int jmax = layer.getSize();
                //left
                newX = x + (jmax / 2);

                for (int j = 0; j < jmax; j++) {
                    int typeId = layer.getTypeId(j);
                    if (typeId != -1) {
                        Block b = world.getBlockAt(newX, newY, newZ);
                        undo.putBlock(b);
                        b.setTypeIdAndData(typeId, (byte) layer.getDurability(j), false);
                    }

                    //to right
                    newX--;
                }

                newY++;
            }

            lastBuiltStairs = count;
        }

        return groundLayer;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Road getRoad() {
        return road;
    }

    public boolean isTunnel() {
        return tunnel;
    }

    public void setTunnel(boolean tunnel) {
        this.tunnel = tunnel;
    }
}
