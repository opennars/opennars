/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.reinforcementlearning.domains;

import automenta.vivisect.swing.NWindow;

import java.awt.*;

/**
 * Reinforcement Learning interface
 * @author thorsten
 */
public interface RLEnvironment {

    /** current observation */
    double[] observe();

    /** current reward */
    double getReward();

    /** set the next action (0 <= action < numActions)
     *  returns false if the action was not successfully applied
     * */
    boolean takeAction(int action);

    /** advance world simulation by 1 frame */
    void frame();

    int numActions();

    default int numStates() { return observe().length; }


    @Deprecated
    Component component();
    @Deprecated
    default NWindow newWindow() {
        return new NWindow(getClass().toString(), component()).show(800,600);
    }


    default float getMaxReward() { return 1.0f; }
    default float getMinReward() { return -1.0f; }



}
