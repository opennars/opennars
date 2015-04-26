package nars.rl;

import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.gs.collections.impl.map.mutable.primitive.ObjectDoubleHashMap;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.io.Symbols;
import nars.io.Texts;
import nars.nal.DirectProcess;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.TruthValue;
import nars.nal.concept.Concept;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.term.Term;
import vnc.ConceptMap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * QL Agent coprocessor "Brain" operating in terms of (unprocessed) discrete Q-states
 * TODO generify the state,action term types
 */
abstract public class NARQL extends AbstractHaiQBrain<Term,Term> {

    public NAR nar;

    /**
     * initializes that mapping which tracks concepts as they appear and disappear, maintaining mapping to the current instance
     */



    /** q-value matrix:  q[state][action] */
    public final HashBasedTable<Term,Term,Concept> q = HashBasedTable.create();
    private ConceptMap conceptMap;
    protected ConceptMap.ConceptMapSet actions;
    protected ConceptMap.ConceptMapSet states;



    /** eligibility trace */
    public final HashBasedTable<Term,Term,Double> e = HashBasedTable.create();

    /** delta-Q temporary buffer */
    protected final HashBasedTable<Term,Term,Double> dq = HashBasedTable.create();

    /** term cache
     * TODO make weak */
    final HashBasedTable<Term,Term,Implication> termCache = HashBasedTable.create();


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
    protected float stateUpdateConfidence = 0.9f;

    //TODO belief update priority, durability etc
    //TODO reward goal priority, durability etc


    protected float qAutonomicGoalConfidence = 0; //set to 0 to disable qAutonomous
    protected float qAutonomicBeliefConfidence = 0; //set to 0 to disable qAutonomous
    float actionPriority = Global.DEFAULT_GOAL_PRIORITY;
    float actionDurability = Global.DEFAULT_GOAL_DURABILITY;


    boolean initialized = true;

    public NARQL(NAR nar) {
        super();

        setAlpha(0.4f);
        setEpsilon(0.1f);
        setLambda(0.5f);

        this.nar = nar;



        actions = new ConceptMap.ConceptMapSet(nar) {

            @Override
            public boolean contains(Concept c) {
                return isAction(c.term);
            }

            @Override
            protected void onConceptForget(final Concept c) {
                super.onConceptForget(c);
                Term action = c.term;
                q.columnMap().remove(action);
            }

        };

        states = new ConceptMap.ConceptMapSet(nar) {

            @Override
            public boolean contains(Concept c) {
                return isState(c.term);
            }

            @Override
            protected void onConceptForget(final Concept c) {
                super.onConceptForget(c);

                Term state = c.term;
                q.rowMap().remove(state);
            }

        };

        conceptMap = new ConceptMap(nar) {
            @Override
            protected void onCycle() {
                super.onCycle();
                if (initialized) {

                    init();

                    initialized = false;
                }
            }

            @Override
            public boolean contains(Concept c) {
                Term x = c.term;
                if (!(x instanceof Implication)) return false;
                if (x.getTemporalOrder() != implicationOrder) return false;


                Implication i = (Implication)x;
                return (isState(i.getSubject()) && isAction(i.getPredicate()));
            }

            @Override
            protected void onConceptForget(final Concept c) {
                Implication i = (Implication)c.term;
                q.remove(state(i), action(i));
            }

            @Override
            protected void onConceptNew(Concept c) {
                System.out.println("concept new " + c );

                Implication i = (Implication)c.term;
                q.put(state(i), action(i), c);
            }

        };

    }

    public void setqUpdateConfidence(float qUpdateConfidence) {
        this.qUpdateConfidence = qUpdateConfidence;
    }

    public void init() {




    }

    public Term state(Implication t) {
        if (t.getTemporalOrder()!=implicationOrder) return null;
        Term subj = t.getSubject();
        if (isState(subj)) return subj;
        return null;
    }

    public Term action(Implication t) {
        if (t.getTemporalOrder()!=implicationOrder) return null;
        Term pred = t.getPredicate();
        if (isAction(pred)) return pred;
        return null;
    }

    @Override
    public void eligibility(Term state, Term action, double eligibility) {
        e.put(state, action, eligibility);
    }

    @Override
    public double eligibility(Term state, Term action) {
        Double E = e.get(state, action);
        if (E == null) return 0;
        return E;
    }

    @Override
    public Term getRandomAction() {
        final List<Term> t = Lists.newArrayList(getActions());
        if (t.isEmpty()) return null;
        int n = (int)(Math.random() * t.size());
        return t.get(n);
    }

    @Override
    public Iterable<Term> getStates() {
        return states;
    }

    @Override
    public Iterable<Term> getActions() {
        return actions;
    }

    public Implication qterm(Term s, Term a) {
        Implication i = termCache.get(s, a);
        if (i == null) {
            i = Implication.make(s, a, implicationOrder);
            if (i!=null)
                termCache.put(s, a, i);
        }
        return i;
    }


    abstract public boolean isState(Term s);
    abstract public boolean isAction(Term a);

    public String getRewardTerm() {
        return "<SELF --> good>";
    }

    /**
     * this should return operations which call an operator that calls this instance's learn(state, reward) function at the end of its execution
     */
    //abstract public Operation getActionOperation(int s);


    @Override
    public void qAdd(final Term state, final Term action, final double dQ) {

        if (qUpdateConfidence == 0) return;

        final Double currentDQ = dq.get(state, action);
        final Double next;

        if (currentDQ == null)
            next = dQ;
        else
            next = dQ + currentDQ;

        dq.put(state, action, next);
    }

    protected void qCommit() {
        if (qUpdateConfidence == 0) return;

        //input all dQ values
        for (Table.Cell<Term, Term, Double> c : dq.cellSet()) {
            Term state = c.getRowKey();
            Term action = c.getColumnKey();
            qCommit(state, action, c.getValue());
        }

        dq.clear();
    }

    protected void qCommit(Term state, Term action, double dq) {

        if (Math.abs(dq) < updateThresh) {
            //setAlpha(Math.min(getAlpha() + 0.01, 1.0));
            //System.out.println(dq + " delta-Q too small, alpha=" + getAlpha());
            return;
        }

        double q = q(state, action);
        double nq = q + dq;
        if (nq > 1d) nq = 1d;
        if (nq < -1d) nq = -1d;

        Term qt = qterm(state, action);
        //System.out.println(qt + " qUpdate: " + Texts.n4(q) + " + " + dq + " -> " + " (" + Texts.n4(nq) + ")");

        double nextFreq = (nq / 2) + 0.5f;

        //TODO avoid using String

        String updatedBelief = qt + (statePunctuation + " :|: %" + Texts.n2(nextFreq) + ";" + Texts.n2(qUpdateConfidence) + "%");

        DirectProcess.run(nar, updatedBelief);

    }


    @Override
    public double q(Term state, Term action) {
        Concept c = q.get(state, action);
        if (c == null) return 0f;
        Sentence s = statePunctuation == Symbols.GOAL ? c.getStrongestGoal(true, true) : c.getStrongestBelief();
        if (s == null) return 0f;
        TruthValue t = s.truth;
        if (t == null) return 0f;

        //TODO try expectation

        return ((t.getFrequency() - 0.5f) * 2.0f); // (t.getFrequency() - 0.5f) * 2f * t.getConfidence();

    }

    /** fire all actions (ex: to teach them at the beginning) */
    public void possibleDesire(float goalConf) {
        possibleDesire(q.columnKeySet(), goalConf);
    }

    public void possibleDesire(float goalConf, Iterable<String> actions) {
        possibleDesire(Iterables.transform(actions, new Function<String, Term>() {
            @Nullable
            @Override
            public Term apply(String t) {
                return nar.term(t);
            }
        }), goalConf);
    }
    /** fire all actions (ex: to teach them at the beginning) */
    public void possibleDesire(Iterable<Term> actions, float goalConf) {
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
        DirectProcess.run(nar,
                "$" + priority + ";" + durability + "$ " + action + punctuation + " :|: %" + freq + ';' + conf + '%'
        );
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
        Term actualLastAction = lastAction;

        // System.out.println(confidence + " " + Arrays.toString(state));

        for (Task i : state) {
            lastAction = actualLastAction;

            float freq = i.sentence.truth.getFrequency();
            float confidence = i.sentence.truth.getConfidence();


            Term action = qlearn(i.sentence.getTerm(), reward, null, freq * confidence);


            //act.addToValue(action, confidence);

            act.put(action, Math.max(act.get(action), confidence));


        }

        double e = getEpsilon();
        if (e > 0) {
            if (Memory.randomNumber.nextDouble() < e)
                return lastAction = getRandomAction();
        }

        //choose maximum action
        return act.keysView().max();
    }




    public Term learn(Term state, final double reward, Term nextAction, double confidence) {
        believeReward((float) reward);
        goalReward();

        Term l = super.learn(state, reward, nextAction, confidence);
        qCommit();
        return l;
    }

    @Override
    protected Term qlearn(Term state, double reward, Term nextAction, double confidence) {
        //belief about current state
        if (stateUpdateConfidence > 0) {
            //TODO avoid using String
            DirectProcess.run(nar, (state) + ". :|: %" + confidence + ";" + stateUpdateConfidence + "%");
        }

        return super.qlearn(state, reward, nextAction, confidence);
    }

    protected void goalReward() {
        //seek reward goal
        if (rewardGoalConfidence > 0) {
            String rt = getRewardTerm();
            String r = rt + "! :|: %1.0;" + rewardGoalConfidence + '%';
            DirectProcess.run(nar, r);
        }
    }

    protected void believeReward(float reward) {
        //belief about current reward amount
        if (rewardBeliefConfidence > 0) {
            String rt = getRewardTerm();

            //expects -1..+1 as reward range input
            float rFreq = reward /2.0f + 0.5f;

            //clip reward to bounds
            if (rFreq < 0) rFreq = 0;
            if (rFreq > 1f) rFreq = 1f;

            String r = rt + ". :|: %" + rFreq + ';' + rewardBeliefConfidence + '%';
            DirectProcess.run(nar, r);
        }
    }

    public void setqAutonomicGoalConfidence(float qAutonomicGoalConfidence) {
        this.qAutonomicGoalConfidence = qAutonomicGoalConfidence;
    }

    public void setqAutonomicBeliefConfidence(float qAutonomicBeliefConfidence) {
        this.qAutonomicBeliefConfidence = qAutonomicBeliefConfidence;
    }


}
