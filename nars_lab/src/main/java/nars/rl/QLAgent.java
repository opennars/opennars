package nars.rl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.Memory;
import nars.NAR;
import nars.event.FrameReaction;
import nars.nal.NALOperator;
import nars.nal.Task;
import nars.nal.concept.Concept;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.term.Term;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.List;

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
    final ObjectIntHashMap<Operation> operationToAction = new ObjectIntHashMap();

    private float initialPossibleDesireConfidence = 0.25f;

    final float actedConfidence = 0.75f; //similar to Operator.exec


    private final ArrayRealVector actByExpectation, actByQ, lastActByQ;
    private final ArrayRealVector actByPriority;
    private RealVector combinedDesire;
    final double actionMomentum = 0.15; //smooths the action vectors

    /** if NARS does not specify one by the next frame,
     *  whether to invoke an action in the environment
     *  decided by q-values directly
     */
    private boolean autopilot = true;

    private Operation lastAction;
    double lastReward = 0;
    private Concept operatorConcept;


    /**
     * corresponds to the numeric operation as specified by the environment
     */
    protected Operation getAction(final int i) {
        Operation o = operationCache[i];
        if (o == null) {
            //TODO avoid String here
            o = operationCache[i] =
                    (Operation)nar.term(operationTerm + "(" + i + ")");
            operationToAction.put(o, i);
        }
        return o;
    }
    protected int getAction(final Operation o) {
        return operationToAction.getIfAbsent(o, -1);
    }



    public QLAgent(NAR nar, String operationTerm, String rewardTerm, @Deprecated RLEnvironment env, Perception... perceptions) {
        this(nar, operationTerm, nar.term(rewardTerm), env, perceptions);
    }

    /**
     * @param nar
     * @param env
     * @param p

     */
    public QLAgent(NAR nar, String operationTerm, Term rewardTerm, @Deprecated RLEnvironment env, Perception... perceptions) {
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
        this.actByQ = new ArrayRealVector(env.numActions());
        this.lastActByQ = new ArrayRealVector(env.numActions());
        this.actByExpectation = new ArrayRealVector(env.numActions());
        this.combinedDesire = new ArrayRealVector(env.numActions());


        opReg = nar.on(operator = new Operator("^" + operationTerm) {


            @Override
            protected java.util.List<Task> execute(Operation operation, Term[] args) {

                if (args.length != 2) { // || args.length==3) { //left, self
                    //System.err.println(this + " ?? " + Arrays.toString(args));
                    return null;
                }

                Term ta = ((Operation) operation.getTerm()).getArgument(0);
                int a = getAction(operation);
                if (a !=-1)
                    io.desire(a, operation);

                return null;
            }
        });


    }

    public Concept getOperatorConcept() {
        if ((operatorConcept ==null || getOperatorConcept().isDeleted()))
            operatorConcept = nar.concept(operator);
        return operatorConcept;
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

        //spontaneous(initialPossibleDesireConfidence);
        setActedBeliefConfidence(actedConfidence / getNumActions());
        setActedGoalConfidence(actedConfidence / getNumActions());
    }

    /** fast immediate checks to discount terms which are definitely not representative of a state */
    public boolean isRowPrefilter(Term s) {
        //TODO use a standard subject for all state data that can be tested quickly
        return (s.operator()== NALOperator.INHERITANCE);
    }

    @Override
    public boolean isRow(Term s) {
        if (!isRowPrefilter(s)) return false;
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

    /* the effective action desire value, as aggregated between frames from NARS executions */
    public double getDesire(int action) {
        return combinedDesire.getEntry(action);
    }
    public double getNARDesire(int action) {
        return actByExpectation.getEntry(action);
    }
    public double getQDesire(int action) {
        return lastActByQ.getEntry(action);
    }

    public int getNumActions() {
        return env.numActions();
    }

    public Concept getActionConcept(int i) {
        return cols.values.get( getAction(i) );
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

    protected int getMaxDesiredAction() {
        int best = -1;
        double highest = Double.MIN_VALUE;

        //TODO check fairness of the equal value conditions
        for (int i = 0; i < getNumActions(); i++) {
            double  v = combinedDesire.getEntry(i);
            if ((v > highest) ||
                    //if equal, decide randomly if it should replace it, to be fair
                    ((v >= highest) && (Memory.randomNumber.nextBoolean()))
                    ) {
                highest = v;
                best = i;
            }
        }

        return best;
    }


    /**
     * decides which action, TODO make this configurable
     */
    public Operation decide() {

        final int actions = getNumActions();

        final double epsilon = brain.getEpsilon();
        //do not apply epsilon random decisions if decisionThreshold=1f
        float decisionThreshold = nar.param.decisionThreshold.floatValue();
        if (decisionThreshold < 1.0f && epsilon > 0 && Memory.randomNumber.nextFloat() < epsilon) {
            int a = Memory.randomNumber.nextInt(actions);

            //for display purposes:
            combinedDesire.set(0);
            combinedDesire.setEntry(a, 0.5f);

            return getAction(a);
        }


        normalizeActionVector(actByExpectation);
        normalizeActionVector(actByQ);



        for (int i = 0; i < combinedDesire.getDimension(); i++) {

            //50%/50% q and nars
            double d = ( actByExpectation.getEntry(i) + actByQ.getEntry(i) );

            double p = combinedDesire.getEntry(i);

            double n = (p * actionMomentum) + (1.0 - actionMomentum) * d;
            combinedDesire.setEntry(i, d);
        }


        int winner = getMaxDesiredAction();

        //System.out.println(actByExpectation + " " + actByQ + " " + winner);


        if (winner == -1) {
            return null; //no winner?
        }

        //double alignment = normalizedDesire.dotProduct(actByExpectation);

        //System.out.print("NARS exec: '" + winner + "' -> " + winner);
        //System.out.println("  volition_coherency: " + Texts.n4(alignment * 100.0) + "%");

        //reset for next cycle
        actByExpectation.set(0);
        actByPriority.set(0);

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


        incoming.clear();


    }

    public void sense(Task environmentState) {
        if (stateChanged(environmentState)) {
            environmentState.mulPriority(sensedStatePriorityChanged);
        }
        else {
            //float sensedStatePrioritySame = 1.0f - environmentState.getTruth().getConfidence();
            environmentState.mulPriority(sensedStatePrioritySame);
        }
        input(environmentState);
    }


    @Override
    public QEntry newEntry(Concept concept, Term state, Operation action) {
        return new QEntry(concept, this);
    }


    /**
     * learn an entire input vector. each entry in state should be between 0 and 1 reprsenting the degree to which that state is active
     * @param state - set of input Tasks (beliefs) which will be input to the belief, and also interpreted by the qlearning system according to their freq/conf
     * @param reward
     * @param confidence
     * @return
     */
    public void learn(final Operation nextAction, final Iterable<Task> stateTasks, final double reward) {


        // System.out.println(confidence + " " + Arrays.toString(state));

        double alpha = brain.getAlpha();
        double gamma = brain.getGamma();
        double lambda = brain.getLambda();

        double sumDeltaQ = 0;
        final double GammaLambda = gamma * lambda;

        int numTasks = 0;
        for (Task i : stateTasks) {

            float freq = i.sentence.truth.getFrequency();
            float confidence = i.sentence.truth.getConfidence();
            float expect = freq * confidence;

            S state = (S)i.sentence.getTerm();

            //brain.qlearn(lastAction, , reward, nextAction, freq * confidence);


            double qLast;
            if (lastAction!=null) {
                //qLast = qSentence(state, lastAction);
                qLast = q(state, lastAction);
            }
            else {
                //qLast = Math.random();
                qLast = 0;
            }

            if (!Double.isFinite(qLast)) {
                // the entry does not exist.
                // input the task as a belief to create it, and maybe it will be available in a subsequent cycle
                //System.out.println("qState missing: " + i);
                //qLast = Math.random();
                qLast = 0;
            }

            //double sq = QEntry.getQSentence(i.sentence) * confidence;
            double nq = q(state, nextAction);

            //TODO compare: q(state, nextAction) with q(i.sentence)
            //double deltaQ = reward + gamma * q(state, nextAction) - qLast;
            double deltaQ = reward + gamma * nq - qLast;

            if (lastAction!=null)
                brain.qUpdate(state, lastAction, Double.NaN, 1, nq);

            final double alphaDeltaQ = alpha * deltaQ;
            sumDeltaQ += alphaDeltaQ;

            //brain.qUpdate(state, nextAction, sumDeltaQ, GammaLambda, 0);
            numTasks++;
        }

        if (numTasks > 0) {

            //System.out.println("deltaQ = " + sumDeltaQ);

            List<S> s = Lists.newArrayList(getStates()); //copy to avoid CME because the update procedure can change the set of states
            for (S i : s) {
                for (Operation k : brain.getActions()) {
                    brain.qUpdate(i, k, sumDeltaQ, GammaLambda, 0);
                }
            }

        }



        for (int i = 0; i < getNumActions(); i++) {
            Operation oa = getAction(i);

            double qSum = 0;
            for (S state : rows) {
                QEntry v = getEntry(state, oa);
                if (v!=null) {
                    double q = v.getQ();

                    if (Double.isFinite(q)) {
                        //double e = v.getE();
                        //if (e != 0) {
                            double qv = q;
                            qSum += qv;
                        //}
                    }
                }
            }

            lastActByQ.setEntry(i, actByQ.getEntry(i));
            actByQ.setEntry(i, qSum);
        }


        lastAction = nextAction;
        lastReward = reward;

    }

    protected static void normalizeActionVector(RealVector r) {
        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        int dim = r.getDimension();
        for (int i = 0; i < dim;i++) {
            double v = r.getEntry(i);
            sum += v;
            if (v < min) min = v;
            if (v > max) max = v;
        }

        for (int i = 0; i < dim;i++) {
            if ((sum == 0) || (min == max)) {
                r.setEntry(i, 1.0 / dim);
                continue;
            }

            double v = r.getEntry(i);
            v =  (v - min) / (max - min) / sum;
            r.setEntry(i, v);
        }

    }

    public Iterable<S> getStates() { return rows; }
}
