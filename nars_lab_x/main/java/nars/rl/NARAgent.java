package nars.rl;

import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.NAR;
import nars.nal.nal8.OpReaction;
import nars.nal.nal8.Operator;
import nars.term.Term;
import nars.util.event.EventEmitter;

/**
 * Generic interface for agent interface controllers
 */
abstract public class NARAgent {

    public final RLEnvironment env;

    /** for fast lookup of operation terms, since they will be used frequently */
    public final Operation[] operationCache;
    public final ObjectIntHashMap<Operation> operationToAction = new ObjectIntHashMap();

    public final Operator operator;
    private EventEmitter.Registrations opReg;
    public final NAR nar;

    public NARAgent(NAR nar, RLEnvironment env, Term operator) {
        super();

        this.nar = nar;
        this.env = env;
        this.operationCache = new Operation[env.numActions()];

        this.operator = new Operator(operator);

    }

    /** call at the end of an implementation's constructor */
    protected void init() {
        opReg = nar.on(getOperator(operator.the()));
    }

    protected abstract OpReaction getOperator(Term operator);
}
