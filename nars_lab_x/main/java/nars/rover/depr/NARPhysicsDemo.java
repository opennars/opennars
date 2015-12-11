//package nars.rover;
//
//import nars.Memory;
//import nars.NAR;
//import nars.io.in.ChangedTextInput;
//import nars.model.impl.Default;
//import nars.nal.Task;
//import nars.nal.nal8.operator.NullOperator;
//
//import nars.nal.term.Term;
//import org.jbox2d.common.MathUtils;
//
//import java.util.List;
//
//
//
//public class NARPhysicsDemo extends NARPhysics<RobotArm> {
//    private final RobotArm arm;
//
//    float shoulderAngle = 0;
//    float shoulderRange = MathUtils.PI/2f;
//    float elbowRange = MathUtils.PI/2f;
//    float elbowAngle = 0, fingerAngle = 0;
//
//    int t = 0;
//    int cangle = 0;
//    int angleResolution = 16;
//    int angleDiv = 40;
//    int trainingPeriod = 100;
//    int actionTestPeriod = 10;
//    int numAngles = 4;
//
//    float decisionThreshold = 0.3f;
//
//    boolean autonomous = false;
//    private final ChangedTextInput upperArmSensor;
//    private final ChangedTextInput lowerArmSensor;
//
//    public NARPhysicsDemo(final NAR n) {
//        super(n, 1.0f/30.0f,new RobotArm() {
//
//            @Override
//            public void sight(boolean[] hit) {
//                int totalHit = 0;
//                for (boolean x : hit)
//                    if (x) totalHit++;
//
//                if (totalHit > 0) {
//                    float th = totalHit / ((float)hit.length);
//                    //n.addInput("sight. :|: %1.00;" + Texts.n2(th) + "%");
//                }
//
//            }
//        });
//
//        arm = getModel();
//        nar.on(new NullOperator("^joint") {
//            @Override
//            protected List<Task> execute(Operation operation, Memory memory) {
//
//                Term[] args = operation.argArray();
//                if ((autonomous) || (operation.getTask().isInput())) {
//
//
//
//                    String as = args[1].toString();
//
//                    float dA = 0.1f;
//
//                    switch (args[0].toString()) {
//                        case "shoulder":
//                            float a = shoulderAngle;
//
//                            if (as.equals("left")) a -= dA;
//                            else if (as.equals("right")) a += dA;
//                            else if (as.startsWith("a")) {
//                                int i = Integer.parseInt(as.substring(1));
//                                double radians = Math.toRadians(i);
//                                a = (float) radians;
//                            }
//                            forceMoveShoulder(false, a);
//
//                            break;
//                        case "elbow":
//                            if (args[1].toString().equals("left")) elbowAngle -= dA;
//                            else if (args[1].toString().equals("right")) elbowAngle += dA;
//                            if (elbowAngle < -elbowRange) elbowAngle = -elbowRange;
//                            if (elbowAngle > elbowRange) elbowAngle = elbowRange;
//                            break;
//                    }
//                }
//
//                return super.execute(operation, memory);
//            }
//        });
//
//        upperArmSensor = new ChangedTextInput(nar);
//        lowerArmSensor = new ChangedTextInput(nar);
//    }
//
//
//    public void forceMoveShoulder(boolean addInput, float angle) {
//        int a = (int)angle;
//        if (addInput)
//            nar.input("(^joint,shoulder,a" + a + ")!");
//
//        shoulderAngle = angle;
//        if (shoulderAngle < -shoulderRange) shoulderAngle = -shoulderRange;
//        if (shoulderAngle > shoulderRange) shoulderAngle = shoulderRange;
//    }
//
//    public String angleState(String x, double v, int steps) {
//
//        v = MathUtils.reduceAngle((float)v);
//        v = ((int)(v * steps)) / steps;
//
//        double p = v / MathUtils.TWOPI;
//
//        int s = (int) Math.round(p * steps);
//        return ("<(*,angle_" + s + ") --> " + x + ">. :|:");
//    }
//
//    //boolean implication = false;
//
//
//    @Override
//    public void frame() {
//        super.frame();
//
//        /*
//        if (!implication) {
//            new Window("Implications", new SentenceGraphPanel(nar, nar.memory.executive.graph.implication)).show(500, 500);
//            implication = true;
//        }
//                */
//
//
//
//
//        if (t < trainingPeriod) {
//            (nar.param).executionThreshold.set(0.75);
//            if (t % actionTestPeriod == 0) {
//                cangle++;
//                if (cangle > numAngles) {
//                    cangle = -numAngles;
//                }
//                forceMoveShoulder(true, (angleDiv*cangle));
//            }
//        }
//        else if (t == trainingPeriod) {
//            (nar.param).executionThreshold.set(decisionThreshold);
//            System.out.println(); System.out.println(); System.out.println(); System.out.println();
//            System.out.println("AUTONOMOUS");
//            System.out.println(); System.out.println(); System.out.println(); System.out.println();
//            autonomous = true;
//        }
//
////        if (Math.random() < 0.1f) {
////            nar.addInput("(^joint,elbow,left)!");
////        }
////        if (Math.random() < 0.1f) {
////            nar.addInput("(^joint,elbow,right)!");
////        }
//        fingerAngle = (float)Math.sin(t)/2f+0.9f;
//
//
//
//        lowerArmSensor.set(angleState("lowerArm",arm.lowerArm.getAngle(),angleResolution));
//        upperArmSensor.set(angleState("upperArm",arm.upperArm.getAngle(),angleResolution));
////        if (nar.getTime() % 20 == 0) {
////            float s = (float)(Math.random() * 4f - 2f);
////            float e = (float)(Math.random() * 4f - 2f);
////            float f = (float)(Math.random() * 2f - 1f);
////        }
//        arm.set(shoulderAngle, elbowAngle, fingerAngle);
//
//        t++;
//    }
//
//    public static void main(String[] args) {
//        Default d = new Default();
//        d.setTiming(Memory.Timing.Simulation);
//        NAR n = new NAR(d);
//
//        (n.param).duration.set(20);
//        (n.param).executionThreshold.set(0.75f);
//        (n.param).outputVolume.set(5);
//        //PhysicsModel model = new Car();
//        //model = new LiquidTimer();
//
//        new NARPhysicsDemo(n).start(5, 50);
//
//
//    }
//}
