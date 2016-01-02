package nars.rover;

import nars.Global;
import nars.rover.physics.TestbedPanel;
import nars.rover.physics.TestbedSettings;
import nars.rover.physics.gl.JoglAbstractDraw;
import nars.rover.robot.Robotic;
import nars.time.SimulatedClock;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;

import java.util.List;

/**
 * NARS Rover
 *
 * @author me
 */
public class Sim extends PhysicsModel {


    private final SimulatedClock clock;
    /* how often to input mission, in frames */
    public int missionPeriod = 8;

    boolean wraparound = false;

    public final List<Robotic> robots = Global.newArrayList();
    final static int angleResolution = 13;


    PhysicsRun phy = new PhysicsRun(10, this);
    private long delayMS;
    private float fps;
    private boolean running = false;



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


    public void setFPS(float f) {
        this.fps = f;
        delayMS = (long) (1000f / fps);
    }

    public void run(float fps) {
        setFPS(fps);

        running = true;
        while (running) {
            cycle();
            try {
                Thread.sleep(delayMS);
            } catch (InterruptedException e) {
            }
        }

    }

    public void stop() {
        running = false;
    }


    public void add(Robotic r) {
        r.init(this);
        robots.add(r);
    }

    public void remove(Body r) {
        getWorld().destroyBody(r);
    }


    private static final double TWO_PI = 2 * Math.PI;

    public static double normalizeAngle(final double theta) {
        double normalized = theta % TWO_PI;
        normalized = (normalized + TWO_PI) % TWO_PI;
        if (normalized > Math.PI) {
            normalized -= TWO_PI;
        }
        if (normalized < 0) {
            normalized += TWO_PI;
        }
        return normalized;
    }

    public int cnt = 0;

    static String[] angleTerms = new String[angleResolution];

    public static String angleTerm(final float a) {
        float h = (float) normalizeAngle(a);
        h /= MathUtils.PI*2.0f;
        int i = (int) (h * angleResolution / 1f);
        String t;
        final int ha = angleResolution;

//        if (i == 0) {
//            t = "forward";
//        } else if (i == angleResolution / 4) {
//            t = "left";
//        } else if (i == -angleResolution / 4) {
//            t = "right";
//        } else if ((i == (angleResolution / 2 - 1)) || (i == -(angleResolution / 2 - 1))) {
//            t = "reverse";
//        } else {


            if (angleTerms[i] == null) {
                //angleTerms[i] = "(angle," + i + ")";

                angleTerms[i] = "a" + i;

//
//                String s;
//
//                if (i == 0) s = "(forward, 0)"; //center is special
//                else {
//                    if (i > angleResolution/2) i = -(angleResolution/2 - i);
//                    s = "(" + ((i < 0) ? "left" : "right") + ',' + Math.abs(i) + ")";
//                }
//
//                angleTerms[i+ha] = s;
            }

            t = angleTerms[i];
        //}

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

    @Override
    public BodyDef bodyDefCallback(BodyDef body) {
        return body;
    }

    @Override
    public FixtureDef fixDefCallback(FixtureDef fixture) {
        return fixture;
    }


    public interface Edible {

    }

    public static class FoodMaterial extends Material implements Edible {

        static final Color3f foodFill = new Color3f(0.15f, 0.6f, 0.15f);

        @Override
        public void before(Body b, JoglAbstractDraw d, float time) {

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
        public void before(Body b, JoglAbstractDraw d, float time) {
            d.setFillColor(wallFill);
        }

        @Override
        public String toString() {
            return "wall";
        }
    }
    public static class PoisonMaterial extends Material implements Edible {

        static final Color3f poisonFill = new Color3f(0.45f, 0.15f, 0.15f);

        @Override
        public void before(Body b, JoglAbstractDraw d, float time) {
            d.setFillColor(poisonFill);
        }
        @Override
        public String toString() {
            return "poison";
        }
    }

    public Sim(SimulatedClock clock, RoverWorld world) {
        this.clock = clock;

        this.world = world;

        init(phy.model);

        cycle();

        this.world.init(this);
    }



    @Override
    public void step(float timeStep, TestbedSettings settings, TestbedPanel panel) {
        cnt++;

        super.step(timeStep, settings, panel);

        for (Robotic r : robots) {
            r.step(1);
        }

        clock.add(1);

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

    @Override
    public void initTest(boolean deserialized) {


        getWorld().setGravity(new Vec2());
        getWorld().setAllowSleep(false);

    }

    protected final void cycle() {
        phy.cycle(fps);
    }





    @Override     public String getTestName() {
        return "NARS Rover";
    }

}
