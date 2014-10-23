package nars.narclear;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.build.ContinuousBagNARBuilder;
import nars.entity.Task;
import nars.io.ChangedTextInput;
import nars.io.Texts;
import nars.language.Term;
import nars.narclear.jbox2d.TestbedSettings;
import nars.narclear.jbox2d.j2d.DrawPhy2D;
import nars.operator.NullOperator;
import nars.operator.Operation;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;

/**
 * NARS Rover
 *
 * @author me
 */
public class Rover2 extends PhysicsModel {
    public int decrease_of_importance_step=30;
    public int cnt=0;
    public int do_sth_importance=0;
    
    float curiosity = 0.01f;
    
    /* how often to input mission, in cycles */
    int missionPeriod = 200;

    boolean wraparound = false;
    
    public static RoverModel rover;
    private final NAR nar;

    

    public static enum Material implements DrawPhy2D.DrawProperty {
        Food, Wall, Block;
        
        static final Color foodStroke = new Color(0.25f, 1f, 0.25f);
        static final Color foodFill = new Color(0.15f, 0.9f, 0.15f);

        static final Color wallStroke = new Color(0.25f, 0.25f, 0.25f);
        static final Color wallFill = new Color(0.5f, 0.5f, 0.5f);
        
        @Override        
        public void before(Body b, DrawPhy2D d) {
            switch (this) {
                case Food:
                    d.setStrokeColor(foodStroke);
                    d.setFillColor(foodFill);
                    break;
                case Wall:
                    d.setStrokeColor(wallStroke);
                    d.setFillColor(wallFill);
                    break;
            }
        }
    };
    
    public Rover2(NAR nar) {
        this.nar = nar;
    }

    public class RoverModel {

        private final Body torso;

        Vec2 pooledHead = new Vec2();
        Vec2 point1 = new Vec2();
        Vec2 point2 = new Vec2();
        Vec2 d = new Vec2();
        
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
            float aStep = 1.5f / pixels;
            float retinaArc = aStep;
            int retinaResolution = 5; //should be odd # to balance
            float L = 35.0f;
            Vec2 frontRetina = new Vec2(0, 0.5f);
            int distanceResolution = 7;
            for (int i = -pixels/2; i <= pixels/2; i++) {
                final int ii = i;
                vision.add(new VisionRay("front" + i, torso, frontRetina, MathUtils.PI/2f + aStep*i*1.2f,
                            retinaArc, retinaResolution, L, distanceResolution) {
                                
                               float touchThresholdDistance = 0.15f;
                                
                               @Override
                               public void onTouch(Body touched, float di) {
                                   if (di <= touchThresholdDistance) {
                                    if (Math.abs(ii) <= 1) { //mouth
                                        if (touched.getUserData() == Material.Food) {
                                             eat(touched); 
                                        }
                                    }
                                   }
                                        
                               }
                            }
                );
            }
            
            
            pixels=5;
            aStep = 1.2f/pixels;
            retinaResolution = 3;
            L = 5.5f;
            retinaArc = 0.9f;
            
            for (int i = -pixels/2; i <= pixels/2; i++) {
                float angle = -(MathUtils.PI/2f + aStep*i*4);
                float d1 = 0.5f;
                Vec2 backRetina = new Vec2((float)Math.cos(angle)*d1, (float)Math.sin(angle)*d1);
                
                vision.add(new VisionRay("back" + i, torso, backRetina, angle,
                           retinaArc, retinaResolution,
                           L, distanceResolution));
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
            float x = (float) Math.random() * sz - sz / 2f;
            float y = (float) Math.random() * sz - sz / 2f;
            //world.AddABlock(Phys, sz, sz);
            food.setTransform(new Vec2(x*2.0f,y*2.0f), food.getAngle());            
            //Phys.getWorld().destroyBody(hit);
            nar.addInput("<goal --> eat>. :|:");                                       
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
            private final int resolution;
            private final float arc;
            
            
            final Color3f laserUnhitColor = new Color3f(0.25f, 0.25f, 0.25f);
            final Color3f laserHitColor = new Color3f(laserUnhitColor.x, laserUnhitColor.y, laserUnhitColor.z);
            final Color3f sparkColor = new Color3f(0.4f, 0.9f, 0.4f);
            final Color3f normalColor = new Color3f(0.9f, 0.9f, 0.4f);

            public VisionRay(String id, Body body, Vec2 point, float angle, float arc, int resolution, float length, int steps) {
                this.id = id;
                this.body = body;
                this.point = point;
                this.angle = angle;
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
                    getWorld().raycast(ccallback, point1, point2);

                    if (ccallback.m_hit) {
                        float d = ccallback.m_point.sub(point1).length() / distance;
                        
                        laserHitColor.x = Math.min(1.0f, laserUnhitColor.x + 0.75f * (1.0f - d));
                                                
                        draw().drawPoint(ccallback.m_point, 5.0f, sparkColor);
                        draw().drawSegment(point1, ccallback.m_point, laserHitColor);
                        pooledHead.set(ccallback.m_normal);
                        pooledHead.mulLocal(.5f).addLocal(ccallback.m_point);
                        draw().drawSegment(ccallback.m_point, pooledHead, normalColor);
                        
                        totalDist += d;
                        if (d < minDist) {
                            hit = ccallback.body;
                            minDist = d;
                        }
                    } else {
                        draw().drawSegment(point1, point2, laserUnhitColor);
                        totalDist += 1;
                    }
                }
                
                

                if (hit!=null) {  
                    
                    float meanDist = totalDist / resolution;
                    float percentDiff = Math.abs(meanDist - minDist);
                    float conf = 0.5f + 0.5f * (1.0f - percentDiff);
                    if (conf > 0.99f) conf = 0.99f;
                    System.out.println(minDist + " " + meanDist + " " + percentDiff + " " + conf);
                            
                    float di = minDist; 
                    
                    String dist = "unknown";                    
                    if (distanceSteps == 2) {
                        dist = "hit";
                    }
                    else if (distanceSteps < 10) {
                        dist = Texts.n1(di);
                    }
                    
                    onTouch(hit, di);
                    
                    String material = hit.getUserData()!=null ? hit.getUserData().toString() : "sth";
                    
                    //float freq = 0.5f + 0.5f * di;
                    float freq = 1f;
                    
                    //sight.set("<(*," + id + ",sth) --> see>. :|:");
                    String ss = "$" + Texts.n2(conf) + "$ " + "<(*," + id + "," + dist + "," + material +") --> see>. :|: %" + Texts.n2(freq) + ";" + Texts.n2(conf) + "%";
                    sight.set(ss);
                    
                }
                else {
                    sight.set("<(*," + id + ",empty) --> see>. :|:");
                }
            }
            
            public void onTouch(Body hit, float di) {
            }
        }
        

        boolean feel_motion=true; //todo add option in gui

        public void step() {
            if(cnt%missionPeriod==0) {
                nar.addInput("<goal --> eat>!"); //also remember on goal
            }
                    
            for (VisionRay v : vision)
                v.step();
            
            /*if(cnt>=do_sth_importance) {
                cnt=0;
                do_sth_importance+=decrease_of_importance_step; //increase
                nar.addInput("(^motor,random)!");
            }*/
            
            if (Math.random() < curiosity) {
                nar.addInput("(^motor,random)!");
            }
            
            if(feel_motion) {
                feelMotion();
            }
            
            cnt++;
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
                feltAngularVelocity.set("$0.1;0.1$ <0 --> feltAngularMotion>. :|: %1.00;0.90%");
                //feltAngularVelocity.set("feltAngularMotion. :|: %0.00;0.90%");   
            } else {
                String direction;
                String da = Texts.n1(a);// + ",radPerFrame";
                if (xa < 0) {
                    direction = "left";
                } else /*if (xa > 0)*/ {
                    direction = "right";
                }
                feltAngularVelocity.set("$0.1;0.1$ <(*," + da + "," + direction + ") --> feltAngularMotion>. :|:");
               // //feltAngularVelocity.set("<" + direction + " --> feltAngularMotion>. :|: %" + da + ";0.90%");
            }

            float h = torso.getAngle() % (MathUtils.TWOPI);
            if (h < 0) {
                h += MathUtils.TWOPI;
            }
            h /= MathUtils.TWOPI;
            String dh = "a" + (int)(h*18);   // + ",rad";
            feltOrientation.set("$0.1;0.1$ <" + dh + " --> feltOrientation>. :|:");

            float speed = Math.abs(torso.getLinearVelocity().length());
            if (speed > 0.9f) {
                speed = 0.9f;
            }
            String sp = Texts.n1(speed);
            feltSpeed.set("$0.1;0.1$ <" + sp + " --> feltSpeed>. :|:");
            //feltSpeed.set("feltSpeed. :|: %" + sp + ";0.90%");
        }

        public void stop() {
            torso.setAngularVelocity(0);
            torso.setLinearVelocity(new Vec2());
        }

    }
    

    @Override
    public void step(float timeStep, TestbedSettings settings) {
        super.step(timeStep, settings);

        rover.step();
        
        //wraparound
        if (wraparound) {
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

    
    
    public class RoverWorld {
        private final PhysicsModel p;

        public RoverWorld(PhysicsModel p, float w, float h) {
            this.p = p;
            
            for (int i = 0; i < 20; i++) {
                addFood(w, h);
            }
            
            float wt = 1f;
            addWall(0, h, w, wt, 0);
            addWall(-w, 0, wt, h, 0);
            addWall(w, 0, wt, h, 0);
            addWall(0, -h, w, wt, 0);            
        }

        public void addFood(float w, float h) {
            float x = (float) Math.random() * w - w / 2f;
            float y = (float) Math.random() * h - h / 2f;
            
            float minSize = 0.25f;
            float maxSize = 1.5f;
            
            float bw = (float)(minSize + Math.random() * (maxSize - minSize));
            float bh = (float)(minSize + Math.random() * (maxSize - minSize));
            float a = 0;
            float mass = 0.25f;
            Body b = addBlock(x*2.0f, y*2.0f, bw, bh, a, mass);
            b.applyAngularImpulse((float)Math.random());
            b.setUserData(Material.Food);
        }

        
        public Body addWall(float x, float y, float w, float h, float a) {
            Body b = addBlock(x, y, w, h, a, 0);
            b.setUserData(Material.Wall);
            return b;
        }
        
        public Body addBlock(float x, float y, float w, float h, float a, float mass) {
            
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(w, h);

            BodyDef bd = new BodyDef();
            if (mass!=0) {
                bd.setLinearDamping(0.95f);
                bd.setAngularDamping(0.8f);
                bd.type = BodyType.DYNAMIC;
            }
            else {
                bd.type = BodyType.STATIC;
            }
            

            bd.position.set(x, y);
            Body body = p.getWorld().createBody(bd);            
            body.createFixture(shape, mass);
            body.setAngularDamping(10);
            body.setLinearDamping(15);
            return body;
        }
    }

    public RoverWorld world;
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
    public static float linearSpeed = 2000f;
                
    public static boolean allow_imitate=true;

    static final ArrayList<String> randomActions=new ArrayList<>();
    static {
        randomActions.add("(^motor,turn,left)! :|:");
        randomActions.add("(^motor,turn,right)! :|:");
        //randomActions.add("(^motor,backward)! :|:");
        randomActions.add("(^motor,forward)! :|:");
    }
    
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
                        case "random": //tend forward
                            //nar.addInput("(^motor,forward). :|:\n100\n");
                            //nar.addInput("(^motor,forward). :|:\n");
                            //rover.thrust(0, linearSpeed);
                            //nar.step(100);
                            
 
                            //if(true) { //allow_subcons
                                int candid=(int)(Math.random()*randomActions.size());
                                nar.addInput(randomActions.get(candid));
                                /*if(candid>=3)
                                    rover.thrust(0, linearSpeed);
                                if(candid==2)
                                    rover.thrust(0, -linearSpeed);
                                if(candid==1)
                                    rover.rotate(-rotationSpeed);
                                if(candid==0)
                                    rover.rotate(rotationSpeed);*/
//                            } else {
//                                int candid=(int)(Math.random()*randomActions.size()-0.001);
//                                nar.addInput(randomActions.get(candid));
//                            }
                            
                            //{"(^motor,turn,left)! :|:", "(^motor,turn,right)! :|:", "(^motor,forward)! :|:", "(^motor,backward)! :|:"};
                            
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
        //NAR nar = new DiscretinuousBagNARBuilder().
        NAR nar = new ContinuousBagNARBuilder().
                setConceptBagLevels(100).
                setConceptBagSize(1024).simulationTime().
                build();
        
        float framesPerSecond = 50f;
        int cyclesPerFrame = 500; //was 200        
        nar.param().noiseLevel.set(0);
        
        

        // RoverWorld.world= new RoverWorld(rv, 48, 48);
        new NARPhysics<Rover2>(nar, 1.0f / framesPerSecond, new Rover2(nar)) {

            @Override
            public void keyPressed(KeyEvent e) {
                 
                Rover2.RoverModel rover=Rover2.rover;
                float rotationSpeed = Rover2.rotationSpeed;
                float linearSpeed = Rover2.linearSpeed;

                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if(!Rover2.allow_imitate) {
                        nar.addInput("(^motor,forward). :|:");
                    } else {
                        nar.addInput("(^motor,forward)! :|:");
                    }
                    rover.thrust(0, linearSpeed);
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if(!Rover2.allow_imitate) {
                        nar.addInput("(^motor,backward). :|:");
                    } else {
                        nar.addInput("(^motor,backward)! :|:");
                    }
                    rover.thrust(0, -linearSpeed);
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    if(!Rover2.allow_imitate) {
                        nar.addInput("(^motor,turn,left). :|:");
                    } else {
                        nar.addInput("(^motor,turn,left)! :|:");
                    }
                    rover.rotate(rotationSpeed);
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if(!Rover2.allow_imitate) {
                        nar.addInput("(^motor,turn,right). :|:");
                    } else {
                        nar.addInput("(^motor,turn,right)! :|:");
                    }
                    rover.rotate(-rotationSpeed);
                }

            }

            
        };
        
        nar.startFPS(framesPerSecond, cyclesPerFrame, 1.0f);

       // new NWindow("Tasks",new TaskTree(nar)).show(300,600);
    }

}
