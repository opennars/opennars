package nars.grid2d;

/**
 * GridObject with a specific position
 */
public abstract class LocalGridObject implements GridObject {

    public float cx, cy, cheading; //current drawn location, for animation
    
    public int x;
    public int y;
    public int heading; //in degrees

    public LocalGridObject(int x, int y) {
        setPosition(x, y);
    }
    
    
    public void setPosition(int x, int y) {
        this.cx = this.x = x;
        this.cy = this.y = y;
    }
    
    public int x() { return x; }
    public int y() { return y; }    
}
