package nars.rl.hai;

import jurls.core.LearnerAndActor;


/**
 * original HaiQ SOM+QLearn brain
 */
public class HsomQBrain extends LearnerAndActor {

    double Q[][][]; //state, action
    double et[][][]; //eligiblity trace
    int nActions, nStates;
    double Alpha = 0.1, Gamma = 0.5, Lambda = 0.9; //0.1 0.5 0.9

    double Epsilon = 0.01; //random rate

    Hsom som;

    int lastStateX = 0, lastStateY = 0, lastAction = 0;

    public HsomQBrain(int nactions, int nstates) {
        nActions = nactions;
        nStates = nstates;
        Q = new double[nStates][nStates][nActions];
        et = new double[nStates][nStates][nActions];
        som = new Hsom(nstates, nstates);
    }


    /**
     * returns action #
     */
    public int act(double[] input, double reward) {
        som.learn(input);
        return q(som.winnerx, som.winnery, reward);
    }
    
    public static double random(double max) { return Math.random() * max;    }

    
    int q(final int StateX, final int StateY, final double reward) {

        final double[][][] Q = this.Q; //local reference
        final double[][][] et = this.et;
        final int actions = nActions;
        final int states = nStates;

        int maxk = 0;
        double maxval = Double.NEGATIVE_INFINITY;
        for (int k = 0; k < actions; k++) {
            double v = Q[StateX][StateY][k];
            if (v > maxval) {
                maxk = k;
                maxval = v;
            }
        }
        
        int Action;
        if (random(1.0) < Epsilon) {
            Action = (int) random(actions);
        } else {
            Action = maxk;
        }
        
        double DeltaQ = reward + Gamma * Q[StateX][StateY][Action] - 
                Q[lastStateX][lastStateY][lastAction];
        
        et[lastStateX][lastStateY][lastAction] += 1;
        
        final double AlphaDeltaQ = Alpha * DeltaQ;
        final double GammaLambda = Gamma * Lambda;
        for (int i = 0; i < states; i++) {
            for (int j = 0; j < states; j++) {
                for (int k = 0; k < actions; k++) {
                    final double e = et[i][j][k];
                    Q[i][j][k] += AlphaDeltaQ * e;
                    et[i][j][k] = GammaLambda * e;
                }
            }
        }

        lastStateX = StateX;
        lastStateY = StateY;
        lastAction = Action;
        return Action;
    }

    public void setAlpha(double Alpha) {
        this.Alpha = Alpha;
    }

    public void setGamma(double Gamma) {
        this.Gamma = Gamma;
    }

    public void setLambda(double Lambda) {
        this.Lambda = Lambda;
    }

    public double getAlpha() {
        return Alpha;
    }

    public double getLambda() {
        return Lambda;
    }

    public double getGamma() {
        return Gamma;
    }

    @Override
    public int learnAndAction(double[] nextState, double nextReward, double[] previousState, int previousAction) {
        return act(nextState, nextReward);
    }

    @Override
    public void stop() {

    }

    /*
    int Quantify(double val, int quantsteps) {
        double step = 1 / ((double) quantsteps);
        double wander = 0.0;
        int ind = -1;
        while (wander <= val) {
            wander += step;
            ind++;
        }
        return ind;
    }
    */

}
