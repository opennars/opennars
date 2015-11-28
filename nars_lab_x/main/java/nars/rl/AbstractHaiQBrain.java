package nars.rl;

import java.util.AbstractList;
import java.util.List;


//TODO update a collection of states all at once, to reduce the lookup/hashing of table access
//  in: A qlearn(final S state, final double reward, A nextAction, double confidence) {

abstract public class AbstractHaiQBrain<S,A> {

    /** learning rate */
    double alpha = 0.1;

    /** farsight */
    double gamma = 0.5f;

    /** value of λ=1.0 effectively makes algorithm run an online Monte Carlo in which the effects of all future interactions are fully considered in updating each Q-value of an episode." */
    double lambda = 0.1; //0.1 0.5 0.9

    /** random rate */
    double epsilon = 0;


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
        public double eligibility(Integer state, Integer action) {
            return et[state][action];
        }

        @Override
        public double q(Integer state, Integer action) {
            return Q[state][action];
        }

        @Override
        public void qUpdate(Integer state, Integer action, double dqDivE, double eMult, double eAdd) {
            final double e = et[state][action];
            if (Double.isFinite(dqDivE))
                Q[state][action] += dqDivE * e;
            if (Double.isFinite(eMult))
                et[state][action] = e * eMult + eAdd;
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

    
    private static double random(double max) { return Math.random() * max;    }

    abstract public double eligibility(S state, A action);


    abstract public void qUpdate(S state, A action, double dqDivE, double eMult, double eAdd);
    abstract public double q(S state, A action);


//    /** learn a discrete state */
//    public A learn(final S state, final double reward) {
//        return learn(state, reward, 1f);
//    }
//
//    public A learn(final S state, final double reward, double confidence) {
//        return learn(state, reward, null, confidence);
//    }

    abstract public Iterable<S> getStates();
    abstract public Iterable<A> getActions();

    @Deprecated protected void qlearn(A lastAction, final S state, final double reward, A nextAction, double confidence) {

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

        double deltaQ = reward + gamma * q(state, nextAction) - qLast;

        if (lastAction!=null)
            qUpdate(state, lastAction, Double.NaN, 1, confidence);

        final double AlphaDeltaQ = /* confidence * */ alpha * deltaQ;
        final double GammaLambda = gamma * lambda;
        for (S i : getStates()) {
            for (A k : getActions()) {
                qUpdate(i, k, AlphaDeltaQ, GammaLambda, 0);
            }
        }

        /*
        //https://github.com/davidrobles/reinforcement-learning/blob/master/src/net/davidrobles/rl/algorithms/QLearning.java
        double updateValue = reward + (gamma * nextStateNextActionValue) - table.getValue(state, action);
        double newValue = table.getValue(state, action) + (alpha * updateValue);


        //https://github.com/davidrobles/reinforcement-learning/blob/master/src/net/davidrobles/rl/algorithms/TabularSARSA.java
        double updateValue = reward + (gamma * nextStateNextActionValue) - table.getValue(state, action);
        double newValue = table.getValue(state, action) + (alpha * updateValue);
        table.setValue(new QPair<S, A>(state, action), newValue);
         */

    }

//    /**
//     * returns action #
//     */
//    public A learn(final S state, final double reward, A nextAction, double confidence) {
//        return qlearn(state, reward, nextAction, confidence);
//    }

    abstract public A getRandomAction();


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
