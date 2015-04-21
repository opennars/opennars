package nars.rl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import nars.Global;
import nars.NAR;
import nars.io.Symbols;
import nars.io.Texts;
import nars.nal.DirectProcess;
import nars.nal.Sentence;
import nars.nal.TruthValue;
import nars.nal.concept.Concept;
import nars.nal.nal5.Implication;
import nars.nal.nal8.Operation;
import nars.nal.term.Term;
import nars.rl.hai.AbstractHaiQBrain;
import vnc.ConceptMap;

import java.util.HashSet;
import java.util.Set;

/**
 * QL Agent coprocessor "Brain" operating in terms of (unprocessed) discrete Q-states
 */
abstract public class BaseQLAgent extends AbstractHaiQBrain {

    public NAR nar;
    public final int nactions;
    public final int nstates;

    /**
     * initializes that mapping which tracks concepts as they appear and disappear, maintaining mapping to the current instance
     */
    ConceptMap.SeededConceptMap seed;

    /**
     * state <-> index map
     */
    public final BiMap<Term, Integer> states;

    /**
     * action <-> index map
     */
    public final BiMap<Operation, Integer> actions;

    final double dq[][]; //pending delta-Q to apply

    /**
     * q-value matrix:  q[state][action]
     */
    public final Concept[][] q;

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




    public BaseQLAgent(NAR nar, int nstates, int nactions) {
        super(nstates, nactions);

        setAlpha(0.4f);
        setEpsilon(0.1f);
        setLambda(0.5f);

        this.nar = nar;
        this.nstates = nstates;
        this.nactions = nactions;

        states = HashBiMap.create(nstates);
        actions = HashBiMap.create(nactions);
        this.q = new Concept[nstates][nactions];
        this.dq = new double[nstates][nactions];

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

        Set<Term> qseeds = new HashSet();
        for (int s = 0; s < nstates; s++) {

            states.put(getStateTerm(s), s);

            for (int a = 0; a < nactions; a++) {
                if (s == 0) {
                    actions.put(getActionOperation(a), a);
                }

                qseeds.add(qterm(s, a));
            }
        }



        seed = new ConceptMap.SeededConceptMap(nar, qseeds) {

            @Override
            protected void onFrame() {

            }

            @Override
            protected void onCycle() {

            }

            @Override
            protected void onConceptForget(Concept c) {
                Implication t = (Implication) c.getTerm();
                int[] x = qterm(t);
                if (x != null) {
                    int s = x[0];
                    int a = x[1];
                    q[s][a] = null;
                    initializeQ(s, a);
                }
            }

            @Override
            protected void onConceptNew(Concept c) {
                Implication t = (Implication) c.getTerm();
                int[] x = qterm(t);
                if (x != null)
                    q[x[0]][x[1]] = c;

                //System.out.println( Arrays.deepToString(q) );
            }
        };



        //nar.memory.on((ConceptBuilder)this);

        //System.out.println("states:\n" + states);
        //System.out.println("actions:\n" + actions);

        for (int s = 0; s < nstates; s++) {
            for (int a = 0; a < nactions; a++) {
                initializeQ(s, a);
            }
        }

    }

    private int[] qterm(Implication t) {
        Term s = t.getSubject();
        Operation p = (Operation) t.getPredicate();

        int state = states.get(s);
        int action = actions.get(p);

        return new int[]{state, action};
    }

    /**
     * called when a new qterm is created at initialization. any related update task can be performed in overriding subclass method
     */
    protected void initializeQ(int s, int a) {

    }

    public Term qterm(int s, int a) {
        return nar.term("<" + getStateTerm(s) + "=/>" + getActionOperation(a) + ">");
    }

    abstract public Term getStateTerm(int s);

    public String getRewardTerm() {
        return "<SELF --> good>";
    }

    /**
     * this should return operations which call an operator that calls this instance's learn(state, reward) function at the end of its execution
     */
    abstract public Operation getActionOperation(int s);




    @Override
    public void qAdd(int state, int action, double dq) {
        if (qUpdateConfidence == 0) return;
        this.dq[state][action] += dq;
    }

    protected void qCommit() {
        if (qUpdateConfidence == 0) return;

        final double[][] dq = this.dq; //local reference

        //input all dQ values
        for (int s = 0; s < nstates; s++)
            for (int a = 0; a < nactions; a++) {
                double d = dq[s][a];
                qCommit(s, a, d);
                dq[s][a] = 0;
            }

    }

    protected void qCommit(int state, int action, double dq) {

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
    public double q(int state, int action) {
        Concept c = q[state][action];
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
        for (int i = 0; i < nactions; i++) {
            autonomic(i, Symbols.GOAL, goalConf);
        }

    }

    public void autonomic(int action) {
        autonomic(action, Symbols.GOAL, qAutonomicGoalConfidence);
        autonomic(action, Symbols.JUDGMENT, qAutonomicBeliefConfidence);

    }
    public void autonomic(int action, char punctuation, float conf) {
        if (conf > 0)
            autonomic(action, punctuation, 1.0f, conf, actionPriority, actionDurability);
    }

    public void autonomic(int action, char punctuation, float freq, float conf, float priority, float durability) {
        Operation a = actions.inverse().get(action);

        DirectProcess.run(nar,
                "$" + priority + ";" + durability + "$ " + a + punctuation + " :|: %" + freq + ';' + conf + '%'
        );
    }

//    public void react() {
//        act(getNextAction(), Symbols.GOAL);
//    }


    @Override
    public synchronized int learn(double[] state, double reward, float confidence) {
        believeReward((float) reward);
        goalReward();


        int l = super.learn(state, reward, confidence);
        qCommit();
        return l;
    }

    public int learn(final int state, final double reward, int nextAction, double confidence) {
        believeReward((float) reward);
        goalReward();

        int l = super.learn(state, reward, nextAction, confidence);
        qCommit();
        return l;
    }

    @Override
    protected int qlearn(int state, double reward, int nextAction, double confidence) {
        //belief about current state
        if (stateUpdateConfidence > 0) {
            DirectProcess.run(nar, getStateTerm(state) + ". :|: %" + confidence + ";" + stateUpdateConfidence + "%");
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
