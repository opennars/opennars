package nars.rl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.NAR;
import nars.Op;
import nars.concept.Concept;
import nars.nal.nal4.Product;
import nars.nal.nal8.OpReaction;
import nars.nal.nal8.Operator;
import nars.nal.nal8.decide.DecideAboveDecisionThreshold;
import nars.nal.nal8.decide.DecideAllGoals;
import nars.nal.nal8.decide.Decider;
import nars.nal.nal8.operator.SynchOperator;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.Truth;
import nars.util.event.FrameReaction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Additional interfaces for interacting with RL environment,
 * deciding next action, and managing goal and reward states
 */
public class QLAgent<S extends Term> extends NARAgent {



    private final List<Perception> perceptions;
    private final Term rewardTerm;
    public final QLAgentTermMatrix ql;
    private final FrameDecisionOperator decision;


    java.util.List<Task> before = new ArrayList();
    java.util.List<Task> now = new ArrayList();


    private float initialPossibleDesireConfidence = 0.25f;

    final float actedConfidence = 0.9f; //similar to Operator.exec



    /** if NARS does not specify one by the next frame,
     *  whether to invoke an action in the environment
     *  decided by q-values directly
     */
    private boolean autopilot = true;

    private Operation lastAction;
    double lastReward = 0;
    private Concept operatorConcept;

    private final ArrayRealVector actByQ, lastActByQ;
    private final ArrayRealVector combinedDesire;

    /** proportion how much QL affects decision vs. general control mechanism decisions; 0.5 = equal */
    float qDecisionFactor = 0.5f;



    /**
     * corresponds to the numeric operation as specified by the environment
     */
    protected Operation getAction(final int i) {
        Operation o = operationCache[i];
        if (o == null) {
            //TODO avoid String here
            o = operationCache[i] =
                    Operation.op( Product.make(Atom.the(i)), operator );
            operationToAction.put(o, i);
        }
        return o;
    }

    protected int getAction(final Operation o) {
        return operationToAction.getIfAbsent(o, -1);
    }

    /** sets the influence of q-learning on the overall behavior of the system.
     *
     * @param qDecisionFactor
     * @param qConceptChangeConfidence
     *
     * set to 0,0 to disable ql influence entirely
     */
    public void setQLFactor(float qDecisionFactor, float qConceptChangeConfidence) {
        this.qDecisionFactor = qDecisionFactor;
        ql.setqUpdateConfidence(qConceptChangeConfidence);
    }

    /* 1.0 = unaffected, 0 = no input (eyes shut) */
    public void setInputGain(float gain) {
        ql.setInputPriorityMult(gain);
    }

    public QLAgent(NAR nar, String operationTerm, String rewardTerm, @Deprecated RLEnvironment env, Perception... perceptions) {
        this(nar, operationTerm, (Compound)nar.term(rewardTerm), env, perceptions);
    }

    /**
     * @param nar
     * @param env
     * @param p
     */
    public QLAgent(NAR nar, String operatorTerm, Compound rewardTerm, @Deprecated RLEnvironment env, Perception... perceptions) {
        super(nar, env, Atom.the(operatorTerm));

        ql = new QLAgentTermMatrix(nar);

        this.rewardTerm = rewardTerm;

        this.perceptions = new ArrayList();
        for (Perception p : perceptions) {
            add(p);
        }

        final int numActions = env.numActions();

        this.actByQ = new ArrayRealVector(numActions);
        this.lastActByQ = new ArrayRealVector(numActions);
        this.combinedDesire = new ArrayRealVector(numActions);

        this.decision = new FrameDecisionOperator(operator, env.numActions());

        new FrameReaction(nar) {

            @Override
            public void onFrame() {
                QLAgent.this.onFrame();
            }
        };

        init();
    }

    @Override
    public OpReaction getOperator(Term operationTerm) {
        return decision;
    }


    public synchronized void add(Perception p) {
        perceptions.add(p);
        p.init(env, QLAgent.this);
    }

    public synchronized void remove(Perception p) {
        perceptions.remove(p);
    }

    public Concept getOperatorConcept() {
        if (operatorConcept ==null)
            operatorConcept = ql.nar.concept(operator);
        return operatorConcept;
    }

    /** fast immediate checks to discount terms which are definitely not representative of a state */
    public boolean isRowPrefilter(Term s) {
        //TODO use a standard subject for all state data that can be tested quickly
        return (s.op()== Op.INHERITANCE);
    }

    /* the effective action desire value, as aggregated between frames from NARS executions */
    public double getDesire(int action) {
        return combinedDesire.getEntry(action);
    }
    public double getNARDesire(int action) {
        return decision.actByExpectation.getEntry(action);
    }
    public double getQDesire(int action) {
        return actByQ.getEntry(action);
    }

    public int getNumActions() {
        return env.numActions();
    }

    public Concept getActionConcept(int i) {
        return nar.concept( getAction(i) );
    }

    //v = new ConceptMatrixEntry<>(c, this);


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


    protected int getMaxDesiredAction() {
        int best = -1;
        double highest = Double.MIN_VALUE;

        //TODO check fairness of the equal value conditions
        for (int i = 0; i < getNumActions(); i++) {
            double  v = combinedDesire.getEntry(i);
            if ((v > highest) ||
                    //if equal, decide randomly if it should replace it, to be fair
                    ((v >= highest) && (nar.memory.random.nextBoolean()))
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

        final double epsilon = ql.brain.getEpsilon();
        //do not apply epsilon random decisions if decisionThreshold=1f
        float decisionThreshold = ql.nar.param.executionThreshold.floatValue();
        if (decisionThreshold < 1.0f && epsilon > 0 && nar.memory.random.nextFloat() < epsilon) {
            int a = nar.memory.random.nextInt(actions);

            //for display purposes:
            combinedDesire.set(0);
            combinedDesire.setEntry(a, 0.5f);

            return getAction(a);
        }


        normalizeActionVector(decision.actByExpectation);
        normalizeActionVector(actByQ);


        //System.out.println(actByQ + " " + actByExpectation);

        for (int i = 0; i < combinedDesire.getDimension(); i++) {

            //50%/50% q and nars
            double d = (
                    (1.0f - qDecisionFactor) * decision.actByExpectation.getEntry(i) +
                    qDecisionFactor * actByQ.getEntry(i) );

            double p = combinedDesire.getEntry(i);

            double n = (p * decision.actionMomentum) + (1.0 - decision.actionMomentum) * d;
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
        decision.clear();

        return getAction(winner);
    }

    protected void onFrame() {
        long t = ql.nar.time();

        env.frame();

        Operation nextAction = decide();


        if (nextAction != null) {
            int i = Integer.parseInt((nextAction).arg(0).toString());

            boolean actionSucceeded = env.takeAction(i);

            if (actionSucceeded) {
                ql.acted(nextAction);
            }
            else {
                nextAction = null;
            }
        }

        double[] o = env.observe();



        for (final Perception p : perceptions) {
            Iterables.addAll(QLAgent.this.now, p.perceive(ql.nar, o, t));
        }


        for (Task task : QLAgent.this.now) {
            sense(task);
        }

        double r = env.getReward();
        ql.believeReward((float) r, env.getMinReward(), env.getMaxReward());
        ql.goalReward();


        if (nextAction!=null)
            learn(nextAction, before, QLAgent.this.now, r);

        //System.out.println(nextAction + " " + lastAction);

        //swap buffers
        List<Task> tmp = before;
        before = now;


        now = tmp;
        now.clear();
    }

    public void sense(Task environmentState) {
        if (ql.stateChanged(environmentState)) {
            environmentState.getBudget().mulPriority(ql.sensedStatePriorityChanged);
        }
        else {
            //float sensedStatePrioritySame = 1.0f - environmentState.getTruth().getConfidence();
            environmentState.getBudget().mulPriority(ql.sensedStatePrioritySame);
        }
        ql.input(environmentState);
    }


    /**
     * learn an entire input vector. each entry in state should be between 0 and 1 reprsenting the degree to which that state is active
     * @param state - set of input Tasks (beliefs) which will be input to the belief, and also interpreted by the qlearning system according to their freq/conf
     * @param reward
     * @param confidence
     * @return
     */
    public void learn(final Operation nextAction, final List<Task> lastStateTasks, final List<Task> currentStateTasks, final double reward) {


        // System.out.println(confidence + " " + Arrays.toString(state));

        //double alpha = brain.getAlpha();
        double gamma = ql.brain.getGamma();
        double lambda = ql.brain.getLambda();

        double sumDeltaQ = 0;
        final double GammaLambda = gamma * lambda;


        int numLastTasks = lastStateTasks.size();
        int numTaskTransitions = currentStateTasks.size() * numLastTasks;

        for (Task lastTask: lastStateTasks) {

            S lastState = (S) lastTask.getTerm();

            double alpha = lastTask.getTruth().getExpectation();
            //double lastTaskExpectation = lastTask.getTruth().getExpectation();

            final double qLast;
            if (lastAction!=null)
                qLast = ql.q(lastState, lastAction, alpha);
            else
                qLast = 0;

            for (Task i : currentStateTasks) {

                //float freq = i.sentence.truth.getFrequency();
                //float confidence = i.sentence.truth.getConfidence();
                //double currentTaskExpectation = freq * confidence;

                S state = (S) i.getTerm();


                //brain.qlearn(lastAction, , reward, nextAction, freq * confidence);

//
//                double qLast;
//                if (lastAction != null) {
//                    //qLast = qSentence(state, lastAction);
//                    qLast = q(state, lastAction);
//                } else {
//                    //qLast = Math.random();
//                    qLast = 0;
//                }

//                if (!Double.isFinite(qLast)) {
//                    // the entry does not exist.
//                    // input the task as a belief to create it, and maybe it will be available in a subsequent cycle
//                    //System.out.println("qState missing: " + i);
//                    //qLast = Math.random();
//                    qLast = 0;
//                }

                //double sq = QEntry.getQSentence(i.sentence) * confidence;


                //TODO compare: q(state, nextAction) with q(i.sentence)
                //double DeltaQ = reward + Gamma * Q[StateX][StateY][Action] - Q[lastStateX][lastStateY][lastAction];
                //double deltaQ = reward + gamma * q(state, nextAction) - q(lastState, lastAction);
                double deltaQ = reward + (gamma * ql.q(state, nextAction))

                        //update eligiblity trace according to the expectation
                        //et[lastStateX][lastStateY][lastAction] += 1;
                        - qLast;

                sumDeltaQ += alpha * deltaQ;

                //brain.qUpdate(state, nextAction, sumDeltaQ, GammaLambda, 0);
            }
        }

        if (numTaskTransitions == 0) numTaskTransitions = 1;

        List<S> s = Lists.newArrayList(getStates()); //copy to avoid CME because the update procedure can change the set of states
        List<Operation> a = Lists.newArrayList(ql.brain.getActions());
        for (S i : s) {
            for (Operation k : a) {
                ql.brain.qUpdate(i, k, sumDeltaQ / numTaskTransitions, GammaLambda / numTaskTransitions, 0);
            }
        }


        int numRows = ql.rows.size();
        for (int i = 0; i < getNumActions(); i++) {
            Operation oa = getAction(i);

            double qSum = 0;

            for (S state : ql.rows) {
                QEntry v = ql.getEntry(state, oa);
                if (v!=null) {
                    double q = v.getQ();

                    if (Double.isFinite(q)) {
                           qSum += q;
                    }
                }
            }

            if (numRows == 0) numRows = 1;
            qSum /= numRows;

            lastActByQ.setEntry(i, actByQ.getEntry(i));
            actByQ.setEntry(i, qSum);
        }



        lastAction = nextAction;
        lastReward = reward;
    }

    protected static void normalizeActionVector(RealVector r) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        if (r.getL1Norm()==0) {
            return;
        }

        r.unitize();

        int dim = r.getDimension();
        for (int i = 0; i < dim;i++) {
            double v = r.getEntry(i);
            if (v < min) min = v;
            if (v > max) max = v;
        }

        if (min == max) {
            r.set(1.0/dim);
            return;
        }

        double sum = 0;

        for (int i = 0; i < dim;i++) {
            double v = r.getEntry(i);
            v =  (v - min) / (max - min);
            sum += v;
            r.setEntry(i, v);
        }

        r.mapDivideToSelf(sum);
    }


    public Iterable<S> getStates() { return ql.rows; }

    public class QLAgentTermMatrix extends QLTermMatrix<S,Operation> {

        public QLAgentTermMatrix(NAR nar) {
            super(nar);
        }

        public void off() {
            super.off();

            //opReg.off();
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

            setActedBeliefConfidence(actedConfidence);
            setActedGoalConfidence(0);
        }

        @Override
        public boolean isRow(Term s) {
            if (!isRowPrefilter(s)) return false;
            for (Perception p : perceptions) {
                if (p.isState(s)) {
                    //adds the state
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isCol(Term a) {
            if ((a.complexity() == 5) && (!a.hasVar())) //"act(X)"
                if (a instanceof Operation)
                    return cols.contains((Operation)a);
            return false;
        }


        public QEntry newEntry(Concept c, S row, Operation col) {
            return new QEntry(c, this);
        }
    }

    public class FrameDecisionOperator extends SynchOperator {

        public final ArrayRealVector actByExpectation;
        public final ArrayRealVector actByPriority;
        final double actionMomentum = 0.0; //smooths the action vectors

        boolean filterNegativeGoals = true;

        public FrameDecisionOperator(Operator operationTerm, int numActions) {
            super(operationTerm.the());

            this.actByPriority = new ArrayRealVector(numActions);
            this.actByExpectation = new ArrayRealVector(numActions);
        }

        @Override
        public Decider decider() {
            if (filterNegativeGoals)
                return DecideAboveDecisionThreshold.the;
            else
                return DecideAllGoals.the;
        }

        public void desire(int action, Operation operation) {
            Truth t = operation.getTask().getTruth();


            desire(action, operation.getTask().getPriority(),
                    t.getExpectation());
        }

        protected void desire(int action, float priority, float expectation) {
            actByPriority.addToEntry(action, priority);
            actByExpectation.addToEntry(action, expectation);

            //ALTERNATIVE: sum expectation * taskPriority
            //actByExpectation.addToEntry(action, expectation * priority);
        }


        @Override
        protected void noticeExecuted(Operation operation) {
            //dont notice
        }

        @Override
        public List<Task> apply(Operation operation) {

            if (operation.numArgs() != 2) { // || args.length==3) { //left, self
                //System.err.println(this + " ?? " + Arrays.toString(args));
                return null;
            }

            Term ta = ((Operation) operation.getTerm()).arg(0);
            int a = getAction(operation);
            if (a !=-1)
                desire(a, operation);

            return null;
        }

        public void clear() {
            actByExpectation.set(0);
            actByPriority.set(0);
        }
    }
}
