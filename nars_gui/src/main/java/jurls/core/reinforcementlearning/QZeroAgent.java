    /*
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 * 
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 * 
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jurls.core.reinforcementlearning;

import java.util.Arrays;
import jurls.core.LearnerAndActor;
import jurls.core.approximation.ParameterizedFunction;
import jurls.core.approximation.ParameterizedFunctionGenerator;
import jurls.core.utils.ActionValuePair;
import jurls.core.utils.Utils;

/**
 *
 * @author thorsten
 */
public class QZeroAgent implements LearnerAndActor {

    public final ParameterizedFunction parameterizedFunction;
    private final int numActions;
    private final QZeroParameters qZeroParameters;
    private final ActionSelector actionSelector;
    public int numIterations = 0;

    public QZeroAgent(
            QZeroParameters qZeroParameters,
            ParameterizedFunctionGenerator parameterizedFunctionGenerator,
            ActionSelector as,
            double[] previousSate,
            int numActions
    ) {
        this.parameterizedFunction = parameterizedFunctionGenerator.generate(previousSate.length + 1);
        this.numActions = numActions;
        this.qZeroParameters = qZeroParameters;
        this.actionSelector = as;
    }

    @Override
    public int learnAndAction(double[] state, double reward, double[] previousState, int previousAction) {
        double q0 = parameterizedFunction.value(Utils.join(previousState, previousAction));
        double q = q0 + qZeroParameters.getAlpha() * (reward
                + this.qZeroParameters.getGamma() * Utils.v(
                        parameterizedFunction, state, numActions
                ).getA() - q0);
        parameterizedFunction.learn(Utils.join(previousState, previousAction), q);

        numIterations++;
        return chooseAction(state);
    }

    public ActionValuePair[] getActionProbabilities(double[] state) {
        ActionValuePair[] actionValuePairs = new ActionValuePair[numActions];

        for (int i = 0; i < numActions; ++i) {
            actionValuePairs[i] = new ActionValuePair(
                    i, Utils.q(parameterizedFunction, state, i)
            );
        }

        return actionSelector.fromQValuesToProbabilities(actionValuePairs);
    }

    public int chooseAction(double[] state) {
        ActionValuePair[] actionProbabilityPairs = getActionProbabilities(state);
        Arrays.sort(actionProbabilityPairs, (ActionValuePair o1, ActionValuePair o2) -> (int) Math.signum(o1.getV() - o2.getV()));

        double x = Math.random();
        int i = -1;

        while (x > 0) {
            ++i;
            x -= actionProbabilityPairs[i].getV();
        }

        return actionProbabilityPairs[i].getA();
    }

    @Override
    public int getNumInternalIterations() {
        return numIterations;
    }

    @Override
    public String getDebugString() {
        return "No. iterations : " + numIterations;
    }

}
