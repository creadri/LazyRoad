package com.creadri.lazyroad;

import creadri.util.Messages;
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
    private int oldX = Integer.MIN_VALUE;
    private int oldY = Integer.MIN_VALUE;
    private int oldZ = Integer.MIN_VALUE;
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
        int ymin = y - 2 - road.getMaxGradient();
        if (ymin <= 0) {
            ymin = 1;
        }
        while (y >= ymin && isToIgnore(world.getBlockAt(x, y, z))) {
            y--;
        }


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

        if (count % 20 == 0) {
            String msg = LazyRoad.messages.getMessage("roadCount");
            msg = Messages.setField(msg, "%blocks%", Integer.toString(count));
            player.sendMessage(msg);
        }
    }

    private void drawNorth(World world, int x, int y, int z, boolean tunnel) {
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

                if (id != -1) {
                    // replace the block
                    Block block = world.getBlockAt(newX, newY, newZ);
                    // memorize for undo operation
                    if (id != 0 || block.getTypeId() != 0) {
                        // only place if it's non-air
                        undo.putBlock(block);
                        block.setTypeIdAndData(id, data, false);
                    }
                }

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

                    if (id != -1) {
                        // replace the block
                        Block block = world.getBlockAt(newX, newY, newZ);
                        if (id != 0 || block.getTypeId() != 0) {
                            // only place if it's non-air
                            undo.putBlock(block);
                            block.setTypeIdAndData(id, data, false);
                        }
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

                buildBlock = false;

                for (int j = 0; j < width; j++) {
                    // getting the block information
                    int id = ids[i % height][j];
                    byte data = datas[i % height][j];

                    if (id != -1) {
                        Block block = world.getBlockAt(newX, newY, newZ);
                        if (isToIgnore(block)) {
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
            } while (buildBlock && buildUntil > 0 && newY > 0);
        }
    }

    private void drawSouth(World world, int x, int y, int z, boolean tunnel) {
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

                if (id != -1) {
                    // replace the block
                    Block block = world.getBlockAt(newX, newY, newZ);
                    // memorize for undo operation
                    if (id != 0 || block.getTypeId() != 0) {
                        // only place if it's non-air
                        undo.putBlock(block);
                        block.setTypeIdAndData(id, data, false);
                    }
                }

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

                    if (id != -1) {
                        // replace the block
                        Block block = world.getBlockAt(newX, newY, newZ);
                        if (id != 0 || block.getTypeId() != 0) {
                            // only place if it's non-air
                            undo.putBlock(block);
                            block.setTypeIdAndData(id, data, false);
                        }
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

                buildBlock = false;

                for (int j = 0; j < width; j++) {
                    // getting the block information
                    int id = ids[i % height][j];
                    byte data = datas[i % height][j];

                    if (id != -1) {
                        Block block = world.getBlockAt(newX, newY, newZ);
                        if (isToIgnore(block)) {
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
            } while (buildBlock && buildUntil > 0 && newY > 0);
        }
    }

    private void drawWest(World world, int x, int y, int z, boolean tunnel) {
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

                if (id != -1) {
                    // replace the block
                    Block block = world.getBlockAt(newX, newY, newZ);
                    // memorize for undo operation
                    if (id != 0 || block.getTypeId() != 0) {
                        // only place if it's non-air
                        undo.putBlock(block);
                        block.setTypeIdAndData(id, data, false);
                    }
                }

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

                    if (id != -1) {
                        // replace the block
                        Block block = world.getBlockAt(newX, newY, newZ);
                        if (id != 0 || block.getTypeId() != 0) {
                            // only place if it's non-air
                            undo.putBlock(block);
                            block.setTypeIdAndData(id, data, false);
                        }
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

                buildBlock = false;

                for (int j = 0; j < width; j++) {
                    // getting the block information
                    int id = ids[i % height][j];
                    byte data = datas[i % height][j];

                    if (id != -1) {
                        Block block = world.getBlockAt(newX, newY, newZ);
                        if (isToIgnore(block)) {
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
            } while (buildBlock && buildUntil > 0 && newY > 0);
        }
    }

    private void drawEast(World world, int x, int y, int z, boolean tunnel) {
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

                if (id != -1) {
                    // replace the block
                    Block block = world.getBlockAt(newX, newY, newZ);
                    // memorize for undo operation
                    if (id != 0 || block.getTypeId() != 0) {
                        // only place if it's non-air
                        undo.putBlock(block);
                        block.setTypeIdAndData(id, data, false);
                    }
                }

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

                    if (id != -1) {
                        // replace the block
                        Block block = world.getBlockAt(newX, newY, newZ);
                        if (id != 0 || block.getTypeId() != 0) {
                            // only place if it's non-air
                            undo.putBlock(block);
                            block.setTypeIdAndData(id, data, false);
                        }
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

                buildBlock = false;

                for (int j = 0; j < width; j++) {
                    // getting the block information
                    int id = ids[i % height][j];
                    byte data = datas[i % height][j];

                    if (id != -1) {
                        Block block = world.getBlockAt(newX, newY, newZ);
                        if (isToIgnore(block)) {
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
            } while (buildBlock && buildUntil > 0 && newY > 0);
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

    public boolean isToIgnore(Block b) {
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
}
