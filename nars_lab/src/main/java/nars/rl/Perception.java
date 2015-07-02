package nars.rl;

import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.NAR;
import nars.task.Task;
import nars.term.Term;

/* dimension reduction / input processing implementations */
public interface Perception  {

    /**
     * initialize for a QLAgent that will use it
     * returns the # dimensions the processed perception
     * will be represented in
     */
    public void init(RLEnvironment env, QLAgent agent);

    /**
     * process the next vector of input; calls agent methods (ex: input) in reaction to input at time 't'
     */
    public Iterable<Task> perceive(NAR nar, double[] input, double t);

    /**
     * whether the given term is an input state involved in q-learning
     */
    public boolean isState(Term t);
}
