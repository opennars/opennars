//package nars.rover;
//
//import automenta.vivisect.Video;
//import nars.Memory;
//import nars.NAR;
//import nars.io.in.ChangedTextInput;
//import nars.io.Texts;
//import nars.model.impl.Default;
//import nars.nal.Task;
//import nars.nal.nal8.operator.NullOperator;
//
//import nars.nal.term.Term;
//import nars.rover.jbox2d.TestbedPanel;
//import nars.rover.jbox2d.TestbedSettings;
//import org.jbox2d.collision.shapes.PolygonShape;
//import org.jbox2d.common.Color3f;
//import org.jbox2d.common.MathUtils;
//import org.jbox2d.common.Vec2;
//import org.jbox2d.dynamics.Body;
//import org.jbox2d.dynamics.BodyDef;
//import org.jbox2d.dynamics.BodyType;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.KeyEvent;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//
///**
// * NARS Rover
// *
// * @author me
// */
//public class Rover extends PhysicsModel {
//    public static int decrease_of_importance_step=30;
//    public static int cnt=0;
//    public static int do_sth_importance=0;
//    public static RoverModel rover;
//    private final NAR nar;
//
//    public Rover(NAR nar) {
//        this.nar = nar;
//    }
//
//    public class RoverModel {
//
//        private final Body torso;
//
//        Vec2 pooledHead = new Vec2();
//        Vec2 point1 = new Vec2();
//        Vec2 point2 = new Vec2();
//        Vec2 d = new Vec2();
//        Color3f laserColor = new Color3f(0.5f, 0.5f, 0.5f);
//        List<VisionRay> vision = new ArrayList();
//
//        //public class ChangedNumericInput //discretizer
//        ChangedTextInput feltAngularVelocity = new ChangedTextInput(nar);
//        ChangedTextInput feltOrientation = new ChangedTextInput(nar);
//        ChangedTextInput feltSpeed = new ChangedTextInput(nar);
//
//
//        //public class DistanceInput extends ChangedTextInput
//        public RoverModel(RoverWorld world,PhysicsModel p) {
//            float mass = 2.25f;
//            PolygonShape shape = new PolygonShape();
//            shape.setAsBox(0.5f, 0.5f);
//
//            BodyDef bd = new BodyDef();
//            bd.linearDamping = 0.9f;
//            bd.angularDamping = 0.9f;
//
//            bd.type = BodyType.DYNAMIC;
//            bd.position.set(0, 0);
//            torso = p.getWorld().createBody(bd);
//            torso.createFixture(shape, mass);
//
////            {
////                Body prevBody = ground;
////
//            // Define upper arm.
//            /*for (float axis = -1f; axis <= 1f; axis += 2f) {
//                Body upperArm;
//                RevoluteJoint leftShoulderJoint;
//                Body lowerArm;
//                RevoluteJoint elbowJoint;
//
//                PolygonShape upperArmShape = new PolygonShape();
//                upperArmShape.setAsBox(0.3f, 0.1f);
//                PolygonShape lowerArmShape = new PolygonShape();
//                lowerArmShape.setAsBox(0.4f, 0.07f);
//
//                BodyDef bdUA = new BodyDef();
//                bdUA.type = BodyType.DYNAMIC;
//                bdUA.position.set(axis * 0.82f, 0f);
//                upperArm = getWorld().createBody(bdUA);
//                upperArm.createFixture(upperArmShape, 0.1f);
//
//                RevoluteJointDef rjd = new RevoluteJointDef();
//                rjd.initialize(torso, upperArm, new Vec2(axis * 0.55f, 0.0f));
//                rjd.motorSpeed = 0; //1.0f * MathUtils.PI;
//                //rjd.maxMotorTorque = 10000.0f;
//                rjd.enableMotor = true;
//                float min = -MathUtils.PI / 1f * 0.1f;
//                float max = MathUtils.PI / 1f * 0.2f;
//                rjd.lowerAngle = min;
//                rjd.upperAngle = max;
//                rjd.enableLimit = true;
//                rjd.collideConnected = false;
//                leftShoulderJoint = (RevoluteJoint) getWorld().createJoint(rjd);
//
//                BodyDef bdLA = new BodyDef();
//                bdLA.type = BodyType.DYNAMIC;
//                bdLA.position.set(axis * 1.5f, 0f);
//                lowerArm = getWorld().createBody(bdLA);
//                lowerArm.createFixture(lowerArmShape, 0.05f);
//
//                RevoluteJointDef rjd2 = new RevoluteJointDef();
//                rjd2.initialize(upperArm, lowerArm, new Vec2(axis * 1.2f, 0.0f));
//                rjd2.enableMotor = true;
//                rjd2.lowerAngle = -1.2f;
//                rjd2.upperAngle = 1.2f;
//                rjd2.enableLimit = true;
//                rjd.collideConnected = false;
//                elbowJoint = (RevoluteJoint) getWorld().createJoint(rjd2);
//
//            }*/
//
//
//
//            int pixels = 8;
//            float aStep = 1.8f / pixels;
//            float retinaArc = aStep;
//            int retinaResolution = 5; //should be odd # to balance
//            float L = 50.0f;
//            Vec2 frontRetina = new Vec2(0, 0.5f);
//            for (int i = -pixels/2; i <= pixels/2; i++) {
//                vision.add(new VisionRay(world,"front" + i, torso, frontRetina, MathUtils.PI/2f + aStep*i*1.0f,
//                            retinaArc, retinaResolution, L, 3));
//            }
//
//            /*pixels=3;
//            Vec2 backRetina = new Vec2(0, -0.5f);
//            for (int i = -pixels/2; i <= pixels/2; i++) {
//                vision.add(new VisionRay(world,"back" + i, torso, backRetina, -(MathUtils.PI/2f + aStep*i*4),
//                           retinaArc, retinaResolution,
//                           5.5f, 3));
//            }*/
//
//            //Vec2 backRetina = new Vec2(0, -0.5f);
//            //vision.add(new VisionRay("back", torso, backRetina, -MathUtils.PI/2f, L/2f, 3));
//
//
//            /*
//            int n = 0;
//            float LS = 0.4f;
//            float LT = 1.95f;
//            for (float sonarAngle = 0f; sonarAngle < MathUtils.TWOPI; sonarAngle+=0.6f) {
//                float ca = (float)Math.cos(sonarAngle) * LT;
//                float sa = (float)Math.sin(sonarAngle) * LT;
//                vision.add(new VisionRay("radar" + n, torso,
//                        new Vec2(ca, sa), sonarAngle + MathUtils.PI/16, LS, 2));
//                n++;
//            }
//            */
//
//        }
//
//        public class VisionRay {
//            final Vec2 point; //where the retina receives vision at
//            final float angle;
//            private final float distance;
//            final ChangedTextInput sight = new ChangedTextInput(nar);
//            final String id;
//            RobotArm.RayCastClosestCallback ccallback = new RobotArm.RayCastClosestCallback();
//            private final Body body;
//            private final int distanceSteps;
//            private final int resolution;
//            private final float arc;
//            RoverWorld world;
//
//            public VisionRay(RoverWorld world,String id, Body body, Vec2 point, float angle, float arc, int resolution, float length, int steps) {
//                this.world=world;
//                this.id = id;
//                this.body = body;
//                this.point = point;
//                this.angle = angle;
//                this.arc = arc;
//                this.resolution = resolution;
//                this.distance = length;
//                this.distanceSteps = steps;
//            }
//
//            int n=0;
//            public void step() {
//                n++;
//                point1 = body.getWorldPoint(point);
//
//                Body hit = null;
//
//                float minDist = 999999;
//
//                float dArc = arc / resolution;
//                for (int r = 0; r < resolution; r++) {
//                    float da = (-arc / 2f) + dArc * r;
//                    d.set(distance * MathUtils.cos(da + angle + body.getAngle()), distance * MathUtils.sin(da + angle + body.getAngle()));
//                    point2.set(point1);
//                    point2.addLocal(d);
//
//                    ccallback.init();
//                    getWorld().raycast(ccallback, point1, point2);
//
//                    if (ccallback.m_hit) {
//                        float d = ccallback.m_point.sub(point1).length() / distance;
//                        /*Vec2 v1=point1;
//                         Vec2 v2=hit.getTransform().p;
//                         double dx=v1.x-v2.x;
//                         double dy=v1.y-v2.y;
//                         float d = (float) Math.sqrt(dx*dx+dy*dy)/distance;*/
//
//                        //draw().drawPoint(ccallback.m_point, 5.0f, new Color3f(0.4f, 0.9f, 0.4f));
//                        //draw().drawSegment(point1, ccallback.m_point, new Color3f(0.8f, 0.8f, 0.8f));
//                        pooledHead.set(ccallback.m_normal);
//                        pooledHead.mulLocal(.5f).addLocal(ccallback.m_point);
//                        //draw().drawSegment(ccallback.m_point, pooledHead, new Color3f(0.9f, 0.9f, 0.4f));
//
//
//                        if (d < minDist) {
//                            minDist = d;
//                            hit = ccallback.body;
//                        }
//                    } else {
//                        //draw().drawSegment(point1, point2, laserColor);
//                    }
//                }
//
//                boolean good=world.goods.contains(hit);
//
//                if (hit!=null) {
//
//                    float di = minDist;
//                    if(id.startsWith("back")) {
//                        if(good) {
//                            sight.set("<goal --> reached>. :|: %0.0;0.90%");
//                        } else {
//                            sight.set("<goal --> reached>. :|: %1.0;0.90%");
//                        }
//                        return;
//                    }
//
//                    String dist = "unknown";
//                    if (distanceSteps == 2) {
//                        dist = "hit";
//                    }
//                    else if (distanceSteps < 10) {
//                        dist = String.valueOf(Texts.n1(di));
//                    }
//
//                    if(n%500==0) {
//                        nar.input("<goal --> reached>!"); //also remember on goal
//                    }
//                    if(di <= 0.2f) {
//                        float x = (float) Math.random() * sz - sz / 2f;
//                        float y = (float) Math.random() * sz - sz / 2f;
//                        //world.AddABlock(Phys, sz, sz);
//                        hit.setTransform(new Vec2(x*2.0f,y*2.0f), hit.getAngle());
//                        //Phys.getWorld().destroyBody(hit);
//                        if(good) {
//                            sight.set("<goal --> reached>. :|:");
//                        } else {
//                            sight.set("<goal --> reached>. :|: %0.0;0.90%");
//                        }
//
//                    }
//                    String Sgood= good ? "good" : "bad";
//                    //sight.set("<(*," + id + ",sth) --> see>. :|:");
//                    //sight.set("<(*," + id + "," + dist + ","+Sgood+") --> see>. :|:");
//                    //sight.set("<(*," + id + "," + dist + ","+Sgood+") --> see>. :|:");
//                    if(Sgood.equals("bad") && n%25==0) {
//                        nar.input("(--,<(*," + id + ",good) --> see>). :|:");
//                    } else if(n%50==0) {
//                        //nar.addInput("<(*," + id + ",good) --> see>. :|:");
//                        nar.input("<(*," + id + "," + dist + "," + Sgood + ") --> see>. :|:");
//                    }
//                }
//                else {
//                    //no need to do that
//                    //sight.set("<(*," + id + ",empty) --> see>. :|:");
//                }
//            }
//        }
//
//        boolean feel_motion=false; //todo add option in gui
//
//        public void step() {
//            for (VisionRay v : vision)
//                v.step();
//
//            if(Rover.cnt>=do_sth_importance) {
//                Rover.cnt=0;
//                Rover.do_sth_importance+=decrease_of_importance_step; //increase
//                nar.input("(^motor,random)!");
//            }
//
//            if(feel_motion) {
//                feelMotion();
//            }
//            Rover.cnt++;
//        }
//
//
//        public void thrust(float angle, float force) {
//            angle += torso.getAngle() + Math.PI / 2; //compensate for initial orientation
//
//            torso.applyForceToCenter(new Vec2((float) Math.cos(angle) * force, (float) Math.sin(angle) * force));
//        }
//
//        public void rotate(float torque) {
//            torso.applyTorque(torque);
//        }
//
//        protected void feelMotion() {
//            //radians per frame to a discretized value
//            float xa = torso.getAngularVelocity();
//            float angleScale = 1.50f;
//            float a = (float) (Math.log(Math.abs(xa * angleScale) + 1f));
//            float maxAngleVelocityFelt = 0.8f;
//            if (a > maxAngleVelocityFelt) {
//                a = maxAngleVelocityFelt;
//            }
//
//            if (a < 0.1) {
//                feltAngularVelocity.set("$0.1;0.1$ <0 --> feltAngularMotion>. :|: %1.00;0.90%");
//                //feltAngularVelocity.set("feltAngularMotion. :|: %0.00;0.90%");
//            } else {
//                String direction;
//                char da = Texts.n1(a);// + ",radPerFrame";
//                if (xa < 0) {
//                    direction = "left";
//                } else /*if (xa > 0)*/ {
//                    direction = "right";
//                }
//                feltAngularVelocity.set("$0.1;0.1$ <(*," + da + "," + direction + ") --> feltAngularMotion>. :|:");
//               // //feltAngularVelocity.set("<" + direction + " --> feltAngularMotion>. :|: %" + da + ";0.90%");
//            }
//
//            float h = torso.getAngle() % (MathUtils.TWOPI);
//            if (h < 0) {
//                h += MathUtils.TWOPI;
//            }
//            h /= MathUtils.TWOPI;
//            String dh = "a" + (int)(h*18);   // + ",rad";
//            feltOrientation.set("$0.1;0.1$ <" + dh + " --> feltOrientation>. :|:");
//
//            float speed = Math.abs(torso.getLinearVelocity().length());
//            if (speed > 0.9f) {
//                speed = 0.9f;
//            }
//            char sp = Texts.n1(speed);
//            feltSpeed.set("$0.1;0.1$ <" + sp + " --> feltSpeed>. :|:");
//            //feltSpeed.set("feltSpeed. :|: %" + sp + ";0.90%");
//        }
//
//        public void stop() {
//            torso.setAngularVelocity(0);
//            torso.setLinearVelocity(new Vec2());
//        }
//
//    }
//
//    @Override
//    public void step(float timeStep, TestbedSettings settings, TestbedPanel panel) {
//        super.step(timeStep, settings, panel);
//
//        rover.step();
//        if(rover.torso.getTransform().p.x>sz) {
//            rover.torso.setTransform(new Vec2(-sz,rover.torso.getTransform().p.y),rover.torso.getAngle());
//        }
//        if(rover.torso.getTransform().p.y>sz) {
//            rover.torso.setTransform(new Vec2(rover.torso.getTransform().p.x,-sz),rover.torso.getAngle());
//        }
//        if(rover.torso.getTransform().p.x<-sz) {
//            rover.torso.setTransform(new Vec2(sz,rover.torso.getTransform().p.y),rover.torso.getAngle());
//        }
//        if(rover.torso.getTransform().p.y<-sz) {
//            rover.torso.setTransform(new Vec2(rover.torso.getTransform().p.x,sz),rover.torso.getAngle());
//        }
//    }
//
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
//
//        }
//
//        public RoverPanel(RoverModel rover) {
//            super(new BorderLayout());
//
//            {
//                JPanel motorPanel = new JPanel(new GridLayout(0, 2));
//
//                motorPanel.add(new InputButton("Stop", "(^motor,stop). :|:"));
//                motorPanel.add(new InputButton("Forward", "(^motor,forward). :|:"));
//                motorPanel.add(new InputButton("TurnLeft", "(^motor,turn,left). :|:"));
//                motorPanel.add(new InputButton("TurnRight", "(^motor,turn,right). :|:"));
//                motorPanel.add(new InputButton("Backward", "(^motor,backward). :|:"));
//
//                add(motorPanel, BorderLayout.SOUTH);
//            }
//        }
//
//    }
//
//    public static PhysicsModel Phys;
//    public class RoverWorld {
//
//        public RoverWorld(PhysicsModel p, float w, float h) {
//            Phys=p;
//            for (int i = 0; i < 10; i++) {
//                AddABlock(p, w, h,false);
//            }
//            /*for (int i = 0; i < 10; i++) {
//                AddABlock(p, w, h,true);
//            }*/
//        }
//
//        public void AddABlock(PhysicsModel p, float w, float h, boolean bad) {
//            float x = (float) Math.random() * w - w / 2f;
//            float y = (float) Math.random() * h - h / 2f;
//            float bw = 1.0f;
//            float bh = 1.6f;
//            float a = 0;
//            addBlock(p, x*2.0f, y*2.0f, bw, bh, a,bad);
//        }
//
//
//        public HashSet<Body> goods=new HashSet<Body>();
//        public HashSet<Body> bads=new HashSet<Body>();
//        public void addBlock(PhysicsModel p, float x, float y, float w, float h, float a,boolean bad) {
//            float mass = 0.25f;
//            PolygonShape shape = new PolygonShape();
//            if(bad) {
//                shape.set(new Vec2[]{new Vec2(0,0),new Vec2(0,w*3),new Vec2(h*2.5f,w*2.5f)}, 3);
//            }
//            else {
//                shape.setAsBox(w, h);
//            }
//
//            BodyDef bd = new BodyDef();
//            bd.linearDamping = 0.95f;
//            bd.angularDamping = 0.8f;
//
//            bd.type = BodyType.DYNAMIC;
//            bd.position.set(x, y);
//            Body body = p.getWorld().createBody(bd);
//            body.createFixture(shape, mass);
//            body.setAngularDamping(10);
//            body.setLinearDamping(15);
//            if(!bad) {
//                goods.add(body);
//            }
//            else {
//                bads.add(body);
//            }
//        }
//    }
//
//    public static RoverWorld world;
//    //public static RoverWorld world;
//    public static int sz=48;
//    @Override
//    public void initTest(boolean deserialized) {
//        getWorld().setGravity(new Vec2());
//        getWorld().setAllowSleep(false);
//
//        world = new RoverWorld(this, sz, sz);
//
//        rover = new RoverModel(world,this);
//        rover.torso.setAngularDamping(20);
//        rover.torso.setLinearDamping(10);
//
//        //new NWindow("Rover Control", new RoverPanel(rover)).show(300, 200);
//
//        addAxioms();
//        addOperators();
//
//    }
//
//    public static float rotationSpeed = 100f;
//    public static float linearSpeed = 5000f;
//
//    public static boolean allow_imitate=false;
//
//    protected void addOperators() {
//        nar.on(new NullOperator("^motor") {
//            @Override
//            protected List<Task> execute(Operation operation, Memory memory) {
//                Term[] args = operation.argArray();
//                Term t1 = args[0];
//                float priority = operation.getTask().getPriority();
//
//
//                if (args.length > 2) {
//                    Term t2 = args[1];
//                    switch (t1.name().toString() + "," + t2.name().toString()) {
//                        case "turn,left":
//                            rover.rotate(rotationSpeed);
//                            break;
//                        case "turn,right":
//                            rover.rotate(-rotationSpeed);
//                            break;
//                    }
//                } else {
//                    switch (t1.name().toString()) {
//                        case "forward":
//                            rover.thrust(0, linearSpeed);
//                            break;
//                        case "backward":
//                            rover.thrust(0, -linearSpeed);
//                            break;
//                        case "stop":
//                            rover.stop();
//                            break;
//                        case "random": //tend forward
//                            //nar.addInput("(^motor,random). :|:\n");
//                            rover.thrust(0, linearSpeed);
//                            //nar.step(100);
//
//
//                            if (true) { //allow_subcons
//                                ArrayList<String> candids = new ArrayList<>();
//                                candids.add("(^motor,turn,left). :|:");
//                                candids.add("(^motor,turn,right). :|:");
//                                //candids.add("(^motor,backward). :|:");
//                                candids.add("(^motor,forward). :|:");
//                                candids.add("(^motor,forward). :|:");
//                                int candid = (int) (Math.random() * candids.size() - 0.001);
//                                nar.input(candids.get(candid));
//                                if (candid >= 3)
//                                    rover.thrust(0, linearSpeed);
//                                if (candid == 2)
//                                    rover.thrust(0, -linearSpeed);
//                                if (candid == 1)
//                                    rover.rotate(-rotationSpeed);
//                                if (candid == 0)
//                                    rover.rotate(rotationSpeed);
//                            } else {
//                                ArrayList<String> candids = new ArrayList<>();
//                                candids.add("(^motor,turn,left)! :|:");
//                                candids.add("(^motor,turn,right)! :|:");
//                                candids.add("(^motor,backward)! :|:");
//                                candids.add("(^motor,forward)! :|:");
//                                int candid = (int) (Math.random() * candids.size() - 0.001);
//                                nar.input(candids.get(candid));
//                            }
//
//                            //{"(^motor,turn,left)! :|:", "(^motor,turn,right)! :|:", "(^motor,forward)! :|:", "(^motor,backward)! :|:"};
//
//                            break;
//                    }
//                }
//                Rover.cnt = 0;
//                return null;
//            }
//        });
//
//    }
//
//    protected void addAxioms() {
//        //nar.addInput("<{positiveNumber,negativeNumber} --> number>. %1.00;0.99%");
//        //nar.addInput("<{0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9} --> positiveNumber>. %1.00;0.99%");
//        //nar.addInput("<{-0.0,-0.1,-0.2,-0.3,-0.4,-0.5,-0.6,-0.7,-0.8,-0.9} --> negativeNumber>. %1.00;0.99%");
//        //nar.addInput("<0.0 <-> -0.0>. %1.00;0.99%");
//        //nar.addInput("<{0.0,0.1} --> zeroishNumber>. %1.00;0.99%");
//        //nar.addInput("<{0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9} --> positiveNumber>. %1.00;0.99%");
//        nar.input("<0 <-> 0.0>. %1.00;0.99%");
//        nar.input("<0.0 <-> 0.1>. %1.00;0.50%");
//        nar.input("<0.1 <-> 0.2>. %1.00;0.50%");
//        nar.input("<0.2 <-> 0.3>. %1.00;0.50%");
//        nar.input("<0.3 <-> 0.4>. %1.00;0.50%");
//        nar.input("<0.4 <-> 0.5>. %1.00;0.50%");
//        nar.input("<0.5 <-> 0.6>. %1.00;0.50%");
//        nar.input("<0.6 <-> 0.7>. %1.00;0.50%");
//        nar.input("<0.7 <-> 0.8>. %1.00;0.50%");
//        nar.input("<0.8 <-> 0.9>. %1.00;0.50%");
//        //nar.addInput("<feltOrientation <-> feltAngularMotion>?");
//        //nar.addInput("<feltSpeed <-> feltAngularMotion>?");
//        nar.input("<{left,right,forward,backward} --> direction>.");
//
//    }
//
//    @Override
//    public String getTestName() {
//        return "NARS Rover";
//    }
//
//
//    public static void main(String[] args) {
//
//        Video.themeInvert();
//
//        //NAR nar = new Default().
//        //NAR nar = new DiscretinuousBagNARBuilder().
//        NAR nar = new NAR(new Default().simulationTime());
//
//        float framesPerSecond = 50f;
//        int cyclesPerFrame = 10; //was 200
//        (nar.param).outputVolume.set(0);
//        (nar.param).duration.set(cyclesPerFrame);
//
//       // RoverWorld.world= new RoverWorld(rv, 48, 48);
//        NARPhysics phys=new NARPhysics<Rover>(nar, 1.0f / framesPerSecond, new Rover(nar)) {
//
//            @Override
//            public void frame() {
//                super.frame();
//                nar.memory.timeSimulationAdd(cyclesPerFrame);
//            }
//
//            @Override
//            public void keyPressed(KeyEvent e) {
//
//                Rover.RoverModel rover=Rover.rover;
//                float rotationSpeed = Rover.rotationSpeed;
//                float linearSpeed = Rover.linearSpeed;
//
//                if (e.getKeyCode() == KeyEvent.VK_UP) {
//                    if(!Rover.allow_imitate) {
//                        nar.input("(^motor,forward). :|:");
//                    } else {
//                        nar.input("(^motor,forward)! :|:");
//                    }
//                    rover.thrust(0, linearSpeed);
//                }
//                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//                    if(!Rover.allow_imitate) {
//                        nar.input("(^motor,backward). :|:");
//                    } else {
//                        nar.input("(^motor,backward)! :|:");
//                    }
//                    rover.thrust(0, -linearSpeed);
//                }
//                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
//                    if(!Rover.allow_imitate) {
//                        nar.input("(^motor,turn,left). :|:");
//                    } else {
//                        nar.input("(^motor,turn,left)! :|:");
//                    }
//                    rover.rotate(rotationSpeed);
//                }
//                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
//                    if(!Rover.allow_imitate) {
//                        nar.input("(^motor,turn,right). :|:");
//                    } else {
//                        nar.input("(^motor,turn,right)! :|:");
//                    }
//                    rover.rotate(-rotationSpeed);
//                }
//
//            }
//
//
//        };
//
//        //nar.start(((long) (1000f / framesPerSecond)));//, cyclesPerFrame, 1.0f);
//        //setSpeed(1.0f);
//       // new NWindow("Tasks",new TaskTree(nar)).show(300,600);
//    }
//
//}
