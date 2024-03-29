package project.helpers;

import java.io.Serializable;

/**
 * simple coordinate class used
 * */
public class Coordinate implements Serializable {
    short x = -1;
    short y = -1;

    public Coordinate(short x, short y) {
        this.x = x;
        this.y = y;
    }

    public short getX() {
        return x;
    }

    public short getY() {
        return y;
    }

    public Boolean compare(Coordinate coordinate){
        if((this.x == coordinate.getX()) && (this.y == coordinate.getY())){
            return true;
        }
        else{
            return false;
        }
    }
}
