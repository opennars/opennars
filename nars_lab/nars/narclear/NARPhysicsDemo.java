package nars.narclear;

import java.util.Arrays;
import java.util.List;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.entity.Task;
import nars.language.Term;
import nars.operator.NullOperator;
import nars.operator.Operation;
import org.jbox2d.common.MathUtils;



public class NARPhysicsDemo extends NARPhysics<RobotArm> {
    private final RobotArm arm;
    
    float shoulderAngle = 0;
    float shoulderRange = MathUtils.PI;
    float elbowRange = MathUtils.PI;
    float elbowAngle = 0, fingerAngle = 0;
    
    public NARPhysicsDemo(NAR n) {
        super(n, new RobotArm());
        arm = getModel();
        n.memory.addOperator(new NullOperator("^joint") {
            @Override protected List<Task> execute(Operation operation, Term[] args, Memory memory) {

                System.out.println(Arrays.toString(args));
                
                float dA = 0.1f;
                switch (args[0].toString()) {
                    case "shoulder":
                        if (args[1].toString().equals("left")) shoulderAngle-=dA;
                        else if (args[1].toString().equals("right")) shoulderAngle+=dA;
                        if (shoulderAngle < -shoulderRange) shoulderAngle = -shoulderRange;
                        if (shoulderAngle > shoulderRange) shoulderAngle = shoulderRange;
                        break;
                    case "elbow":
                        if (args[1].toString().equals("left")) elbowAngle-=dA;
                        else if (args[1].toString().equals("right")) elbowAngle+=dA;
                        if (elbowAngle < -elbowRange) elbowAngle = -elbowRange;
                        if (elbowAngle > elbowRange) elbowAngle = elbowRange;
                        break;                        
                }
                
                return super.execute(operation, args, memory);
            }
        });
    }

    public String angleState(String x, double v, int steps, double min, double max) {
        double p = (v - min) / (max-min);
        if (p < 0) p = 0;
        if (p > 1.0) p = 1;
        int s = (int) Math.round(p * steps);
        return ("<(*,angle_" + s + ") --> " + x + ">. :|:");
    }
    
    //boolean implication = false;
    
    @Override
    public void cycle() {
        super.cycle();
        
        /*
        if (!implication) {
            new Window("Implications", new SentenceGraphPanel(nar, nar.memory.executive.graph.implication)).show(500, 500);            
            implication = true;
        }
                */
                
        
        if (Math.random() < 0.1f) {
            nar.addInput("(^joint,shoulder,left)!");
        }
        if (Math.random() < 0.1f) {
            nar.addInput("(^joint,shoulder,right)!");
        }
        
        
        nar.addInput(angleState("shoulder",shoulderAngle,5,-shoulderRange,shoulderRange));
//        if (nar.getTime() % 20 == 0) {
//            float s = (float)(Math.random() * 4f - 2f);
//            float e = (float)(Math.random() * 4f - 2f);
//            float f = (float)(Math.random() * 2f - 1f);
//        }
        arm.set(shoulderAngle, elbowAngle, fingerAngle);
    }

    public static void main(String[] args) {
        NAR n = new DefaultNARBuilder().build();
        
        //PhysicsModel model = new Car();
        //model = new LiquidTimer();
        
        new NARPhysicsDemo(n).start(15, 1500);
        
        
    }
}
