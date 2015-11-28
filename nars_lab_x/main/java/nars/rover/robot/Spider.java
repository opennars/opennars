package nars.rover.robot;

import nars.rover.Sim;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

public class Spider extends Robotic {

    int arms;
    float armLength = 4.5f;
    float armWidth = 1.0f;
    int armSegments;

    float torsoRadius = 3.4f;

    float servoRange = ((float) Math.PI / 2.0f) * 0.9f;
    int servoSteps = 7;

    int numRetinasPerSegment = 32;
    int retinaLevels = 4;

    final int velocityLevels = 16;

    int orientationSteps = 9;

    double initialVisionDistance = 10.0;

    float armSegmentExponent;

    //public final CritterdingBrain brain = new CritterdingBrain();
    //List<Retina> retinas = new LinkedList();
    private final float ix;
    private final float iy;
    //private final Material m;

    //private final BrainWiring brainWiring;

    public Spider(String id, int arms, int armSegments, float armSegmentExponent, float ix, float iy) {
        super(id);
        this.arms = arms;
        this.armSegments = armSegments;
        this.armSegmentExponent = armSegmentExponent;
        this.ix = ix;
        this.iy = iy;
        //this.brainWiring = bw;
        //m = new Material(color, Color.DARK_GRAY, 2);
    }

    public void addArm(Body base, float x, float y, float angle, int armSegments, float armLength, float armWidth) {
        Body[] arm = new Body[armSegments];

        Body prev = base;

        double dr = getArmLength(armLength, 0) / 2.0;

        for (int i = 0; i < armSegments; i++) {

            float al = getArmLength(armLength, i);
            float aw = getArmWidth(armWidth, i);

            float ax = (float) (x + Math.cos(angle) * dr);
            float ay = (float) (y + Math.sin(angle) * dr);
            final Body b = arm[i] = sim.create(new Vec2(ax, ay),
                    sim.rectangle(new Vec2(al, aw), new Vec2(), angle), BodyType.DYNAMIC); //, angle, 1.0f, m);

            float rx = (float) (x + Math.cos(angle) * (dr - al / 2.0f));
            float ry = (float) (y + Math.sin(angle) * (dr - al / 2.0f));


            RevoluteJointDef rv = new RevoluteJointDef();
            rv.initialize(arm[i], prev, new Vec2(rx, ry));
            rv.enableLimit = true;
            rv.enableMotor = true;
            rv.upperAngle = 1;
            rv.lowerAngle = -1;
            //rv.referenceAngle = angle;

            sim.getWorld().createJoint(rv);

//            new RevoluteJointByIndexVote(brain, j, -servoRange, servoRange, servoSteps);
//
//            new QuantizedScalarInput(brain, 4) {
//                @Override
//                public float getValue() {
//                    return j.getJointAngle() / (float) (Math.PI * 2.0f);
//                }
//            };
//            new QuantizedScalarInput(brain, velocityLevels) {
//                @Override
//                public float getValue() {
//                    final float zl = b.getLinearVelocity().len2();
//                    if (zl == 0) return 0;
//                    float xx = b.getLinearVelocity().x;
//                    xx *= xx;
//                    return xx / zl;
//                }
//            };
//            new QuantizedScalarInput(brain, velocityLevels) {
//                @Override
//                public float getValue() {
//                    final float zl = b.getLinearVelocity().len2();
//                    if (zl == 0) return 0;
//                    float yy = b.getLinearVelocity().y;
//                    yy *= yy;
//                    return yy / zl;
//                }
//            };
//
//            new QuantizedScalarInput(brain, orientationSteps) {
//                @Override
//                public float getValue() {
//                    return b.getAngle() / (float) (2.0 * Math.PI);
//                }
//            };
//            new QuantizedScalarInput(brain, velocityLevels) {
//                @Override
//                public float getValue() {
//                    return b.getAngularVelocity() / (float) (2.0 * Math.PI);
//                }
//            };

            //DEPRECATED
            //brain.addInput(new RevoluteJointAngle(j));
            //brain.addInput(new VelocityAxis(b, true));
            //brain.addInput(new VelocityAxis(b, false));
            //Orientation.newVector(brain, b, orientationSteps);
            //brain.addInput(new VelocityAngular(b));

//            brain.addOutput(new ColorBodyTowards(b, color, 0.95f));
//            brain.addOutput(new ColorBodyTowards(b, new Color(color.r * 0.5f, color.g * 0.5f, color.b * 0.5f, color.a * 0.25f), 0.95f));

//            int n = numRetinasPerSegment;
//            for (float z = 0; z < n; z++) {
//
//                float a = z * (float) (Math.PI * 2.0 / ((float) n));
//                retinas.add(new Retina(brain, b, new Vector2(0, 0), a, (float) initialVisionDistance, retinaLevels));
//            }
            //TODO Retina.newVector(...)

            //y -= al*0.9f;
            dr += al * 0.9f;

            prev = arm[i];
        }


    }

    @Override
    public void init(Sim p) {
        this.sim = p;
        Body base = sim.create(new Vec2(ix, iy), sim.circle(torsoRadius), BodyType.DYNAMIC); //ix, iy, 1.0f, m);

        float da = (float) ((Math.PI * 2.0) / arms);
        float a = 0;
        for (int i = 0; i < arms; i++) {
            float ax = (float) (ix + (Math.cos(a) * torsoRadius));
            float ay = (float) (iy + (Math.sin(a) * torsoRadius));
            addArm(base, ax, ay, a, armSegments, armLength, armWidth);
            a += da;
        }

//        //base's eyes
//        int n = numRetinasPerSegment;
//        for (float z = 0; z < n; z++) {
//
//            float ba = z * (float) (Math.PI * 2.0 / ((float) n));
//            retinas.add(new Retina(brain, base, new Vector2(0, 0), ba, (float) initialVisionDistance, retinaLevels));
//
//            brain.addOutput(new Thruster(base, ba));
//        }
//
//
//        brainWiring.wireBrain(brain);
//
//        new BrainReport(brain);

    }

    @Override
    public RoboticMaterial getMaterial() {
        return new RoboticMaterial(this);
    }

    @Override
    protected Body newTorso() {
        return null;
    }

//    @Override
//    protected void update(double dt) {
//        for (final Retina r : retinas) {
//            r.update();
//        }
//
//
//        brain.forward();
//
//        brain.forwardOutputs();
//
//    }



    private float getArmLength(float armLength, int i) {
        //return armLength * (1.0f - ( (float)i ) / ((float) armSegments ) * 0.5f);

        return armLength * (float)Math.pow(armSegmentExponent, i);   //0.618 = golden ratio
    }

    private float getArmWidth(float armWidth, int i) {
        //return armWidth;
        return armWidth * (float)Math.pow(armSegmentExponent, i);
    }

}
        