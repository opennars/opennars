package nars.rl;

import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.NAR;
import nars.nal.Task;
import nars.nal.nal1.Inheritance;
import nars.nal.term.Compound;
import nars.nal.term.Term;

import java.util.ArrayList;
import java.util.List;

/** inputs the perceived data directly as unipolar frequency data
 *  TODO normalization
 * */
public class RawPerception implements Perception {

    private final float confidence;
    private final String id;

    private RLEnvironment env;
    private QLAgent agent;
    final List<Compound> states = new ArrayList();

    public RawPerception(String id, float confidence) {
        this.confidence = confidence;
        this.id = id;
    }

    @Override
    public void init(RLEnvironment env, QLAgent agent) {
        this.env = env;
        this.agent = agent;

    }

    @Override
    public Iterable<Task> perceive(NAR nar, double[] input, double t) {
        List<Task> tasks = new ArrayList(input.length);

        //simple binary +/- 0 discretization
        for (int i = 0; i < input.length; i++) {

            float f = getFrequency(input[i]);

            Compound x;
            if (states.size() > i) {
                x = states.get(i);
            }
            else {
                states.add(x = newState(nar, i));
            }

            tasks.add(nar.memory.newTask(x).judgment().present().truth(f, confidence).get());
        }

        return tasks;
    }

    public String getStateTerm(int s) {
        return getStateTermBinaryProduct(s);
    }

    public String getStateTermBinaryProduct(int t) {
        if (t == 0) return "(0)";

        String x = "(";
        //String bin = Integer.toBinaryString(t);
        String bin = Integer.toOctalString(t);

        for (char c : bin.toCharArray()) {
            x += c + ",";
        }
        return x.substring(0,x.length() - 1) + ')';
    }

    public String getStateTermOrdinal(int s) {
        return "i" + s;
    }

    public Compound newState(NAR nar, int i) {
        return (Compound) nar.term("<" + id + " --> [" + getStateTerm(i) + "]>");
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
        if ((t instanceof Inheritance) /*&& (t.getComplexity() == 4)*/ ) {
            if (s.startsWith("<" + id + " --> [") && s.endsWith("]>")) {
            //if (s.startsWith("<{" + id) && s.endsWith("} --> state>")) {
                return true;
            }
        }
        return false;
    }
}
