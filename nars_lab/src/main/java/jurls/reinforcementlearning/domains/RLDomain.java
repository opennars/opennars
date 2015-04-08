/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.reinforcementlearning.domains;

import java.awt.Component;

/**
 *
 * @author thorsten
 */
public interface RLDomain {

    public double[] observe();

    public double reward();

    public void takeAction(int action);

    public void worldStep();

    public Component component();

    public int numActions();

}
