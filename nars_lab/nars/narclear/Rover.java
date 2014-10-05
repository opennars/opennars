package nars.narclear;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.entity.Task;
import nars.gui.NWindow;
import nars.io.ChangedTextInput;
import nars.io.Texts;
import nars.language.Term;
import nars.narclear.jbox2d.TestbedSettings;
import nars.operator.NullOperator;
import nars.operator.Operation;
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
    private final NAR nar;

    public Rover(NAR nar) {
        this.nar = nar;
    }

    
    public class RoverModel {

        private final Body torso;

        RobotArm.RayCastClosestCallback ccallback = new RobotArm.RayCastClosestCallback();
        Vec2 pooledHead = new Vec2();
        Vec2 point1 = new Vec2();
        Vec2 point2 = new Vec2();
        Vec2 d = new Vec2();
        Color3f laserColor = new Color3f(0.55f, 0, 0.45f);
        
        //public class ChangedNumericInput //discretizer
        
        ChangedTextInput feltAngularVelocity = new ChangedTextInput(nar);
        ChangedTextInput feltOrientation = new ChangedTextInput(nar);
        ChangedTextInput feltSpeed  = new ChangedTextInput(nar);
        ChangedTextInput sight = new ChangedTextInput(nar);
        
        //public class DistanceInput extends ChangedTextInput
                
        public RoverModel(PhysicsModel p) {
            float mass = 2.25f;
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(0.5f, 0.5f);

            BodyDef bd = new BodyDef();
            bd.setLinearDamping(0.9f);
            bd.setAngularDamping(0.9f);

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
            
            see(hit);
            feelMotion();
            
        }

        protected void see(boolean[] hit) {
                int totalHit = 0;
                for (boolean x : hit)
                    if (x) totalHit++;
                
                
                float th = totalHit / ((float)hit.length);
                
                //method 1:
                String sp = Texts.n1(th/3f);
                sight.set("<" + sp + " --> sight>. :|:");
                
                
                //method 2:
                //sight.set("sight. :|: %" + Texts.n2(th) + ";0.90%");
        }

        public void thrust(float angle, float force) {
            angle += torso.getAngle() + Math.PI / 2; //compensate for initial orientation

            torso.applyForceToCenter(new Vec2((float) Math.cos(angle) * force, (float) Math.sin(angle) * force));
        }

        public void rotate(float torque) {
            torso.applyTorque(torque);
        }
        
        protected void feelMotion() {
            //radians per frame to a discretized value
            float xa = torso.getAngularVelocity();
            float angleScale = 1.50f;
            float a = (float)(Math.log(Math.abs(xa*angleScale)+1f));
            float maxAngleVelocityFelt = 0.8f;
            if (a > maxAngleVelocityFelt) a = maxAngleVelocityFelt;
            
            
            if (a < 0.1) {
                feltAngularVelocity.set("<0 --> feltAngularMotion>. :|: %1.00;0.90%");
                //feltAngularVelocity.set("feltAngularMotion. :|: %0.00;0.90%");   
            }
            else {
                String direction;
                String da = Texts.n1(a);// + ",radPerFrame";
                if (xa < 0) direction = "left";
                else /*if (xa > 0)*/ direction = "right";            
                feltAngularVelocity.set("<(*," + da + "," + direction + ") --> feltAngularMotion>. :|:");
                //feltAngularVelocity.set("<" + direction + " --> feltAngularMotion>. :|: %" + da + ";0.90%");
            }
            
            
            float h = torso.getAngle() % (MathUtils.TWOPI);
            if (h < 0) h+=MathUtils.TWOPI;
            h = h/MathUtils.TWOPI;
            String dh = Texts.n1(h);// + ",rad";
            feltOrientation.set("<" + dh + " --> feltOrientation>. :|:");
            
            float speed = Math.abs(torso.getLinearVelocity().length());
            if (speed > 0.9f) speed = 0.9f;
            String sp = Texts.n1(speed);
            feltSpeed.set("<" + sp + " --> feltSpeed>. :|:");
            //feltSpeed.set("feltSpeed. :|: %" + sp + ";0.90%");
        }
        
        public void stop() {
            torso.setAngularVelocity(0);
            torso.setLinearVelocity(new Vec2());
        }

    }

    @Override
    public void step(TestbedSettings settings) {
        super.step(settings);

        rover.step();

    }

    public class RoverPanel extends JPanel {

        public class InputButton extends JButton implements ActionListener {
            private final String command;

            public InputButton(String label, String command) {
                super(label);
                addActionListener(this);
                this.command = command;
            }
            
            @Override public void actionPerformed(ActionEvent e) {
                nar.addInput(command);
            }

        }
        
        public RoverPanel(RoverModel rover) {
            super(new BorderLayout());

            {
                JPanel motorPanel = new JPanel(new GridLayout(0,2));
                
                motorPanel.add(new InputButton("Stop", "(^motor,stop)!"));
                motorPanel.add(new InputButton("Forward", "(^motor,forward)!"));
                motorPanel.add(new InputButton("TurnLeft", "(^motor,turn,left)!"));
                motorPanel.add(new InputButton("TurnRight", "(^motor,turn,right)!"));
                motorPanel.add(new InputButton("Backward", "(^motor,backward)!"));

                add(motorPanel, BorderLayout.SOUTH);
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
            body.setAngularDamping(10);
            body.setLinearDamping(15);

        }
    }

    @Override
    public void initTest(boolean deserialized) {
        getWorld().setGravity(new Vec2());
        getWorld().setAllowSleep(false);

        RoverWorld world = new RoverWorld(this, 48, 48);

        rover = new RoverModel(this);
        rover.torso.setAngularDamping(20);
        rover.torso.setLinearDamping(10);

        new NWindow("Rover Control", new RoverPanel(rover)).show(300, 200);

        addAxioms();
        addOperators();
        
    }
    
    protected void addOperators() {
        nar.addPlugin(new NullOperator("^motor") {
            @Override protected List<Task> execute(Operation operation, Term[] args, Memory memory) {
                Term t1 = args[0];
                float priority = operation.getTask().budget.getPriority();
                
                float rotationSpeed = 20f;
                float linearSpeed = 100f;
                
                if (args.length > 1) {
                    Term t2 = args[1];
                    switch (t1.name().toString() + "," + t2.name().toString()) {
                        case "turn,left":  rover.rotate(rotationSpeed);  break;
                        case "turn,right": rover.rotate(-rotationSpeed);  break;                        
                    }
                }
                else {
                    switch (t1.name().toString()) {
                        case "forward":  rover.thrust(0, linearSpeed); break;
                        case "backward": rover.thrust(0, -linearSpeed); break;
                        case "stop": rover.stop(); break;
                    }
                }
                return null;
            }            
        });
        
    }
    protected void addAxioms() {
        //nar.addInput("<{positiveNumber,negativeNumber} --> number>. %1.00;0.99%");
        //nar.addInput("<{0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9} --> positiveNumber>. %1.00;0.99%");
        //nar.addInput("<{-0.0,-0.1,-0.2,-0.3,-0.4,-0.5,-0.6,-0.7,-0.8,-0.9} --> negativeNumber>. %1.00;0.99%");
        //nar.addInput("<0.0 <-> -0.0>. %1.00;0.99%");
        //nar.addInput("<{0.0,0.1} --> zeroishNumber>. %1.00;0.99%");
        //nar.addInput("<{0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9} --> positiveNumber>. %1.00;0.99%");
        nar.addInput("<0 <-> 0.0>. %1.00;0.90%");
        nar.addInput("<0.0 <-> 0.1>. %1.00;0.75%");
        nar.addInput("<0.1 <-> 0.2>. %1.00;0.75%");
        nar.addInput("<0.2 <-> 0.3>. %1.00;0.75%");
        nar.addInput("<0.3 <-> 0.4>. %1.00;0.75%");
        nar.addInput("<0.4 <-> 0.5>. %1.00;0.75%");
        nar.addInput("<0.5 <-> 0.6>. %1.00;0.75%");
        nar.addInput("<0.6 <-> 0.7>. %1.00;0.75%");
        nar.addInput("<0.7 <-> 0.8>. %1.00;0.75%");
        nar.addInput("<0.8 <-> 0.9>. %1.00;0.75%");
        
    }

    @Override
    public String getTestName() {
        return "NARS Rover";
    }

    public static void main(String[] args) {
        NAR nar = new DefaultNARBuilder().build();
        new NARPhysics<Rover>(nar, new Rover(nar)) {

        };
        nar.param().duration.set(50);
        nar.start(50,500);
        nar.param().noiseLevel.set(0);

    }

}
