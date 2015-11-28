/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rover.robot;

import nars.NAR;
import nars.io.in.ChangedTextInput;
import nars.rover.Sim;
import nars.rover.obj.VisionRay;
import nars.rover.physics.gl.JoglAbstractDraw;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;

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

    //float tasteDistanceThreshold = 1.0f;
    final static int retinaPixels = 11;


    int retinaRaysPerPixel = 2; //rays per vision sensor

    float aStep = (float)(Math.PI*2f) / retinaPixels;

    float L = 25f; //vision distance

    Vec2 mouthPoint = new Vec2(2.7f, 0); //0.5f);
    @Deprecated int distanceResolution = 4;

    double mouthArc = Math.PI/6f; //in radians
    float biteDistanceThreshold = 0.05f;


    float linearDamping = 0.8f;
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


        linearVelocity = new SimpleAutoRangeTruthFrequency(nar, nar.term("<motion-->[linear]>"), new AutoRangeTruthFrequency(0.0f));
        motionAngle = new SimpleAutoRangeTruthFrequency(nar, nar.term("<motion-->[angle]>"), new BipolarAutoRangeTruthFrequency());
        facingAngle = new SimpleAutoRangeTruthFrequency(nar, nar.term("<motion-->[facing]>"), new BipolarAutoRangeTruthFrequency());

        addMotorController();
        //addMotorOperator();

        addAxioms();

        //String p = "$0.99;0.75;0.90$ ";

        //randomActions.add("motor($direction)!");
        //TODO : randomActions.add("motor($direction,$direction)!");



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

            VisionRay v = new VisionRay(this, torso,
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



            ((JoglAbstractDraw)draw).addLayer(v);

            senses.add(v);
        }
        return torso;
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
        /*new CycleDesire("motor(forward)", strongestTask, nar) {
            @Override
            float onFrame(float desire) {
                thrustRelative(desire * linearThrustPerCycle);
                return desire;
            }

        };*/
        /*new CycleDesire("motor(reverse)", strongestTask, nar) {
            @Override float onCycle(float desire) {
                thrustRelative(desire * -linearThrustPerCycle);
                return desire;
            }
        };*/

        new BiCycleDesire("motor(forward)", "motor(reverse)", strongestTask,nar) {

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

        new BiCycleDesire("motor(left)", "motor(right)", strongestTask,nar) {

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
//        new CycleDesire("motor(left)", strongestTask, nar) {
//            @Override float onCycle(float desire) {
//
//                return desire;
//            }
//        };
//        new CycleDesire("motor(right)", strongestTask, nar) {
//            @Override float onCycle(float desire) {
//
//                return desire;
//            }
//        };
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



        facingAngle.observe( angVelocity ); // );
        //nar.inputDirect(nar.task("<facing-->[" +  + "]>. :|:"));
        nar.input(nar.task("<A-->[w" + Sim.angleTerm(torso.getAngle()) + "]>. :|:"));

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
