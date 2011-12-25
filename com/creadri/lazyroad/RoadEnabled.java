package com.creadri.lazyroad;

import org.bukkit.Location;
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
    private boolean straight = true;
    private int oldX;
    private int oldY;
    private int oldZ;
    private Direction oldDir;
    private Undo undo;
    private World world;

    public RoadEnabled(Player player, Road road) {
        Location loc = player.getLocation();
        this.world = loc.getWorld();
        this.oldX = loc.getBlockX();
        this.oldZ = loc.getBlockZ();
        this.oldY = getYFirstBlock(oldX, loc.getBlockY(), oldZ);
        this.oldDir = getDirection(player.getLocation());

        this.road = road;
        this.count = 0;
        this.undo = new Undo(world);
    }

    public void setPillar(Pillar pillar) {
        this.pillar = pillar;
    }

    public boolean isStraight() {
        return straight;
    }

    public void setStraight(boolean straight) {
        this.straight = straight;
    }

    public void undo() {
        undo.undo();
    }

    public Undo getUndo() {
        return undo;
    }

    public void drawRoad(Player player) {

        Location playerLocation = player.getLocation();

        // player current coordinates
        int x = playerLocation.getBlockX();
        int z = playerLocation.getBlockZ();

        // do not bother check the height
        if (x == oldX && z == oldZ) {
            return;
        }

        // get y coordinate
        int y = getYFirstBlock(x, playerLocation.getBlockY(), z);

        // constraint the y by the tunnel mode or to make stairs
        if (hasBuilt && tunnel) {
            // for tunnel mode, always keep old Y
            y = oldY;
        } else if (hasBuilt && (oldY - y) != 0) {
            // limit the y value for stairs to apply correctly
            if ((count - lastBuiltStairs) < road.getMaxGradient()) {
                y = oldY;
            } else if (oldY - y > 1) {
                y = oldY - 1;
            } else if (y - oldY > 1) {
                y = oldY + 1;
            }
        }

        Direction dir = getDirection(playerLocation);

        switch (dir) {
            case EAST:
                // checking if going backward or heading to opposite direction
                if ((hasBuilt && (oldZ - z) <= 0) || oldDir == Direction.WEST) {
                    return;
                }
                // constraint if it's going straight
                if (straight) {
                    x = oldX;
                }

                if (drawCorner(x, y, z, dir)) {
                    return;
                }

                // draw this stupid road
                drawEast(x, y, z, tunnel);

                break;
            case NORTH:
                // checking if going backward or heading to opposite direction
                if ((hasBuilt && (oldX - x) <= 0) || oldDir == Direction.SOUTH) {
                    return;
                }
                // constraint if it's going straight
                if (straight) {
                    z = oldZ;
                }

                if (drawCorner(x, y, z, dir)) {
                    return;
                }

                // draw this stupid road
                drawNorth(x, y, z, tunnel);

                break;
            case SOUTH:
                // checking if going backward or heading to opposite direction
                if ((hasBuilt && (x - oldX) <= 0) || oldDir == Direction.NORTH) {
                    return;
                }
                // constraint if it's going straight
                if (straight) {
                    z = oldZ;
                }

                if (drawCorner(x, y, z, dir)) {
                    return;
                }

                // draw this stupid road
                drawSouth(x, y, z, tunnel);

                break;
            case WEST:
                // checking if going backward or heading to opposite direction
                if ((hasBuilt && (z - oldZ) <= 0) || oldDir == Direction.EAST) {
                    return;
                }
                // constraint if it's going straight
                if (straight) {
                    x = oldX;
                }

                if (drawCorner(x, y, z, dir)) {
                    return;
                }

                // draw this stupid road
                drawWest(x, y, z, tunnel);

                break;
        }

        // saving current data to old ones
        oldDir = dir;
        oldX = x;
        oldZ = z;
        oldY = y;

        hasBuilt = true;

        count++;

        if (count % 20 == 0) {
            LazyRoad.messages.sendPlayerMessage(player, "messages.roadCount", count);;
        }
    }

    private Direction getDirection(Location loc) {
        // get the direction of the player N, S, W, E
        float rot = loc.getYaw() % 360;
        if (rot < 0) {
            rot += 360;
        }

        if ((rot >= 0 && rot < 45) || (rot >= 315 && rot <= 360)) {
            // WEST
            return Direction.WEST;
        } else if (rot >= 45 && rot < 135) {
            // NORTH
            return Direction.NORTH;
        } else if (rot >= 135 && rot < 225) {
            // EAST
            return Direction.EAST;
        } else if (rot >= 225 && rot < 315) {
            // SOUTH
            return Direction.SOUTH;
        }
        return null;
    }

    private void putBlock(int x, int y, int z, int id, byte data, Direction dir) {
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

    private int getYFirstBlock(int x, int y, int z) {
        int ymin = y - 3;
        if (ymin <= 0) {
            ymin = 1;
        }
        while (y >= ymin && isToIgnore(world.getBlockAt(x, y, z))) {
            y--;
        }
        return y;
    }

    private boolean drawCorner(int x, int y, int z, Direction dir) {

        // same direction, no corner
        if (oldDir == dir) {
            return false;
        }
        
        RoadPart part = road.getRoadPartToBuild(1);
        
        if (part == null) {
            return false;
        }

        // check though all the 8 cases
        if (oldDir == Direction.NORTH && dir == Direction.EAST) {

            int newY = y - part.getGroundLayer();
            int height = part.getHeight();
            int jmax = part.getWidth() / 2;

            // browse all heights layers
            for (int i = 0; i < height; i++) {

                int[] ids = part.getIds(i);
                byte[] datas = part.getData(i);

                for (int j = 0; j <= jmax; j++) {
                    // forward shift
                    for (int a = 0; a <= j; a++) {
                        putBlock(oldX - a, newY, oldZ + j, ids[jmax - j], datas[jmax - j], oldDir);
                    }
                    // east shift
                    for (int a = 0; a <= jmax + j; a++) {
                        putBlock(oldX - j, newY, oldZ - a + j, ids[jmax - j], datas[jmax - j], dir);
                    }
                    // backward shift
                    for (int a = j; a < jmax; a++) {
                        putBlock(oldX + j + 1, newY, oldZ - a - 1, ids[jmax + j + 1], datas[jmax + j + 1], dir);
                    }
                }
                newY++;
            }

            oldZ = oldZ - (tunnel ? jmax - 1 : jmax);
            oldDir = dir;
            return true;

        } else if (oldDir == Direction.NORTH && dir == Direction.WEST) {

            int newY = y - part.getGroundLayer();
            int height = part.getHeight();
            int jmax = part.getWidth() / 2;

            // browse all heights layers
            for (int i = 0; i < height; i++) {

                int[] ids = part.getIds(i);
                byte[] datas = part.getData(i);

                for (int j = 0; j <= jmax; j++) {
                    // forward shift
                    for (int a = 0; a <= j; a++) {
                        putBlock(oldX - a, newY, oldZ - j, ids[jmax - j], datas[jmax - j], oldDir);
                    }
                    // west shift
                    for (int a = 0; a <= jmax + j; a++) {
                        putBlock(oldX - j, newY, oldZ + a - j, ids[jmax - j], datas[jmax - j], dir);
                    }
                    // backward shift
                    for (int a = j; a < jmax; a++) {
                        putBlock(oldX + j + 1, newY, oldZ + a + 1, ids[jmax + j + 1], datas[jmax + j + 1], dir);
                    }
                }
                newY++;
            }

            oldZ = oldZ + (tunnel ? jmax - 1 : jmax);
            oldDir = dir;
            return true;

        } else if (oldDir == Direction.SOUTH && dir == Direction.EAST) {

            int newY = y - part.getGroundLayer();
            int height = part.getHeight();
            int jmax = part.getWidth() / 2;

            // browse all heights layers
            for (int i = 0; i < height; i++) {

                int[] ids = part.getIds(i);
                byte[] datas = part.getData(i);

                for (int j = 0; j <= jmax; j++) {
                    // forward shift
                    for (int a = 0; a <= j; a++) {
                        putBlock(oldX + a, newY, oldZ + j, ids[jmax - j], datas[jmax - j], oldDir);
                    }
                    // east shift
                    for (int a = 0; a <= jmax + j; a++) {
                        putBlock(oldX + j, newY, oldZ - a + j, ids[jmax - j], datas[jmax - j], dir);
                    }
                    // backward shift
                    for (int a = j; a < jmax; a++) {
                        putBlock(oldX - j - 1, newY, oldZ - a - 1, ids[jmax + j + 1], datas[jmax + j + 1], dir);
                    }
                }
                newY++;
            }

            oldZ = oldZ - (tunnel ? jmax - 1 : jmax);
            oldDir = dir;
            return true;

        } else if (oldDir == Direction.SOUTH && dir == Direction.WEST) {

            int newY = y - part.getGroundLayer();
            int height = part.getHeight();
            int jmax = part.getWidth() / 2;

            // browse all heights layers
            for (int i = 0; i < height; i++) {

                int[] ids = part.getIds(i);
                byte[] datas = part.getData(i);

                for (int j = 0; j <= jmax; j++) {
                    // forward shift
                    for (int a = 0; a <= j; a++) {
                        putBlock(oldX + a, newY, oldZ - j, ids[jmax - j], datas[jmax - j], oldDir);
                    }
                    // west shift
                    for (int a = 0; a <= jmax + j; a++) {
                        putBlock(oldX + j, newY, oldZ + a - j, ids[jmax - j], datas[jmax - j], dir);
                    }
                    // backward shift
                    for (int a = j; a < jmax; a++) {
                        putBlock(oldX - j - 1, newY, oldZ + a + 1, ids[jmax + j + 1], datas[jmax + j + 1], dir);
                    }
                }
                newY++;
            }

            oldZ = oldZ + (tunnel ? jmax - 1 : jmax);
            oldDir = dir;
            return true;

        } else if (oldDir == Direction.EAST && dir == Direction.NORTH) {

            int newY = y - part.getGroundLayer();
            int height = part.getHeight();
            int jmax = part.getWidth() / 2;

            // browse all heights layers
            for (int i = 0; i < height; i++) {

                int[] ids = part.getIds(i);
                byte[] datas = part.getData(i);

                for (int j = 0; j <= jmax; j++) {
                    // forward shift
                    for (int a = 0; a <= j; a++) {
                        putBlock(oldX + j, newY, oldZ - a, ids[jmax - j], datas[jmax - j], oldDir);
                    }
                    // east shift
                    for (int a = 0; a <= jmax + j; a++) {
                        putBlock(oldX - a + j, newY, oldZ - j, ids[jmax - j], datas[jmax - j], dir);
                    }
                    // backward shift
                    for (int a = j; a < jmax; a++) {
                        putBlock(oldX - a - 1, newY, oldZ + j + 1, ids[jmax + j + 1], datas[jmax + j + 1], dir);
                    }
                }
                newY++;
            }

            oldX = oldX - (tunnel ? jmax - 1 : jmax);
            oldDir = dir;
            return true;

        } else if (oldDir == Direction.EAST && dir == Direction.SOUTH) {

            int newY = y - part.getGroundLayer();
            int height = part.getHeight();
            int jmax = part.getWidth() / 2;

            // browse all heights layers
            for (int i = 0; i < height; i++) {

                int[] ids = part.getIds(i);
                byte[] datas = part.getData(i);

                for (int j = 0; j <= jmax; j++) {
                    // forward shift
                    for (int a = 0; a <= j; a++) {
                        putBlock(oldX - j, newY, oldZ - a, ids[jmax - j], datas[jmax - j], oldDir);
                    }
                    // east shift
                    for (int a = 0; a <= jmax + j; a++) {
                        putBlock(oldX + a - j, newY, oldZ - j, ids[jmax - j], datas[jmax - j], dir);
                    }
                    // backward shift
                    for (int a = j; a < jmax; a++) {
                        putBlock(oldX + a + 1, newY, oldZ + j + 1, ids[jmax + j + 1], datas[jmax + j + 1], dir);
                    }
                }
                newY++;
            }

            oldX = oldX + (tunnel ? jmax - 1 : jmax);
            oldDir = dir;
            return true;

        } else if (oldDir == Direction.WEST && dir == Direction.NORTH) {

            int newY = y - part.getGroundLayer();
            int height = part.getHeight();
            int jmax = part.getWidth() / 2;

            // browse all heights layers
            for (int i = 0; i < height; i++) {

                int[] ids = part.getIds(i);
                byte[] datas = part.getData(i);

                for (int j = 0; j <= jmax; j++) {
                    // forward shift
                    for (int a = 0; a <= j; a++) {
                        putBlock(oldX + j, newY, oldZ + a, ids[jmax - j], datas[jmax - j], oldDir);
                    }
                    // east shift
                    for (int a = 0; a <= jmax + j; a++) {
                        putBlock(oldX - a + j, newY, oldZ + j, ids[jmax - j], datas[jmax - j], dir);
                    }
                    // backward shift
                    for (int a = j; a < jmax; a++) {
                        putBlock(oldX - a - 1, newY, oldZ - j - 1, ids[jmax + j + 1], datas[jmax + j + 1], dir);
                    }
                }
                newY++;
            }

            oldX = oldX - (tunnel ? jmax - 1 : jmax);
            oldDir = dir;
            return true;

        } else if (oldDir == Direction.WEST && dir == Direction.SOUTH) {

            int newY = y - part.getGroundLayer();
            int height = part.getHeight();
            int jmax = part.getWidth() / 2;

            // browse all heights layers
            for (int i = 0; i < height; i++) {

                int[] ids = part.getIds(i);
                byte[] datas = part.getData(i);

                for (int j = 0; j <= jmax; j++) {
                    // forward shift
                    for (int a = 0; a <= j; a++) {
                        putBlock(oldX - j, newY, oldZ + a, ids[jmax - j], datas[jmax - j], oldDir);
                    }
                    // east shift
                    for (int a = 0; a <= jmax + j; a++) {
                        putBlock(oldX + a - j, newY, oldZ + j, ids[jmax - j], datas[jmax - j], dir);
                    }
                    // backward shift
                    for (int a = j; a < jmax; a++) {
                        putBlock(oldX + a + 1, newY, oldZ - j - 1, ids[jmax + j + 1], datas[jmax + j + 1], dir);
                    }
                }
                newY++;
            }

            oldX = oldX + (tunnel ? jmax - 1 : jmax);
            oldDir = dir;
            return true;

        }

        return false;
    }

    private void drawNorth(int x, int y, int z, boolean tunnel) {
        /**
         * DRAWING ROAD MAIN PART
         */
        RoadPart part = road.getRoadPartToBuild(count);

        if (part == null) {
            return;
        }

        int groundLayer = part.getGroundLayer();
        // new coords
        int newX = tunnel ? x - 1 : x;
        int newY = y - part.getGroundLayer();
        int newZ = z;
        // information about the array of informations
        int height = part.getHeight();
        int width = part.getWidth();

        int[][] ids = part.getIds();
        byte[][] datas = part.getDatas();

        for (int i = 0; i < height; i++) {

            // go to the left
            newZ = z + (width / 2);

            for (int j = 0; j < width; j++) {

                // the block to place
                int id = ids[i][j];
                byte data = datas[i][j];

                putBlock(newX, newY, newZ, id, data, Direction.NORTH);

                newZ--;
            }
            newY++;
        }


        /**
         * DRAWING STAIRS
         */
        if (hasBuilt && y - oldY != 0) {

            RoadPart stairs = road.getStairs();

            newX = tunnel ? x - 1 : x;
            newY = (y - oldY) > 0 ? y : y + 1;
            newZ = z;

            height = stairs.getHeight();
            width = stairs.getWidth();

            ids = stairs.getIds();
            datas = stairs.getDatas();

            for (int i = 0; i < height; i++) {

                // go to the left
                newZ = z + (width / 2);

                for (int j = 0; j < width; j++) {

                    // the block to place
                    int id = ids[i][j];
                    byte data = datas[i][j];
                    if (y - oldY > 0) {
                        putBlock(newX, newY, newZ, id, data, Direction.NORTH);
                    } else {
                        putBlock(newX, newY, newZ, id, data, Direction.SOUTH);
                    }

                    newZ--;
                }
                newY++;
            }

            lastBuiltStairs = count;
        }


        /**
         * DRAWING PILLARS
         */
        if (pillar != null) {

            PillarPart pillarPart = pillar.getRoadPartToBuild(count);

            if (pillarPart == null) {
                System.out.println("pillar is null");
                return;
            }

            newX = tunnel ? x - 1 : x;
            newY = y - groundLayer - 1;
            newZ = z;

            int buildUntil = pillarPart.getBuildUntil();
            if (buildUntil == 0) {
                buildUntil = Integer.MAX_VALUE;
            }

            height = pillarPart.getHeight();
            width = pillarPart.getWidth();

            ids = pillarPart.getIds();
            datas = pillarPart.getDatas();


            // build the pillar
            int i = 0;
            boolean buildBlock = false;
            do {
                // go to the left
                newZ = z + (width / 2);
                int h = i >= height ? height - 1 : i;
                buildBlock = false;

                for (int j = 0; j < width; j++) {
                    // getting the block information
                    int id = ids[h][j];
                    byte data = datas[h][j];
                    
                    if (id != -1) {
                        Block block = world.getBlockAt(newX, newY, newZ);
                        if (isToIgnoreForPillar(block)) {
                            if (id != 0 || block.getTypeId() != 0) {
                                undo.putBlock(block);
                                block.setTypeIdAndData(id, data, false);
                                buildBlock = true;
                            }
                        }
                    }

                    //to right
                    newZ--;
                }
                buildUntil--;
                newY--;
                i++;
            } while ((buildBlock || i < height) && buildUntil > 0 && newY > 0);
        }
    }

    private void drawSouth(int x, int y, int z, boolean tunnel) {
        /**
         * DRAWING ROAD MAIN PART
         */
        RoadPart part = road.getRoadPartToBuild(count);

        if (part == null) {
            return;
        }

        int groundLayer = part.getGroundLayer();
        // new coords
        int newX = tunnel ? x + 1 : x;
        int newY = y - part.getGroundLayer();
        int newZ = z;
        // information about the array of informations
        int height = part.getHeight();
        int width = part.getWidth();

        int[][] ids = part.getIds();
        byte[][] datas = part.getDatas();

        for (int i = 0; i < height; i++) {

            // go to the left
            newZ = z - (width / 2);

            for (int j = 0; j < width; j++) {

                // the block to place
                int id = ids[i][j];
                byte data = datas[i][j];

                putBlock(newX, newY, newZ, id, data, Direction.SOUTH);

                newZ++;
            }
            newY++;
        }


        /**
         * DRAWING STAIRS
         */
        if (hasBuilt && y - oldY != 0) {

            RoadPart stairs = road.getStairs();

            newX = tunnel ? x + 1 : x;
            newY = (y - oldY) > 0 ? y : y + 1;
            newZ = z;

            height = stairs.getHeight();
            width = stairs.getWidth();

            ids = stairs.getIds();
            datas = stairs.getDatas();

            for (int i = 0; i < height; i++) {

                // go to the left
                newZ = z - (width / 2);

                for (int j = 0; j < width; j++) {

                    // the block to place
                    int id = ids[i][j];
                    byte data = datas[i][j];

                    if (y - oldY > 0) {
                        putBlock(newX, newY, newZ, id, data, Direction.SOUTH);
                    } else {
                        putBlock(newX, newY, newZ, id, data, Direction.NORTH);
                    }

                    newZ++;
                }
                newY++;
            }

            lastBuiltStairs = count;
        }


        /**
         * DRAWING PILLARS
         */
        if (pillar != null) {

            PillarPart pillarPart = pillar.getRoadPartToBuild(count);

            if (pillarPart == null) {
                return;
            }

            newX = tunnel ? x + 1 : x;
            newY = y - groundLayer - 1;
            newZ = z;

            int buildUntil = pillarPart.getBuildUntil();
            if (buildUntil == 0) {
                buildUntil = Integer.MAX_VALUE;
            }

            height = pillarPart.getHeight();
            width = pillarPart.getWidth();

            ids = pillarPart.getIds();
            datas = pillarPart.getDatas();


            // build the pillar
            int i = 0;
            boolean buildBlock = false;
            do {
                // go to the left
                newZ = z - (width / 2);

                int h = i >= height ? height - 1 : i;

                buildBlock = false;

                for (int j = 0; j < width; j++) {
                    // getting the block information
                    int id = ids[h][j];
                    byte data = datas[h][j];

                    if (id != -1) {
                        Block block = world.getBlockAt(newX, newY, newZ);
                        if (isToIgnoreForPillar(block)) {
                            if (id != 0 || block.getTypeId() != 0) {
                                undo.putBlock(block);
                                block.setTypeIdAndData(id, data, false);
                                buildBlock = true;
                            }
                        }
                    }

                    //to right
                    newZ++;
                }
                buildUntil--;
                newY--;
                i++;
            } while ((buildBlock || i < height) && buildUntil > 0 && newY > 0);
        }
    }

    private void drawWest(int x, int y, int z, boolean tunnel) {
        /**
         * DRAWING ROAD MAIN PART
         */
        RoadPart part = road.getRoadPartToBuild(count);

        if (part == null) {
            return;
        }

        int groundLayer = part.getGroundLayer();
        // new coords
        int newX = x;
        int newY = y - part.getGroundLayer();
        int newZ = tunnel ? z + 1 : z;
        // information about the array of informations
        int height = part.getHeight();
        int width = part.getWidth();

        int[][] ids = part.getIds();
        byte[][] datas = part.getDatas();

        for (int i = 0; i < height; i++) {

            // go to the left
            newX = x + (width / 2);

            for (int j = 0; j < width; j++) {

                // the block to place
                int id = ids[i][j];
                byte data = datas[i][j];

                putBlock(newX, newY, newZ, id, data, Direction.WEST);

                newX--;
            }
            newY++;
        }


        /**
         * DRAWING STAIRS
         */
        if (hasBuilt && y - oldY != 0) {

            RoadPart stairs = road.getStairs();

            newX = x;
            newY = (y - oldY) > 0 ? y : y + 1;
            newZ = tunnel ? z + 1 : z;

            height = stairs.getHeight();
            width = stairs.getWidth();

            ids = stairs.getIds();
            datas = stairs.getDatas();

            for (int i = 0; i < height; i++) {

                // go to the left
                newX = x + (width / 2);

                for (int j = 0; j < width; j++) {

                    // the block to place
                    int id = ids[i][j];
                    byte data = datas[i][j];

                    if (y - oldY > 0) {
                        putBlock(newX, newY, newZ, id, data, Direction.WEST);
                    } else {
                        putBlock(newX, newY, newZ, id, data, Direction.EAST);
                    }

                    newX--;
                }
                newY++;
            }

            lastBuiltStairs = count;
        }


        /**
         * DRAWING PILLARS
         */
        if (pillar != null) {

            PillarPart pillarPart = pillar.getRoadPartToBuild(count);

            if (pillarPart == null) {
                return;
            }

            newX = x;
            newY = y - groundLayer - 1;
            newZ = tunnel ? z + 1 : z;

            int buildUntil = pillarPart.getBuildUntil();
            if (buildUntil == 0) {
                buildUntil = Integer.MAX_VALUE;
            }

            height = pillarPart.getHeight();
            width = pillarPart.getWidth();

            ids = pillarPart.getIds();
            datas = pillarPart.getDatas();


            // build the pillar
            int i = 0;
            boolean buildBlock = false;
            do {
                // go to the left
                newX = x + (width / 2);
                int h = i >= height ? height - 1 : i;
                buildBlock = false;

                for (int j = 0; j < width; j++) {
                    // getting the block information
                    int id = ids[h][j];
                    byte data = datas[h][j];

                    if (id != -1) {
                        Block block = world.getBlockAt(newX, newY, newZ);
                        if (isToIgnoreForPillar(block)) {
                            if (id != 0 || block.getTypeId() != 0) {
                                undo.putBlock(block);
                                block.setTypeIdAndData(id, data, false);
                                buildBlock = true;
                            }
                        }
                    }

                    //to right
                    newX--;
                }
                buildUntil--;
                newY--;
                i++;
            } while ((buildBlock || i < height) && buildUntil > 0 && newY > 0);
        }
    }

    private void drawEast(int x, int y, int z, boolean tunnel) {
        /**
         * DRAWING ROAD MAIN PART
         */
        RoadPart part = road.getRoadPartToBuild(count);

        if (part == null) {
            return;
        }

        int groundLayer = part.getGroundLayer();
        // new coords
        int newX = x;
        int newY = y - part.getGroundLayer();
        int newZ = tunnel ? z - 1 : z;
        // information about the array of informations
        int height = part.getHeight();
        int width = part.getWidth();

        int[][] ids = part.getIds();
        byte[][] datas = part.getDatas();

        for (int i = 0; i < height; i++) {

            // go to the left
            newX = x - (width / 2);

            for (int j = 0; j < width; j++) {

                // the block to place
                int id = ids[i][j];
                byte data = datas[i][j];

                putBlock(newX, newY, newZ, id, data, Direction.EAST);

                newX++;
            }
            newY++;
        }


        /**
         * DRAWING STAIRS
         */
        if (hasBuilt && y - oldY != 0) {

            RoadPart stairs = road.getStairs();

            newX = x;
            newY = (y - oldY) > 0 ? y : y + 1;
            newZ = tunnel ? z - 1 : z;

            height = stairs.getHeight();
            width = stairs.getWidth();

            ids = stairs.getIds();
            datas = stairs.getDatas();

            for (int i = 0; i < height; i++) {

                // go to the left
                newX = x - (width / 2);

                for (int j = 0; j < width; j++) {

                    // the block to place
                    int id = ids[i][j];
                    byte data = datas[i][j];

                    if (y - oldY > 0) {
                        putBlock(newX, newY, newZ, id, data, Direction.EAST);
                    } else {
                        putBlock(newX, newY, newZ, id, data, Direction.WEST);
                    }

                    newX++;
                }
                newY++;
            }

            lastBuiltStairs = count;
        }


        /**
         * DRAWING PILLARS
         */
        if (pillar != null) {

            PillarPart pillarPart = pillar.getRoadPartToBuild(count);

            if (pillarPart == null) {
                return;
            }

            newX = x;
            newY = y - groundLayer - 1;
            newZ = tunnel ? z - 1 : z;

            int buildUntil = pillarPart.getBuildUntil();
            if (buildUntil == 0) {
                buildUntil = Integer.MAX_VALUE;
            }

            height = pillarPart.getHeight();
            width = pillarPart.getWidth();

            ids = pillarPart.getIds();
            datas = pillarPart.getDatas();


            // build the pillar
            int i = 0;
            boolean buildBlock = false;
            do {
                // go to the left
                newX = x - (width / 2);
                int h = i >= height ? height - 1 : i;
                buildBlock = false;

                for (int j = 0; j < width; j++) {
                    // getting the block information
                    int id = ids[h][j];
                    byte data = datas[h][j];

                    if (id != -1) {
                        Block block = world.getBlockAt(newX, newY, newZ);
                        if (isToIgnoreForPillar(block)) {
                            if (id != 0 || block.getTypeId() != 0) {
                                undo.putBlock(block);
                                block.setTypeIdAndData(id, data, false);
                                buildBlock = true;
                            }
                        }
                    }

                    //to right
                    newX++;
                }
                buildUntil--;
                newY--;
                i++;
            } while ((buildBlock || i < height) && buildUntil > 0 && newY > 0);
        }
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

    private boolean isToIgnore(Block b) {
        int i = b.getTypeId();

        return i == 0
                || i == 6
                || (i >= 8 && i <= 11)
                || (i >= 17 && i <= 18)
                || (i >= 30 && i <= 32)
                || (i >= 37 && i <= 40)
                || i == 50
                || i == 51
                || i == 55
                || i == 59
                || i == 70
                || i == 72
                || i == 78;
    }

    private boolean isToIgnoreForPillar(Block b) {
        int i = b.getTypeId();

        return i == 0
                || i == 6
                || (i >= 8 && i <= 11)
                || (i >= 30 && i <= 32)
                || (i >= 37 && i <= 40)
                || i == 50
                || i == 51
                || i == 55
                || i == 59
                || i == 70
                || i == 72
                || i == 78;
    }

    public boolean isHasBuilt() {
        return hasBuilt;
    }

    public int getLastBuiltStairs() {
        return lastBuiltStairs;
    }

    public int getOldX() {
        return oldX;
    }

    public int getOldY() {
        return oldY;
    }

    public int getOldZ() {
        return oldZ;
    }

    public Pillar getPillar() {
        return pillar;
    }

    public World getWorld() {
        return world;
    }
}
