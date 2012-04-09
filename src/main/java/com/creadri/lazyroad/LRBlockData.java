package com.creadri.lazyroad;

/**
 *
 * @author creadri Made it's own class by VeraLapsa
 *
 */
public class LRBlockData {

    private int id;
    private byte data;
    private Direction dir;

    public LRBlockData(int id, byte data, Direction dir, boolean saving) {
        this.id = id;
        this.data = data;
        this.dir = dir;

        if (!saving) {
            setPlaceDirection();
        }
    }

    private void setPlaceDirection() {
        // directional items
        switch(dir){
            case EAST:
                data = (byte) rotate90(id, (int) data);
                break;
            case WEST:
                data = (byte) rotate90Reverse(id,(int) data);
                break;
            case SOUTH:
                data = (byte) flip(id, (int) data);
                break;
            default:
                break;
        }
    }

    /**
     * Rotate a block's data value 90 degrees (north->east->south->west->north);
     *
     * @param type
     * @param data
     * @return
     */
    public static int rotate90(int type, int data) {
        int dir;
        switch (type) {
            case 50:
            case 75:
            case 76:
                switch (data) {
                    case 1:
                        return 3;
                    case 2:
                        return 4;
                    case 3:
                        return 2;
                    case 4:
                        return 1;
                }
                break;

            case 66:
                switch (data) {
                    case 6:
                        return 7;
                    case 7:
                        return 8;
                    case 8:
                        return 9;
                    case 9:
                        return 6;
                }
            /*
             * FALL-THROUGH
             */

            case 27:
            case 28:
                switch (data & 0x7) {
                    case 0:
                        return 1 | (data & ~0x7);
                    case 1:
                        return (data & ~0x7);
                    case 2:
                        return 5 | (data & ~0x7);
                    case 3:
                        return 4 | (data & ~0x7);
                    case 4:
                        return 2 | (data & ~0x7);
                    case 5:
                        return 3 | (data & ~0x7);
                }
                break;

            case 53:
            case 67:
            case 108:
            case 109:
            case 114:
                int top = data & 0x4;
                dir = data & 0x3;
                switch (dir) {
                    case 0:
                        return 2 | top;
                    case 1:
                        return 3 | top;
                    case 2:
                        return 1 | top;
                    case 3:
                        return top;
                }
                break;

            case 69:
            case 77:
                int thrown = data & 0x8;
                int withoutThrown = data & ~0x8;
                switch (withoutThrown) {
                    case 1:
                        return 3 | thrown;
                    case 2:
                        return 4 | thrown;
                    case 3:
                        return 2 | thrown;
                    case 4:
                        return 1 | thrown;
                }
                break;

            case 64:
            case 71:
                int topHalf = data & 0x8;
                int swung = data & 0x4;
                int withoutFlags = data & ~(0x8 | 0x4);
                switch (withoutFlags) {
                    case 0:
                        return 1 | topHalf | swung;
                    case 1:
                        return 2 | topHalf | swung;
                    case 2:
                        return 3 | topHalf | swung;
                    case 3:
                        return topHalf | swung;
                }
                break;

            case 63:
                return (data + 4) % 16;

            case 65:
            case 68:
            case 54:
            case 61:
            case 62:
            case 23:
                switch (data) {
                    case 2:
                        return 5;
                    case 3:
                        return 4;
                    case 4:
                        return 2;
                    case 5:
                        return 3;
                }
                break;

            case 86:
            case 91:
                switch (data) {
                    case 0:
                        return 1;
                    case 1:
                        return 2;
                    case 2:
                        return 3;
                    case 3:
                        return 0;
                }
                break;

            case 93:
            case 94:
                dir = data & 0x03;
                int delay = data - dir;
                switch (dir) {
                    case 0:
                        return 1 | delay;
                    case 1:
                        return 2 | delay;
                    case 2:
                        return 3 | delay;
                    case 3:
                        return delay;
                }
                break;

            case 96:
                int withoutOrientation = data & ~0x3;
                int orientation = data & 0x3;
                switch (orientation) {
                    case 0:
                        return 3 | withoutOrientation;
                    case 1:
                        return 2 | withoutOrientation;
                    case 2:
                        return withoutOrientation;
                    case 3:
                        return 1 | withoutOrientation;
                }
                break;

            case 33:
            case 29:
            case 34:
                final int rest = data & ~0x7;
                switch (data & 0x7) {
                    case 2:
                        return 5 | rest;
                    case 3:
                        return 4 | rest;
                    case 4:
                        return 2 | rest;
                    case 5:
                        return 3 | rest;
                }
                break;

            case 99:
            case 100:
                if (data >= 10) {
                    return data;
                }
                return (data * 3) % 10;

            case 106:
                return ((data << 1) | (data >> 3)) & 0xf;

            case 107:
                return ((data + 1) & 0x3) | (data & ~0x3);

        }

        return data;
    }

    /**
     * Rotate a block's data value -90 degrees
     * (north<-east<-south<-west<-north);
     *
     *
     * @param type
     * @param data
     * @return
     */
    public static int rotate90Reverse(int type, int data) {
        // case ([0-9]+): return ([0-9]+) -> case \2: return \1
        int dir;
        switch (type) {
            case 50:
            case 75:
            case 76:
                switch (data) {
                    case 3:
                        return 1;
                    case 4:
                        return 2;
                    case 2:
                        return 3;
                    case 1:
                        return 4;
                }
                break;

            case 66:
                switch (data) {
                    case 7:
                        return 6;
                    case 8:
                        return 7;
                    case 9:
                        return 8;
                    case 6:
                        return 9;
                }
            /*
             * FALL-THROUGH
             */

            case 27:
            case 28:
                int power = data & ~0x7;
                switch (data & 0x7) {
                    case 1:
                        return power;
                    case 0:
                        return 1 | power;
                    case 5:
                        return 2 | power;
                    case 4:
                        return 3 | power;
                    case 2:
                        return 4 | power;
                    case 3:
                        return 5 | power;
                }
                break;

            case 53:
            case 67:
            case 108:
            case 109:
            case 114:
                int top = data & 0x4;
                dir = data & 0x3;
                switch (dir) {
                    case 2:
                        return top;
                    case 3:
                        return 1 | top;
                    case 1:
                        return 2 | top;
                    case 0:
                        return 3 | top;
                }
                break;

            case 69:
            case 77:
                int thrown = data & 0x8;
                int withoutThrown = data & ~0x8;
                switch (withoutThrown) {
                    case 3:
                        return 1 | thrown;
                    case 4:
                        return 2 | thrown;
                    case 2:
                        return 3 | thrown;
                    case 1:
                        return 4 | thrown;
                }
                break;

            case 64:
            case 71:
                int topHalf = data & 0x8;
                int swung = data & 0x4;
                int withoutFlags = data & ~(0x8 | 0x4);
                switch (withoutFlags) {
                    case 1:
                        return topHalf | swung;
                    case 2:
                        return 1 | topHalf | swung;
                    case 3:
                        return 2 | topHalf | swung;
                    case 0:
                        return 3 | topHalf | swung;
                }
                break;

            case 63:
                return (data + 12) % 16;

            case 65:
            case 68:
            case 54:
            case 61:
            case 62:
            case 23:
                switch (data) {
                    case 5:
                        return 2;
                    case 4:
                        return 3;
                    case 2:
                        return 4;
                    case 3:
                        return 5;
                }
                break;

            case 86:
            case 91:
                switch (data) {
                    case 1:
                        return 0;
                    case 2:
                        return 1;
                    case 3:
                        return 2;
                    case 0:
                        return 3;
                }
                break;

            case 93:
            case 94:
                dir = data & 0x03;
                int delay = data - dir;
                switch (dir) {
                    case 1:
                        return delay;
                    case 2:
                        return 1 | delay;
                    case 3:
                        return 2 | delay;
                    case 0:
                        return 3 | delay;
                }
                break;

            case 96:
                int withoutOrientation = data & ~0x3;
                int orientation = data & 0x3;
                switch (orientation) {
                    case 3:
                        return withoutOrientation;
                    case 2:
                        return 1 | withoutOrientation;
                    case 0:
                        return 2 | withoutOrientation;
                    case 1:
                        return 3 | withoutOrientation;
                }

            case 29:
            case 33:
            case 34:
                final int rest = data & ~0x7;
                switch (data & 0x7) {
                    case 5:
                        return 2 | rest;
                    case 4:
                        return 3 | rest;
                    case 2:
                        return 4 | rest;
                    case 3:
                        return 5 | rest;
                }
                break;

            case 99:
            case 100:
                if (data >= 10) {
                    return data;
                }
                return (data * 7) % 10;

            case 106:
                return ((data >> 1) | (data << 3)) & 0xf;

            case 107:
                return ((data + 3) & 0x3) | (data & ~0x3);
        }

        return data;
    }

    /**
     * Flip a block's data value.
     *
* @param type
     * @param data
     * @return
     */
    public static int flip(int type, int data) {
        return rotate90(type, rotate90(type, data));
    }

    public byte getData() {
        return data;
    }

    public Direction getDir() {
        return dir;
    }

    public int getId() {
        return id;
    }
}
