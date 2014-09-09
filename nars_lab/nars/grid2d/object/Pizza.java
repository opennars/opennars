package nars.grid2d.object;

import java.awt.Color;
import nars.grid2d.Effect;
import nars.grid2d.LocalGridObject;

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
        float scale = (float) Math.sin(space.getTime() / 7f) * 0.05f + 1.0f;
        space.pushMatrix();
        space.translate(cx, cy);
        space.pushMatrix();
        space.scale(scale * 0.8f);
        space.fill(Color.YELLOW.getRGB(), 255);
        space.ellipse(0, 0, 1, 1);
        space.popMatrix();
        if (!"".equals(doorname)) {
            space.textSize(0.2f);
            space.fill(255, 0, 0);
            space.pushMatrix();
            space.text(doorname, 0, 0);
            space.popMatrix();
        }
        space.popMatrix();
    }
}
