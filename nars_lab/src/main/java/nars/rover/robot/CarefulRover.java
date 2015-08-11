package nars.rover.robot;

import nars.Memory;
import nars.NAR;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.NullOperator;
import nars.rover.physics.gl.JoglDraw;
import nars.task.Task;
import nars.term.Term;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;

import java.util.ArrayList;
import java.util.List;

/**
 * Motor with full and precise control over its sense input
 */
public class CarefulRover extends AbstractPolygonBot {

    float linearDamping = 0.9f;
    float angularDamping = 0.6f;
    float restitution = 0.9f; //bounciness
    float friction = 0.5f;



    final ArrayList<String> randomActions = new ArrayList<>();
    private float visionDistanceFactor = 1f;


    public void randomAction() {
        int candid = (int) (Math.random() * randomActions.size());
        nar.input(randomActions.get(candid));
    }

    public static String command(Operation operation, Memory memory) {
        Term[] args = operation.argArray();
        Term t1 = args[0];

        //float priority = operation.getTask().getPriority();

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
        return command;
    }

    public CarefulRover(String id, NAR nar) {
        super(id, nar);

        nar.on(new NullOperator("vision") {
            @Override
            protected List<Task> execute(Operation o, Memory memory) {

                final String command = command(o, memory);

                switch (command) {
                    case "far":
                        visionDistanceFactor = 2f;
                        break;
                    case "near":
                        visionDistanceFactor = 1f;
                        break;
                }

                return null;
            }
        });

        randomActions.add("vision(far)!");
        randomActions.add("vision(near)!");

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

        nar.on(new NullOperator("motor") {

            @Override
            protected List<Task> execute(Operation operation, Memory memory) {

                String command = command(operation, memory);

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

    }

    @Override
    public RoboticMaterial getMaterial() {
        return new RoboticMaterial(this) {


        };
    }

    @Override
    protected Body newTorso() {
        PolygonShape shape = new PolygonShape();

        Vec2[] vertices = {new Vec2(3.0f, 0.0f), new Vec2(-1.0f, +2.0f), new Vec2(-1.5f, 0), new Vec2(-1.0f, -2.0f)};
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

        Vec2 center = new Vec2(0,0);

        int numPixels = 16;
        float da = (float)(Math.PI*2/numPixels);
        float a = 0;
        for (int i = 0; i < numPixels; i++) {
            VisionRay v = new VisionRay(torso,
                        /*eats ?*/ center /*: new Vec2(0,0)*/,
                    a, da, 3, 10, 8) {

                @Override protected float getDistance() {
                    return distance * visionDistanceFactor;
                }
            };

            draw.addLayer(v);
            senses.add(v);

            a += da;
        }

            return torso;

    }

}
