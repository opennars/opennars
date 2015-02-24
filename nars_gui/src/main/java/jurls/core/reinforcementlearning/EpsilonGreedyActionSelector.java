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
public class EpsilonGreedyActionSelector implements ActionSelector {

    public static class Parameters {

        private double epsilon;

        public Parameters(double epsilon) {
            this.epsilon = epsilon;
        }

        public double getEpsilon() {
            return epsilon;
        }

        public void setEpsilon(double epsilon) {
            this.epsilon = epsilon;
        }
    }

    private final Parameters parameters;

    public EpsilonGreedyActionSelector(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public ActionValuePair[] fromQValuesToProbabilities(ActionValuePair[] actionValuePairs) {
        int bestPair = 0;
        
        for(int i = 0;i < actionValuePairs.length;++i){
            if(actionValuePairs[i].getV() > actionValuePairs[bestPair].getV())
                bestPair = i;
        }
        
        ActionValuePair[] ret = new ActionValuePair[actionValuePairs.length];
        
        for(int i = 0;i < actionValuePairs.length;++i){
            ret[i] = new ActionValuePair(
                    actionValuePairs[i].getA(),
                    i == bestPair ?
                            1 - parameters.epsilon :
                            parameters.epsilon / (ret.length - 1)
            );
        }
        
        return ret;
    }

}
