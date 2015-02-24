/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core;

/**
 *
 * @author thorsten
 */
public interface LearnerAndActor {

    public int learnAndAction(double[] nextState, double nextReward, double[] previousState, int previousAction);

    public int getNumInternalIterations();

    public String getDebugString();
}
