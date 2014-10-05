package nars.narclear;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.gui.NWindow;
import nars.narclear.jbox2d.TestbedSettings;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

/**
 * NARS Rover
 *
 * @author me
 */
public class Rover extends PhysicsModel {

    private RoverModel rover;

    public class RoverModel {

        private final Body torso;

        RobotArm.RayCastClosestCallback ccallback = new RobotArm.RayCastClosestCallback();
        Vec2 pooledHead = new Vec2();
        Vec2 point1 = new Vec2();
        Vec2 point2 = new Vec2();
        Vec2 d = new Vec2();
        Color3f laserColor = new Color3f(0.55f, 0, 0.45f);

        public RoverModel(PhysicsModel p) {
            float mass = 0.25f;
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(0.5f, 0.5f);

            BodyDef bd = new BodyDef();
            bd.setLinearDamping(0.99f);
            bd.setAngularDamping(0.99f);

            bd.type = BodyType.DYNAMIC;
            bd.position.set(0, 0);
            torso = p.getWorld().createBody(bd);
            torso.createFixture(shape, mass);

//            {
//                Body prevBody = ground;
//
            // Define upper arm.
            for (float axis = -1f; axis <= 1f; axis += 2f) {
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
                float max = MathUtils.PI / 1f * 0.4f;
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

            }

        }

        public void step() {
            int pixels = 5;
            float angle = 0.9f;

            float focusAngle = -angle / 2f;
            float aStep = focusAngle / pixels;
            float L = 11.0f;
            float a = (float) (torso.getAngle() + Math.PI / 2f - focusAngle / 2f);
            boolean[] hit = new boolean[pixels];

            for (int i = 0; i < pixels; i++) {
                point1 = torso.getWorldPoint(new Vec2(0, 0.5f));

                d.set(L * MathUtils.cos(a), L * MathUtils.sin(a));
                point2.set(point1);
                point2.addLocal(d);

                ccallback.init();
                getWorld().raycast(ccallback, point1, point2);

                if (ccallback.m_hit) {
                    getDebugDraw().drawPoint(ccallback.m_point, 5.0f, new Color3f(0.4f, 0.9f, 0.4f));
                    getDebugDraw().drawSegment(point1, ccallback.m_point, new Color3f(0.8f, 0.8f, 0.8f));
                    pooledHead.set(ccallback.m_normal);
                    pooledHead.mulLocal(.5f).addLocal(ccallback.m_point);
                    getDebugDraw().drawSegment(ccallback.m_point, pooledHead, new Color3f(0.9f, 0.9f, 0.4f));
                    hit[i] = true;
                } else {
                    getDebugDraw().drawSegment(point1, point2, laserColor);
                }
                a += aStep;
            }
            sight(hit);

        }

        protected void sight(boolean[] hit) {

        }

        public void thrust(float angle, float force) {
            angle += torso.getAngle() + Math.PI / 2; //compensate for initial orientation

            torso.applyForceToCenter(new Vec2((float) Math.cos(angle) * force, (float) Math.sin(angle) * force));
        }

        public void rotate(float torque) {
            torso.applyTorque(torque);
        }

    }

    @Override
    public void step(TestbedSettings settings) {
        super.step(settings);

        rover.step();

    }

    public static class RoverPanel extends JPanel {

        public RoverPanel(RoverModel rover) {
            super(new BorderLayout());

            {
                JPanel motor = new JPanel(new FlowLayout());
                
                JButton forward = new JButton("Forward");
                forward.addActionListener(new ActionListener() {
                    @Override public void actionPerformed(ActionEvent e) {
                        rover.thrust(0, 10.0f);
                    }
                });
                motor.add(forward);

               
                JButton turnleft = new JButton("Left");
                turnleft.addActionListener(new ActionListener() {
                    @Override public void actionPerformed(ActionEvent e) {
                        rover.rotate(-4.0f);
                    }
                });
                motor.add(turnleft);
                
                JButton turnright = new JButton("Right");
                turnleft.addActionListener(new ActionListener() {
                    @Override public void actionPerformed(ActionEvent e) {
                        rover.rotate(4.0f);
                    }
                });
                motor.add(turnright);
                
                JButton backward = new JButton("Backward");
                backward.addActionListener(new ActionListener() {
                    @Override public void actionPerformed(ActionEvent e) {
                        rover.thrust(0, -10.0f);
                    }
                });
                motor.add(backward);

                add(motor, BorderLayout.SOUTH);
            }
        }

    }

    public class RoverWorld {

        public RoverWorld(PhysicsModel p, float w, float h) {
            for (int i = 0; i < 10; i++) {
                float x = (float) Math.random() * w - w / 2f;
                float y = (float) Math.random() * h - h / 2f;
                float bw = 1.0f;
                float bh = 1.6f;
                float a = 0;
                addBlock(p, x, y, bw, bh, a);
            }
        }

        public void addBlock(PhysicsModel p, float x, float y, float w, float h, float a) {
            float mass = 0.25f;
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(w, h);

            BodyDef bd = new BodyDef();
            bd.setLinearDamping(0.95f);
            bd.setAngularDamping(0.8f);

            bd.type = BodyType.DYNAMIC;
            bd.position.set(x, y);
            Body body = p.getWorld().createBody(bd);
            body.createFixture(shape, mass);

        }
    }

    @Override
    public void initTest(boolean deserialized) {
        getWorld().setGravity(new Vec2());
        getWorld().setAllowSleep(false);

        RoverWorld world = new RoverWorld(this, 48, 48);

        rover = new RoverModel(this);

        new NWindow("Rover Control", new RoverPanel(rover)).show(300, 200);

    }

    @Override
    public String getTestName() {
        return "NARS Rover";
    }

    public static void main(String[] args) {
        NAR nar = new DefaultNARBuilder().build();
        new NARPhysics<Rover>(nar, new Rover()) {

        };
        nar.start(50, 1);

    }

}
