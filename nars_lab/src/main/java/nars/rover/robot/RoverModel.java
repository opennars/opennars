/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rover.robot;

import nars.Memory;
import nars.NAR;
import nars.Symbols;
import nars.budget.Budget;
import nars.clock.SimulatedClock;
import nars.concept.Concept;
import nars.event.FrameReaction;
import nars.io.in.ChangedTextInput;
import nars.nal.nal2.Property;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.NullOperator;
import nars.process.TaskProcess;
import nars.rl.gng.NeuralGasNet;
import nars.rover.PhysicsModel;
import nars.rover.RoverEngine;
import nars.rover.RoverEngine.Material;
import nars.rover.depr.RobotArm;
import nars.rover.physics.gl.JoglDraw;
import nars.rover.physics.j2d.SwingDraw.LayerDraw;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.DefaultTruth;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author me
 */
public class RoverModel {

    int mission = 0;
    //public float curiosity = 0.1f;
    int motionPeriod = 3;


    public final Body torso;
    Vec2 pooledHead = new Vec2();
    Vec2 point1 = new Vec2();
    Vec2 point2 = new Vec2();
    Vec2 d = new Vec2();
    Deque<Vec2> positions = new ArrayDeque();
    List<VisionRay> vision = new ArrayList();
    //public class ChangedNumericInput //discretizer
    private final RoverEngine sim;
    public final NAR nar;
    private final ChangedTextInput feltAngularVelocity;
    private final ChangedTextInput feltOrientation;
    private final ChangedTextInput feltSpeed;
    private final ChangedTextInput feltSpeedAvg;
    private final ChangedTextInput mouthInput;
    private final World world;
    private DebugDraw draw = null;

    final double minVisionInputProbability = 0.9f;
    final double maxVisionInputProbability = 1.0f;

    //float tasteDistanceThreshold = 1.0f;
    final static int retinaPixels = 12;


    int retinaRaysPerPixel = 4; //rays per vision sensor

    float aStep = (float)(Math.PI*2f) / retinaPixels;

    float L = 25f; //vision distance

    Vec2 mouthPoint = new Vec2(3.0f, 0); //0.5f);
    @Deprecated int distanceResolution = 6;
    float mass = 2f;

    double mouthArc = Math.PI/6f; //in radians
    float biteDistanceThreshold = 0.05f;
    Vec2[] vertices = {new Vec2(3.0f, 0.0f), new Vec2(-1.0f, +2.0f), new Vec2(-1.0f, -2.0f)};

    float linearDamping = 0.9f;
    float angularDamping = 0.6f;

    float restitution = 0.9f; //bounciness
    float friction = 0.5f;

    public float linearThrustPerCycle = 8f;
    public float angularSpeedPerCycle = 0.44f * 0.25f;


    /** maps a scalar changing quality to a frequency value, with autoranging
     *  determined by a history window of readings
     * */
    public static class AutoRangeTruthFrequency {
        NeuralGasNet net;
        float threshold;

        public AutoRangeTruthFrequency(float thresho) {
            net = new NeuralGasNet(1,4);
            this.threshold = thresho;
        }

        /** returns estimated frequency */
        public float observe(float value) {
            if (Math.abs(value) < threshold ) {
                return 0.5f;
            }

            learn(value);

            double[] range = net.getDimensionRange(0);
            return (float) getFrequency(range, value);
        }

        protected double getFrequency(double[] range, float value) {
            double proportional;

            if ((range[0] == range[1]) || (!Double.isFinite(range[0])))
                proportional = 0.5;
            else
                proportional = (value - range[0]) / (range[1] - range[0]);

            if (proportional > 1f) proportional = 1f;
            if (proportional < 0f) proportional = 0f;

            return proportional;
        }

        protected void learn(float value) {
            net.learn(value);
        }


    }

    public static class BipolarAutoRangeTruthFrequency extends AutoRangeTruthFrequency {

        public BipolarAutoRangeTruthFrequency() {
            this(0);
        }

        public BipolarAutoRangeTruthFrequency(float thresh) {
            super(thresh);
        }

        @Override protected void learn(float value) {
            //learn the absolute value because the stimate will include the negative range as freq < 0.5f
            super.learn(Math.abs(value));
            super.learn(0);
        }

        protected double getFrequency(double[] range, float value) {
            double proportional;

            if ((0 == range[1]) || (!Double.isFinite(range[0])))
                proportional = 0.5;
            else
                proportional = ((value) / (range[1]))/2f + 0.5f;

            //System.out.println(value + "-> +-" + range[1] + " " + " -> " + proportional);

            if (proportional > 1f) proportional = 1f;
            if (proportional < 0f) proportional = 0f;

            return proportional;
        }
    }

    public static class SimpleAutoRangeTruthFrequency  {
        private final Compound term;
        private final NAR nar;
        private final AutoRangeTruthFrequency model;

        public SimpleAutoRangeTruthFrequency(NAR nar, Compound term, AutoRangeTruthFrequency model) {
            super();
            this.term = term;
            this.nar = nar;
            this.model = model;

            model.net.setEpsW(0.04f);
            model.net.setEpsN(0.01f);
        }

        public void observe(float value) {
            float freq = model.observe(value);
            //System.out.println(range[0] + ".." + range[1]);
            //System.out.println(b);

            float conf = 0.75f;
            Task t;
            nar.inputDirect(t = nar.task(term).belief().present().truth(freq, conf).get());

            //System.out.println(t);
        }
    }

    final SimpleAutoRangeTruthFrequency linearVelocity;
    final SimpleAutoRangeTruthFrequency motionAngle;
    final SimpleAutoRangeTruthFrequency facingAngle;

    public class RoverMaterial extends Material {

        final String id;
        private final Color3f color;
        private final Color c;

        public RoverMaterial(String id) {
            this.id = id;



            float h = ((id + id).hashCode() % 10)/10f;
            c = Color.getHSBColor(h, 0.5f, 0.95f);
            color = new Color3f();

        }

        @Override
        public void before(Body b, JoglDraw d, float time) {
            float bb = nar.memory.emotion.busy() * 0.5f + 0.5f;
            color.set(c.getRed()/256.0f * bb, c.getGreen()/256.0f * bb, c.getBlue()/256.0f * bb);
            d.setFillColor(color);
        }

        @Override
        public String toString() {
            return id;
        }
    }

    //public class DistanceInput extends ChangedTextInput
    public RoverModel(String id, NAR nar, PhysicsModel p) {
        this.sim = (RoverEngine) p;

        this.nar = nar;
        

        this.world = sim.getWorld();

        mouthInput = new ChangedTextInput(nar);
        feltAngularVelocity = new ChangedTextInput(nar);
        feltOrientation = new ChangedTextInput(nar);
        feltSpeed = new ChangedTextInput(nar);
        feltSpeedAvg = new ChangedTextInput(nar);

    

        PolygonShape shape = new PolygonShape();
        shape.set(vertices, vertices.length);
        //shape.m_centroid.set(bodyDef.position);
        BodyDef bd = new BodyDef();
        bd.linearDamping=(linearDamping);
        bd.angularDamping=(angularDamping);
        bd.type = BodyType.DYNAMIC;
        bd.position.set(0, 0);
        torso = p.getWorld().createBody(bd);
        Fixture f = torso.createFixture(shape, mass);
        f.setRestitution(restitution);
        f.setFriction(friction);

        linearVelocity = new SimpleAutoRangeTruthFrequency(nar, nar.term("<motion-->[linear]>"), new AutoRangeTruthFrequency(0.02f));
        motionAngle = new SimpleAutoRangeTruthFrequency(nar, nar.term("<motion-->[angle]>"), new BipolarAutoRangeTruthFrequency());
        facingAngle = new SimpleAutoRangeTruthFrequency(nar, nar.term("<motion-->[facing]>"), new BipolarAutoRangeTruthFrequency());

        torso.setUserData(new RoverMaterial(id));

        //for (int i = -pixels / 2; i <= pixels / 2; i++) {
        for (int i = 0; i < retinaPixels; i++) {
            final int ii = i;
            final float angle = /*MathUtils.PI / 2f*/ aStep * i;
            final boolean eats = ((angle < mouthArc / 2f) || (angle > (Math.PI*2f) - mouthArc/2f));

            //System.out.println(i + " " + angle + " " + eats);

            VisionRay v = new VisionRay(torso,
                    /*eats ?*/ mouthPoint /*: new Vec2(0,0)*/,
                    angle, aStep, retinaRaysPerPixel, L, distanceResolution) {


                @Override
                public void onTouch(Body touched, float di) {
                    if (touched == null) return;

                    if (touched.getUserData() instanceof RoverEngine.Edible) {

                        if (eats) {

                            if (di <= biteDistanceThreshold)
                                eat(touched);
                            /*} else if (di <= tasteDistanceThreshold) {
                                //taste(touched, di );
                            }*/
                        }
                    }
                }
            };
            v.sparkColor = new Color3f(0.5f, 0.4f, 0.4f);
            v.normalColor = new Color3f(0.4f, 0.4f, 0.4f);

            ((JoglDraw)p.draw()).addLayer(v);

            vision.add(v);
        }

        addMotorController();
        //addMotorOperator();

        addAxioms();

        //String p = "$0.99;0.75;0.90$ ";

        //randomActions.add("motor($direction)!");
        //TODO : randomActions.add("motor($direction,$direction)!");

        randomActions.add("motor(left)!");
        //randomActions.add("motor(left,left)!");
        randomActions.add("motor(right)!");
        //randomActions.add("motor(right,right)!");
        //randomActions.add("motor(forward,forward)!"); //too much actions are not good,
        randomActions.add("motor(forward)!"); //however i would agree if <motor(forward,forward) --> motor(forward)>.
        //randomActions.add("motor(forward,forward)!");
        //randomActions.add("motor(forward)!");
        randomActions.add("motor(reverse)!");
        randomActions.add("motor(stop)!");
        //randomActions.add("motor(random)!");


    }


    protected void thrustRelative(float f) {
        if (f == 0) {
            torso.setLinearVelocity(new Vec2());
        } else {
            thrust(0, f * linearThrustPerCycle);
        }
    }


    protected void rotateRelative(float f) {
        rotate(f * angularSpeedPerCycle);
    }

    protected void curious(float freq, float conf) {
        nar.input("motor(random)! %" + freq + "|" + conf + "%");
    }
    protected void addAxioms() {

        //alpha curiosity parameter
        if (nar.time() < 10) {
            curious(1.0f, 0.65f);
        }
        else {
            curious(0.75f, 0.15f);
        }



        //nar.input("<{left,right,forward,reverse} --> direction>.");
        //nar.input("<{wall,empty,food,poison} --> material>.");
        //nar.input("<{0,x,xx,xxx,xxxx,xxxxx,xxxxxx,xxxxxxx,xxxxxxxx,xxxxxxxxx,xxxxxxxxxx} --> magnitude>.");
        //nar.input("<{0,1,2,3,4,5,6,7,8,9} --> magnitude>.");

        //nar.input("< ( ($n,#x) &| ($n,#y) ) =/> lessThan(#x,#y) >?");

        /*
        for (int i = 0; i < 2; i++) {
            String x = "lessThan(" + XORShiftRandom.global.nextInt(10) + "," +
                    XORShiftRandom.global.nextInt(10) + ")?";

            nar.input(x);
        }
        */

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

    final ArrayList<String> randomActions = new ArrayList<>();



    public void randomAction() {
        int candid = (int) (Math.random() * randomActions.size());
        nar.input(randomActions.get(candid));
    }

    @FunctionalInterface
    public interface ConceptDesire {
        public float getDesire(Concept c);
    }

    public abstract static class CycleDesire extends FrameReaction {

        private final ConceptDesire desireFunction;
        private final Term term;
        private final NAR nar;
        transient private Concept concept;
        boolean feedbackEnabled = true;
        float threshold = 0f;

        /** budget to apply if the concept is not active */
        private Budget remember = new Budget(0.5f, Symbols.GOAL, new DefaultTruth(1.0f, 0.9f));

        public CycleDesire(String term, ConceptDesire desireFunction, NAR nar) {
            this(desireFunction, (Term)nar.term(term), nar);
        }

        public CycleDesire(ConceptDesire desireFunction, Term term, NAR nar) {
            super(nar);
            this.desireFunction = desireFunction;
            this.nar = nar;
            this.term = term;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + concept + "]";
        }

        public void setFeedback(boolean feedback) {
            this.feedbackEnabled = feedback;
        }

        public float getDesireIfConceptMissing() { return 0; }

        /** @return feedback belief value, or Float.NaN to not apply it */
        abstract float onFrame(float desire);

        @Override
        public void onFrame() {

            Concept c = getConcept();

            if (c != null) {
                float d = desireFunction.getDesire(c);

                if (d > threshold) {
                    float feedback = onFrame(d);

                    if (feedbackEnabled && Float.isFinite(feedback))
                        nar.inputDirect(getFeedback(feedback));
                }
            }
            else {
                onFrame(getDesireIfConceptMissing());
            }

        }

        public TaskSeed getFeedback(float feedback) {
            //since it's expectation, using 0.99 conf is like preserving the necessary truth as was desired, if feedback = desire
            return nar.task((Compound) term).present().belief().truth(feedback, 0.99f);
        }

        public Concept getConcept() {

            if (concept != null && concept.isDeleted()) {
                concept = null;
            }

            if (concept == null) {
                concept = nar.concept(term);
            }

            //try remembering it
            if (concept == null) {
                concept = nar.memory.conceptualize(term, remember);
            }

            return concept;
        }
    }

    /** bipolar cycle desire, which resolves two polar opposite desires into one */
    public abstract static class BiCycleDesire  {

        private final CycleDesire positive;
        private final CycleDesire negative;

        public float positiveDesire, negativeDesire;
        float threshold = 0f;
        private NAR nar;

        public BiCycleDesire(String positiveTerm, String negativeTerm, ConceptDesire desireFunction, NAR n) {
            this.nar = n;
            this.positive = new CycleDesire(positiveTerm, desireFunction, n) {

                @Override
                float onFrame(final float desire) {
                    positiveDesire = desire;
                    return Float.NaN;
                }
            };
            //this will be executed directly after positive, so we put the event handler in negative
            this.negative = new CycleDesire(negativeTerm, desireFunction, n) {

                @Override
                float onFrame(float negativeDesire) {
                    BiCycleDesire.this.negativeDesire = negativeDesire;

                    frame(positiveDesire, negativeDesire);


                    return Float.NaN;
                }

            };
        }

        protected void frame(final float positiveDesire, final float negativeDesire) {

            float net = positiveDesire - negativeDesire;
            boolean isPos = (net > 0);
            if (!isPos)
                net = -net;

            float posFeedback = 0, negFeedback = 0;
            if (net > threshold) {
                final float feedback = onFrame(net, isPos);
                if (Float.isFinite(feedback)) {
                    if (isPos) {
                        posFeedback = feedback;
                        nar.inputDirect(this.positive.getFeedback(posFeedback));
                        nar.inputDirect(this.negative.getFeedback(0));

//                        //counteract the interference
//                        negFeedback = (negativeDesire - (positiveDesire - feedback));
//                        if (negFeedback < 0) negFeedback = 0;
//                        Task iit = this.negative.getFeedback(negFeedback)
//                                .goal()
//                                .truth(negFeedback, 0.75f) //adjust confidence too
//                                .get();
//
//                        nar.inputDirect(iit);

                    } else {
                        negFeedback = feedback;
                        nar.inputDirect(this.negative.getFeedback(negFeedback));
                        nar.inputDirect(this.positive.getFeedback(0));

//                        //counteract the interference
//                        posFeedback = (positiveDesire - (negativeDesire - feedback));
//                        if (posFeedback < 0) posFeedback = 0;
//                        Task iit = this.positive.getFeedback(posFeedback)
//                                .goal()
//                                .truth(posFeedback, 0.75f)
//                                .get();
//
//                        nar.inputDirect(iit);


                    }
                }

            }

        }

        abstract float onFrame(float desire, boolean positive);

    }

    public static final ConceptDesire strongestTask = (c ->  c.getGoals().getMeanProjectedExpectation(c.time()) );

    protected void addMotorController() {
        new CycleDesire("motor(random)", strongestTask, nar) {
            @Override float onFrame(float desire) {
                //variable causes random movement
                double v = Math.random();
                if (v > (desire - 0.5f)*2f) {
                    return Float.NaN;
                }

                //System.out.println(v + " " + (desire - 0.5f)*2f);

                float strength = 0.65f;
                float negStrength = 1f - strength;
                String tPos = "%" + strength + "|" + desire + "%";
                String tNeg = "%" + negStrength + "|" + desire + "%";

                v = Math.random();
                if (v < 0.25f) {
                    nar.inputDirect(nar.task("motor(left)! " + tPos));
                    nar.inputDirect(nar.task("motor(right)! " + tNeg));
                } else if (v < 0.5f) {
                    nar.inputDirect(nar.task("motor(left)! " + tNeg));
                    nar.inputDirect(nar.task("motor(right)! " + tPos));
                } else if (v < 0.75f) {
                    nar.inputDirect(nar.task("motor(forward)! " + tPos));
                    nar.inputDirect(nar.task("motor(reverse)! " + tNeg));
                } else {
                    nar.inputDirect(nar.task("motor(forward)! " + tNeg));
                    nar.inputDirect(nar.task("motor(reverse)! " + tPos));
                }
                return desire;
            }
        };
        /*new CycleDesire("motor(forward,SELF)", strongestTask, nar) {
            @Override
            float onFrame(float desire) {
                thrustRelative(desire * linearThrustPerCycle);
                return desire;
            }

        };*/
        /*new CycleDesire("motor(reverse,SELF)", strongestTask, nar) {
            @Override float onCycle(float desire) {
                thrustRelative(desire * -linearThrustPerCycle);
                return desire;
            }
        };*/

        new BiCycleDesire("motor(forward,SELF)", "motor(reverse,SELF)", strongestTask,nar) {

            @Override
            float onFrame(float desire, boolean positive) {
                if (positive) {
                    thrustRelative(desire * linearThrustPerCycle);
                }
                else {
                    thrustRelative(desire * -linearThrustPerCycle);
                }
                return desire;
            }
        };

        new BiCycleDesire("motor(left,SELF)", "motor(right,SELF)", strongestTask,nar) {

            @Override
            float onFrame(float desire, boolean positive) {

                if (positive) {
                    rotateRelative(+80*desire);
                }
                else {
                    rotateRelative(-80*desire);
                }
                return desire;
            }
        };
//
//        new CycleDesire("motor(left,SELF)", strongestTask, nar) {
//            @Override float onCycle(float desire) {
//
//                return desire;
//            }
//        };
//        new CycleDesire("motor(right,SELF)", strongestTask, nar) {
//            @Override float onCycle(float desire) {
//
//                return desire;
//            }
//        };
    }

    protected void addMotorOperator() {
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

                if (command.equals("$1")) {
                    //variable causes random movement
                    double v = Math.random();
                    if (v < 0.25f) {
                        command = "left";
                    } else if (v < 0.5f) {
                        command = "right";
                    } else if (v < 0.75f) {
                        command = "forward";
                    } else {
                        command = "reverse";
                    }
                }

                int rspeed = 30;
                switch (command) {
                    case "right":
                        rotateRelative(-rspeed);
                        break;
                    case "right,right":
                        rotateRelative(-rspeed*2);
                        break;
                    case "left":
                        rotateRelative(+rspeed);
                        break;
                    case "left,left":
                        rotateRelative(+rspeed*2);
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
                        stop();
                        break;
                    case "random":
                        randomAction();
                        break;
                }

                return null;
            }
        });

    }

    public void inputMission() {

        addAxioms();

        nar.goal("<goal --> [health]>", 1.00f, 0.90f);
        nar.goal("<goal --> [health]>. :|:", 0.50f, 0.99f); //reset

        try {
            if (mission == 0) {
                //seek food
                //curiosity = 0.05f;

                //nar.goal("<goal --> food>", 1.00f, 0.90f);
                nar.input("<goal --> [food]>! :|:");
                nar.goal("<goal --> [food]>. :|:", 0.50f, 0.99f); //reset


                //nar.input("goal(food)! %1.00;0.99%");
                //nar.input("goal(stop)! %0.00;0.99%");
                //nar.addInput("Wall! %0.00;0.50%");
                //nar.input("goal(see)! %1.00;0.70%");
                //nar.input("goal(moved)! %1.00;0.70%");
                //nar.input("goal(rotated)! %1.00;0.70%");
            } else if (mission == 1) {
                //rest
                //curiosity = 0;
                nar.input("moved(0)! %1.00;0.9%");
                nar.input("<goal --> [food]>! %0.00;0.9%");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //..
    }



    public void taste(Body eatable, float distance) {
//        Rover2.Material m = (Rover2.Material)eatable.getUserData();
//        if (m instanceof Rover2.FoodMaterial) {
//            float c = 1.0f / (1.0f + (distance - biteDistanceThreshold) / (tasteDistanceThreshold - biteDistanceThreshold));
//            mouthInput.set("<goal --> food>. :|: %0." + (0.5f + c / 2f) + ";0." + (c / 2f) + "%");
//        }
    }

    public void eat(Body eaten) {
        Material m = (Material)eaten.getUserData();
        if (m instanceof RoverEngine.FoodMaterial) {
            nar.input("<goal --> [food]>. :|: %0.90;0.90%");
            nar.input("<goal --> [health]>. :|: %0.90;0.90%");
        }
        else if (m instanceof RoverEngine.PoisonMaterial) {
            nar.input("<goal --> [health]>. :|: %0.00;0.90%");
        }
        else {
            return;
        }

        float x = (float) Math.random() * RoverEngine.sz - RoverEngine.sz / 2f;
        float y = (float) Math.random() * RoverEngine.sz - RoverEngine.sz / 2f;
        //random new position
        eaten.setTransform(new Vec2(x * 2.0f, y * 2.0f), eaten.getAngle());
    }

    public DebugDraw getDraw() {
        return draw;
    }

    public class VisionRay implements LayerDraw {

        final Vec2 point; //where the retina receives vision at
        final float angle;
        private final float distance;
        final ChangedTextInput sight =
                //new SometimesChangedTextInput(nar, minVisionInputProbability);
                new ChangedTextInput(nar);

        RobotArm.RayCastClosestCallback ccallback = new RobotArm.RayCastClosestCallback();
        private final Body body;
        private final int resolution;
        private final float arc;
        final Color3f laserUnhitColor = new Color3f(0.25f, 0.25f, 0.25f);
        final Color3f laserHitColor = new Color3f(laserUnhitColor.x, laserUnhitColor.y, laserUnhitColor.z);
        Color3f sparkColor = new Color3f(0.4f, 0.9f, 0.4f);
        Color3f normalColor = new Color3f(0.9f, 0.9f, 0.4f);
        final Color3f rayColor = new Color3f(); //current ray color
        private final String angleTerm;
        private float distMomentum = 0f;
        private float hitDist;
        private Body hit;
        private float confMomentum = 0;
        private float conf;
        private Concept angleConcept;
        private Atom thisAngle;

        public VisionRay(Body body, Vec2 point, float angle, float arc, int resolution, float length, int steps) {
            this.body = body;
            this.point = point;
            this.angle = angle;
            this.angleTerm = sim.angleTerm(angle);
            this.arc = arc;
            this.resolution = resolution;
            this.distance = length;
        }
        

        List<Runnable> toDraw = new CopyOnWriteArrayList();



        public synchronized void step(boolean feel, boolean drawing) {
            toDraw.clear();

            float conceptPriority;
            float conceptDurability;
            float conceptQuality;

            if (angleConcept == null) {
                angleConcept = nar.memory.concept(angleTerm);
            }

            if (angleConcept!=null) {
                conceptPriority = angleConcept.getPriority();
                conceptDurability = angleConcept.getDurability();
                conceptQuality = angleConcept.getQuality();

                //sight.setProbability(Math.max(minVisionInputProbability, Math.min(1.0f, maxVisionInputProbability * conceptPriority)));
                //sight.setProbability(minVisionInputProbability);
            }
            else {
                conceptPriority = 0;
                conceptDurability = 0;
                conceptQuality = 0;
            }

            point1 = body.getWorldPoint(point);
            Body hit = null;
            float minDist = distance * 1.1f; //far enough away
            float totalDist = 0;
            float dArc = arc / resolution;

            float angOffset = 0; //(float)Math.random() * (-arc/4f);

            for (int r = 0; r < resolution; r++) {
                float da = (-arc / 2f) + dArc * r + angOffset;
                final float V = da + angle + body.getAngle();
                d.set(distance * (float)Math.cos(V), distance * (float)Math.sin(V));
                point2.set(point1);
                point2.addLocal(d);
                ccallback.init();
                
                try {
                    world.raycast(ccallback, point1, point2);
                }
                catch (Exception e) { System.err.println("Phys2D raycast: " + e + " " + point1 + " " + point2 ); e.printStackTrace(); }

                Vec2 endPoint = null;
                if (ccallback.m_hit) {
                    float d = ccallback.m_point.sub(point1).length() / distance;
                    if (drawing) {
                        rayColor.set(laserHitColor);
                        rayColor.x = Math.min(1.0f, laserUnhitColor.x + 0.75f * (1.0f - d));
                        Vec2 pp = ccallback.m_point.clone();
//                        toDraw.add(new Runnable() {
//                            @Override public void run() {
//
//                                getDraw().drawPoint(pp, 5.0f, sparkColor);
//
//                            }
//                        });

                        endPoint = ccallback.m_point;
                    }
                    
                    //pooledHead.set(ccallback.m_normal);
                    //pooledHead.mulLocal(.5f).addLocal(ccallback.m_point);
                    //draw.drawSegment(ccallback.m_point, pooledHead, normalColor, 0.25f);                    
                    totalDist += d;
                    if (d < minDist) {
                        hit = ccallback.body;
                        minDist = d;
                    }
                } else {
                    rayColor.set(normalColor);
                    totalDist += 1;
                    endPoint = point2;
                }

                if ((drawing) && (endPoint!=null)) {

                    //final float alpha = rayColor.x *= 0.2f + 0.8f * (senseActivity + conceptPriority)/2f;
                    //rayColor.z *= alpha - 0.35f * senseActivity;
                    //rayColor.y *= alpha - 0.35f * conceptPriority;

                    rayColor.x = conceptPriority*conceptPriority;
                    rayColor.y = conceptDurability*conceptDurability;
                    rayColor.z = conceptQuality*conceptQuality;
                    float alpha = Math.min(
                            (0.7f * conceptPriority * conceptDurability * conceptQuality) + 0.3f,
                            1f
                    );
                    rayColor.x = Math.min(rayColor.x*0.7f+0.3f, 1f);
                    rayColor.y = Math.min(rayColor.y*0.7f+0.3f, 1f);
                    rayColor.z = Math.min(rayColor.z*0.7f+0.3f, 1f);
                    rayColor.x = Math.max(rayColor.x, 0f);
                    rayColor.y = Math.max(rayColor.y, 0f);
                    rayColor.z = Math.max(rayColor.z, 0f);
                    final Vec2 finalEndPoint = endPoint.clone();
                    Color3f rc = new Color3f(rayColor.x, rayColor.y, rayColor.z);
                    final float thick = 2f;
                    toDraw.add(new Runnable() {

                        @Override
                        public void run() {
                            ((JoglDraw)getDraw()).drawSegment(point1, finalEndPoint, rc.x, rc.y, rc.z, alpha, 1f * thick);
                        }
                    });

                }
            }
            if (hit != null) {
                float meanDist = totalDist / resolution;
                float percentDiff = (float) Math.sqrt(Math.abs(meanDist - minDist));
                float conf = 0.70f + 0.25f * (1.0f - percentDiff);
                if (conf > 0.99f) {
                    conf = 0.99f;
                }
                
                //perceiveDist(hit, conf, meanDist);
                perceiveDist(hit, conf, meanDist);
            } else {
                perceiveDist(hit, 0.5f, 1.0f);
            }
            
            updatePerception();
        }

        protected void perceiveDist(Body hit, float newConf, float nextHitDist) {

            hitDist = (distMomentum * hitDist) + (1f - distMomentum) * nextHitDist;
            conf = (confMomentum * conf) + (1f - confMomentum) * newConf;
            
            if (hit!=null)
                this.hit = hit;
            
        }
        
        protected void updatePerception() {
            onTouch(hit, hitDist);




            
            if ((hit == null) || (hitDist > 1.0f)) {
                inputVisionFreq(hitDist, "confusion");
                return;
            }
            else if (conf < 0.01f) {
                inputVisionFreq(hitDist, "unknown");
                return;
            }
            else {
                String material = hit.getUserData() != null ? hit.getUserData().toString() : "sth";
                inputVisionFreq(hitDist, material);

            }


            


        }

        @Deprecated private String inputVisionDiscrete(float dist, String material) {
            float freq = 1f;
            String sdist = RoverEngine.f(dist);
            //String ss = "<(*," + angleTerm + "," + dist + ") --> " + material + ">. :|: %" + Texts.n1(freq) + ";" + Texts.n1(conf) + "%";
            return "see(" + material + "," + angleTerm + "," + sdist + "). :|: %" + freq + ";" + conf + "%";
        }

        private TaskProcess inputVisionFreq(float dist, String material) {
            float freq = 0.5f + 0.5f * dist;
            //String ss = "<(*," + angleTerm + "," + dist + ") --> " + material + ">. :|: %" + Texts.n1(freq) + ";" + Texts.n1(conf) + "%";
            //String x = "<see_" + angleTerm + " --> [" + material + "]>. %" + freq + "|" + conf + "%";

            //TODO move to constructor
            if (thisAngle == null)
                thisAngle = Atom.the("see_" + angleTerm);
            Compound tt = Property.make( thisAngle ,  Atom.the(material) );

            return nar.inputDirect(nar.task(tt).belief().present().truth(freq, conf).get());
        }

        public void onTouch(Body hit, float di) {
        }

        @Override
        public void drawGround(JoglDraw d, World w) {
            draw = d;
            for (Runnable r : toDraw) {
                r.run();
            }
        }

        @Override
        public void drawSky(JoglDraw d, World w) {

        }
    }
    boolean feel_motion = true; //todo add option in gui

    public void step(int time) {
        if (sim.cnt % sim.missionPeriod == 0) {
            inputMission();
        }
        
        for (VisionRay v : vision) {
            v.step(true, true);
        }
        /*if(cnt>=do_sth_importance) {
        cnt=0;
        do_sth_importance+=decrease_of_importance_step; //increase
        nar.addInput("(^motor,random)!");
        }*/
        if (feel_motion && sim.cnt % motionPeriod == 0) {
            feelMotion();
        }
        /*if (Math.random() < curiosity) {
            randomAction();
        }*/

        nar.frame();

        ((SimulatedClock)nar.memory.clock).add(1);

    }

    public void thrust(float angle, float force) {
        angle += torso.getAngle();// + Math.PI / 2; //compensate for initial orientation
        //torso.applyForceToCenter(new Vec2((float) Math.cos(angle) * force, (float) Math.sin(angle) * force));
        Vec2 v = new Vec2((float) Math.cos(angle) * force, (float) Math.sin(angle) * force);
        torso.setLinearVelocity(v);
        //torso.applyLinearImpulse(v, torso.getWorldCenter(), true);
    }

    public void rotate(float v) {
        torso.setAngularVelocity(v);
        //torso.applyAngularImpulse(v);
        //torso.applyTorque(torque);
    }

    protected void feelMotion() {
        //radians per frame to angVelocity discretized value
        float xa = torso.getAngularVelocity();
        float angleScale = 1.50f;
        float angVelocity = (float) (Math.log(Math.abs(xa * angleScale) + 1f)) / 2f;
        float maxAngleVelocityFelt = 0.8f;
        if (angVelocity > maxAngleVelocityFelt) {
            angVelocity = maxAngleVelocityFelt;
        }
//        if (angVelocity < 0.1) {
//            feltAngularVelocity.set("rotated(" + RoverEngine.f(0) + "). :|: %0.95;0.90%");
//            //feltAngularVelocity.set("feltAngularMotion. :|: %0.00;0.90%");
//        } else {
//            String direction;
//            if (xa < 0) {
//                direction = sim.angleTerm(-MathUtils.PI);
//            } else /*if (xa > 0)*/ {
//                direction = sim.angleTerm(+MathUtils.PI);
//            }
//            feltAngularVelocity.set("rotated(" + RoverEngine.f(angVelocity) + "," + direction + "). :|:");
//            // //feltAngularVelocity.set("<" + direction + " --> feltAngularMotion>. :|: %" + da + ";0.90%");
//        }


        float linVelocity = torso.getLinearVelocity().length();
        linearVelocity.observe(linVelocity);



        Vec2 currentPosition = torso.getWorldCenter();
        if (!positions.isEmpty()) {
            Vec2 last = positions.getLast();
            if (last!=null) {
                Vec2 movement = currentPosition.sub(last);
                double theta = Math.atan2(movement.y, movement.x);
                motionAngle.observe((float)theta);
            }
        }
        positions.addLast(currentPosition.clone());


//        feltOrientation.set("oriented(" + sim.angleTerm(torso.getAngle()) + "). :|:");
//
//        if (linVelocity == 0)
//            feltSpeed.set("moved(0). :|:");
//        else
//            feltSpeed.set("moved(" + (lvel < 0 ? "n" : "p") +  "," + RoverEngine.f(linVelocity) + "). :|:");


        //String motion = "<(" + RoverEngine.f(linVelocity) + ',' + sim.angleTerm(torso.getAngle()) + ',' + RoverEngine.f(angVelocity) + ") --> motion>. :|:";



        facingAngle.observe( torso.getAngle() );
        //nar.inputDirect(nar.task("<facing-->[" +  + "]>. :|:"));
        nar.inputDirect(nar.task("<angvel-->[" + RoverEngine.f(angVelocity) + "]>. :|:"));

        //System.out.println("  " + motion);
        //feltSpeed.set(motion);

        //feltSpeed.set("feltSpeed. :|: %" + sp + ";0.90%");
        //int positionWindow1 = 16;

        /*if (positions.size() >= positionWindow1) {
            Vec2 prevPosition = positions.removeFirst();
            float dist = prevPosition.sub(currentPosition).length();
            float scale = 1.5f;
            dist /= positionWindow1;
            dist *= scale;
            if (dist > 1.0f) {
                dist = 1.0f;
            }
            feltSpeedAvg.set("<(*,linVelocity," + Rover2.f(dist) + ") --> feel" + positionWindow1 + ">. :\\:");
        }*/

    }

    public void stop() {
        torso.setAngularVelocity(0);
        torso.setLinearVelocity(new Vec2());
    }
    
}
