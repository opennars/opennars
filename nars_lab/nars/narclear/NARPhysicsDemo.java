package nars.narclear;

import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.narclear.jbox2d.test.Car;



public class NARPhysicsDemo {
    public static void main(String[] args) {
        NAR n = new DefaultNARBuilder().build();
        
        PhysicsModel model = new Car();
        //model = new LiquidTimer();
        
        new NARPhysics(n, model).start(30);
    }
}
