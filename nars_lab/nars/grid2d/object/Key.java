package nars.grid2d.object;

import java.awt.Color;
import nars.grid2d.Effect;
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
    public void update(Effect nextEffect) {
    }

    @Override
    public void draw() {
        float scale = (float)Math.sin(space.getTime()/7f)*0.05f + 1.0f;
        float a = space.getTime()/10;
        
        space.pushMatrix();
        space.translate(cx, cy);
        space.rotate(a);
        space.scale(scale*0.8f);
        
        space.fill(Color.RED.getRGB());
        space.rect(-0.4f, -0.15f/2, 0.8f, 0.15f);
        space.rect(-0.5f, -0.2f, 0.3f, 0.4f);
        space.rect(0.3f, 0, 0.1f, 0.15f);
        space.rect(0.1f, 0, 0.1f, 0.15f);
        
        space.popMatrix();
    }
    
    
}
