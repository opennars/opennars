package nars.rl;

import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.NAR;
import nars.nal.nal1.Inheritance;
import nars.rl.hai.Hsom;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Term;

import java.util.Collections;

/** TODO inputs the perceived data in a raw numerically discretized form for each dimension */

public class HaiSOMPerception implements Perception {

    private final String id;
    private final float confidence;
    private final Atom idTerm;
    private int somSize;
    private Hsom som = null;
    private QLAgent agent;
    private RLEnvironment env;

    public HaiSOMPerception(String id, int somSize, float confidence) {
        this.id = id;
        this.somSize = somSize;
        this.confidence = confidence;
        this.idTerm = Atom.the(id);
    }

    @Override
    public void init(RLEnvironment env, QLAgent agent) {
        this.env = env;

        this.agent = agent;

        if (somSize == -1) somSize = env.numStates()+1;
        som = new Hsom(somSize, env.numStates());
    }


    @Override
    public Iterable<Task> perceive(NAR nar, double[] input, double t) {

        som.learn(input);
        //int s = som.winnerx * env.inputDimension() + som.winnery;
        //System.out.println(Arrays.toString(input) + " " + reward );
        //System.out.println(som.winnerx + " " + som.winnery + " -> " + s);

        //System.out.println(Arrays.deepToString(q));
        // agent.learn(s, reward);

        int x = som.winnerx;
        int y = som.winnery;

        return Collections.singleton(
                //TODO avoid String parsing
                //nar.task("<state --> [(*," + id + x + "," + id + y + ")]>. :|: %1.00;" + confidence + '%')
                nar.task("<{(*,s" + x + ",s" + y + ")} --> " + id + ">. :|: %1.00;" + confidence + '%')
        );
    }

    @Override
    public boolean isState(Term t) {
        //TODO better pattern recognizer
        String s = t.toString();
        int complexity = t.complexity();

        //allow complxity to increase to a certain amount to include aggregate states
        if ((t instanceof Inheritance) && (complexity >= 6) && (complexity <= 9)) {
            if (((Inheritance)t).getPredicate().equals(idTerm)) {
                //System.out.println(t + " " + t.getComplexity());
                return true;
            }
        }
        return false;
    }
}
