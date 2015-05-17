package nars.exec;

import nars.Events;
import nars.Memory;
import nars.NAR;
import nars.nal.concept.Concept;
import nars.nal.Task;
import nars.nal.term.Atom;
import nars.nal.term.Term;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executes a set of certain specified operations
 * according to a function of truth, priority, etc..
 * ex: If above threshold, fires at a specified frequency function of the truth summary() or priority()
 */
public class BeliefTruthExecutive extends AbstractExecutive {


    private final Operator operator;

    public float basePeriod = 1.0f; //fundamental frqeuency to scale all action frequencies
    private Memory memory;

    public class Action {

        public final Term term;
        private long lastAct = 0;
        private float period; //(1/freq) in cycles

        public Action(String id) {
            this.term = Atom.get(operator.getTerm().toString().replace("^", "") + id);
            setFrequency(0);
        }

        public Action setFrequency(float newFreq) {
            //TODO handle freq > 1, firing multiple times or a >1 strength variable
            if (newFreq == 0)
                period = 0;
            else
                period = 1.0f / newFreq;
            lastAct = opReg.getMemory().time();
            return this;
        }

        public float getPeriod() {
            return period;
        }

        public float getFrequency() {
            if (period == 0) return 0;
            return 1.00f / period;
        }

        public float getActualFrequency() {
            return getFrequency() * basePeriod;
        }

        public void update(long now) {
            if (period == 0) {
                return;
            }

            if (now >= (lastAct + period)) {
                fire(now);
            }

        }

        public void fire(long now) {
            this.lastAct = now;
            execute(operator, term);
        }
    }

    final Map<String, Action> actions = new HashMap();

    public BeliefTruthExecutive(String operatorName) {
        super();

        this.operator = new Operator(operatorName) {
            @Override
            protected List<Task> execute(Operation operation, Term[] args) {
                BeliefTruthExecutive.this.execute(operator, args);
                return null;
            }
        };
    }

    public Action add(String actionName) {
        Action a = new Action(actionName);
        actions.put(actionName, a);
        return a;
    }
    public Action add(String actionName, Runnable r) {
        Action a = new Action(actionName) {
            public void fire(long now) {
                super.fire(now);
                r.run();
            }
        };
        actions.put(actionName, a);
        return a;
    }

    @Override
    public Class[] getEvents() {
        return new Class[] { Events.DecideExecution.class, Events.CycleEnd.class };
    }

    protected void updateActions() {
        long now = memory.time();
        for (Action a : actions.values()) {
            a.update(now);
        }
    }

    @Override
    public void event(Class event, Object[] args) {
        super.event(event, args);
        if (event == Events.CycleEnd.class) {
            updateActions();
        }
    }

    /** called when an action is triggered */
    public void execute(Operator operator, Term... args) {

    }

    @Override
    public void onEnabled(NAR n) {
        opReg = n.on(operator);
        this.memory = n.memory;
    }

    @Override
    public void onDisabled(NAR n) {
        opReg.off();
    }

    @Override
    protected boolean decide(Concept c, Task executableTask) {
        return false;
    }

}
