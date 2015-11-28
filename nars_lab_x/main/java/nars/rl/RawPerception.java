package nars.rl;

import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.NAR;
import nars.Op;
import nars.nal.nal1.Inheritance;
import nars.nal.nal2.Instance;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;

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
    final Term idTerm;

    double min = -1;
    double max = 1.0;

    public RawPerception(String id, float confidence) {
        this.confidence = confidence;
        this.id = id;
        idTerm = Atom.the(id);
    }

    @Override
    public void init(RLEnvironment env, QLAgent agent) {
        this.env = env;
        this.agent = agent;

    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
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

            tasks.add(TaskSeed.make(nar.memory, x).judgment().present().truth(f, confidence));
        }

        return tasks;
    }

    public String getStateTerm(int s) {
        //return getStateTermRadix(s, 2);
        return getStateTermOrdinal(s);
    }

    public String getStateTermRadix(int t, int radix) {
        if (t == 0) return "(0)";

        String x = "(";

        String bin;
        switch(radix) {
            case 2: bin = Integer.toBinaryString(t); break;
            case 8: bin = Integer.toOctalString(t); break;
            default: bin = Integer.toString(t); break;
        }

        for (char c : bin.toCharArray()) {
            x += c + ",";
        }
        return x.substring(0,x.length() - 1) + ')';
    }

    public String getStateTermOrdinal(int s) {
        return "i" + s;
    }

    public Compound newState(NAR nar, int i) {
        //return (Compound) nar.term("<" + getStateTerm(i) + " {-> " + id + ">");
        return Instance.make(Atom.the(getStateTerm(i)), idTerm);
    }

    public float getFrequency(final double d) {
        double f = (d - min) / (max - min);
        return (float)f;
    }

    @Override
    public boolean isState(Term t) {
        if ((t instanceof Inheritance) && (t.complexity() == 4) ) {
            Inheritance ii = (Inheritance)t;
            if (!ii.getPredicate().equals(idTerm))
                return false;
            if (ii.getSubject().op() == Op.SET_EXT) {
                Term subj = ((Compound)ii.getSubject()).term(0);
                if (subj instanceof Atom) {
                    return true;
                }
            }
            return false;
//            String s = t.toString();
//            if (s.startsWith("<" + id + " --> [") && s.endsWith("]>")) {
//                //System.out.println(s + " " + t.getComplexity());
//            //if (s.startsWith("<{" + id) && s.endsWith("} --> state>")) {
//                return true;
//            }
        }
        return false;
    }

    public static class BipolarDirectPerception extends RawPerception {

        public BipolarDirectPerception(String id, float confidence) {
            super(id, confidence);
            setMin(0);
            setMax(1.0);
        }

        @Override
        public Iterable<Task> perceive(NAR nar, double[] input, double t) {
            double[] r = new double[input.length*2];
            int i = 0;
            for (int j = 0; j < input.length; j++) {
                double x = input[j];
                double pos, neg;
                if (x > 0) {
                    pos = x;
                    neg = 0;
                }
                else {
                    neg = -x;
                    pos = 0;
                }

                r[i++] = pos;
                r[i++] = neg;
            }
            return super.perceive(nar, r, t);
        }
    }
}
