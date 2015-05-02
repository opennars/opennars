package nars.rl;

import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.nal.nal1.Inheritance;
import nars.nal.term.Term;
import nars.rl.hai.Hsom;

/** TODO inputs the perceived data in a raw numerically discretized form for each dimension */

public class HaiSOMPerception implements Perception {

    private final String id;
    private final float confidence;
    private int somSize;
    private Hsom som = null;
    private QLAgent agent;
    private RLEnvironment env;

    public HaiSOMPerception(String id, int somSize, float confidence) {
        this.id = id;
        this.somSize = somSize;
        this.confidence = confidence;
    }

    @Override
    public void init(RLEnvironment env, QLAgent agent) {
        this.env = env;

        this.agent = agent;

        if (somSize == -1) somSize = env.inputDimension()+1;
        som = new Hsom(somSize, env.inputDimension());
    }


    @Override
    public void perceive(double[] input, double t) {

        som.learn(input);
        //int s = som.winnerx * env.inputDimension() + som.winnery;
        //System.out.println(Arrays.toString(input) + " " + reward );
        //System.out.println(som.winnerx + " " + som.winnery + " -> " + s);

        //System.out.println(Arrays.deepToString(q));
        // agent.learn(s, reward);

        int x = som.winnerx;
        int y = som.winnery;

        agent.perceive("<(*," + id + x + "," + id + y + ") --> [state]>", 1, confidence);
    }

    @Override
    public boolean isState(Term t) {
        //TODO better pattern recognizer
        String s = t.toString();
        if ((t instanceof Inheritance) /*&& (t.getComplexity() == 6)*/) {
            if (s.startsWith("<(*," + id) && s.endsWith(") --> [state]>")) {
                //System.out.println(t + " " + t.getComplexity());
                return true;
            }
        }
        return false;
    }
}
