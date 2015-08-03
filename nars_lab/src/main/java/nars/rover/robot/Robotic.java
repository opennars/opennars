package nars.rover.robot;

import nars.NAR;
import nars.Symbols;
import nars.budget.Budget;
import nars.clock.SimulatedClock;
import nars.concept.Concept;
import nars.event.FrameReaction;
import nars.io.in.ChangedTextInput;
import nars.nal.nal2.Property;
import nars.nal.nal7.Tense;
import nars.rl.gng.NeuralGasNet;
import nars.rover.Material;
import nars.rover.PhysicsModel;
import nars.rover.Sim;
import nars.rover.physics.gl.JoglDraw;
import nars.rover.physics.j2d.SwingDraw;
import nars.rover.util.RayCastClosestCallback;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.DefaultTruth;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by me on 7/18/15.
 */
abstract public class Robotic {

    public Body torso;
    //public class ChangedNumericInput //discretizer
    public Sim sim;
    public final String id;
    public JoglDraw draw;
    float mass = 1f;

    public Robotic(String id) {
        this.id = id;
    }

    public void init(Sim p) {
        this.sim = p;
        this.draw = (JoglDraw)p.draw();
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
        public void before(Body b, JoglDraw d, float time) {
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
        public void before(Body b, JoglDraw d, float time) {
            float bb = nar.memory.emotion.busy() * 0.5f + 0.5f;
            //color.set(c.getRed()/256.0f * bb, c.getGreen()/256.0f * bb, c.getBlue()/256.0f * bb);
            float hh = nar.memory.emotion.happy() * 0.5f + 0.5f;
            color.set(Math.min(bb,1f), hh, 0);
            d.setFillColor(color);
        }


    }
}
