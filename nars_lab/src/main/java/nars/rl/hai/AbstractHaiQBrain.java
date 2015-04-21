package nars.rl.hai;

import nars.Memory;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.Arrays;


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

    int lastAction = 0;

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

    /**
     * learn an entire input vector. each entry in state should be between 0 and 1 reprsenting the degree to which that state is active
     * @param state
     * @param reward
     * @param confidence
     * @return
     */
    public synchronized int learn(final double[] state, final double reward, float confidence) {
        ArrayRealVector act = new ArrayRealVector(nActions);

        //HACK - allow learn to update lastAction but restore to the value before this method was called, and then set the final value after all learning completed
        int actualLastAction = lastAction;

        // System.out.println(confidence + " " + Arrays.toString(state));

        for (int i = 0; i < state.length; i++) {
            lastAction = actualLastAction;
            int action = learn(i, reward, state[i] * confidence);

            //act.addToEntry(action, confidence);

            act.setEntry(action, Math.max(act.getEntry(action), confidence));
        }

        if (epsilon > 0) {
            if (Memory.randomNumber.nextDouble() < epsilon)
                return lastAction = getRandomAction();
        }

        //choose maximum action
        return lastAction = act.getMaxIndex();
    }

    public int learn(final int state, final double reward) {
        return learn(state, reward, 1f);
    }

    public int learn(final int state, final double reward, double confidence) {
        return learn(state, reward, -1, confidence);
    }

    /**
     * returns action #
     */
    public int learn(final int state, final double reward, int nextAction, double confidence) {

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

        double DeltaQ = reward + gamma * q(state, nextAction) -  q(state, lastAction);
        
        et[state][lastAction] += confidence;

        final double AlphaDeltaQ = confidence * alpha * DeltaQ;
        final double GammaLambda = gamma * lambda;
        for (int i = 0; i < states; i++) {
            for (int k = 0; k < actions; k++) {
                final double e = et[i][k];
                qAdd(i, k, AlphaDeltaQ * e);
                et[i][k] = GammaLambda * e;
            }
        }

        lastAction = nextAction;
        return nextAction;
    }

    public int getRandomAction() {
        return (int) random(nActions);
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
