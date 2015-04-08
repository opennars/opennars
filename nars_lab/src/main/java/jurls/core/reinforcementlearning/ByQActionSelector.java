/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.reinforcementlearning;

import jurls.core.utils.ActionValuePair;

/**
 *
 * @author thorsten
 */
public class ByQActionSelector implements ActionSelector {

    @Override
    public ActionValuePair[] fromQValuesToProbabilities(double epsilon, ActionValuePair[] actionValuePairs) {
        ActionValuePair[] ret = new ActionValuePair[actionValuePairs.length];

        for (int i = 0; i < actionValuePairs.length; ++i) {
            ret[i] = new ActionValuePair(
                    actionValuePairs[i].getA(),
                    actionValuePairs[i].getV()
            );
        }

        double min = Double.MAX_VALUE;
        for (int i = 0; i < ret.length; ++i) {
            if (ret[i].getV() < min) {
                min = ret[i].getV();
            }
        }

        for (int i = 0; i < ret.length; ++i) {
            ret[i].setV(ret[i].getV() + min);
        }

        double sum = 0;
        for (int i = 0; i < ret.length; ++i) {
            sum += ret[i].getV();
        }

        for (int i = 0; i < ret.length; ++i) {
            ret[i].setV(ret[i].getV() / sum);
        }

        for (int i = 0; i < ret.length; ++i) {
            ret[i].setV(Math.pow(ret[i].getV(), 2));
        }

        sum = 0;
        for (int i = 0; i < ret.length; ++i) {
            sum += ret[i].getV();
        }

        for (int i = 0; i < ret.length; ++i) {
            ret[i].setV(ret[i].getV() / sum);
        }
        return ret;
    }

}
