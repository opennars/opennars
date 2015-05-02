package nars.rl;

import nars.Memory;
import nars.nal.term.Term;

import java.util.AbstractList;
import java.util.List;


abstract public class AbstractHaiQBrain<S,A> {

    /** learning rate */
    double alpha = 0.1;

    /** farsight */
    double gamma = 0.5;

    /** value of λ=1.0 effectively makes algorithm run an online Monte Carlo in which the effects of all future interactions are fully considered in updating each Q-value of an episode." */
    double lambda = 0.9; //0.1 0.5 0.9

    /** random rate */
    double epsilon = 0.01;

    protected A lastAction = null;

    public AbstractHaiQBrain() {
    }

    public static List<Integer> range(final int begin, final int end) {
        return new AbstractList<Integer>() {
            @Override
            public Integer get(final int index) {
                return begin + index;
            }

            @Override
            public int size() {
                return end - begin;
            }
        };
    }

    public A getLastAction() {
        return lastAction;
    }

    public void setLastAction(A a) {
        this.lastAction = a;
    }

    public static class ArrayHaiQBrain extends AbstractHaiQBrain<Integer,Integer> {

        int nActions, nStates;

        final Iterable<Integer> actionRange, stateRange;

        private final double[][] Q;
        double et[][]; //eligiblity trace

        public ArrayHaiQBrain(int states, int actions) {
            this.nActions = actions;
            actionRange = range(0, nActions);
            this.nStates = states;
            stateRange = range(0, states);
            Q = new double[nStates][nActions];
            et = new double[nStates][nActions];
        }


        @Override public Integer getRandomAction() {
            return (int) random(nActions);
        }

        @Override
        public void eligibility(Integer state, Integer action, double eligibility) {
            et[state][action] = eligibility;
        }

        @Override
        public double eligibility(Integer state, Integer action) {
            return et[state][action];
        }

        @Override
        public void qAdd(Integer state, Integer action, double dq) {
            Q[state][action] += dq;
        }

        @Override
        public double q(Integer state, Integer action) {
            return Q[state][action];
        }

        @Override
        public Iterable<Integer> getStates() {
            return stateRange;
        }

        @Override
        public Iterable<Integer> getActions() {
            return actionRange;
        }

    }

    
    public static double random(double max) { return Memory.randomNumber.nextDouble() * max;    }

    abstract public void eligibility(S state, A action, double eligibility);
    abstract public double eligibility(S state, A action);


    abstract public void qAdd(S state, A action, double dq);
    abstract public double q(S state, A action);


    /** learn a discrete state */
    public A learn(final S state, final double reward) {
        return learn(state, reward, 1f);
    }

    public A learn(final S state, final double reward, double confidence) {
        return learn(state, reward, null, confidence);
    }

    abstract public Iterable<S> getStates();
    abstract public Iterable<A> getActions();

    protected A qlearn(final S state, final double reward, A nextAction, double confidence) {


        if (nextAction == null) {
            A maxk = null;
            double maxval = Double.NEGATIVE_INFINITY;
            for (A k : getActions()) {
                double v = q(state, k);
                if (v > maxval) {
                    maxk = k;
                    maxval = v;
                }
            }

            if (epsilon > 0 && random(1.0) < epsilon) {
                nextAction = getRandomAction();
            } else {
                nextAction = maxk;
            }
        }

        /*
        Q-learning
        def learn(self, state1, action1, reward, state2):
            maxqnew = max([self.getQ(state2, a) for a in self.actions])
            self.learnQ(state1, action1,
                        reward, reward + self.gamma*maxqnew)


        SARSA
        def learn(self, state1, action1, reward, state2, action2):
            qnext = self.getQ(state2, action2)
            self.learnQ(state1, action1,
                        reward, reward + self.gamma * qnext)
        */

        double qLast;
        if (lastAction!=null)
            qLast = q(state, lastAction);
        else
            qLast = 0;

        double DeltaQ = reward + gamma * q(state, nextAction) -  qLast;

        if (lastAction!=null)
            eligibility(state, lastAction, eligibility(state, lastAction) + confidence);

        final double AlphaDeltaQ = confidence * alpha * DeltaQ;
        final double GammaLambda = gamma * lambda;
        for (S i : getStates()) {
            for (A k : getActions()) {
                final double e = eligibility(i, k);
                qAdd(i, k, AlphaDeltaQ * e);
                eligibility(i, k, GammaLambda * e);
            }
        }

        lastAction = nextAction;
        return nextAction;
    }

    /**
     * returns action #
     */
    public A learn(final S state, final double reward, A nextAction, double confidence) {
        return qlearn(state, reward, nextAction, confidence);
    }

    abstract public A getRandomAction();

    public A getNextAction() {
        return lastAction;
    }

    public void setAlpha(double Alpha) {
        this.alpha = Alpha;
    }

    public void setGamma(double Gamma) {
        this.gamma = Gamma;
    }

    public void setLambda(double Lambda) {
        this.lambda = Lambda;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getLambda() {
        return lambda;
    }

    public double getGamma() {
        return gamma;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public double getEpsilon() {
        return epsilon;
    }
}
