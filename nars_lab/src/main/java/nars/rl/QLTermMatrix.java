package nars.rl;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.gs.collections.impl.map.mutable.primitive.ObjectDoubleHashMap;
import com.sun.istack.internal.Nullable;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.io.Symbols;
import nars.nal.DirectProcess;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.TruthValue;
import nars.nal.concept.Concept;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.util.index.ConceptMatrix;

import java.util.List;

/**
 * QL Agent coprocessor "Brain" operating in terms of (unprocessed) discrete Q-states
 * S = state
 * A = action
 */
abstract public class QLTermMatrix<S extends Term, A extends Term> extends ConceptMatrix<S,A,Implication, QEntry> {

    public AbstractHaiQBrain<S,A> brain;


    /** pending tasks to execute to prevent CME */
    transient private final List<Task> pending = Global.newArrayList();

    final int implicationOrder = TemporalRules.ORDER_NONE; //TemporalRules.ORDER_FORWARD;

    /**
     * what type of state implication (q-entry) affected: belief (.) or goal (!)
     */
    char statePunctuation = Symbols.GOAL;

    /**
     * min threshold of q-update necessary to cause an effect
     */
    float updateThresh = Global.TRUTH_EPSILON;

    //boolean updateEachFrame = false; //TODO specify as a frequency either in # per frame or cycle (ex: hz)

    //OPERATING PARAMETERS ------------------------
    /** confidence of update beliefs (a learning rate); set to zero to disable */
    float qUpdateConfidence = 0.55f;

    /** confidence of reward update beliefs; set to zero to disable reward beliefs */
    float rewardBeliefConfidence = 0.9f;

    /** confidence of reward command goal; set to zero to disable reward beliefs */
    float rewardGoalConfidence = 0.9f;

    /** confidence of state belief updates */
    @Deprecated protected float stateUpdateConfidence = 0.9f;

    //TODO belief update priority, durability etc
    //TODO reward goal priority, durability etc


    protected float qAutonomicGoalConfidence = 0; //set to 0 to disable qAutonomous
    protected float qAutonomicBeliefConfidence = 0; //set to 0 to disable qAutonomous
    float actionPriority = Global.DEFAULT_GOAL_PRIORITY;
    float actionDurability = Global.DEFAULT_GOAL_DURABILITY;



    public QLTermMatrix(NAR nar) {
        super(nar);

        //disable checking for empty action columns; this is an optimization since actions should not be rmeoved from the table
        setCheckForEmptyColumns(false);

        brain = new AbstractHaiQBrain<S, A>() {


            @Override
            public double eligibility(S state, A action) {
                QEntry v = getEntry(state, action);
                if (v == null) return 0;
                return v.getE();
            }

            @Override
            public void qAdd(S state, A action, double dqDivE, double eMult, double eAdd) {

                if (qUpdateConfidence == 0) return;

                QEntry v = getEntry(state, action);
                if (v == null) {
                    v = newEntry(null);
                    table.put(state, action, v);
                }

                if (Double.isFinite(dqDivE))
                    v.addDQ(dqDivE);
                if (Double.isFinite(eMult))
                    v.updateE(eMult, eAdd);

            }

            @Override
            public double q(S state, A action) {
                return QLTermMatrix.this.q(state,action);
            }

            @Override
            protected A qlearn(S state, double reward, A nextAction, double confidence) {
                //belief about current state
                if (stateUpdateConfidence > 0) {

                    //TODO avoid using String
                    DirectProcess.run(nar, (state) + ". :|: %" + confidence + ";" + stateUpdateConfidence + "%");
                }

                return super.qlearn(state, reward, nextAction, confidence);
            }

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


    public double q(final S state, final A action) {
        QEntry v = getEntry(state, action);
        if (v == null) return 0;

        Concept c = v.getConcept();
        if (c == null) return 0;

        Sentence s = statePunctuation == Symbols.GOAL ? c.getStrongestGoal(true, true) : c.getStrongestBelief();
        if (s == null) return 0f;
        TruthValue t = s.truth;
        if (t == null) return 0f;

        //TODO try expectation

        return ((t.getFrequency() - 0.5f) * 2.0f); // (t.getFrequency() - 0.5f) * 2f * t.getConfidence();
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



    protected synchronized void qCommit() {
        if (qUpdateConfidence == 0) return;


        //input all dQ values
        for (Table.Cell<S, A, QEntry> c : table.cellSet()) {
            S state = c.getRowKey();
            A action = c.getColumnKey();
            Task t = qCommit(state, action, c.getValue());
            if (t!=null)
                pending.add(t);
        }

        for (int i = 0; i< pending.size(); i++) {
            Task t = pending.get(i);
            DirectProcess.run(nar, t);
        }
        pending.clear();
    }

    protected Task qCommit(S state, A action, QEntry c) {

        double dq = c.clearDQ(updateThresh);
        if (dq == 0) return null;

        double q = q(state, action);
        double nq = q + dq;
        if (nq > 1d) nq = 1d;
        if (nq < -1d) nq = -1d;

        Term qt = qterm(state, action);
        //System.out.println(qt + " qUpdate: " + Texts.n4(q) + " + " + dq + " -> " + " (" + Texts.n4(nq) + ")");

        float nextFreq = (float)((nq / 2f) + 0.5f);



        //String updatedBelief = qt + (statePunctuation + " :|: %" + Texts.n2(nextFreq) + ";" + Texts.n2(qUpdateConfidence) + "%");
        Task t = nar.memory.newTask((Compound)qt).punctuation(statePunctuation).present().truth(nextFreq, qUpdateConfidence).get();

        return t;

    }


    /** fire all actions (ex: to teach them at the beginning) */
    public void possibleDesire(float goalConf) {
        possibleDesire(table.columnKeySet(), goalConf);
    }

    public void possibleDesire(float goalConf, Iterable<String> actions) {
        possibleDesire(Iterables.transform(actions, new Function<String, A>() {
            @Nullable
            @Override
            public A apply(String t) {
                return (A) nar.term(t);
            }
        }), goalConf);
    }
    /** fire all actions (ex: to teach them at the beginning) */
    public void possibleDesire(Iterable<A> actions, float goalConf) {
        for (Term a : actions) {
            possibleDesire(goalConf, a);
        }
    }

    /** fire all actions (ex: to teach them at the beginning) */
    public void possibleDesire(float goalConf, Term... actions) {
        for (Term a : actions) {
            autonomic(a, Symbols.GOAL, goalConf);
        }
    }


    public void autonomic(Term action) {
        autonomic(action, Symbols.GOAL, qAutonomicGoalConfidence);
        autonomic(action, Symbols.JUDGMENT, qAutonomicBeliefConfidence);

    }
    public void autonomic(Term action, char punctuation, float conf) {
        if (conf > 0)
            autonomic(action, punctuation, 1.0f, conf, actionPriority, actionDurability);
    }

    public void autonomic(Term action, char punctuation, float freq, float conf, float priority, float durability) {

        //TODO avoid using String to build the task
        Task t = nar.memory.newTask((Compound)action).punctuation(punctuation).truth(freq, conf).budget(priority, durability).get();
        DirectProcess.run(nar, t);
    }

//    public void react() {
//        act(getNextAction(), Symbols.GOAL);
//    }

    /**
     * learn an entire input vector. each entry in state should be between 0 and 1 reprsenting the degree to which that state is active
     * @param state - set of input Tasks (beliefs) which will be input to the belief, and also interpreted by the qlearning system according to their freq/conf
     * @param reward
     * @param confidence
     * @return
     */
    public synchronized Term learn(final Iterable<Task> state, final double reward) {

        ObjectDoubleHashMap<Term> act = new ObjectDoubleHashMap();

        //HACK - allow learn to update lastAction but restore to the value before this method was called, and then set the final value after all learning completed
        A actualLastAction = brain.getLastAction();

        // System.out.println(confidence + " " + Arrays.toString(state));

        for (Task i : state) {
            brain.setLastAction(actualLastAction);

            float freq = i.sentence.truth.getFrequency();
            float confidence = i.sentence.truth.getConfidence();


            Term action = brain.qlearn((S)i.sentence.getTerm(), reward, null, freq * confidence);


            //act.addToValue(action, confidence);

            act.put(action, Math.max(act.get(action), confidence));


        }

        double e = brain.getEpsilon();
        if (e > 0) {
            if (Memory.randomNumber.nextDouble() < e) {
                A l = brain.getRandomAction();
                brain.setLastAction(l);
                return l;
            }
        }

        //choose maximum action
        if (!act.isEmpty())
            return act.keysView().max();
        else
            return null;
    }




    public Term learn(S state, final double reward, A nextAction, double confidence) {
        believeReward((float) reward);
        goalReward();

        Term l = brain.learn(state, reward, nextAction, confidence);
        qCommit();
        return l;
    }



    protected void goalReward() {
        //seek reward goal
        if (rewardGoalConfidence > 0) {
            DirectProcess.run(nar,
                nar.memory.newTask((Compound)getRewardTerm()).present().goal().truth(1.0f, rewardGoalConfidence).get()
            );
        }
    }

    protected void believeReward(float reward) {
        //belief about current reward amount
        if (rewardBeliefConfidence > 0) {

            //expects -1..+1 as reward range input
            float rFreq = reward /2.0f + 0.5f;

            //clip reward to bounds
            if (rFreq < 0) rFreq = 0;
            if (rFreq > 1f) rFreq = 1f;

            DirectProcess.run(nar,
                    nar.memory.newTask((Compound)getRewardTerm()).judgment().present().truth(rFreq, rewardGoalConfidence).get()
            );
        }
    }

    public void setAutonomicGoalConfidence(float qAutonomicGoalConfidence) {
        this.qAutonomicGoalConfidence = qAutonomicGoalConfidence;
    }

    public void setqAutonomicBeliefConfidence(float qAutonomicBeliefConfidence) {
        this.qAutonomicBeliefConfidence = qAutonomicBeliefConfidence;
    }

}
