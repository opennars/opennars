package nars.rl.hai;

import nars.Memory;


abstract public class AbstractHaiQBrain {

    //double Q[][]; //state, action
    double et[][]; //eligiblity trace
    int nActions, nStates;

    /** learning rate */
    double alpha = 0.1;

    /** farsight */
    double gamma = 0.5;

    /** value of λ=1.0 effectively makes algorithm run an online Monte Carlo in which the effects of all future interactions are fully considered in updating each Q-value of an episode." */
    double lambda = 0.9; //0.1 0.5 0.9

    /** random rate */
    double epsilon = 0.01;

    int lastState = 0, lastAction = 0;

    public AbstractHaiQBrain(int nstates, int nactions) {
        nActions = nactions;
        nStates = nstates;
        et = new double[nStates][nActions];
    }


    public static class ArrayHaiQBrain extends AbstractHaiQBrain {

        private final double[][] Q;

        public ArrayHaiQBrain(int states, int actions) {
            super(states, actions);
            Q = new double[nStates][nActions];
        }

        @Override
        public void qAdd(int state, int action, double dq) {
            Q[state][action] += dq;
        }

        @Override
        public double q(int state, int action) {
            return Q[state][action];
        }

    }

    
    public static double random(double max) { return Memory.randomNumber.nextDouble() * max;    }


    abstract public void qAdd(int state, int action, double dq);
    abstract public double q(int state, int action);

    public int learn(final int state, final double reward, float confidence) {
        return learn(state, reward, -1, confidence);
    }

    /**
     * returns action #
     */
    public int learn(final int state, final double reward, int nextAction, float confidence) {

        final double[][] et = this.et; //local reference
        final int actions = nActions;
        final int states = nStates;



        if (nextAction == -1) {
            int maxk = -1;
            double maxval = Double.NEGATIVE_INFINITY;
            for (int k = 0; k < actions; k++) {
                double v = q(state, k);
                if (v > maxval) {
                    maxk = k;
                    maxval = v;
                }
            }

            if (epsilon > 0 && random(1.0) < epsilon) {
                nextAction = (int) random(actions);
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

        double DeltaQ = reward + gamma * q(state, nextAction) -  q(state, lastAction);
        
        et[state][lastAction] += 1;

        final double AlphaDeltaQ = alpha * DeltaQ;
        final double GammaLambda = gamma * lambda;
        for (int i = 0; i < states; i++) {
            for (int k = 0; k < actions; k++) {
                final double e = et[i][k];
                qAdd(i, k, AlphaDeltaQ * e);
                et[i][k] = GammaLambda * e;
            }
        }

        lastState = state;
        lastAction = nextAction;
        return nextAction;
    }

    public int getNextAction() {
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
}
