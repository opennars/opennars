package nars.rover.robot;

import nars.NAR;
import nars.rover.Material;
import nars.rover.Sim;
import nars.rover.physics.gl.JoglAbstractDraw;
import org.jbox2d.common.Color3f;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import java.awt.*;

/**
 * Created by me on 7/18/15.
 */
abstract public class Robotic {

    public Body torso;
    //public class ChangedNumericInput //discretizer
    public Sim sim;
    public final String id;
    public JoglAbstractDraw draw;
    float mass = 1f;

    public Robotic(String id) {
        this.id = id;
    }

    public void init(Sim p) {
        this.sim = p;
        this.draw = (JoglAbstractDraw)p.draw();
        this.torso = newTorso();
        this.torso.setUserData(getMaterial());
    }


    public String getID() {
        return id;
    }

    public abstract RoboticMaterial getMaterial();

    /** create the body and return its central component */
    protected abstract Body newTorso();

    public World getWorld() {
        return sim.getWorld();
    }

    public void step(int i) {

    }


    public static class RoboticMaterial extends Material {


        protected final Color3f color;
        private final Robotic robot;

        public RoboticMaterial(Robotic r) {
            super();
            this.robot = r;

            float h = (getID().hashCode() % 10) / 10f;
            Color c = Color.getHSBColor(h, 0.5f, 0.95f);
            color = new Color3f(c.getRed()*256f, c.getGreen()*256f, c.getBlue()*256f);
        }

        @Override
        public void before(Body b, JoglAbstractDraw d, float time) {
//            color.set(color.x,
//                    color.y,
//                    color.z);
            d.setFillColor(color);
        }

        @Override
        public String toString() {
            return getID();
        }

        public String getID() {
            return robot.getID();
        }
    }

    public static class NARRoverMaterial extends RoboticMaterial {

        private final NAR nar;

        public NARRoverMaterial(Robotic r, NAR nar) {
            super(r);
            this.nar = nar;
        }

        @Override
        public void before(Body b, JoglAbstractDraw d, float time) {
            float bb = nar.memory.emotion.busy() * 0.5f + 0.5f;
            //color.set(c.getRed()/256.0f * bb, c.getGreen()/256.0f * bb, c.getBlue()/256.0f * bb);
            float hh = nar.memory.emotion.happy() * 0.5f + 0.5f;
            color.set(Math.min(bb,1f), hh, 0);
            d.setFillColor(color);
        }


    }
}
