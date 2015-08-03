/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rover.robot;

import nars.Memory;
import nars.NAR;
import nars.io.in.ChangedTextInput;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.NullOperator;
import nars.rover.Sim;
import nars.rover.physics.gl.JoglDraw;
import nars.task.Task;
import nars.term.Term;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Triangular mobile vehicle
 */
public class Rover extends AbstractPolygonBot {

    private final ChangedTextInput feltAngularVelocity;
    private final ChangedTextInput feltOrientation;
    private final ChangedTextInput feltSpeed;
    private final ChangedTextInput feltSpeedAvg;
    private final ChangedTextInput mouthInput;

    final double minVisionInputProbability = 0.9f;
    final double maxVisionInputProbability = 1.0f;

    //float tasteDistanceThreshold = 1.0f;
    final static int retinaPixels = 12;


    int retinaRaysPerPixel = 4; //rays per vision sensor

    float aStep = (float)(Math.PI*2f) / retinaPixels;

    float L = 25f; //vision distance

    Vec2 mouthPoint = new Vec2(2.7f, 0); //0.5f);
    @Deprecated int distanceResolution = 6;

    double mouthArc = Math.PI/6f; //in radians
    float biteDistanceThreshold = 0.05f;


    float linearDamping = 0.9f;
    float angularDamping = 0.6f;

    float restitution = 0.9f; //bounciness
    float friction = 0.5f;


    final SimpleAutoRangeTruthFrequency linearVelocity;
    final SimpleAutoRangeTruthFrequency motionAngle;
    final SimpleAutoRangeTruthFrequency facingAngle;

    //public class DistanceInput extends ChangedTextInput
    public Rover(String id, NAR nar) {
        super(id, nar);


        mouthInput = new ChangedTextInput(nar);
        feltAngularVelocity = new ChangedTextInput(nar);
        feltOrientation = new ChangedTextInput(nar);
        feltSpeed = new ChangedTextInput(nar);
        feltSpeedAvg = new ChangedTextInput(nar);


        linearVelocity = new SimpleAutoRangeTruthFrequency(nar, nar.term("<motion-->[linear]>"), new AutoRangeTruthFrequency(0.02f));
        motionAngle = new SimpleAutoRangeTruthFrequency(nar, nar.term("<motion-->[angle]>"), new BipolarAutoRangeTruthFrequency());
        facingAngle = new SimpleAutoRangeTruthFrequency(nar, nar.term("<motion-->[facing]>"), new BipolarAutoRangeTruthFrequency());

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

    @Override
    public void init(Sim p) {
        super.init(p);




    }

    @Override
    public RoboticMaterial getMaterial() {
        return new NARRoverMaterial(this, nar);
    }

    @Override
    protected Body newTorso() {
        PolygonShape shape = new PolygonShape();

        Vec2[] vertices = {new Vec2(3.0f, 0.0f), new Vec2(-1.0f, +2.0f), new Vec2(-1.0f, -2.0f)};
        shape.set(vertices, vertices.length);
        //shape.m_centroid.set(bodyDef.position);
        BodyDef bd = new BodyDef();
        bd.linearDamping=(linearDamping);
        bd.angularDamping=(angularDamping);
        bd.type = BodyType.DYNAMIC;
        bd.position.set(0, 0);

        Body torso = getWorld().createBody(bd);
        Fixture f = torso.createFixture(shape, mass);
        f.setRestitution(restitution);
        f.setFriction(friction);



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

                    if (touched.getUserData() instanceof Sim.Edible) {

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



            ((JoglDraw)draw).addLayer(v);

            vision.add(v);
        }
        return torso;
    }


    final ArrayList<String> randomActions = new ArrayList<>();



    public void randomAction() {
        int candid = (int) (Math.random() * randomActions.size());
        nar.input(randomActions.get(candid));
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
                    nar.input(nar.task("motor(left)! " + tPos));
                    nar.input(nar.task("motor(right)! " + tNeg));
                } else if (v < 0.5f) {
                    nar.input(nar.task("motor(left)! " + tNeg));
                    nar.input(nar.task("motor(right)! " + tPos));
                } else if (v < 0.75f) {
                    nar.input(nar.task("motor(forward)! " + tPos));
                    nar.input(nar.task("motor(reverse)! " + tNeg));
                } else {
                    nar.input(nar.task("motor(forward)! " + tNeg));
                    nar.input(nar.task("motor(reverse)! " + tPos));
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


    @Override
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
            Vec2 movement = currentPosition.sub( positions.poll() );
            double theta = Math.atan2(movement.y, movement.x);
            motionAngle.observe((float)theta);
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
        nar.input(nar.task("<angvel-->[" + Sim.f(angVelocity) + "]>. :|:"));

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

}
