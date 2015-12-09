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
        for (ActionValuePair aRet6 : ret) {
            if (aRet6.getV() < min) {
                min = aRet6.getV();
            }
        }

        for (ActionValuePair aRet5 : ret) {
            aRet5.setV(aRet5.getV() + min);
        }

        double sum = 0;
        for (ActionValuePair aRet4 : ret) {
            sum += aRet4.getV();
        }

        for (ActionValuePair aRet3 : ret) {
            aRet3.setV(aRet3.getV() / sum);
        }

        for (ActionValuePair aRet2 : ret) {
            double v = aRet2.getV();
            aRet2.setV(
                    //Math.pow(v, 2)
                    v * v
            );
        }

        sum = 0;
        for (ActionValuePair aRet1 : ret) {
            sum += aRet1.getV();
        }

        for (ActionValuePair aRet : ret) {
            aRet.setV(aRet.getV() / sum);
        }
        return ret;
    }

}
