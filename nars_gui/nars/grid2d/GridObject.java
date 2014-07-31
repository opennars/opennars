package nars.grid2d;

import processing.core.PApplet;

/**
 *
 * @author me
 */


abstract public class GridObject {
    
    abstract public void update(Grid2DSpace p);
    
    abstract public void draw(Grid2DSpace p);
    
}
