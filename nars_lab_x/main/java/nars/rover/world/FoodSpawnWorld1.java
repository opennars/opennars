/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rover.world;

import nars.rover.Material;
import nars.rover.PhysicsModel;
import nars.rover.RoverWorld;

/**
 *
 * @author me
 */
public class FoodSpawnWorld1 extends RoverWorld {
    private final float w;
    private final float h;
    private final float foodToPoisonRatio;
    private final int numFood;

    public FoodSpawnWorld1(int numFood, float w, float h, float foodtoPoisonRatio) {
        super();
        this.w = w;
        this.h = h;

        this.numFood = numFood;
        this.foodToPoisonRatio = foodtoPoisonRatio;

    }

    @Override
    public void init(PhysicsModel p) {
        super.init(p);


        int numFood = 100;

        float foodSpawnR = w / 1.5f;
        for (int i = 0; i < numFood; i++) {
            float minSize = 0.8f;
            float maxSize = 2.0f;
            float mass = 0.1f;
            addFood(foodSpawnR, foodSpawnR, minSize, maxSize, mass,
                    Math.random() < foodToPoisonRatio ?
                            Material.food : Material.poison
            );
        }
        float wt = 1f;
        addWall(0, h, w, wt, 0);
        addWall(-w, 0, wt, h, 0);
        addWall(w, 0, wt, h, 0);
        addWall(0, -h, w, wt, 0);

    }
}
