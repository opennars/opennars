package nars.rl;

import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.nal.nal1.Inheritance;
import nars.nal.term.Term;

/** TODO inputs the perceived data directly as frequency data */
public class RawPerception implements Perception {

    private final float confidence;
    private final String id;

    private RLEnvironment env;
    private NARQLAgent agent;

    public RawPerception(String id, float confidence) {
        this.confidence = confidence;
        this.id = id;
    }

    @Override
    public void init(RLEnvironment env, NARQLAgent agent) {
        this.env = env;
        this.agent = agent;
    }

    @Override
    public void perceive(double[] input, double t) {
        //simple binary +/- 0 discretization
        for (int i = 0; i < input.length; i++) {

            float f = getFrequency(input[i]);
            agent.perceive("<{" + id + i + "} --> state>", f, confidence);
        }

    }

    public float getFrequency(double d) {
        float f = (float)(d / 2f) + 0.5f;
        f = Math.min(1.0f, f);
        f = Math.max(0.0f, f);
        return f;
    }

    @Override
    public boolean isState(Term t) {
        //TODO better pattern recognizer
        String s = t.toString();
        if ((t instanceof Inheritance)/* && (t.getComplexity() == 4)*/) {
            if (s.startsWith("<{" + id) && s.endsWith("} --> state>")) {
                return true;
            }
        }
        return false;
    }
}
