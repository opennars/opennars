package nars.testchamber.object;

import nars.testchamber.Effect;
import nars.testchamber.LocalGridObject;

import java.awt.*;

/**
 *
 * @author me
 */
public class Pizza extends LocalGridObject {

    public Pizza(int x, int y, String doorname) {
        super(x, y);
        this.doorname = doorname;
    }

    @Override
    public void update(Effect nextEffect) {
    }
    float animationLerpRate = 0.5f; //LERP interpolation rate

    @Override
    public void draw() {
        cx = (cx * (1.0f - animationLerpRate)) + (x * animationLerpRate);
        cy = (cy * (1.0f - animationLerpRate)) + (y * animationLerpRate);
        cheading = (cheading * (1.0f - animationLerpRate / 2.0f)) + (heading * animationLerpRate / 2.0f);
        float scale = (float) Math.sin(Math.PI / 7.0f) * 0.05f + 1.0f;
        space.pushMatrix();
        space.translate(cx, cy);
        space.pushMatrix();
        space.scale(scale * 0.8f);
        space.fill(Color.ORANGE.getRGB(), 255);
        space.ellipse(0, 0, 1.0f, 1.0f);
        space.fill(Color.YELLOW.getRGB(), 255);
        space.ellipse(0, 0, 0.8f, 0.8f);
        
        space.popMatrix();
        if (doorname != null && !doorname.isEmpty()) {
            space.textSize(0.2f);
            space.fill(255, 0, 0);
            space.pushMatrix();
            space.text(doorname, 0, 0);
            space.popMatrix();
        }
        
        //eyes
        space.fill(Color.RED.getRGB(), 255);
        space.rotate((float)(Math.PI/ 180.0f * cheading));
        space.ellipse(-0.15f,0.2f,0.1f,0.1f);
        space.ellipse(0.15f,0.2f,0.1f,0.1f);
        space.ellipse(-0.2f,-0.2f,0.1f,0.1f);
        space.ellipse(0.2f,-0.2f,0.1f,0.1f);
        space.ellipse(0.0f,-0.0f,0.1f,0.1f);
        
        space.popMatrix();
    }
}
