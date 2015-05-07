package nars.rl;

import com.google.common.collect.Iterables;
import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.Memory;
import nars.NAR;
import nars.event.FrameReaction;
import nars.io.Texts;
import nars.nal.Task;
import nars.nal.concept.Concept;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.term.Term;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;

/**
 * Additional interfaces for interacting with RL environment,
 * deciding next action, and managing goal and reward states
 */
public class QLAgent<S extends Term> extends QLTermMatrix<S, Operation> {


    private final RLEnvironment env;
    private final EnvironmentReaction io;

    private final Perception[] perceptions;
    private final Term rewardTerm;
    private final String operationTerm;
    private final Operator operator;
    private final NAR.OperatorRegistration opReg;


    final java.util.List<Task> incoming = new ArrayList();

    /** for fast lookup of operation terms, since they will be used frequently */
    final Operation[] operationCache;

    private float initialPossibleDesireConfidence = 0.85f;

    final float actedConfidence = 0.9f; //similar to Operator.exec

    final float actionDesireDecay = 0.1f;
    private final ArrayRealVector actByExpectation;
    private final ArrayRealVector actByPriority;
    private RealVector normalizedActionDesire;


    /** if NARS does not specify one by the next frame,
     *  whether to invoke an action in the environment
     *  decided by q-values directly
     */
    private boolean autopilot = true;

    private Operation lastAction;
    double lastReward = 0;


    /**
     * corresponds to the numeric operation as specified by the environment
     */
    protected Operation getAction(final int i) {
        Operation o = operationCache[i];
        if (o == null) {
            //TODO avoid String here
            o = operationCache[i] =
                    (Operation)nar.term("(^" + operationTerm + "," + i + ")");
        }
        return o;
    }



    public QLAgent(NAR nar, String operationTerm, String rewardTerm, RLEnvironment env, Perception... perceptions) {
        this(nar, operationTerm, nar.term(rewardTerm), env, perceptions);
    }

    /**
     * @param nar
     * @param env
     * @param p

     */
    public QLAgent(NAR nar, String operationTerm, Term rewardTerm, RLEnvironment env, Perception... perceptions) {
        super(nar);

        this.operationTerm = operationTerm;
        this.rewardTerm = rewardTerm;

        //HACK TODO this is necessary to disable the superclass's state belief update in methods we end up calling, this class has its own belief update method that should only be called
        stateUpdateConfidence = 0;

        this.perceptions = perceptions;
        for (Perception p : perceptions) {
            p.init(env, this);
        }

        operationCache = new Operation[env.numActions()];


        this.env = env;

        this.io = new EnvironmentReaction();

        this.actByPriority = new ArrayRealVector(env.numActions());
        this.actByExpectation = new ArrayRealVector(env.numActions());
        this.normalizedActionDesire = new ArrayRealVector(env.numActions());


        opReg = nar.on(operator = new Operator("^" + operationTerm) {


            @Override
            protected java.util.List<Task> execute(Operation operation, Term[] args) {

                if (args.length != 2) { // || args.length==3) { //left, self
                    //System.err.println(this + " ?? " + Arrays.toString(args));
                    return null;
                }

                Term ta = ((Operation) operation.getTerm()).getArgument(0);
                String tas = ta.toString();
                for (int cc = 0; cc < tas.length(); cc++)
                    if (!Character.isDigit(tas.charAt(cc))) //only accept numbers
                        return null;

                int a = Integer.parseInt(ta.toString());
                io.desire(a, operation);

                return null;
            }
        });

    }

    public void off() {
        super.off();
        opReg.off();
    }

    @Override
    public Term getRewardTerm() {
        return rewardTerm;
    }

    @Override
    public void init() {
        super.init();

        for (int i = 0; i < env.numActions(); i++) {
            Operation a = getAction(i);
            cols.include(a);
        }

        spontaneous(initialPossibleDesireConfidence);
        setActedBeliefConfidence(actedConfidence);
        setActedGoalConfidence(actedConfidence);
        brain.setAlpha(0.05f);
    }

    @Override
    public boolean isRow(Term s) {
        for (Perception p : perceptions) {
            if (p.isState(s))
                return true;
        }
        return false;
    }

    @Override
    public boolean isCol(Term a) {
        if (a instanceof Operation)
            return cols.contains((Operation)a);
        return false;
    }

    /* action desire value, as aggregated between frames from NARS executions */
    public double getActionDesire(int action) {
        return actByExpectation.getEntry(action);
    }

    public int getNumActions() {
        return env.numActions();
    }


//    /**
//     * adds a perception belief of a given strength (0..1.0) to the input buffer
//     */
//    public void perceive(String term, float freq, float conf) {
//        perceive((S)nar.term(term), freq, conf);
//    }
//
//    /**
//     * adds a perception belief of a given strength (0..1.0) to the input buffer
//     */
//    public Task perceive(S term, float freq, float conf) {
//        Task t = nar.memory.newTask((Compound)term)
//                .judgment()
//                .present()
//                .truth(freq, conf)
//                .get();
//        incoming.add(t);
//        return t;
//    }


    /**
     * interface to an RL world/experiment/etc..
     * implements a per-frame reaction in which an action
     * is decided either by NARS or an Agent, the world updated,
     * and the new input processed by the Agent's perception interface
     */
    class EnvironmentReaction extends FrameReaction {

        public EnvironmentReaction() {
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



    /**
     * decides which action, TODO make this configurable
     */
    public Operation decide() {

        final int actions = getNumActions();

        double e = brain.getEpsilon();
        if (e > 0) {
            if (Memory.randomNumber.nextDouble() < e) {
//                /*A l = brain.getRandomAction();
//                brain.setLastAction(l);
//                return l;*/
//                spontaneous((float) (initialPossibleDesireConfidence));

                int a = (int) (Math.random() * getNumActions());

                float v = initialPossibleDesireConfidence / (2 * actions);

                actByExpectation.addToEntry(a, v);
            }

        }

        double m = actByExpectation.getL1Norm(); //sum of absolute value of elements, to detect a zero vector
        if (m == 0) return null;

        int winner = actByExpectation.getMaxIndex();
        if (winner == -1) {
            return null; //no winner?
        }

        normalizedActionDesire = actByExpectation.unitVector();
        double alignment = normalizedActionDesire.dotProduct(actByExpectation);

        //System.out.print("NARS exec: '" + winner + "' -> " + winner);
        //System.out.println("  volition_coherency: " + Texts.n4(alignment * 100.0) + "%");

        actByExpectation.mapMultiplyToSelf(actionDesireDecay); //zero
        actByPriority.mapMultiplyToSelf(actionDesireDecay); //zero


        return getAction(winner);
    }

    protected void onFrame() {
        long now = nar.time();

        env.frame();

        Operation nextAction = decide();

        if (nextAction != null) {
            int i = Integer.parseInt((nextAction).getArgument(0).toString());

            boolean actionSucceeded = env.takeAction(i);

            if (actionSucceeded) {
                acted(nextAction);
            }
            else {

            }
        }



        double[] o = env.observe();

        for (final Perception p : perceptions) {
            Iterables.addAll(incoming, p.perceive(nar, o, nar.time()));
        }


        for (Task t : incoming) {
            sense(t);
        }

        double r = env.getReward();
        believeReward((float) r);
        goalReward();


        learn(nextAction, incoming, r);


        qCommit();


        incoming.clear();


    }

    public void sense(Task environmentState) {
        if (stateChanged(environmentState)) {
            environmentState.mulPriority(sensedStatePriorityChanged);
        }
        else {
            environmentState.mulPriority(sensedStatePrioritySame);
        }
        input(environmentState);
    }

    @Override
    public QEntry newEntry(Concept concept) {
        return new QEntry(concept);
    }


    /**
     * learn an entire input vector. each entry in state should be between 0 and 1 reprsenting the degree to which that state is active
     * @param state - set of input Tasks (beliefs) which will be input to the belief, and also interpreted by the qlearning system according to their freq/conf
     * @param reward
     * @param confidence
     * @return
     */
    public synchronized void learn(final Operation nextAction, final Iterable<Task> state, final double reward) {


        // System.out.println(confidence + " " + Arrays.toString(state));

        for (Task i : state) {

            float freq = i.sentence.truth.getFrequency();
            float confidence = i.sentence.truth.getConfidence();


            brain.qlearn(lastAction, (S)i.sentence.getTerm(), reward, nextAction, freq * confidence);


            //act.addToValue(action, confidence);

            //act.put(action, Math.max(act.get(action), confidence));


        }


        lastAction = nextAction;
        lastReward = reward;

    }


}
