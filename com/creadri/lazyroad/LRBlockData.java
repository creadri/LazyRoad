package com.creadri.lazyroad;

/**
 *
 * @author creadri
 * Made it's own class by VeraLapsa
 * 
 */
public class LRBlockData {
    
    private int id;
    private byte data;
    private Direction dir;
    
    public LRBlockData(int id, byte data, Direction dir, boolean saving){
        this.id = id;
        this.data = data;
        this.dir = dir;
        
        if (saving) {
            return;
        } else {
            setPlaceDirection();
        }
    }
    
    private void setPlaceDirection(){
        // directional items
        if (dir != Direction.NORTH) { // north is the default direction for items

            // torches(50) and off and on redstone torches(75,76)
            if ((id == 50 || id == 75 || id == 76) && data != (byte) 5) {
                if (dir == Direction.SOUTH ) {
                    if (data == (byte) 1) {
                        data = (byte) 2;
                    } else if (data == (byte) 2) {
                        data = (byte) 1;
                    } else if (data == (byte) 3) {
                        data = (byte) 4;
                    } else if (data == (byte) 4) {
                        data = (byte) 3;
                    }
                } else if (dir == Direction.EAST) {
                    if (data == (byte) 1) {
                        data = (byte) 4;
                    } else if (data == (byte) 2) {
                        data = (byte) 3;
                    } else if (data == (byte) 3) {
                        data = (byte) 1;
                    } else if (data == (byte) 4) {
                        data = (byte) 2;
                    }
                } else if (dir == Direction.WEST) {
                    if (data == (byte) 1) {
                        data = (byte) 3;
                    } else if (data == (byte) 2) {
                        data = (byte) 4;
                    } else if (data == (byte) 3) {
                        data = (byte) 2;
                    } else if (data == (byte) 4) {
                        data = (byte) 1;
                    }
                }
            }

            // Stairs
            if ((id == 53 || id == 67 || id == 108 || id == 109 || id == 114)) {  
                
                byte vrtdir = (byte) ( data & 0x04); // AND to get if Stair is upside down
                data = (byte) ( data & 0x03); // AND to get the Dir
                
                if (dir == Direction.SOUTH ) {
                    if (data == (byte) 0) {
                        data = (byte) 1;
                    } else if (data == (byte) 1) {
                        data = (byte) 0;
                    } else if (data == (byte) 2) {
                        data = (byte) 3;
                    } else if (data == (byte) 3) {
                        data = (byte) 2;
                    }
                } else if (dir == Direction.EAST) {
                    if (data == (byte) 0) {
                        data = (byte) 2;
                    } else if (data == (byte) 1) {
                        data = (byte) 3;
                    } else if (data == (byte) 2) {
                        data = (byte) 1;
                    } else if (data == (byte) 3) {
                        data = (byte) 0;
                    }
                } else if (dir == Direction.WEST) {
                    if (data == (byte) 0) {
                        data = (byte) 3;
                    } else if (data == (byte) 1) {
                        data = (byte) 2;
                    } else if (data == (byte) 2) {
                        data = (byte) 0;
                    } else if (data == (byte) 3) {
                        data = (byte) 1;
                    }
                }
                // OR the upside down bit
                data = (byte) (data | vrtdir);
            }
            
            
            // Dispensers, Chests, Furnaces, ladder, and wall signs
            if ((id == 23 || id == 54 || id == 61 || id == 62 || id == 65 || id == 68 )) {
                if (dir == Direction.SOUTH) {
                    if (data == (byte) 2) {
                        data = (byte) 3;
                    } else if (data == (byte) 3) {
                        data = (byte) 2;
                    } else if (data == (byte) 4) {
                        data = (byte) 5;
                    } else if (data == (byte) 5) {
                        data = (byte) 4;
                    }
                } else if (dir == Direction.EAST) {
                    if (data == (byte) 2) {
                        data = (byte) 5;
                    } else if (data == (byte) 3) {
                        data = (byte) 4;
                    } else if (data == (byte) 4) {
                        data = (byte) 2;
                    } else if (data == (byte) 5) {
                        data = (byte) 3;
                    }
                } else if (dir == Direction.WEST) {
                    if (data == (byte) 2) {
                        data = (byte) 4;
                    } else if (data == (byte) 3) {
                        data = (byte) 5;
                    } else if (data == (byte) 4) {
                        data = (byte) 3;
                    } else if (data == (byte) 5) {
                        data = (byte) 2;
                    }
                }
            }
            
            // button
            if (id == 77) {
                if (dir == Direction.SOUTH) {
                    if (data == (byte) 1) {
                        data = (byte) 2;
                    } else if (data == (byte) 2) {
                        data = (byte) 1;
                    } else if (data == (byte) 3) {
                        data = (byte) 4;
                    } else if (data == (byte) 4) {
                        data = (byte) 3;
                    }
                } else if (dir == Direction.EAST) {
                    if (data == (byte) 1) {
                        data = (byte) 3;
                    } else if (data == (byte) 2) {
                        data = (byte) 4;
                    } else if (data == (byte) 3) {
                        data = (byte) 2;
                    } else if (data == (byte) 4) {
                        data = (byte) 1;
                    }
                } else if (dir == Direction.WEST) {
                    if (data == (byte) 1) {
                        data = (byte) 4;
                    } else if (data == (byte) 2) {
                        data = (byte) 3;
                    } else if (data == (byte) 3) {
                        data = (byte) 1;
                    } else if (data == (byte) 4) {
                        data = (byte) 2;
                    }
                }
            }
            
            
            // Pumpkins and Jack-O-Lanturns
            if ((id == 86 || id == 91)) {
                if (dir == Direction.SOUTH) {
                    if (data == (byte) 0) {
                        data = (byte) 2;
                    } else if (data == (byte) 1) {
                        data = (byte) 3;
                    } else if (data == (byte) 2) {
                        data = (byte) 0;
                    } else if (data == (byte) 3) {
                        data = (byte) 1;
                    }
                } else if (dir == Direction.EAST) {
                    if (data == (byte) 0) {
                        data = (byte) 1;
                    } else if (data == (byte) 1) {
                        data = (byte) 2;
                    } else if (data == (byte) 2) {
                        data = (byte) 3;
                    } else if (data == (byte) 3) {
                        data = (byte) 0;
                    }
                } else if (dir == Direction.WEST) {
                    if (data == (byte) 0) {
                        data = (byte) 3;
                    } else if (data == (byte) 1) {
                        data = (byte) 0;
                    } else if (data == (byte) 2) {
                        data = (byte) 1;
                    } else if (data == (byte) 3) {
                        data = (byte) 2;
                    }
                }
            }
            
            // Diodes(93,94)
            if (( id == 93 || id == 94)) {               
                byte tick = (byte) ( 0x0C & data); // AND the 3 and 4 bits to get the tick duration
                data = (byte) ( 0x03 & data); // AND the 1 and 2 bits to get direction
                
                if (dir == Direction.SOUTH) {
                    if (data == (byte) 0) {
                        data = (byte) 2;
                    } else if (data == (byte) 1) {
                        data = (byte) 3;
                    } else if (data == (byte) 2) {
                        data = (byte) 0;
                    } else if (data == (byte) 3) {
                        data = (byte) 1;
                    }
                } else if (dir == Direction.EAST) {
                    if (data == (byte) 0) {
                        data = (byte) 1;
                    } else if (data == (byte) 1) {
                        data = (byte) 2;
                    } else if (data == (byte) 2) {
                        data = (byte) 3;
                    } else if (data == (byte) 3) {
                        data = (byte) 0;
                    }
                } else if (dir == Direction.WEST) {
                    if (data == (byte) 0) {
                        data = (byte) 3;
                    } else if (data == (byte) 1) {
                        data = (byte) 0;
                    } else if (data == (byte) 2) {
                        data = (byte) 1;
                    } else if (data == (byte) 3) {
                        data = (byte) 2;
                    }
                }
                
                // OR the Tick back on to data
                data = (byte) (data | tick);
            }
            
            //trap door
            if (id == 96) {
                byte open = (byte) (data & 0x04);
                data = (byte) (data & 0x03);
                
                if (dir == Direction.SOUTH) {
                    if (data == (byte) 0) {
                        data = (byte) 1;
                    } else if (data == (byte) 1) {
                        data = (byte) 0;
                    } else if (data == (byte) 2) {
                        data = (byte) 3;
                    } else if (data == (byte) 3) {
                        data = (byte) 2;
                    }
                } else if (dir == Direction.EAST) {
                    if (data == (byte) 0) {
                        data = (byte) 3;
                    } else if (data == (byte) 1) {
                        data = (byte) 2;
                    } else if (data == (byte) 2) {
                        data = (byte) 0;
                    } else if (data == (byte) 3) {
                        data = (byte) 1;
                    }
                } else if (dir == Direction.WEST) {
                    if (data == (byte) 0) {
                        data = (byte) 2;
                    } else if (data == (byte) 1) {
                        data = (byte) 3;
                    } else if (data == (byte) 2) {
                        data = (byte) 1;
                    } else if (data == (byte) 3) {
                        data = (byte) 0;
                    }
                }
                
                data = (byte) (data | open);
            }
            
            //pistons
            if ((id == 29 || id == 33)) {
                if (dir == Direction.SOUTH) {
                    if (data == (byte) 2) {
                        data = (byte) 2;
                    } else if (data == (byte) 3) {
                        data = (byte) 3;
                    } else if (data == (byte) 4) {
                        data = (byte) 5;
                    } else if (data == (byte) 5) {
                        data = (byte) 4;
                    }
                } else if (dir == Direction.EAST) {
                    if (data == (byte) 2) {
                        data = (byte) 5;
                    } else if (data == (byte) 3) {
                        data = (byte) 4;
                    } else if (data == (byte) 4) {
                        data = (byte) 2;
                    } else if (data == (byte) 5) {
                        data = (byte) 3;
                    }
                } else if (dir == Direction.WEST) {
                    if (data == (byte) 2) {
                        data = (byte) 4;
                    } else if (data == (byte) 3) {
                        data = (byte) 5;
                    } else if (data == (byte) 4) {
                        data = (byte) 3;
                    } else if (data == (byte) 5) {
                        data = (byte) 2;
                    }
                }
            }
            
            //fence gate
            if (id == 107) {
                byte open = (byte) (data & 0x04);
                data = (byte) (data & 0x03);
                
                if (dir == Direction.SOUTH) {
                    if (data == (byte) 0) {
                        data = (byte) 2;
                    } else if (data == (byte) 1) {
                        data = (byte) 3;
                    } else if (data == (byte) 2) {
                        data = (byte) 0;
                    } else if (data == (byte) 3) {
                        data = (byte) 1;
                    }
                } else if (dir == Direction.EAST) {
                    if (data == (byte) 0) {
                        data = (byte) 1;
                    } else if (data == (byte) 1) {
                        data = (byte) 2;
                    } else if (data == (byte) 2) {
                        data = (byte) 3;
                    } else if (data == (byte) 3) {
                        data = (byte) 0;
                    }
                } else if (dir == Direction.WEST) {
                    if (data == (byte) 0) {
                        data = (byte) 3;
                    } else if (data == (byte) 1) {
                        data = (byte) 0;
                    } else if (data == (byte) 2) {
                        data = (byte) 1;
                    } else if (data == (byte) 3) {
                        data = (byte) 2;
                    }
                }
                data = (byte) (data | open);
            }
            
        }
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
