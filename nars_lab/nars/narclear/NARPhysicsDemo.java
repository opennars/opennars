package nars.narclear;

import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;



public class NARPhysicsDemo extends NARPhysics<RobotArm> {
    private final RobotArm arm;
    
    public NARPhysicsDemo(NAR n) {
        super(n, new RobotArm());
        arm = getModel();
    }

    @Override
    public void cycle() {
        super.cycle();
        
        
        if (nar.getTime() % 20 == 0) {
            float s = (float)(Math.random() * 4f - 2f);
            float e = (float)(Math.random() * 4f - 2f);
            float f = (float)(Math.random() * 2f - 1f);
            arm.set(s, e, f);
        }
    }


    
    
    
    
    
    
    public static void main(String[] args) {
        NAR n = new DefaultNARBuilder().build();
        
        //PhysicsModel model = new Car();
        //model = new LiquidTimer();
        
        new NARPhysicsDemo(n).start(30);
        
    }
}
