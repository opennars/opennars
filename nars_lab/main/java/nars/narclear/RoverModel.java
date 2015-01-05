/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.narclear;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import nars.core.NAR;
import nars.io.ChangedTextInput;
import nars.io.Texts;
import nars.narclear.jbox2d.j2d.DrawPhy2D;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

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
    private final World world;
    private final DrawPhy2D draw;

    //public class DistanceInput extends ChangedTextInput
    public RoverModel(PhysicsModel p, final Rover2 sim) {
        this.sim = sim;
        this.nar = sim.nar;
        

        this.world = sim.getWorld();
        this.draw = (DrawPhy2D) sim.draw();
        
        feltAngularVelocity = new ChangedTextInput(sim.nar);
        feltOrientation = new ChangedTextInput(sim.nar);
        feltSpeed = new ChangedTextInput(sim.nar);
        feltSpeedAvg = new ChangedTextInput(sim.nar);

    
        float mass = 2.25f;
        Vec2[] vertices = {new Vec2(0.0f, 2.0f), new Vec2(+2.0f, -2.0f), new Vec2(-2.0f, -2.0f)};
        PolygonShape shape = new PolygonShape();
        shape.set(vertices, vertices.length);
        //shape.m_centroid.set(bodyDef.position);
        BodyDef bd = new BodyDef();
        bd.setLinearDamping(0.9f);
        bd.setAngularDamping(0.9f);
        bd.type = BodyType.DYNAMIC;
        bd.position.set(0, 0);
        torso = p.getWorld().createBody(bd);
        Fixture f = torso.createFixture(shape, mass);
        f.setRestitution(0.01f);
        f.setFriction(0.5f);
        //            {
        //                Body prevBody = ground;
        //
        // Define upper arm.
        /*for (float axis = -1f; axis <= 1f; axis += 2f) {
        Body upperArm;
        RevoluteJoint leftShoulderJoint;
        Body lowerArm;
        RevoluteJoint elbowJoint;
        PolygonShape upperArmShape = new PolygonShape();
        upperArmShape.setAsBox(0.3f, 0.1f);
        PolygonShape lowerArmShape = new PolygonShape();
        lowerArmShape.setAsBox(0.4f, 0.07f);
        BodyDef bdUA = new BodyDef();
        bdUA.type = BodyType.DYNAMIC;
        bdUA.position.set(axis * 0.82f, 0f);
        upperArm = getWorld().createBody(bdUA);
        upperArm.createFixture(upperArmShape, 0.1f);
        RevoluteJointDef rjd = new RevoluteJointDef();
        rjd.initialize(torso, upperArm, new Vec2(axis * 0.55f, 0.0f));
        rjd.motorSpeed = 0; //1.0f * MathUtils.PI;
        //rjd.maxMotorTorque = 10000.0f;
        rjd.enableMotor = true;
        float min = -MathUtils.PI / 1f * 0.1f;
        float max = MathUtils.PI / 1f * 0.2f;
        rjd.lowerAngle = min;
        rjd.upperAngle = max;
        rjd.enableLimit = true;
        rjd.collideConnected = false;
        leftShoulderJoint = (RevoluteJoint) getWorld().createJoint(rjd);
        BodyDef bdLA = new BodyDef();
        bdLA.type = BodyType.DYNAMIC;
        bdLA.position.set(axis * 1.5f, 0f);
        lowerArm = getWorld().createBody(bdLA);
        lowerArm.createFixture(lowerArmShape, 0.05f);
        RevoluteJointDef rjd2 = new RevoluteJointDef();
        rjd2.initialize(upperArm, lowerArm, new Vec2(axis * 1.2f, 0.0f));
        rjd2.enableMotor = true;
        rjd2.lowerAngle = -1.2f;
        rjd2.upperAngle = 1.2f;
        rjd2.enableLimit = true;
        rjd.collideConnected = false;
        elbowJoint = (RevoluteJoint) getWorld().createJoint(rjd2);
        }*/
        int pixels = 5;
        float aStep = 1.8f / pixels;
        float retinaArc = aStep;
        int retinaResolution = 11; //should be odd # to balance
        float L = 35.0f;
        Vec2 frontRetina = new Vec2(0, 0.5f);
        int distanceResolution = 9;
        for (int i = -1; i <= 1; i++) {
            final int ii = i;
            vision.add(new VisionRay(torso, frontRetina, MathUtils.PI / 2f + aStep * i, retinaArc, retinaResolution, L, distanceResolution) {
                float touchThresholdDistance = 0.1f;

                @Override
                public void onTouch(Body touched, float di) {
                    if (di <= touchThresholdDistance) {
                        if (Math.abs(ii) <= 1) {
                            //mouth
                            if (touched.getUserData() == Rover2.Material.Food) {
                                eat(touched);
                            }
                        }
                    }
                }
            });
        }
        pixels = 6;
        aStep = 1.2f / pixels;
        retinaResolution = 11;
        L = 35.5f;
        retinaArc = 0.9f;
        for (int i = -pixels / 2; i <= pixels / 2; i++) {
            float angle = -(MathUtils.PI / 2f + aStep * i * 4);
            float d1 = 0.5f;
            Vec2 backRetina = new Vec2((float) Math.cos(angle) * d1, (float) Math.sin(angle) * d1);
            VisionRay v;
            vision.add(v = new VisionRay(torso, backRetina, angle, retinaArc, retinaResolution, L, distanceResolution));
            v.sparkColor = new Color3f(0.4f, 0.4f, 0.9f);
            v.normalColor = new Color3f(0.4f, 0.4f, 0.4f);
        }
        //Vec2 backRetina = new Vec2(0, -0.5f);
        //vision.add(new VisionRay("back", torso, backRetina, -MathUtils.PI/2f, L/2f, 3));
        /*
        int n = 0;
        float LS = 0.4f;
        float LT = 1.95f;
        for (float sonarAngle = 0f; sonarAngle < MathUtils.TWOPI; sonarAngle+=0.6f) {
        float ca = (float)Math.cos(sonarAngle) * LT;
        float sa = (float)Math.sin(sonarAngle) * LT;
        vision.add(new VisionRay("radar" + n, torso,
        new Vec2(ca, sa), sonarAngle + MathUtils.PI/16, LS, 2));
        n++;
        }
         */
    }

    public void eat(Body food) {
        float x = (float) Math.random() * Rover2.sz - Rover2.sz / 2f;
        float y = (float) Math.random() * Rover2.sz - Rover2.sz / 2f;
        //world.AddABlock(Phys, sz, sz);
        food.setTransform(new Vec2(x * 2.0f, y * 2.0f), food.getAngle());
        //Phys.getWorld().destroyBody(hit);
        sim.nar.addInput("<goal --> Food>. :|:");
    }

    public class VisionRay {

        final Vec2 point; //where the retina receives vision at
        final float angle;
        private final float distance;
        final ChangedTextInput sight = new ChangedTextInput(sim.nar);
        RobotArm.RayCastClosestCallback ccallback = new RobotArm.RayCastClosestCallback();
        private final Body body;
        private final int distanceSteps;
        private final int resolution;
        private final float arc;
        final Color3f laserUnhitColor = new Color3f(0.25f, 0.25f, 0.25f);
        final Color3f laserHitColor = new Color3f(laserUnhitColor.x, laserUnhitColor.y, laserUnhitColor.z);
        Color3f sparkColor = new Color3f(0.4f, 0.9f, 0.4f);
        Color3f normalColor = new Color3f(0.9f, 0.9f, 0.4f);
        private final String angleTerm;
        private float distMomentum = 0.75f;
        private float minDist;
        private Body hit;
        private float confMomentum = 0.75f;
        private float conf;

        public VisionRay(Body body, Vec2 point, float angle, float arc, int resolution, float length, int steps) {
            this.body = body;
            this.point = point;
            this.angle = angle;
            this.angleTerm = sim.angleTerm(angle);
            this.arc = arc;
            this.resolution = resolution;
            this.distance = length;
            this.distanceSteps = steps;
        }

        public void step() {
            point1 = body.getWorldPoint(point);
            Body hit = null;
            float minDist = distance * 1.1f; //far enough away
            float totalDist = 0;
            float dArc = arc / resolution;
            for (int r = 0; r < resolution; r++) {
                float da = (-arc / 2f) + dArc * r;
                d.set(distance * MathUtils.cos(da + angle + body.getAngle()), distance * MathUtils.sin(da + angle + body.getAngle()));
                point2.set(point1);
                point2.addLocal(d);
                ccallback.init();
                world.raycast(ccallback, point1, point2);
                if (ccallback.m_hit) {
                    float d = ccallback.m_point.sub(point1).length() / distance;
                    laserHitColor.x = Math.min(1.0f, laserUnhitColor.x + 0.75f * (1.0f - d));
                    draw.drawPoint(ccallback.m_point, 5.0f, sparkColor);
                    draw.drawSegment(point1, ccallback.m_point, laserHitColor, 0.25f);
                    

                    //pooledHead.set(ccallback.m_normal);
                    //pooledHead.mulLocal(.5f).addLocal(ccallback.m_point);
                    //draw.drawSegment(ccallback.m_point, pooledHead, normalColor, 0.25f);
                    
                    totalDist += d;
                    if (d < minDist) {
                        hit = ccallback.body;
                        minDist = d;
                    }
                } else {
                    draw.drawSegment(point1, point2, laserUnhitColor);
                    totalDist += 1;
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
                perceiveDist(hit, 0.99f, 1.0f);
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
                    sight.set("<" + angleTerm + " --> Empty>. :|:");        
                }   
                return;                                
            }
            
            String dist = Rover2.f(minDist);
            
            String material = hit.getUserData() != null ? hit.getUserData().toString() : "sth";
            //float freq = 0.5f + 0.5f * di;
            float freq = 1f;
            //String ss = "<(*," + angleTerm + "," + dist + ") --> " + material + ">. :|: %" + Texts.n1(freq) + ";" + Texts.n1(conf) + "%";
            String ss = "<(*," + angleTerm + "," + dist + ") --> " + material + ">. :|: %" + Texts.n1(freq) + ";" + Texts.n1(conf) + "%";
            sight.set(ss);
            
            
        }
        
        public void onTouch(Body hit, float di) {
        }
    }
    boolean feel_motion = true; //todo add option in gui

    public void step() {
        if (sim.cnt % sim.missionPeriod == 0) {
            sim.inputMission();
        }
        for (VisionRay v : vision) {
            v.step();
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
        sim.cnt++;
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
            feltAngularVelocity.set("<" + Rover2.f(0) + " --> feltAngularMotion>. :|: %1.00;0.90%");
            //feltAngularVelocity.set("feltAngularMotion. :|: %0.00;0.90%");
        } else {
            String direction;
            if (xa < 0) {
                direction = sim.angleTerm(-MathUtils.PI);
            } else /*if (xa > 0)*/ {
                direction = sim.angleTerm(+MathUtils.PI);
            }
            feltAngularVelocity.set("<(*," + Rover2.f(a) + "," + direction + ") --> feltAngularMotion>. :|:");
            // //feltAngularVelocity.set("<" + direction + " --> feltAngularMotion>. :|: %" + da + ";0.90%");
        }
        feltOrientation.set("<" + sim.angleTerm(torso.getAngle()) + " --> feltOrientation>. :|:");
        float speed = Math.abs(torso.getLinearVelocity().length() / 20f);
        if (speed > 0.9f) {
            speed = 0.9f;
        }
        feltSpeed.set("<" + Rover2.f(speed) + " --> feltSpeed>. :|:");
        //feltSpeed.set("feltSpeed. :|: %" + sp + ";0.90%");
        int positionWindow1 = 16;
        Vec2 currentPosition = torso.getWorldCenter();
        if (positions.size() >= positionWindow1) {
            Vec2 prevPosition = positions.removeFirst();
            float dist = prevPosition.sub(currentPosition).length();
            float scale = 1.5f;
            dist /= positionWindow1;
            dist *= scale;
            if (dist > 1.0f) {
                dist = 1.0f;
            }
            feltSpeedAvg.set("<" + Rover2.f(dist) + " --> feltSpeedAvg" + positionWindow1 + ">. :|:");
        }
        positions.addLast(currentPosition.clone());
    }

    public void stop() {
        torso.setAngularVelocity(0);
        torso.setLinearVelocity(new Vec2());
    }
    
}
