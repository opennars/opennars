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

    import jurls.core.LearnerAndActor;
    import jurls.core.approximation.ParameterizedFunctionGenerator;
    import jurls.core.utils.ActionValuePair;
    import jurls.core.utils.Utils;

    import java.util.Arrays;

/**
 *
 * @author thorsten
 */
public class QZeroAgent extends LearnerAndActor {

    private final int numActions;
    private final RLParameters rLParameters;
    private final ActionSelector actionSelector;
    private final double[] stateAction;

    public QZeroAgent(
            RLParameters rLParameters,
            ParameterizedFunctionGenerator parameterizedFunctionGenerator,
            ActionSelector as,
            double[] previousSate,
            int numActions
    ) {
        parameterizedFunction = parameterizedFunctionGenerator.generate(previousSate.length + 1);
        this.numActions = numActions;
        this.rLParameters = rLParameters;
        actionSelector = as;
        stateAction = new double[previousSate.length + 1];
    }

    @Override
    public int learnAndAction(double[] state, double reward, double[] previousState, int previousAction) {
        Utils.join(stateAction, previousState, previousAction);
        double q0 = parameterizedFunction.value(stateAction);
        double q = q0 + rLParameters.getAlpha() * (reward
                + rLParameters.getGamma() * Utils.v(
                        parameterizedFunction,
                        stateAction,
                        state, numActions
                ).getA() - q0);
        Utils.join(stateAction, previousState, previousAction);
        parameterizedFunction.learn(stateAction, q);

        updateCounters();
        return chooseAction(state);
    }

    public ActionValuePair[] getActionProbabilities(double[] state) {
        ActionValuePair[] actionValuePairs = new ActionValuePair[numActions];

        for (int i = 0; i < numActions; ++i) {
            actionValuePairs[i] = new ActionValuePair(
                    i, Utils.q(parameterizedFunction, stateAction, state, i)
            );
        }

        return actionSelector.fromQValuesToProbabilities(rLParameters.getEpsilon(), actionValuePairs);
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
    public String getDebugString(int indent) {
        String ind = Utils.makeIndent(indent);
        return ind + "Q(0)\n" + super.getDebugString(indent);
    }

    @Override
    public void stop() {
    }

}
