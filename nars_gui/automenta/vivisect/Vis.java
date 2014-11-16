package automenta.vivisect;

import processing.core.PGraphics;

/**
 * Something that can be visualized, drawn, or otherwies represented graphically / visually.
 */
public interface Vis {
    
    /** returns true if it should remain visible, false if it is to be removed */
    public boolean draw(PGraphics g);
    
    
    
}
