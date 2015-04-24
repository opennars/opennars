package nars.rl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import nars.Global;
import nars.NAR;
import nars.io.Symbols;
import nars.io.Texts;
import nars.nal.DirectProcess;
import nars.nal.Sentence;
import nars.nal.TruthValue;
import nars.nal.concept.Concept;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.term.Term;
import nars.rl.hai.AbstractHaiQBrain;
import vnc.ConceptMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * QL Agent coprocessor "Brain" operating in terms of (unprocessed) discrete Q-states
 * TODO generify the state,action term types
 */
abstract public class BaseQLAgent extends AbstractHaiQBrain<Term,Term> {

    public NAR nar;

    /**
     * initializes that mapping which tracks concepts as they appear and disappear, maintaining mapping to the current instance
     */

    ConceptMap states;
    ConceptMap actions;


    /** q-value matrix:  q[state][action] */
    final HashBasedTable<Term,Term,Concept> q = HashBasedTable.create();

    /** eligibility trace */
    final HashBasedTable<Term,Term,Double> e = HashBasedTable.create();

    /** delta-Q temporary buffer */
    final HashBasedTable<Term,Term,Double> dq = HashBasedTable.create();

    /** term cache
     * TODO make weak */
    final HashBasedTable<Term,Term,Implication> termCache = HashBasedTable.create();


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
    float stateUpdateConfidence = 0.9f;

    //TODO belief update priority, durability etc
    //TODO reward goal priority, durability etc


    protected float qAutonomicGoalConfidence = 0; //set to 0 to disable qAutonomous
    protected float qAutonomicBeliefConfidence = 0; //set to 0 to disable qAutonomous
    float actionPriority = Global.DEFAULT_GOAL_PRIORITY;
    float actionDurability = Global.DEFAULT_GOAL_DURABILITY;



    public BaseQLAgent(NAR nar) {
        super();

        setAlpha(0.4f);
        setEpsilon(0.1f);
        setLambda(0.5f);

        this.nar = nar;


//        frameReaction = new FrameReaction(nar) {
//            @Override
//            public void onFrame() {
//                if (updateEachFrame) react();
//            }
//        };
    }

    public void setqUpdateConfidence(float qUpdateConfidence) {
        this.qUpdateConfidence = qUpdateConfidence;
    }

    public void init() {


        actions = new ConceptMap(nar) {

            @Override
            public boolean contains(Concept c) {
                return isAction(c.term);
            }

            @Override
            protected void onConceptForget(final Concept c) {
                Term action = c.term;
                Collection<Term> knownStates = new ArrayList(q.rowKeySet()); //copy to avoid concurrentmodification
                for (final Term state : knownStates) {
                    q.remove(state, action);
                    e.remove(state, action);
                }
            }

            @Override
            protected void onConceptNew(Concept c) {           }
        };

        states = new ConceptMap(nar) {

            @Override
            public boolean contains(Concept c) {
                return isState(c.term);
            }

            @Override
            protected void onConceptForget(final Concept c) {
                Term state = c.term;
                Collection<Term> knownActions = new ArrayList(q.columnKeySet()); //copy to avoid concurrentmodification
                for (final Term action : knownActions) {
                    q.remove(state, action);
                    e.remove(state, action);
                }
            }

            @Override
            protected void onConceptNew(Concept c) {           }
        };



    }

    public Term state(Implication t) {
        if (t.getTemporalOrder()!=TemporalRules.ORDER_FORWARD) return null;
        Term subj = t.getSubject();
        if (isState(subj)) return subj;
        return null;
    }

    public Term action(Implication t) {
        if (t.getTemporalOrder()!=TemporalRules.ORDER_FORWARD) return null;
        Term pred = t.getPredicate();
        if (isAction(pred)) return pred;
        return null;
    }

//    private int[] qterm(Implication t) {
//        Term s = t.getSubject();
//        Operation p = (Operation) t.getPredicate();
//
//        int state = states.get(s);
//        int action = actions.get(p);
//
//        return new int[]{state, action};
//    }



    public Implication qterm(Term s, Term a) {
        Implication i = termCache.get(s, a);
        if (i == null) {
            i = Implication.make(s, a, TemporalRules.ORDER_FORWARD);
            termCache.put(s, a, i);
        }
        return i;
    }

    //abstract public Term getStateTerm(int s);

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
    public void autonomicDesire(float goalConf) {
        for (Term a : q.columnKeySet()) {
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


    @Override
    public synchronized Term learn(Map<Term,Double> state, double reward, float confidence) {
        believeReward((float) reward);
        goalReward();


        Term l = super.learn(state, reward, confidence);
        qCommit();
        return l;
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

    private void goalReward() {
        //seek reward goal
        if (rewardGoalConfidence > 0) {
            String rt = getRewardTerm();
            String r = rt + "! :|: %1.0;" + rewardGoalConfidence + '%';
            DirectProcess.run(nar, r);
        }
    }

    private void believeReward(float reward) {
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

}
