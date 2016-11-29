package nars.lab.rl.hai;

import nars.storage.Memory;


/**
 * TODO generalize SOM to N-d
 */
public class HaiQBrain {

    double Q[][][]; //state, action
    double et[][][]; //eligiblity trace
    int nActions = 0, nStates = 0;
    double Alpha = 0.1, Gamma = 0.8, Lambda = 0.1; //0.1 0.5 0.9
    Hsom som;

    int lastStateX = 0, lastStateY = 0, lastAction = 0;

    public HaiQBrain(int nactions, int nstates) {
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
        som.adapt(input);
        return q(som.winnerx, som.winnery, reward);
    }
    
    public static double random(double max) { return Memory.randomNumber.nextDouble() * max;    }

    
    int q(int StateX, int StateY, double reward) {
        
        int maxk = 0;
        double maxval = Double.NEGATIVE_INFINITY;
        for (int k = 0; k < nActions; k++) {
            if (Q[StateX][StateY][k] > maxval) {
                maxk = k;
                maxval = Q[StateX][StateY][k];
            }
        }
        
        int Action;
        if (random(1.0) < Alpha) {
            Action = (int) random(nActions);
        } else {
            Action = maxk;
        }
        
        double DeltaQ = reward + Gamma * Q[StateX][StateY][Action] - 
                Q[lastStateX][lastStateY][lastAction];
        
        et[lastStateX][lastStateY][lastAction] += 1;
        
        final double AlphaDeltaQ = Alpha * DeltaQ;
        final double GammaLambda = Gamma * Lambda;
        for (int i = 0; i < nStates; i++) {
            for (int j = 0; j < nStates; j++) {
                for (int k = 0; k < nActions; k++) {
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
