package nars.rover;

import automenta.vivisect.Video;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.model.impl.Default;
import nars.nal.Task;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.NullOperator;
import nars.nal.term.Term;
import nars.rover.physics.TestbedPanel;
import nars.rover.physics.TestbedSettings;
import nars.rover.physics.gl.JoglDraw;
import nars.rover.robot.RoverModel;
import nars.rover.world.FoodSpawnWorld1;
import org.jbox2d.common.Color3f;
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



    /* how often to input mission, in frames */
    public int missionPeriod = 100;

    boolean wraparound = false;

    public final List<RoverModel> rovers = new ArrayList();
    final int angleResolution = 9;

    float framesPerSecond = 20f;
    PhysicsRun phy = new PhysicsRun(1.0f / framesPerSecond, this);


    public static void main(String[] args) {
        Global.DEBUG = true;
        Global.EXIT_ON_EXCEPTION = true;

        boolean multithread = false;

        Video.themeInvert();

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

        }

        //NAR nar = new CurveBagNARBuilder().

        //NAR nar = new Discretinuous().temporalPlanner(8, 64, 16).


        //new NARPrologMirror(nar, 0.95f, true, true, false);





        final Rover2 game = new Rover2();
        game.init();



        game.cycle();

        {

            NAR nar;
            nar = new NAR(new Default().simulationTime().
                    setActiveConcepts(768).
                    setNovelTaskBagSize(32).setTermLinkBagSize(32));
            nar.param.inputsMaxPerCycle.set(100);
            nar.param.conceptsFiredPerCycle.set(128);
            nar.setCyclesPerFrame(4);
            nar.param.shortTermMemoryHistory.set(2);
            nar.param.temporalRelationsMax.set(3);

            (nar.param).outputVolume.set(3);
            (nar.param).duration.set(5);
            //nar.param.budgetThreshold.set(0.02);
            //nar.param.confidenceThreshold.set(0.02);
            (nar.param).conceptForgetDurations.set(5f);
            (nar.param).taskLinkForgetDurations.set(10f);
            (nar.param).termLinkForgetDurations.set(10f);
            (nar.param).novelTaskForgetDurations.set(10f);

            game.add(new RoverModel(nar, game));
        }
        //nar.start((long)(1000f/framesPerSecond));

        // new NWindow("Tasks",new TaskTree(nar)).show(300,600);

        while (true) {
            game.cycle();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }


//        //new NARPrologMirror(nar,0.75f, true).temporal(true, true);
//        //ItemCounter removedConcepts = new ItemCounter(nar, Events.ConceptForget.class);
//        // RoverWorld.world= new RoverWorld(rv, 48, 48);
//        new NARPhysics<Rover2>(1.0f / framesPerSecond, theRover ) {
//
//            @Override
//            public void init() {
//                super.init();
//
//
//            }
//
//            @Override
//            public void frame() {
//                super.frame();
//
//
//
//            }
//
//
//            @Override
//            public void keyPressed(KeyEvent e) {
//
////                if (e.getKeyChar() == 'm') {
////                    theRover.mission = (theRover.mission + 1) % 2;
////                    System.out.println("Mission: " + theRover.mission);
////                } else if (e.getKeyChar() == 'g') {
////                    System.out.println(nar.memory.cycle);
////                    //removedConcepts.report(System.out);
////                }
//
////                if (e.getKeyCode() == KeyEvent.VK_UP) {
////                    if(!Rover2.allow_imitate) {
////                        nar.addInput("motor(linear,1). :|:");
////                    } else {
////                        nar.addInput("motor(linear,1)!");
////                    }
////                }
////                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
////                    if(!Rover2.allow_imitate) {
////                        nar.addInput("motor(linear,-1). :|:");
////                    } else {
////                        nar.addInput("motor(linear,-1)!");
////                    }
////                }
////                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
////                    if(!Rover2.allow_imitate) {
////                        nar.addInput("motor(turn,-1). :|:");
////                    } else {
////                        nar.addInput("motor(turn,-1)!");
////                    }
////                }
////                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
////                    if(!Rover2.allow_imitate) {
////                        nar.addInput("motor(turn,1). :|:");
////                    } else {
////                        nar.addInput("motor(turn,1)!");
////                    }
////                }
//            }
//
//
//
//
//        };

    }



    public void add(RoverModel r) {


        rovers.add(r);

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

    public int cnt = 0;

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
        }
        if (p > 0.99f) {
            p = 0.99f;
        }
        int i = (int) (p * 10f);
        switch (i) {
            case 9:
                return "5";
            case 8:
            case 7:
                return "4";
            case 6:
            case 5:
                return "3";
            case 4:
            case 3:
                return "2";
            case 2:
            case 1:
                return "1";
            default:
                return "0";
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
        return String.valueOf(i);
    }

    public static abstract class Material implements JoglDraw.DrawProperty {

        public static Material wall = new WallMaterial();
        public static Material food = new FoodMaterial();
        public static Material poison = new PoisonMaterial();

    }

    public interface Edible {

    }

    public static class FoodMaterial extends Material implements Edible {

        static final Color3f foodFill = new Color3f(0.15f, 0.9f, 0.15f);

        @Override
        public void before(Body b, JoglDraw d, float time) {
            d.setFillColor(foodFill);
        }

        @Override
        public String toString() {
            return "food";
        }
    }
    public static class WallMaterial extends Material {
        static final Color3f wallFill = new Color3f(0.5f, 0.5f, 0.5f);
        @Override
        public void before(Body b, JoglDraw d, float time) {
            d.setFillColor(wallFill);
        }

        @Override
        public String toString() {
            return "wall";
        }
    }
    public static class PoisonMaterial extends Material implements Edible {

        static final Color3f poisonFill = new Color3f(0.75f, 0.25f, 0.25f);

        @Override
        public void before(Body b, JoglDraw d, float time) {
            d.setFillColor(poisonFill);
        }
        @Override
        public String toString() {
            return "poison";
        }
    }

    public Rover2() {

    }



    @Override
    public void step(float timeStep, TestbedSettings settings, TestbedPanel panel) {
        cnt++;

        super.step(timeStep, settings, panel);

        for (RoverModel r : rovers) {

            r.step(1);


        }

    }

//    public class RoverPanel extends JPanel {
//
//        public class InputButton extends JButton implements ActionListener {
//
//            private final String command;
//
//            public InputButton(String label, String command) {
//                super(label);
//                addActionListener(this);
//                //this.addKeyListener(this);
//                this.command = command;
//            }
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                nar.input(command);
//            }
//
//        }
//
//        public RoverPanel(RoverModel rover) {
//            super(new BorderLayout());
//
//            {
//                JPanel motorPanel = new JPanel(new GridLayout(0, 2));
//
////                motorPanel.add(new InputButton("Stop", "motor(stop). :|:"));
////                motorPanel.add(new InputButton("Forward", "motor(forward). :|:"));
////                motorPanel.add(new InputButton("TurnLeft", "motor(turn,left). :|:"));
////                motorPanel.add(new InputButton("TurnRight", "motor(turn,right). :|:"));
////                motorPanel.add(new InputButton("Backward", "motor(backward). :|:"));
//                add(motorPanel, BorderLayout.SOUTH);
//            }
//        }
//
//    }
//

    public RoverWorld world;
    public static int sz = 48;

    public void init() {
        init(phy.model);
    }

    @Override
    public void initTest(boolean deserialized) {


        getWorld().setGravity(new Vec2());
        getWorld().setAllowSleep(false);

        //world = new ReactorWorld(this, 32, sz, sz*2);
        world = new FoodSpawnWorld1(this, 32, sz, sz);
        //world = new GridSpaceWorld(this, GridSpaceWorld.newMazePlanet());

    }

    protected void cycle() {
        phy.cycle();
    }





    @Override     public String getTestName() {
        return "NARS Rover";
    }

}
