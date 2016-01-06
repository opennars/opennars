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
public interface ActionSelector {

	ActionValuePair[] fromQValuesToProbabilities(double epsilon,
			ActionValuePair[] actionValuePairs);
}
