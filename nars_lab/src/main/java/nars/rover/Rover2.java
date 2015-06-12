package nars.rover;

import automenta.vivisect.Video;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.model.impl.Default;
import nars.nal.Task;
import nars.nal.nal8.operator.NullOperator;
import nars.nal.nal8.Operation;
import nars.nal.term.Term;
import nars.rover.jbox2d.TestbedPanel;
import nars.rover.jbox2d.TestbedSettings;
import nars.rover.jbox2d.j2d.SwingDraw;
import nars.util.data.random.XORShiftRandom;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * NARS Rover
 *
 * @author me
 */
public class Rover2 extends PhysicsModel {


    float curiosity = 0.05f;

    /* how often to input mission, in frames */
    int missionPeriod = 50;

    boolean wraparound = false;

    public RoverModel rover;
    final NAR nar;
    int mission = 0;
    final int angleResolution = 18;


    public static void main(String[] args) {
        Global.DEBUG = false;
        boolean multithread = false;

        Video.themeInvert();

        NAR nar;
        /*if (multithread) {
            Global.THREADS = 4;
            nar = new NAR(new Neuromorphic(16).simulationTime()
                    .setConceptBagSize(1500).setSubconceptBagSize(4000)
                    .setNovelTaskBagSize(512)
                    .setTermLinkBagSize(150)
                    .setTaskLinkBagSize(60)
                    .setInternalExperience(null));
            nar.setCyclesPerFrame(128);
        }
        else*/ {
            Global.THREADS = 1;
            nar = new NAR(new Default().simulationTime().
                    setConceptBagSize(2500).
                    setSubconceptBagSize(12000).
                    setNovelTaskBagSize(256));
            nar.param.inputsMaxPerCycle.set(100);
            nar.param.conceptsFiredPerCycle.set(4);
            nar.setCyclesPerFrame(4);
        }

        //NAR nar = new CurveBagNARBuilder().

        //NAR nar = new Discretinuous().temporalPlanner(8, 64, 16).


        //new NARPrologMirror(nar, 0.95f, true, true, false);



        float framesPerSecond = 20f;

        nar.param.shortTermMemoryHistory.set(2);
        nar.param.temporalRelationsMax.set(3);

        (nar.param).outputVolume.set(3);
        (nar.param).duration.set(nar.getCyclesPerFrame() * 5);
        //nar.param.budgetThreshold.set(0.02);
        //nar.param.confidenceThreshold.set(0.02);
        (nar.param).conceptForgetDurations.set(5f);
        (nar.param).taskLinkForgetDurations.set(10f);
        (nar.param).termLinkForgetDurations.set(10f);
        (nar.param).novelTaskForgetDurations.set(10f);

        final Rover2 theRover = new Rover2(nar);


        //new NARPrologMirror(nar,0.75f, true).temporal(true, true);
        //ItemCounter removedConcepts = new ItemCounter(nar, Events.ConceptForget.class);
        // RoverWorld.world= new RoverWorld(rv, 48, 48);
        new NARPhysics<Rover2>(nar, 1.0f / framesPerSecond, theRover ) {

            @Override
            public void frame() {
                super.frame();

                nar.memory.timeSimulationAdd(1);

            }


            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyChar() == 'm') {
                    theRover.mission = (theRover.mission + 1) % 2;
                    System.out.println("Mission: " + theRover.mission);
                } else if (e.getKeyChar() == 'g') {
                    System.out.println(nar.memory.cycle);
                    //removedConcepts.report(System.out);
                }

//                if (e.getKeyCode() == KeyEvent.VK_UP) {
//                    if(!Rover2.allow_imitate) {
//                        nar.addInput("motor(linear,1). :|:");
//                    } else {
//                        nar.addInput("motor(linear,1)!");
//                    }
//                }
//                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//                    if(!Rover2.allow_imitate) {
//                        nar.addInput("motor(linear,-1). :|:");
//                    } else {
//                        nar.addInput("motor(linear,-1)!");
//                    }
//                }
//                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
//                    if(!Rover2.allow_imitate) {
//                        nar.addInput("motor(turn,-1). :|:");
//                    } else {
//                        nar.addInput("motor(turn,-1)!");
//                    }
//                }
//                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
//                    if(!Rover2.allow_imitate) {
//                        nar.addInput("motor(turn,1). :|:");
//                    } else {
//                        nar.addInput("motor(turn,1)!");
//                    }
//                }
            }

        };

        //nar.start((long)(1000f/framesPerSecond));

        // new NWindow("Tasks",new TaskTree(nar)).show(300,600);
    }




    private static final double TWO_PI = 2 * Math.PI;

    public static double normalizeAngle(final double theta) {
        double normalized = theta % TWO_PI;
        normalized = (normalized + TWO_PI) % TWO_PI;
        if (normalized > Math.PI) {
            normalized -= TWO_PI;
        }
        return normalized;
    }

    int cnt = 0;

    String[] angleTerms = new String[angleResolution*2];

    public String angleTerm(final float a) {
        float h = (float) normalizeAngle(a);
        h /= MathUtils.PI;
        int i = (int) (h * angleResolution / 2f);
        String t;
        final int ha = angleResolution;

        if (i == 0) {
            t = "forward";
        } else if (i == angleResolution / 4) {
            t = "left";
        } else if (i == -angleResolution / 4) {
            t = "right";
        } else if ((i == (angleResolution / 2 - 1)) || (i == -(angleResolution / 2 - 1))) {
            t = "reverse";
        } else {

            if (angleTerms[i+ha] == null) {

                String s;

                if (i == 0) s = "(forward, 0)"; //center is special
                else
                    s = "(" + ((i < 0) ? "left" : "right") + ',' + Math.abs(i) + ")";

                angleTerms[i+ha] = s;
            }
            t = angleTerms[i+ha];
        }

        return t;
    }

    /**
     * maps a value (which must be in range 0..1.0) to a term name
     */
    public static String f4(double p) {
        if (p < 0) {
            throw new RuntimeException("Invalid value for: " + p);
            //p = 0;
        }
        if (p > 0.99f) {
            p = 0.99f;
        }
        int i = (int) (p * 10f);
        switch (i) {
            case 9:
                return "(n,5)";
            case 8:
            case 7:
                return "(n,4)";
            case 6:
            case 5:
                return "(n,3)";
            case 4:
            case 3:
                return "(n,2)";
            case 2:
            case 1:
                return "(n,1)";
            default:
                return "(n,0)";
        }
    }

    public static String f(double p) {
        if (p < 0) {
            throw new RuntimeException("Invalid value for: " + p);
            //p = 0;
        }
        if (p > 0.99f) {
            p = 0.99f;
        }
        int i = (int) (p * 10f);
        switch (i) {
            case 9:
                return "(n,9)";
            case 8:
                return "(n,8)";
            case 7:
                return "(n,7)";
            case 6:
                return "(n,6)";
            case 5:
                return "(n,5)";
            case 4:
                return "(n,4)";
            case 3:
                return "(n,3)";
            case 2:
                return "(n,2)";
            case 1:
                return "(n,1)";
            default:
                return "(n,0)";
        }
    }

    public static enum Material implements SwingDraw.DrawProperty {

        food, wall, Block;

        static final Color foodStroke = new Color(0.25f, 1f, 0.25f);
        static final Color foodFill = new Color(0.15f, 0.9f, 0.15f);

        static final Color wallStroke = new Color(0.25f, 0.25f, 0.25f);
        static final Color wallFill = new Color(0.5f, 0.5f, 0.5f);

        @Override
        public void before(Body b, SwingDraw d) {
            switch (this) {
                case food:
                    d.setStrokeColor(foodStroke);
                    d.setFillColor(foodFill);
                    break;
                case wall:
                    d.setStrokeColor(wallStroke);
                    d.setFillColor(wallFill);
                    break;
            }
        }
    }

    public Rover2(NAR nar) {
        this.nar = nar;
    }


    protected void thrustRelative(float f) {
        if (f == 0) {
            rover.torso.setLinearVelocity(new Vec2());
        } else {
            rover.thrust(0, f * linearThrustPerCycle);
        }
    }

    protected void rotateRelative(float f) {
        rover.rotate(f * angularSpeedPerCycle);
    }

    protected void addAxioms() {

        nar.input("<{left,right,forward,reverse} --> direction>.");
        nar.input("<{wall,empty,food} --> material>.");
        //nar.input("<{0,x,xx,xxx,xxxx,xxxxx,xxxxxx,xxxxxxx,xxxxxxxx,xxxxxxxxx,xxxxxxxxxx} --> magnitude>.");
        nar.input("<{0,1,2,3,4,5,6,7,8,9} --> magnitude>.");

        nar.input("< ( ($n,#x) &| ($n,#y) ) =/> lessThan(#x,#y) >?");

        for (int i = 0; i < 4; i++) {
            String x = "lessThan(" + XORShiftRandom.global.nextInt(10) + "," +
                    XORShiftRandom.global.nextInt(10) + ")?";

            nar.input(x);
        }
//        nar.input("<0 <-> x>. %0.60;0.60%");
//        nar.input("<x <-> xx>. %0.60;0.60%");
//        nar.input("<xx <-> xxx>. %0.60;0.60%");
//        nar.input("<xxx <-> xxxx>. %0.60;0.60%");
//        nar.input("<xxxx <-> xxxxx>. %0.60;0.60%");
//        nar.input("<xxxxx <-> xxxxxx>. %0.60;0.60%");
//        nar.input("<xxxxxx <-> xxxxxxx>. %0.60;0.60%");
//        nar.input("<xxxxxxx <-> xxxxxxxxx>. %0.60;0.60%");
//        nar.input("<xxxxxxxx <-> xxxxxxxxxx>. %0.60;0.60%");
//        nar.input("<0 <-> xxxxxxxxx>. %0.00;0.90%");

    }

    protected void inputMission() {

        addAxioms();

        try {
            if (mission == 0) {
                //seek food
                curiosity = 0.05f;

                nar.goal(0.95f, 0.9f, "goal(food)", 1.00f, 0.99f);

                nar.input("goal(food)! %1.00;0.99%");
                nar.input("goal(stop)! %0.00;0.99%");
                //nar.addInput("Wall! %0.00;0.50%");
                nar.input("goal(feel)! %1.00;0.70%");
            } else if (mission == 1) {
                //rest
                curiosity = 0;
                nar.input("goal(stop)! %1.00;0.99%");
                nar.input("goal(food)! %0.00;0.99%");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //..
    }

    @Override
    public void step(float timeStep, TestbedSettings settings, TestbedPanel panel) {
        cnt++;

        super.step(timeStep, settings, panel);

        rover.step();

    }

    public class RoverPanel extends JPanel {

        public class InputButton extends JButton implements ActionListener {

            private final String command;

            public InputButton(String label, String command) {
                super(label);
                addActionListener(this);
                //this.addKeyListener(this);
                this.command = command;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                nar.input(command);
            }

        }

        public RoverPanel(RoverModel rover) {
            super(new BorderLayout());

            {
                JPanel motorPanel = new JPanel(new GridLayout(0, 2));

//                motorPanel.add(new InputButton("Stop", "motor(stop). :|:"));
//                motorPanel.add(new InputButton("Forward", "motor(forward). :|:"));
//                motorPanel.add(new InputButton("TurnLeft", "motor(turn,left). :|:"));
//                motorPanel.add(new InputButton("TurnRight", "motor(turn,right). :|:"));
//                motorPanel.add(new InputButton("Backward", "motor(backward). :|:"));
                add(motorPanel, BorderLayout.SOUTH);
            }
        }

    }


    public RoverWorld world;
    public static int sz = 48;

    @Override
    public void initTest(boolean deserialized) {
        getWorld().setGravity(new Vec2());
        getWorld().setAllowSleep(false);

        //world = new ReactorWorld(this, 32, sz, sz*2);
        world = new FoodSpawnWorld1(this, 32, sz, sz);
        //world = new GridSpaceWorld(this, GridSpaceWorld.newMazePlanet());

        rover = new RoverModel(this, this);

        //new NWindow("Rover Control", new RoverPanel(rover)).show(300, 200);
        addAxioms();
        addOperators();

        randomAction();
    }

    public float linearThrustPerCycle = 15f;
    public float angularSpeedPerCycle = 0.24f;

    public static boolean allow_imitate = true;

    static final ArrayList<String> randomActions = new ArrayList<>();

    static {
        String p = "$0.99;0.75;0.90$ ";
        randomActions.add("motor(left)!");
        randomActions.add(p + "motor(left,left)!");
        randomActions.add("motor(right)!");
        randomActions.add(p + "motor(right,right)!");
        //randomActions.add("motor(forward,forward)!"); //too much actions are not good, 
        randomActions.add(p + "motor(forward)!"); //however i would agree if <motor(forward,forward) --> motor(forward)>.
        //randomActions.add("motor(forward,forward)!");
        randomActions.add(p + "motor(forward)!");
        //randomActions.add("motor(reverse)!");
        randomActions.add(p + "motor(stop)!");
        //randomActions.add("motor(random)!");
    }

    protected void randomAction() {
        int candid = (int) (Math.random() * randomActions.size());
        nar.input(randomActions.get(candid));
    }

    protected void addOperators() {
        nar.on(new NullOperator("motor") {

            @Override
            protected List<Task> execute(Operation operation, Memory memory) {

                Term[] args = operation.argArray();
                Term t1 = args[0];

                float priority = operation.getTask().getPriority();

                int al = args.length;
                if (args[al-1].equals(memory.self()))
                    al--;

                String command = "";
                if (al == 1 ) {
                    command = t1.toString();
                }
                if (al == 2 ) {
                    Term t2 = args[1];
                    command = t1.toString() + "," + t2.toString();
                } else if (al == 3 ) {
                    Term t2 = args[1];
                    Term t3 = args[2];
                    command = t1.toString() + "," + t2.toString() + "," + t3.toString();
                }

                //System.out.println(operation + " "+ command);

                switch (command) {
                    case "right":
                        rotateRelative(-10);
                        break;
                    case "right,right":
                        rotateRelative(-20);
                        break;
                    case "left":
                        rotateRelative(10);
                        break;
                    case "left,left":
                        rotateRelative(20);
                        break;
                    case "forward,forward":
                        thrustRelative(3);
                        break;
                    case "forward":
                        thrustRelative(1);
                        break;
                    case "reverse":
                        thrustRelative(-1);
                        break;
                    case "stop":
                        rover.stop();
                        break;
                    case "random":
                        randomAction();
                        break;
                }

                return null;
            }
        });

    }


    @Override     public String getTestName() {
        return "NARS Rover";
    }

}
