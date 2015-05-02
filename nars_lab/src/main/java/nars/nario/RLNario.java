package nars.nario;

import automenta.vivisect.Video;
import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.NAR;
import nars.gui.NARSwing;
import nars.prototype.Default;
import nars.rl.QLAgent;
import nars.rl.Perception;
import nars.rl.RawPerception;

import java.awt.*;

/**
 * Created by me on 4/26/15.
 */
public class RLNario extends NARio implements RLEnvironment {

    public RLNario(NAR nar, Perception... p) {
        super(nar);

        QLAgent agent = new QLAgent(nar, this, p) {

        };

        agent.brain.setEpsilon(0.10);
        agent.init();

        Video.themeInvert();
        new NARSwing(nar);
    }

    public static void main(String[] args) {
        NAR n = new NAR(new Default());
        new RLNario(n, new RawPerception("r", 0.7f));

    }

    @Override
    @Deprecated public double[] observe() {
        return new double[0];
    }

    @Override
    public double reward() {
        return dx / 10.0;
    }

    @Override
    public void takeAction(int action) {

    }

    @Override
    public void worldStep() {

    }

    @Override
    public Component component() {
        return null;
    }

    @Override
    public int numActions() {
        return 5;
    }
}
