/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.reinforcementlearning;

import jurls.core.Expression;
import jurls.core.approximation.ApproxParameters;
import jurls.core.approximation.Functional;
import jurls.core.approximation.ParameterizedFunction;

/**
 *
 * @author thorsten
 */
public class RLAgentMarshaller {

    private RLAgent rLAgent;
    private ParameterizedFunction parameterizedFunction;

    public void reset(
            ParameterizedFunction parameterizedFunction,
            UpdateProcedure updateProcedure,
            ActionSelector actionSelector,
            double[] s0,
            ApproxParameters approxParameters,
            RLParameters rLParameters,
            int numActions
    ) {
        
        //this.parameterizedFunction = (ParameterizedFunction) Expression.optimize((Functional)parameterizedFunction);
        this.parameterizedFunction = parameterizedFunction;
            
        Expression.print(parameterizedFunction);
        
        
        
        rLAgent = new RLAgent(parameterizedFunction, updateProcedure, actionSelector, numActions, s0, approxParameters, rLParameters);
    }

    public RLAgent getRLAgent() {
        return rLAgent;
    }

    public ParameterizedFunction getParameterizedFunction() {
        return parameterizedFunction;
    }
}
