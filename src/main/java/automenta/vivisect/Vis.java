package automenta.vivisect;

import automenta.vivisect.swing.PCanvas;
import processing.core.PGraphics;

/**
 * Something that can be visualized, drawn, or otherwies represented graphically / visually.
 */
public interface Vis {
    
    /** returns true if it should remain visible, false if it is to be removed */
    public boolean draw(PGraphics g);

    /** notifies this when visibility has changed */
    default public void onVisible(boolean showing) {
        
    }

    default public void init(PCanvas p) {
        
    }
    
    
    
}
