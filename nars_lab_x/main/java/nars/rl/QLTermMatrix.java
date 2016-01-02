package nars.rl;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import nars.Global;
import nars.NAR;
import nars.Symbols;
import nars.concept.Concept;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.DefaultTruth;
import nars.util.data.ConceptMatrix;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;


/**
 * QL Agent coprocessor "Brain" operating in terms of (unprocessed) discrete Q-states
 * S = state
 * A = action
 */
abstract public class QLTermMatrix<S extends Term, A extends Term> extends ConceptMatrix<S,A, Implication, QEntry> {

    public final AbstractHaiQBrain<S,A> brain;


    /** pending tasks to execute to prevent CME */
    transient private final List<Task> stateActionImplications = Global.newArrayList();

    final int implicationOrder = TemporalRules.ORDER_FORWARD; //TemporalRules.ORDER_FORWARD;

    /**
     * what type of state implication (q-entry) affected: belief (.) or goal (!)
     */
    char implicationPunctuation = Symbols.JUDGMENT;
    float updateThresh = DefaultTruth.DEFAULT_TRUTH_EPSILON * 0.25f; //seems to be better to aggregate them to a significant amount before generating a new belief otherwise it spams the belief tables


    float sensedStatePriorityChanged = 1.0f; //scales priority by this amount
    float sensedStatePrioritySame = 0.85f; //scales priority by this amount
    float inputPriorityMult = 1.0f;

    /**
     * min threshold of q-update necessary to cause an effect
     */


    //boolean updateEachFrame = false; //TODO specify as a frequency either in # per frame or cycle (ex: hz)

    //OPERATING PARAMETERS ------------------------
    /** confidence of update beliefs (a learning rate); set to zero to disable */
    float qUpdateConfidence = 0.25f;

    /** confidence of reward update beliefs; set to zero to disable reward beliefs */
    float rewardBeliefConfidence = 0.8f;

    /** confidence of reward command goal; set to zero to disable reward beliefs */
    float rewardGoalConfidence = 0.8f;

    //TODO belief update priority, durability etc
    //TODO reward goal priority, durability etc


    protected float actedGoalConfidence = 0; //set to 0 to disable qAutonomous
    protected float actedBeliefConfidence = 0; //set to 0 to disable qAutonomous
    float actionPriority = Global.DEFAULT_GOAL_PRIORITY;
    float actionDurability = Global.DEFAULT_GOAL_DURABILITY;

    float entryConceptualizationPriority = 0.5f;
    float entryConceptualizationDurability = 0.5f;
    float entryConceptualizationQuality = 0.5f;


    WeakHashMap<S,Task> lastState = new WeakHashMap();



    public QLTermMatrix(NAR nar) {
        super(nar);


        brain = new AbstractHaiQBrain<S, A>() {


            @Override
            public double eligibility(S state, A action) {
                QEntry v = getEntry(state, action);
                if (v == null) return 0;
                return v.getE();
            }

            @Override
            public void qUpdate(S state, A action, double dqDivE, double eMult, double eAdd) {


                if (((dqDivE==0) && (eMult == 1) && (eAdd == 0)) || (!Double.isFinite(dqDivE) || !Double.isFinite(eMult) || !Double.isFinite(eAdd))) {
                    return;
                }

                if (action == null) return;

                QEntry v = getEntry(state, action,
                        entryConceptualizationPriority,
                        entryConceptualizationDurability,
                        entryConceptualizationQuality);
                if (v != null) {

                    if (dqDivE!=0) {
                        double e = v.getE();
                        v.addDQ(dqDivE * e);
                    }

                    v.updateE(eMult, eAdd);

                    v.commit(implicationPunctuation, qUpdateConfidence, updateThresh, inputPriorityMult);

                }
            }

            @Override
            public double q(S state, A action) {
                return QLTermMatrix.this.qSentence(state, action);
            }

//            @Deprecated @Override
//            protected A qlearn(S state, double reward, A nextAction, double confidence) {
//                //belief about current state
//                if (stateUpdateConfidence > 0) {
//
//                    //TODO avoid using String
//                    input(nar.task((state) + ". :|: %" + confidence + ";" + stateUpdateConfidence + "%"));
//                }
//
//                return super.qlearn(state, reward, nextAction, confidence);
//            }

            @Override
            public Iterable<S> getStates() {
                return rows;
            }

            @Override
            public Iterable<A> getActions() {
                return cols;
            }

            @Override
            public A getRandomAction() {
                final List<A> t = Lists.newArrayList(getActions());
                if (t.isEmpty()) return null;
                int n = (int)(Math.random() * t.size());
                return t.get(n);
            }

            public boolean isState(S s) { return QLTermMatrix.this.isRow(s); }
            public boolean isAction(A a) {
                return QLTermMatrix.this.isCol(a);
            }

        };

        brain.setAlpha(0.4f);
        brain.setEpsilon(0.1f);
        brain.setLambda(0.5f);

    }



    public double qSentence(final S state, final A action) {
        QEntry v = getEntry(state, action);
        if (v == null) return Double.NaN;
        return v.getQSentence(implicationPunctuation);
    }

    public double q(final S state, final A action) {
        return q(state, action, 0);
    }

    public double q(final S state, final A action, double deltaEligibility) {
        QEntry v = getEntry(state, action);
        if (v == null) return 0;
        if (deltaEligibility!=0) {
            v.addE(deltaEligibility);
        }
        return v.getQ();
    }




    public void setqUpdateConfidence(float qUpdateConfidence) {
        this.qUpdateConfidence = qUpdateConfidence;
    }

    public void init() {

    }


    @Override
    public boolean isEntry(Term x) {
        if (!(x instanceof Implication)) return false;
        if (x.getTemporalOrder() != implicationOrder) return false;


        Implication i = (Implication)x;
        return (isRow((S) i.getSubject()) && isCol((A) i.getPredicate()));
    }

    public S getRow(Implication t) {
        if (t.getTemporalOrder()!=implicationOrder) return null;
        S subj = (S)t.getSubject();
        if (isRow(subj)) return subj;
        return null;
    }

    public A getCol(Implication t) {
        if (t.getTemporalOrder()!=implicationOrder) return null;
        A pred = (A)t.getPredicate();
        if (isCol(pred)) return pred;
        return null;
    }


    public Implication qterm(S s, A a) {
        if (a == null)
            throw new RuntimeException("action null");
        return Implication.make(s, a, implicationOrder);
        /*
        Implication i = termCache.get(s, a);
        if (i == null) {
            i = Implication.make(s, a, implicationOrder);
            if (i!=null)
                termCache.put(s, a, i);
        }
        return i;
        */
    }


    abstract public boolean isRow(Term s);
    abstract public boolean isCol(Term a);

    abstract public Term getRewardTerm();

    /**
     * this should return operations which call an operator that calls this instance's learn(state, reward) function at the end of its execution
     */
    //abstract public Operation getActionOperation(int s);



//    protected synchronized void qCommit() {
//        if (qUpdateConfidence == 0) return;
//
//
//        //input all dQ values
//        for (Table.Cell<S, A, QEntry> c : table.cellSet()) {
//            S state = c.getRowKey();
//            A action = c.getColumnKey();
//            Task t = qCommit(state, action, c.getValue());
//            if (t!=null)
//                stateActionImplications.add(t);
//        }
//
//        for (int i = 0; i< stateActionImplications.size(); i++) {
//            Task t = stateActionImplications.get(i);
//            input(t);
//        }
//        stateActionImplications.clear();
//    }



    public boolean stateChanged(Task newState) {
        S state = (S)newState.getTerm();
        Task last = lastState.get(state);
        boolean changed = (last == null ||
                !newState.getTruth().equals(last.getTruth())
                //TODO compare time?
                //TODO compare priority? (before it was scaled?)
        );
        lastState.put(state, newState);
        last = newState;
        return changed;
    }

    /** fire all actions (ex: to teach them at the beginning) */
    public void spontaneous(float goalConf) {
        spontaneous(cols, goalConf);
    }

    public void spontaneous(float goalConf, Iterable<String> actions) {
        spontaneous(Iterables.transform(actions, new Function<String, A>() {
            @Override
            public A apply(String t) {
                return (A) nar.term(t);
            }
        }), goalConf);
    }

    /** fire all actions (ex: to teach them at the beginning) */
    public void spontaneous(Iterable<A> actions, float goalConf) {
        for (A a : actions) {
            spontaneous(goalConf, a);
        }
    }

    /** fire all actions (ex: to teach them at the beginning) */
    public void spontaneous(Set<Map.Entry<A, Concept>> actions, float goalConf) {
        for (Map.Entry<A, Concept> a : actions) {
            spontaneous(goalConf, a.getKey());
        }
    }

    /** fire all actions (ex: to teach them at the beginning) */
    public void spontaneous(float goalConf, Term... actions) {
        for (Term a : actions) {
            acted(a, Symbols.GOAL, goalConf);
        }
    }


    public void acted(Term action) {
        acted(action, Symbols.GOAL, actedGoalConfidence);
        acted(action, Symbols.JUDGMENT, actedBeliefConfidence);
    }

    public void acted(Term action, char punctuation, float conf) {
        if (conf > 0)
            acted(action, punctuation, 1.0f, conf, actionPriority, actionDurability);
    }

    public void acted(Term action, char punctuation, float freq, float conf, float priority, float durability) {
        Task t = TaskSeed.make(nar.memory, (Compound) action).punctuation(punctuation).truth(freq, conf).budget(priority, durability).present();
        input(t);
    }

//    public void react() {
//        act(getNextAction(), Symbols.GOAL);
//    }



//    public Term learn( S state, final double reward, A nextAction, double confidence) {
//        believeReward((float) reward);
//        goalReward();
//
//        Term l = brain.learn(state, reward, nextAction, confidence);
//        qCommit();
//        return l;
//    }



    protected void goalReward() {
        //seek reward goal
        if (rewardGoalConfidence > 0) {
            input(TaskSeed.make(nar.memory, (Compound) getRewardTerm()).present().goal().truth(1.0f, rewardGoalConfidence));
        }
    }

    /** converts reward scalar to a NAR truth frequency.
     *  mapping:
          reward < 0: mapped to 0..0.5 linearly in range [minReward, 0)
          reward 0 = 0.5 frequency
     *    reward > 0: mapped to 0.5..1 linearly in range (0, maxReward]
     * @param reward
     * @param minReward
     * @param maxReward
     */
    protected void believeReward(float reward, float minReward, float maxReward) {
        //belief about current reward amount
        if (rewardBeliefConfidence > 0) {


            float rFreq;
            if (reward == 0) {
                rFreq = 0.5f;
            }
            else if (reward > 0) {
                rFreq = (reward) / (maxReward);
            }
            else {
                rFreq = -((reward) / (minReward));
            }

            //expects -1..+1 as reward range input
            rFreq = rFreq / 2.0f + 0.5f;

            //clip reward to bounds
            if (rFreq < 0) rFreq = 0;
            if (rFreq > 1f) rFreq = 1f;

            input(TaskSeed.make(nar.memory, (Compound) getRewardTerm()).judgment().present().truth(rFreq, rewardGoalConfidence));
        }
    }

    /** confidence of taken action goals (after action taken), 0 to disable */
    public void setActedGoalConfidence(float qAutonomicGoalConfidence) {
        this.actedGoalConfidence = qAutonomicGoalConfidence;
    }

    /** confidence of taken action beliefs (after action taken), 0 to disable */
    public void setActedBeliefConfidence(float actedBeliefConfidence) {
        this.actedBeliefConfidence = actedBeliefConfidence;
    }

    protected void input(Task t) {
        t.getBudget().mulPriority(inputPriorityMult);

        //System.out.println("ql: " + t);
        TaskProcess.run(nar, t);
        //nar.input(t);
    }

    /** master input gain */
    public void setInputPriorityMult(float inputPriorityMult) {
        this.inputPriorityMult = inputPriorityMult;
    }
}
