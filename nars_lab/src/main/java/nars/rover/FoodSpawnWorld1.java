/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rover;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;

/**
 *
 * @author me
 */
public class FoodSpawnWorld1 extends RoverWorld {
    private final float w;
    private final float h;

    public FoodSpawnWorld1(PhysicsModel p, int numFood, float w, float h) {
        super(p);
        this.w = w;
        this.h = h;
        
        float foodSpawnR = w / 1.5f;
        for (int i = 0; i < numFood; i++) {
            float minSize = 0.8f;
            float maxSize = 4.0f;
            float mass = 4.0f;
            addFood(foodSpawnR, foodSpawnR, minSize, maxSize, mass);
        }
        float wt = 1f;
        addWall(0, h, w, wt, 0);
        addWall(-w, 0, wt, h, 0);
        addWall(w, 0, wt, h, 0);
        addWall(0, -h, w, wt, 0);


    }
    
}
