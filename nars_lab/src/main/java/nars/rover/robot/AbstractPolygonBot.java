package nars.rover.robot;

import nars.NAR;
import nars.Symbols;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.event.FrameReaction;
import nars.io.in.ChangedTextInput;
import nars.nal.nal1.Inheritance;
import nars.nal.nal4.Product;
import nars.nal.nal7.Tense;
import nars.rl.gng.NeuralGasNet;
import nars.rover.Material;
import nars.rover.Sim;
import nars.rover.physics.gl.JoglDraw;
import nars.rover.physics.j2d.SwingDraw;
import nars.rover.util.RayCastClosestCallback;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.DefaultTruth;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by me on 8/3/15.
 */
public abstract class AbstractPolygonBot extends Robotic {

    public AbstractPolygonBot(String id, NAR nar) {
        super(id);
        this.nar = nar;
    }

    public final NAR nar;
    final Deque<Vec2> positions = new ArrayDeque();
    final List<Sense> senses = new ArrayList();
    public float linearThrustPerCycle = 8f;
    public float angularSpeedPerCycle = 0.44f * 0.25f;
    int mission = 0;
    //public float curiosity = 0.1f;
    int motionPeriod = 3;
    Vec2 point1 = new Vec2();
    Vec2 point2 = new Vec2();
    Vec2 d = new Vec2();
    boolean feel_motion = true; //todo add option in gui
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
        if (nar.time() < 50) {
            curious(0.9f, 0.7f);
        }
        else {
            curious(0.75f, 0.05f);
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

    public void inputMission() {

        addAxioms();

        nar.goal("<goal --> [health]>", 1.00f, 0.90f);

        nar.believe("<goal --> [health]>", Tense.Present, 0.50f, 0.99f); //reset

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
        if (m instanceof Sim.FoodMaterial) {
            nar.input("<goal --> [food]>. :|: %0.90;0.90%");
            nar.input("<goal --> [health]>. :|: %0.90;0.90%");
        }
        else if (m instanceof Sim.PoisonMaterial) {
            nar.input("<goal --> [food]>. :|: %0.00;0.90%");
            nar.input("<goal --> [health]>. :|: %0.00;0.90%");
        }
        else {
            return;
        }

        float x = (float) Math.random() * Sim.sz - Sim.sz / 2f;
        float y = (float) Math.random() * Sim.sz - Sim.sz / 2f;
        //random new position
        eaten.setTransform(new Vec2(x * 2.0f, y * 2.0f), eaten.getAngle());
    }

    public DebugDraw getDraw() {
        return draw;
    }


    public void step(int time) {
        if (sim.cnt % sim.missionPeriod == 0) {
            inputMission();
        }

        for (Sense v : senses) {
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

    protected abstract void feelMotion();

    public void stop() {
        torso.setAngularVelocity(0);
        torso.setLinearVelocity(new Vec2());
    }

    @FunctionalInterface
    public interface ConceptDesire {
        public float getDesire(Concept c);
    }


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
            nar.input(t = nar.task(term).belief().present().truth(freq, conf).get());

            //System.out.println(t);
        }
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
                        nar.input(getFeedback(feedback).get());
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
                    Rover.BiCycleDesire.this.negativeDesire = negativeDesire;

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
                        nar.input(this.positive.getFeedback(posFeedback).get());
                        nar.input(this.negative.getFeedback(0).get());

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
                        nar.input(this.negative.getFeedback(negFeedback).get());
                        nar.input(this.positive.getFeedback(0).get());

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

    public interface Sense {

        void step(boolean input, boolean draw);

    }


    public class VisionRay implements Sense, SwingDraw.LayerDraw {

        final Vec2 point; //where the retina receives vision at
        final float angle;
        protected float distance;
        final ChangedTextInput sight =
                //new SometimesChangedTextInput(nar, minVisionInputProbability);
                new ChangedTextInput(nar);
        //private final String seenAngleTerm;

        RayCastClosestCallback ccallback = new RayCastClosestCallback();
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
            //this.seenAngleTerm = //"see_" + sim.angleTerm(angle);
            this.arc = arc;
            this.resolution = resolution;
            this.distance = length;
        }


        Collection<Runnable> toDraw =
                //new ConcurrentLinkedDeque<>();
                new CopyOnWriteArrayList();



        public void step(boolean feel, boolean drawing) {
            toDraw.clear();

            float conceptPriority;
            float conceptDurability;
            float conceptQuality;


            angleConcept = nar.memory.concept(angleTerm);


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

            final float distance = getDistance();
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
                    getWorld().raycast(ccallback, point1, point2);
                }
                catch (Exception e) { System.err.println("Phys2D raycast: " + e + " " + point1 + " " + point2 ); e.printStackTrace(); }

                Vec2 endPoint = null;
                if (ccallback.m_hit) {
                    float d = ccallback.m_point.sub(point1).length() / distance;
                    if (drawing) {
                        rayColor.set(laserHitColor);
                        rayColor.x = Math.min(1.0f, laserUnhitColor.x + 0.75f * (1.0f - d));
                        //Vec2 pp = ccallback.m_point.clone();
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

                    rayColor.x = conceptPriority;
                    rayColor.y = conceptDurability;
                    rayColor.z = conceptQuality;
                    float alpha = Math.min(
                            (0.4f * conceptPriority * conceptDurability * conceptQuality) + 0.1f,
                            1f
                    );
                    rayColor.x = Math.min(rayColor.x*0.9f+0.1f, 1f);
                    rayColor.y = Math.min(rayColor.y*0.9f+0.1f, 1f);
                    rayColor.z = Math.min(rayColor.z*0.9f+0.1f, 1f);
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
                float conf = 0.70f + 0.2f * (1.0f - percentDiff);
                if (conf > 0.9f) {
                    conf = 0.9f;
                }

                //perceiveDist(hit, conf, meanDist);
                perceiveDist(hit, conf, meanDist);
            } else {
                perceiveDist(hit, 0.5f, 1.0f);
            }

            updatePerception();
        }

        protected float getDistance() {
            return distance;
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
            String sdist = Sim.f(dist);
            //String ss = "<(*," + angleTerm + "," + dist + ") --> " + material + ">. :|: %" + Texts.n1(freq) + ";" + Texts.n1(conf) + "%";
            return "see:(" + material + "," + angleTerm + "," + sdist + "). :|: %" + freq + ";" + conf + "%";
        }

        private void inputVisionFreq(float dist, String material) {
            float freq = 0.5f + 0.5f * dist;
            //String ss = "<(*," + angleTerm + "," + dist + ") --> " + material + ">. :|: %" + Texts.n1(freq) + ";" + Texts.n1(conf) + "%";
            //String x = "<see_" + angleTerm + " --> [" + material + "]>. %" + freq + "|" + conf + "%";

            //TODO move to constructor
            if (thisAngle == null)
                thisAngle = Atom.the(angleTerm);
            Compound tt =
                    Inheritance.make(
                        Product.make( thisAngle,  Atom.the(material) ),
                        Atom.the("see")
                    );


            nar.input(nar.task(tt).belief().present().truth(freq, conf).get());
        }

        public void onTouch(Body hit, float di) {
        }

        @Override
        public void drawGround(JoglDraw d, World w) {
            for (Runnable r : toDraw) {
                r.run();
            }
        }

        @Override
        public void drawSky(JoglDraw d, World w) {

        }

        public void setDistance(float d) {
            this.distance = d;
        }
    }

}
