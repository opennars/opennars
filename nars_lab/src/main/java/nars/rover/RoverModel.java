/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rover;

import nars.NAR;
import nars.io.in.ChangedTextInput;
import nars.io.SometimesChangedTextInput;
import nars.io.Texts;
import nars.nal.concept.Concept;
import nars.rover.jbox2d.j2d.JoglDraw;
import nars.rover.jbox2d.j2d.SwingDraw.LayerDraw;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

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
    final Body torso;
    Vec2 pooledHead = new Vec2();
    Vec2 point1 = new Vec2();
    Vec2 point2 = new Vec2();
    Vec2 d = new Vec2();
    Deque<Vec2> positions = new ArrayDeque();
    List<VisionRay> vision = new ArrayList();
    //public class ChangedNumericInput //discretizer
    private final Rover2 sim;
    private final NAR nar;
    private final ChangedTextInput feltAngularVelocity;
    private final ChangedTextInput feltOrientation;
    private final ChangedTextInput feltSpeed;
    private final ChangedTextInput feltSpeedAvg;
    private final ChangedTextInput mouthInput;
    private final World world;
    private DebugDraw draw = null;

    final double minVisionInputProbability = 0.01f;
    final double maxVisionInputProbability = 0.08f;
    float biteDistanceThreshold = 0.10f;
    float tasteDistanceThreshold = 1.0f;
    int pixels = 48;
    int retinaResolution = 1; //should be odd # to balance
    float aStep = (float)Math.PI*2f / pixels;
    float L = 35.0f;
    Vec2 frontRetina = new Vec2(0, 0.5f);
    int distanceResolution = 9;
    int mouthArc = 2;
    float mass = 2.25f;
    Vec2[] vertices = {new Vec2(0.0f, 2.0f), new Vec2(+2.0f, -2.0f), new Vec2(-2.0f, -2.0f)};
    float linearDamping = 0.9f;
    float angularDamping = 0.9f;
    float restitution = 0.01f; //bounciness
    float friction = 0.5f;


    //public class DistanceInput extends ChangedTextInput
    public RoverModel(PhysicsModel p, final Rover2 sim) {
        this.sim = sim;
        this.nar = sim.nar;
        

        this.world = sim.getWorld();

        mouthInput = new ChangedTextInput(nar);
        feltAngularVelocity = new SometimesChangedTextInput(sim.nar, minVisionInputProbability);
        feltOrientation = new SometimesChangedTextInput(sim.nar, minVisionInputProbability);
        feltSpeed = new SometimesChangedTextInput(sim.nar, minVisionInputProbability);
        feltSpeedAvg = new SometimesChangedTextInput(sim.nar, minVisionInputProbability);

    

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

        for (int i = -pixels / 2; i <= pixels / 2; i++) {
            final int ii = i;
            VisionRay v = new VisionRay(torso, frontRetina, MathUtils.PI / 2f + aStep * i, aStep, retinaResolution, L, distanceResolution) {


                @Override
                public void onTouch(Body touched, float di) {
                    if (touched!=null && touched.getUserData() == Rover2.Material.Food) {
                        if (Math.abs(ii) <= mouthArc)  {

                            if (di <= biteDistanceThreshold) {
                                eat(touched);
                            } else if (di <= tasteDistanceThreshold) {
                                taste(touched, di );
                            }
                        }
                    }
                }
            };
            v.sparkColor = new Color3f(0.5f, 0.4f, 0.4f);
            v.normalColor = new Color3f(0.4f, 0.4f, 0.4f);

            ((JoglDraw)p.draw()).addLayer(v);

            vision.add(v);
        }
    }
    public void taste(Body food, float distance) {
        float c = 1.0f / (1.0f + (distance-biteDistanceThreshold)/(tasteDistanceThreshold - biteDistanceThreshold));
        mouthInput.set("<goal --> Food>. :|: %0." + Texts.n1(0.5f + c/2f) + ";0." + Texts.n1(c/2f) + "%");
    }

    public void eat(Body food) {
        float x = (float) Math.random() * Rover2.sz - Rover2.sz / 2f;
        float y = (float) Math.random() * Rover2.sz - Rover2.sz / 2f;

        //random new position
        food.setTransform(new Vec2(x * 2.0f, y * 2.0f), food.getAngle());

        mouthInput.set("$0.95;0.50$ <goal --> Food>. :|: %0.90;0.90%");
    }

    public DebugDraw getDraw() {
        return draw;
    }

    public class VisionRay implements LayerDraw {

        final Vec2 point; //where the retina receives vision at
        final float angle;
        private final float distance;
        final SometimesChangedTextInput sight = new SometimesChangedTextInput(sim.nar, minVisionInputProbability);
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
        float senseActivity = 0.0f;
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

            float conceptActivity = 0f;
            if (angleConcept!=null) {
                conceptActivity = angleConcept.getPriority();
                sight.setProbability(Math.max(minVisionInputProbability, Math.min(1.0f, maxVisionInputProbability * conceptActivity)));
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

                    final float alpha = rayColor.x *= 0.2f + 0.8f * (senseActivity + conceptActivity)/2f;
                    rayColor.z *= alpha - 0.35f * senseActivity;
                    rayColor.y *= alpha - 0.35f * conceptActivity;
                    rayColor.x = Math.min(rayColor.x, 1f);
                    rayColor.y = Math.min(rayColor.y, 1f);
                    rayColor.z = Math.min(rayColor.z, 1f);
                    rayColor.x = Math.max(rayColor.x, 0f);
                    rayColor.y = Math.max(rayColor.y, 0f);
                    rayColor.z = Math.max(rayColor.z, 0f);
                    final Vec2 finalEndPoint = endPoint.clone();
                    Color3f rc = new Color3f(rayColor.x, rayColor.y, rayColor.z);
                    toDraw.add(new Runnable() {

                        @Override
                        public void run() {
                            ((JoglDraw)getDraw()).drawSegment(point1, finalEndPoint, rc.x, rc.y, rc.z, alpha, alpha * 6f);
                        }
                    });

                }
            }
            if (hit != null) {
                float meanDist = totalDist / resolution;
                float percentDiff = Math.abs(meanDist - minDist);
                float conf = 0.85f + 0.15f * (1.0f - percentDiff);
                if (conf > 0.99f) {
                    conf = 0.99f;
                }
                
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

            senseActivity *= 0.9f; //decay
            
            if (hit == null) {
                if (minDist > 0.5f) {
                    if (sight.set("<(*,empty," + angleTerm + ",unknownDistance) --> feel>. :|:")) {
                        senseActivity += minVisionInputProbability;
                    }
                }   
                return;                                
            }
            
            String dist = Rover2.f(minDist);
            
            String material = hit.getUserData() != null ? hit.getUserData().toString() : "sth";
            //float freq = 0.5f + 0.5f * di;
            float freq = 1f;
            //String ss = "<(*," + angleTerm + "," + dist + ") --> " + material + ">. :|: %" + Texts.n1(freq) + ";" + Texts.n1(conf) + "%";
            String ss = "<(*," + material + "," + angleTerm + "," + dist + ") --> " +  "feel>. :|: %" + Texts.n1(freq) + ";" + Texts.n1(conf) + "%";
            if (sight.set(ss)) {
                senseActivity += 10f * minVisionInputProbability;
            }
            
            if (senseActivity > 1.0f) senseActivity = 1f;
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

    public void step() {
        if (sim.cnt % sim.missionPeriod == 0) {
            sim.inputMission();
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
        if (Math.random() < sim.curiosity) {
            sim.randomAction();
        }
    }

    public void thrust(float angle, float force) {
        angle += torso.getAngle() + Math.PI / 2; //compensate for initial orientation
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
            feltAngularVelocity.set("<(*,rotation," + Rover2.f(0) + ") --> feel>. :|: %0.95;0.90%");
            //feltAngularVelocity.set("feltAngularMotion. :|: %0.00;0.90%");
        } else {
            String direction;
            if (xa < 0) {
                direction = sim.angleTerm(-MathUtils.PI);
            } else /*if (xa > 0)*/ {
                direction = sim.angleTerm(+MathUtils.PI);
            }
            feltAngularVelocity.set("<(*,rotation," + Rover2.f(a) + "," + direction + ") --> feel>. :|:");
            // //feltAngularVelocity.set("<" + direction + " --> feltAngularMotion>. :|: %" + da + ";0.90%");
        }
        feltOrientation.set("<(*,orientation," + sim.angleTerm(torso.getAngle()) + ") --> feel>. :|:");
        float speed = Math.abs(torso.getLinearVelocity().length() / 20f);
        if (speed > 0.9f) {
            speed = 0.9f;
        }
        feltSpeed.set("<(*,speed," + Rover2.f(speed) + ") --> feel>. :|:");
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
