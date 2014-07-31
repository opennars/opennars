package nars.grid2d.object;

import java.awt.Color;
import nars.grid2d.Grid2DSpace;
import nars.grid2d.LocalGridObject;

/**
 *
 * @author me
 */


public class Key extends LocalGridObject {

    public Key(int x, int y) {
        super(x, y);
    }

    @Override
    public void update(Grid2DSpace p) {
    }

    @Override
    public void draw(Grid2DSpace p) {
        float scale = (float)Math.sin(p.getTime()/7f)*0.05f + 1.0f;
        float a = p.getTime()/10;
        
        p.pushMatrix();
        p.translate(cx, cy);
        p.rotate(a);
        p.scale(scale);
        
        p.fill(Color.BLACK.getRGB());
        p.rect(-0.4f, -0.15f/2, 0.8f, 0.15f);
        p.rect(-0.5f, -0.2f, 0.3f, 0.4f);
        p.rect(0.3f, 0, 0.1f, 0.15f);
        p.rect(0.1f, 0, 0.1f, 0.15f);
        
        p.popMatrix();
    }
    
    
}
