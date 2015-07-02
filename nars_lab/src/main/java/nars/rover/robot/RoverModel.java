/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rover.robot;

import nars.Memory;
import nars.NAR;
import nars.clock.SimulatedClock;
import nars.io.SometimesChangedTextInput;
import nars.io.in.ChangedTextInput;
import nars.task.Task;
import nars.concept.Concept;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.NullOperator;
import nars.term.Term;
import nars.rover.PhysicsModel;
import nars.rover.RoverEngine;
import nars.rover.RoverEngine.Material;
import nars.rover.depr.RobotArm;
import nars.rover.physics.gl.JoglDraw;
import nars.rover.physics.j2d.SwingDraw.LayerDraw;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.MathUtils;
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
    public float curiosity = 0.1f;


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

    final double minVisionInputProbability = 0.8f;
    final double maxVisionInputProbability = 1.0f;

    //float tasteDistanceThreshold = 1.0f;
    int retinaPixels = 7;


    int retinaRaysPerPixel = 4; //rays per vision sensor

    float aStep = (float)Math.PI*2f / retinaPixels;

    float L = 17f; //vision distance

    Vec2 mouthPoint = new Vec2(3.0f, 0); //0.5f);
    int distanceResolution = 10;
    float mass = 2f;

    double mouthArc = Math.PI/6f; //in radians
    float biteDistanceThreshold = 0.15f;
    Vec2[] vertices = {new Vec2(3.0f, 0.0f), new Vec2(-1.0f, +2.0f), new Vec2(-1.0f, -2.0f)};

    float linearDamping = 0.9f;
    float angularDamping = 0.8f;

    float restitution = 0.01f; //bounciness
    float friction = 0.5f;

    public float linearThrustPerCycle = 30f;
    public float angularSpeedPerCycle = 0.44f;

    public static boolean allow_imitate = true;

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
        feltAngularVelocity = new SometimesChangedTextInput(nar, minVisionInputProbability);
        feltOrientation = new SometimesChangedTextInput(nar, minVisionInputProbability);
        feltSpeed = new SometimesChangedTextInput(nar, minVisionInputProbability);
        feltSpeedAvg = new SometimesChangedTextInput(nar, minVisionInputProbability);

    

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

        torso.setUserData(new RoverMaterial(id));

        //for (int i = -pixels / 2; i <= pixels / 2; i++) {
        for (int i = 0; i < retinaPixels; i++) {
            final int ii = i;
            final float angle = /*MathUtils.PI / 2f*/ aStep * i;
            final boolean eats = ((angle < mouthArc / 2f) || (angle > (Math.PI*2f) - mouthArc/2f));

            System.out.println(i + " " + angle + " " + eats);

            VisionRay v = new VisionRay(torso, eats ? mouthPoint : new Vec2(0,0), angle, aStep, retinaRaysPerPixel, L, distanceResolution) {


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


        addOperators();
        addAxioms();

        //String p = "$0.99;0.75;0.90$ ";

        randomActions.add("motor($direction)!");
        //TODO : randomActions.add("motor($direction,$direction)!");

        randomActions.add("motor(left)!");
        randomActions.add("motor(left,left)!");
        randomActions.add("motor(right)!");
        randomActions.add("motor(right,right)!");
        //randomActions.add("motor(forward,forward)!"); //too much actions are not good,
        randomActions.add("motor(forward)!"); //however i would agree if <motor(forward,forward) --> motor(forward)>.
        //randomActions.add("motor(forward,forward)!");
        randomActions.add("motor(forward)!");
        //randomActions.add("motor(reverse)!");
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

    protected void addAxioms() {

        nar.input("<{left,right,forward,reverse} --> direction>.");
        nar.input("<{wall,empty,food,poison} --> material>.");
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

        nar.goal("<goal --> health>", 1.00f, 0.90f);

        try {
            if (mission == 0) {
                //seek food
                curiosity = 0.05f;

                nar.goal("<goal --> food>", 1.00f, 0.90f);


                //nar.input("goal(food)! %1.00;0.99%");
                //nar.input("goal(stop)! %0.00;0.99%");
                //nar.addInput("Wall! %0.00;0.50%");
                //nar.input("goal(see)! %1.00;0.70%");
                //nar.input("goal(moved)! %1.00;0.70%");
                //nar.input("goal(rotated)! %1.00;0.70%");
            } else if (mission == 1) {
                //rest
                curiosity = 0;
                nar.input("moved(0)! %1.00;0.9%");
                nar.input("<goal --> food>! %0.00;0.9%");
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
            nar.input("<goal --> food>. :|: %0.90;0.90%");
            nar.input("<goal --> health>. :|: %0.90;0.90%");
        }
        else if (m instanceof RoverEngine.PoisonMaterial) {
            nar.input("<goal --> health>. :|: %0.00;0.90%");
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
        final SometimesChangedTextInput sight = new SometimesChangedTextInput(nar, minVisionInputProbability);
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
        private float distMomentum = 0.75f;
        private float minDist;
        private Body hit;
        private float confMomentum = 0.75f;
        private float conf;
        private Concept angleConcept;

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

            float conceptPriority = 0f;
            float conceptDurability = 0f;
            float conceptQuality = 0f;
            if (angleConcept!=null) {
                conceptPriority = angleConcept.getPriority();
                conceptDurability = angleConcept.getDurability();
                conceptQuality = angleConcept.getQuality();
                //sight.setProbability(Math.max(minVisionInputProbability, Math.min(1.0f, maxVisionInputProbability * conceptPriority)));
                sight.setProbability(minVisionInputProbability);
            }

            if (angleConcept == null) {
                angleConcept = nar.memory.concept(angleTerm);
            }
            point1 = body.getWorldPoint(point);
            Body hit = null;
            float minDist = distance * 1.1f; //far enough away
            float totalDist = 0;
            float dArc = arc / resolution;

            float angOffset = 0; //(float)Math.random() * (-arc/4f);

            for (int r = 0; r < resolution; r++) {
                float da = (-arc / 2f) + dArc * r + angOffset;
                d.set(distance * MathUtils.cos(da + angle + body.getAngle()), distance * MathUtils.sin(da + angle + body.getAngle()));
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

                    rayColor.x = conceptPriority;
                    rayColor.y = conceptDurability;
                    rayColor.z = conceptQuality;
                    float alpha = conceptPriority * conceptDurability * conceptQuality;

                    rayColor.x = Math.min(rayColor.x, 1f);
                    rayColor.y = Math.min(rayColor.y, 1f);
                    rayColor.z = Math.min(rayColor.z, 1f);
                    rayColor.x = Math.max(rayColor.x, 0f);
                    rayColor.y = Math.max(rayColor.y, 0f);
                    rayColor.z = Math.max(rayColor.z, 0f);
                    final Vec2 finalEndPoint = endPoint.clone();
                    Color3f rc = new Color3f(rayColor.x, rayColor.y, rayColor.z);
                    final float thick = 4f;
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

        protected void perceiveDist(Body hit, float newConf, float newMinDist) {

            minDist = (distMomentum * minDist) + (1f - distMomentum) * newMinDist;
            conf = (confMomentum * conf) + (1f - confMomentum) * newConf;
            
            if (hit!=null)
                this.hit = hit;
            
        }
        
        protected void updatePerception() {
            onTouch(hit, minDist);




            
            if (hit == null) {
                if (minDist > 0.5f) {
                    if (sight.set("see(empty," + angleTerm + ",NaN). :|:")) {

                    }
                }   
                return;                                
            }

            if (conf < 0.01f) return;

            String dist = RoverEngine.f(minDist);
            
            String material = hit.getUserData() != null ? hit.getUserData().toString() : "sth";
            //float freq = 0.5f + 0.5f * di;
            float freq = 1f;
            //String ss = "<(*," + angleTerm + "," + dist + ") --> " + material + ">. :|: %" + Texts.n1(freq) + ";" + Texts.n1(conf) + "%";
            String ss = "see(" + material + "," + angleTerm + "," + dist + "). :|: %" + freq + ";" + conf + "%";
            if (sight.set(ss)) {

            }
            

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
        if (feel_motion) {
            feelMotion();
        }
        if (Math.random() < curiosity) {
            randomAction();
        }

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
        //torso.setAngularVelocity(v);
        torso.applyAngularImpulse(v);
        //torso.applyTorque(torque);
    }

    protected void feelMotion() {
        //radians per frame to a discretized value
        float xa = torso.getAngularVelocity();
        float angleScale = 1.50f;
        float a = (float) (Math.log(Math.abs(xa * angleScale) + 1f)) / 2f;
        float maxAngleVelocityFelt = 0.8f;
        if (a > maxAngleVelocityFelt) {
            a = maxAngleVelocityFelt;
        }
        if (a < 0.1) {
            feltAngularVelocity.set("rotated(" + RoverEngine.f(0) + "). :|: %0.95;0.90%");
            //feltAngularVelocity.set("feltAngularMotion. :|: %0.00;0.90%");
        } else {
            String direction;
            if (xa < 0) {
                direction = sim.angleTerm(-MathUtils.PI);
            } else /*if (xa > 0)*/ {
                direction = sim.angleTerm(+MathUtils.PI);
            }
            feltAngularVelocity.set("rotated(" + RoverEngine.f(a) + "," + direction + "). :|:");
            // //feltAngularVelocity.set("<" + direction + " --> feltAngularMotion>. :|: %" + da + ";0.90%");
        }
        feltOrientation.set("oriented(" + sim.angleTerm(torso.getAngle()) + "). :|:");
        float lvel = torso.getLinearVelocity().length();
        float speed = Math.abs(lvel / 20f);
        if (speed > 0.9f) {
            speed = 0.9f;
        }
        if (speed == 0)
            feltSpeed.set("moved(0). :|:");
        else
            feltSpeed.set("moved(" + (lvel < 0 ? "n" : "p") +  "," + RoverEngine.f(speed) + "). :|:");
        //feltSpeed.set("feltSpeed. :|: %" + sp + ";0.90%");
        int positionWindow1 = 16;
        Vec2 currentPosition = torso.getWorldCenter();
        /*if (positions.size() >= positionWindow1) {
            Vec2 prevPosition = positions.removeFirst();
            float dist = prevPosition.sub(currentPosition).length();
            float scale = 1.5f;
            dist /= positionWindow1;
            dist *= scale;
            if (dist > 1.0f) {
                dist = 1.0f;
            }
            feltSpeedAvg.set("<(*,speed," + Rover2.f(dist) + ") --> feel" + positionWindow1 + ">. :\\:");
        }*/
        positions.addLast(currentPosition.clone());
    }

    public void stop() {
        torso.setAngularVelocity(0);
        torso.setLinearVelocity(new Vec2());
    }
    
}
