package nars.testchamber.object;

import nars.testchamber.Effect;
import nars.testchamber.LocalGridObject;

import java.awt.*;

/**
 *
 * @author me
 */


public class Key extends LocalGridObject {

    
    
    public Key(int x, int y, String doorname) {
        super(x, y);
        this.doorname=doorname;
    }

    @Override
    public void update(Effect nextEffect) {
    }

    @Override
    public void draw() {
        float scale = (float)Math.sin(space.getTime()/ 7.0f)*0.05f + 1.0f;
        float a = space.getTime()/10;
        
        space.pushMatrix();
        space.translate(cx, cy);
        
        space.pushMatrix();
        space.rotate(a);
        space.scale(scale*0.8f);
        
        space.fill(Color.GREEN.getRGB());
        space.rect(-0.4f, -0.15f/2, 0.8f, 0.15f);
        space.rect(-0.5f, -0.2f, 0.3f, 0.4f);
        space.rect(0.3f, 0, 0.1f, 0.15f);
        space.rect(0.1f, 0, 0.1f, 0.15f);
        space.popMatrix();
        if(doorname != null && !doorname.isEmpty())
        {
            space.textSize(0.2f);
            space.fill(255,0,0);
            space.pushMatrix();
            space.text(doorname,0,0);
            space.popMatrix();
        }
        
        space.popMatrix();

    }
    
    
}
