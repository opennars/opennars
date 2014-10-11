package nars.narclear;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.build.DiscretinuousBagNARBuilder;
import nars.entity.Task;
import nars.gui.NWindow;
import nars.gui.output.TaskTree;
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

    public static RoverModel rover;
    private final NAR nar;

    public Rover(NAR nar) {
        this.nar = nar;
    }

    public class RoverModel {

        private final Body torso;

        Vec2 pooledHead = new Vec2();
        Vec2 point1 = new Vec2();
        Vec2 point2 = new Vec2();
        Vec2 d = new Vec2();
        Color3f laserColor = new Color3f(0.5f, 0.5f, 0.5f);
        List<VisionRay> vision = new ArrayList();

        //public class ChangedNumericInput //discretizer
        ChangedTextInput feltAngularVelocity = new ChangedTextInput(nar);
        ChangedTextInput feltOrientation = new ChangedTextInput(nar);
        ChangedTextInput feltSpeed = new ChangedTextInput(nar);
        

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

            }
            
            
            
            int pixels = 3;
            float aStep = 0.9f / pixels;
            float L = 11.0f;
            Vec2 frontRetina = new Vec2(0, 0.5f);
            for (int i = -pixels/2; i <= pixels/2; i++) {
                vision.add(new VisionRay("front" + i, torso, frontRetina, MathUtils.PI/2f + aStep*i, L, 3));
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

        public class VisionRay {
            final Vec2 point; //where the retina receives vision at
            final float angle;
            private final float distance;
            final ChangedTextInput sight = new ChangedTextInput(nar);
            final String id;
            RobotArm.RayCastClosestCallback ccallback = new RobotArm.RayCastClosestCallback();
            private final Body body;
            private final int distanceSteps;

            public VisionRay(String id, Body body, Vec2 point, float angle, float length) {
                this(id, body, point, angle, length, 10);
            }
            
            public VisionRay(String id, Body body, Vec2 point, float angle, float length, int steps) {
                this.id = id;
                this.body = body;
                this.point = point;
                this.angle = angle;
                this.distance = length;
                this.distanceSteps = steps;
            }
            
            int n=0;
            public void step() {
                n++;
                point1 = body.getWorldPoint(point);

                d.set(distance * MathUtils.cos(angle+body.getAngle()), distance * MathUtils.sin(angle+body.getAngle()));
                point2.set(point1);
                point2.addLocal(d);

                ccallback.init();
                getWorld().raycast(ccallback, point1, point2);

                Body hit = null;
                float d = Float.POSITIVE_INFINITY;
                if (ccallback.m_hit) {
                    d = ccallback.m_point.sub(point).length()/distance;
                    
                    getDebugDraw().drawPoint(ccallback.m_point, 5.0f, new Color3f(0.4f, 0.9f, 0.4f));
                    getDebugDraw().drawSegment(point1, ccallback.m_point, new Color3f(0.8f, 0.8f, 0.8f));
                    pooledHead.set(ccallback.m_normal);
                    pooledHead.mulLocal(.5f).addLocal(ccallback.m_point);
                    getDebugDraw().drawSegment(ccallback.m_point, pooledHead, new Color3f(0.9f, 0.9f, 0.4f));
                    hit = ccallback.body;
                } else {
                    getDebugDraw().drawSegment(point1, point2, laserColor);
                }
                
                Vec2 v1=point1;
                Vec2 v2=point2;
                double dx=v1.x-v2.x;
                double dy=v2.y-v2.y;
                float di=(float) Math.sqrt(dx*dx+dy*dy)/distance;

                if (hit!=null) {                                        
                    String dist = "unknown";                    
                    if (distanceSteps == 2) {
                        dist = "hit";
                    }
                    else if (distanceSteps < 10) {
                        dist = Texts.n1(di);
                    }
                    
                    if(n%500==0) {
                       
                        nar.addInput("<goal --> reached>!"); //also remember on goal
                    }
                    if(di <= 0.02f) {
                        float x = (float) Math.random() * sz - sz / 2f;
                        float y = (float) Math.random() * sz - sz / 2f;
                        //world.AddABlock(Phys, sz, sz);
                        hit.setTransform(new Vec2(x,y), hit.getAngle());
                        //Phys.getWorld().destroyBody(hit);
                        sight.set("<goal --> reached>. :|:");
                        
                    }
                    sight.set("<(*," + id + ",sth) --> see>. :|:");
                    //sight.set("<(*," + id + "," + dist + ") --> see>. :|:");
                }
                else {
                    sight.set("<(*," + id + ",empty) --> see>. :|:");
                }
            }
        }

        public void step() {
            for (VisionRay v : vision)
                v.step();
            
            feelMotion();
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
            float a = (float) (Math.log(Math.abs(xa * angleScale) + 1f));
            float maxAngleVelocityFelt = 0.8f;
            if (a > maxAngleVelocityFelt) {
                a = maxAngleVelocityFelt;
            }

            if (a < 0.1) {
               // feltAngularVelocity.set("<0 --> feltAngularMotion>. :|: %1.00;0.90%");
                //feltAngularVelocity.set("feltAngularMotion. :|: %0.00;0.90%");   
            } else {
                String direction;
                String da = Texts.n1(a);// + ",radPerFrame";
                if (xa < 0) {
                    direction = "left";
                } else /*if (xa > 0)*/ {
                    direction = "right";
                }
                //feltAngularVelocity.set("<(*," + da + "," + direction + ") --> feltAngularMotion>. :|:");
               // //feltAngularVelocity.set("<" + direction + " --> feltAngularMotion>. :|: %" + da + ";0.90%");
            }

            float h = torso.getAngle() % (MathUtils.TWOPI);
            if (h < 0) {
                h += MathUtils.TWOPI;
            }
            h = h / MathUtils.TWOPI;
            String dh = "a" + (int)(h*18);   // + ",rad";
           // feltOrientation.set("<" + dh + " --> feltOrientation>. :|:");

            float speed = Math.abs(torso.getLinearVelocity().length());
            if (speed > 0.9f) {
                speed = 0.9f;
            }
            String sp = Texts.n1(speed);
          //  feltSpeed.set("<" + sp + " --> feltSpeed>. :|:");
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
        if(rover.torso.getTransform().p.x>sz) {
            rover.torso.setTransform(new Vec2(-sz,rover.torso.getTransform().p.y),rover.torso.getAngle());
        }
        if(rover.torso.getTransform().p.y>sz) {
            rover.torso.setTransform(new Vec2(rover.torso.getTransform().p.x,-sz),rover.torso.getAngle());
        }
        if(rover.torso.getTransform().p.x<-sz) {
            rover.torso.setTransform(new Vec2(sz,rover.torso.getTransform().p.y),rover.torso.getAngle());
        }
        if(rover.torso.getTransform().p.y<-sz) {
            rover.torso.setTransform(new Vec2(rover.torso.getTransform().p.x,sz),rover.torso.getAngle());
        }
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
                nar.addInput(command);
            }

            
        }

        public RoverPanel(RoverModel rover) {
            super(new BorderLayout());

            {
                JPanel motorPanel = new JPanel(new GridLayout(0, 2));

                motorPanel.add(new InputButton("Stop", "(^motor,stop). :|:"));
                motorPanel.add(new InputButton("Forward", "(^motor,forward). :|:"));
                motorPanel.add(new InputButton("TurnLeft", "(^motor,turn,left). :|:"));
                motorPanel.add(new InputButton("TurnRight", "(^motor,turn,right). :|:"));
                motorPanel.add(new InputButton("Backward", "(^motor,backward). :|:"));

                add(motorPanel, BorderLayout.SOUTH);
            }
        }

    }

    public static PhysicsModel Phys;
    public class RoverWorld {

        public RoverWorld(PhysicsModel p, float w, float h) {
            Phys=p;
            for (int i = 0; i < 10; i++) {
                AddABlock(p, w, h);
            }
        }

        public void AddABlock(PhysicsModel p, float w, float h) {
            float x = (float) Math.random() * w - w / 2f;
            float y = (float) Math.random() * h - h / 2f;
            float bw = 1.0f;
            float bh = 1.6f;
            float a = 0;
            addBlock(p, x, y, bw, bh, a);
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

    public static RoverWorld world;
    //public static RoverWorld world;
    public static int sz=48;
    @Override
    public void initTest(boolean deserialized) {
        getWorld().setGravity(new Vec2());
        getWorld().setAllowSleep(false);

        world = new RoverWorld(this, sz, sz);
        
        rover = new RoverModel(this);
        rover.torso.setAngularDamping(20);
        rover.torso.setLinearDamping(10);

        //new NWindow("Rover Control", new RoverPanel(rover)).show(300, 200);

        addAxioms();
        addOperators();

    }

    public static float rotationSpeed = 100f;
    public static float linearSpeed = 5000f;
                
    protected void addOperators() {
        nar.addPlugin(new NullOperator("^motor") {
            @Override
            protected List<Task> execute(Operation operation, Term[] args, Memory memory) {
                Term t1 = args[0];
                float priority = operation.getTask().budget.getPriority();

                

                if (args.length > 1) {
                    Term t2 = args[1];
                    switch (t1.name().toString() + "," + t2.name().toString()) {
                        case "turn,left":
                            rover.rotate(rotationSpeed);
                            break;
                        case "turn,right":
                            rover.rotate(-rotationSpeed);
                            break;
                    }
                } else {
                    switch (t1.name().toString()) {
                        case "forward":
                            rover.thrust(0, linearSpeed);
                            break;
                        case "backward":
                            rover.thrust(0, -linearSpeed);
                            break;
                        case "stop":
                            rover.stop();
                            break;
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
        nar.addInput("<0 <-> 0.0>. %1.00;0.99%");
        nar.addInput("<0.0 <-> 0.1>. %1.00;0.50%");
        nar.addInput("<0.1 <-> 0.2>. %1.00;0.50%");
        nar.addInput("<0.2 <-> 0.3>. %1.00;0.50%");
        nar.addInput("<0.3 <-> 0.4>. %1.00;0.50%");
        nar.addInput("<0.4 <-> 0.5>. %1.00;0.50%");
        nar.addInput("<0.5 <-> 0.6>. %1.00;0.50%");
        nar.addInput("<0.6 <-> 0.7>. %1.00;0.50%");
        nar.addInput("<0.7 <-> 0.8>. %1.00;0.50%");
        nar.addInput("<0.8 <-> 0.9>. %1.00;0.50%");
        //nar.addInput("<feltOrientation <-> feltAngularMotion>?");
        //nar.addInput("<feltSpeed <-> feltAngularMotion>?");
        nar.addInput("<{left,right,forward,backward} --> direction>.");

    }

    @Override
    public String getTestName() {
        return "NARS Rover";
    }


    public static void main(String[] args) {
        //NAR nar = new DefaultNARBuilder().build();
        NAR nar = new DiscretinuousBagNARBuilder().
                setConceptBagLevels(100).
                setConceptBagSize(1024).
                realtime().
                build();

       // RoverWorld.world= new RoverWorld(rv, 48, 48);
        new NARPhysics<Rover>(nar, new Rover(nar)) {

        };
        
        //nar.param().duration.set(25);
        nar.start(25, 50);
        nar.param().noiseLevel.set(0);

       // new NWindow("Tasks",new TaskTree(nar)).show(300,600);
    }

}
