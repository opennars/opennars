package nars.rl;

import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.Memory;
import nars.NAR;
import nars.event.FrameReaction;
import nars.io.Texts;
import nars.nal.DirectProcess;
import nars.nal.Task;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.term.Term;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;

/**
 * Additional interfaces for interacting with RL environment
 */
abstract public class QLAgent<S extends Term> extends QLTermMatrix<S, Operation> {


    private final RLEnvironment env;
    private final RL rl;

    private final ArrayRealVector actByExpectation;
    private final ArrayRealVector actByPriority;
    private final Perception[] perceptions;

    public Operation actByQStrongest = null; //default q-action if NARS does not specify one by the next frame
    double lastReward = 0;


    /**
     * corresponds to the numeric operation as specified by the environment
     */
    protected Operation operation(int i) {
        //return nar.term("move(" + i + ")"); //TODO write narseesparser test to handle this, it wont parse
        return nar.term("(^move," + i + ",SELF)");
    }


    final java.util.List<Task> incoming = new ArrayList();

    /**
     * @param nar
     * @param env
     * @param p
     * @param epsilon randomness factor
     */
    public QLAgent(NAR nar, RLEnvironment env, Perception... perceptions) {
        super(nar);

        //HACK TODO this is necessary to disable the superclass's state belief update in methods we end up calling, this class has its own belief update method that should only be called
        stateUpdateConfidence = 0;

        this.perceptions = perceptions;
        for (Perception p : perceptions) {
            p.init(env, this);
        }


        this.env = env;

        this.rl = new RL();

        this.actByPriority = new ArrayRealVector(env.numActions());
        this.actByExpectation = new ArrayRealVector(env.numActions());


        nar.on(new Operator("^move") {


            @Override
            protected java.util.List<Task> execute(Operation operation, Term[] args, Memory memory) {

                if (args.length != 2) { // || args.length==3) { //left, self
                    //System.err.println(this + " ?? " + Arrays.toString(args));
                    return null;
                }

                Term ta = ((Operation) operation.getTerm()).getArgument(0);
                try {
                    int a = Integer.parseInt(ta.toString());
                    rl.desire(a, operation);

                } catch (NumberFormatException e) {

                }

                return null;
            }
        });


    }

    @Override
    public void init() {
        super.init();

        for (int i = 0; i < env.numActions(); i++) {
            Operation a = operation(i);
            cols.include(a);
        }

        possibleDesire(0.75f);

        setqAutonomicGoalConfidence(0.55f);
    }

    @Override
    public boolean isState(Term s) {
        for (Perception p : perceptions) {
            if (p.isState(s))
                return true;
        }
        return false;
    }

    @Override
    public boolean isAction(Term a) {
        if (a instanceof Operation)
            return cols.contains((Operation)a);
        return false;
    }


    /**
     * adds a perception belief of a given strength (0..1.0) to the input buffer
     */
    public void perceive(String term, float freq, float conf) {
        perceive((S)nar.term(term), freq, conf);
    }

    /**
     * adds a perception belief of a given strength (0..1.0) to the input buffer
     */
    public Task perceive(S term, float freq, float conf) {
        Task t = nar.task(term + ". :|: %" + freq + ";" + conf + "%");
        incoming.add(t);
        return t;
    }


    /**
     * interface to an RL world/experiment/etc..
     * implements a per-frame reaction in which an action
     * is decided either by NARS or an Agent, the world updated,
     * and the new input processed by the Agent's perception interface
     */
    class RL extends FrameReaction {

        public RL() {
            super(nar);

        }

        public void desire(int action, Operation operation) {
            desire(action, operation.getTask().getPriority(), operation.getTaskExpectation());
        }

        protected void desire(int action, float priority, float expectation) {
            actByPriority.addToEntry(action, priority);
            actByExpectation.addToEntry(action, expectation);

            //ALTERNATIVE: sum expectation * taskPriority
            //actByExpectation.addToEntry(action, expectation * priority);
        }


        @Override
        public void onFrame() {
            QLAgent.this.onFrame();
        }


    }

    ;

    /**
     * decides which action, TODO make this configurable
     */
    public synchronized Term decide() {

        double m = actByExpectation.getL1Norm();
        if (m == 0) return null;

        int winner = actByExpectation.getMaxIndex();
        if (winner == -1) return null; //no winner?

        RealVector normalized = actByExpectation.unitVector();
        double alignment = normalized.dotProduct(actByExpectation);

        //System.out.print("NARS exec: '" + winner + "' (from " + actByExpectation + " total executions) vs. '" + actByQStrongest + "' qAct");
        //System.out.println("  volition_coherency: " + Texts.n4(alignment * 100.0) + "%");

        actByExpectation.mapMultiplyToSelf(0); //zero
        actByPriority.mapMultiplyToSelf(0); //zero

        return operation(winner);
    }

    protected void onFrame() {
        long now = nar.time();

        Term action = decide();

        if ((action == null) && ((qAutonomicGoalConfidence > 0) || ((qAutonomicBeliefConfidence > 0)))) {
            action = actByQStrongest;
            //System.out.print("QL auto: " + action);

            if (action == null) {
                //no qAction specified either, choose random
                action = operation((int) (Math.random() * env.numActions()));
            }

            /** introduce belief or goal for a QL action */

            autonomic(action);  //provides faster action but may cause illogical feedback loops
            //act(action, Symbols.JUDGMENT); //maybe more "correct" probably because it just notices the "autonomic" QL reaction that was executed
        }

        if (action != null) {
            int i = Integer.parseInt(((Operation) action).getArgument(0).toString());
            env.takeAction(i);
        }

        env.worldStep();


        double r = env.reward();

        //double dr = r - lastReward;


        double[] o = env.observe();

        //System.out.println(Arrays.toString(o) + " " + r);

        System.out.println("  reward=" + Texts.n4(r));

        learn(o, nar.time(), r);

        actByQStrongest = brain.getNextAction();

        lastReward = r;
    }

    private void learn(double[] o, long time, double reward) {
        for (Perception p : perceptions) {
            p.perceive(o, time);
        }

        //System.out.println("INCOMING");
        //System.out.println(incoming);

        for (Task t : incoming) {
            DirectProcess.run(nar, t);
        }

        believeReward((float) reward);
        goalReward();

        learn(incoming, reward);

        qCommit();

        incoming.clear();

    }

}
