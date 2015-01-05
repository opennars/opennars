/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.narclear;

/**
 *
 * @author me
 */
public class FoodSpawnWorld1 extends RoverWorld {
    private final float w;
    private final float h;

    public FoodSpawnWorld1(PhysicsModel p, float w, float h) {
        super(p);
        this.w = w;
        this.h = h;
        
        float foodSpawnR = w / 1.5f;
        int numFood = 30;
        for (int i = 0; i < numFood; i++) {
            addFood(foodSpawnR, foodSpawnR);
        }
        float wt = 1f;
        addWall(0, h, w, wt, 0);
        addWall(-w, 0, wt, h, 0);
        addWall(w, 0, wt, h, 0);
        addWall(0, -h, w, wt, 0);
    }
    
}
