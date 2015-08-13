package nars.rover;

import nars.rover.physics.gl.JoglAbstractDraw;

/**
 * Created by me on 7/18/15.
 */
public abstract class Material implements JoglAbstractDraw.DrawProperty {

    public static Material wall = new Sim.WallMaterial();
    public static Material food = new Sim.FoodMaterial();
    public static Material poison = new Sim.PoisonMaterial();

}
