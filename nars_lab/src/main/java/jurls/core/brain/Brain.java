/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.brain;

import jurls.core.LearnerAndActor;
import jurls.core.approximation.ParameterizedFunctionGenerator;
import jurls.core.reinforcementlearning.RLParameters;
import jurls.core.utils.Utils;

/**
 *
 * @author thorsten
 */
public class Brain extends LearnerAndActor {

    private final StringBuilder debugString = new StringBuilder();
    private final NeuroMap physicsLearner; //region 1
    private final NeuroMap rewardLearner; // region 2
    private final LearnerAndActor soul; // region 3
    private final NeuroMap behaviourLearner; // region 4
    //private NeuroMap stateToAgentMiddleLayer; // region 5

    private final double[] stateMin;
    private final double[] stateMax;
    private final int numActions;
    private boolean running = true;
    private final double[] action = new double[1];

    public Brain(
            int numStateElements,
            int numActions,
            RLParameters rLParameters,
            ParameterizedFunctionGenerator parameterizedFunctionGenerator,
            LearnerAndActor soul
    ) {
        this.numActions = numActions;

        physicsLearner = new NeuroMap(
                numStateElements + 1, numStateElements,
                parameterizedFunctionGenerator,
                100000
        );
        rewardLearner = new NeuroMap(
                numStateElements, 1,
                parameterizedFunctionGenerator,
                100000
        );
        behaviourLearner = new NeuroMap(
                numStateElements, 1,
                parameterizedFunctionGenerator,
                100
        );
        this.soul = soul;
        stateMin = new double[numStateElements];
        stateMax = new double[numStateElements];

        new Thread() {

            private final double[] previousState = new double[numStateElements];
            private final double[] stateAction = new double[numStateElements + 1];
            private final double[] action = new double[1];
            private final double[] nextState = new double[numStateElements];
            private final double[] reward = new double[1];

            @Override
            public void run() {
                while (running) {
                    for (int i = 0; i < numStateElements; ++i) {
                        previousState[i] = Math.random() * (stateMax[i] - stateMin[i]) + stateMin[i];
                    }
                    behaviourLearner.value(action, previousState);
                    int previousAction = Utils.checkAction(numActions,action);
                    Utils.join(stateAction, previousState, previousAction);
                    physicsLearner.value(nextState,stateAction);
                    rewardLearner.value(reward,nextState);

                    int nextAction = soul.learnAndAction(
                            nextState,
                            reward[0],
                            previousState,
                            previousAction
                    );

                    behaviourLearner.learn(nextState, new double[]{nextAction});
                }
            }
        }.start();
    }

    @Override
    public int learnAndAction(double[] nextState, double nextReward, double[] previousState, int previousAction) {
        for (int i = 0; i < stateMin.length; ++i) {
            if (nextState[i] < stateMin[i]) {
                stateMin[i] = nextState[i];
            }
            if (nextState[i] > stateMax[i]) {
                stateMax[i] = nextState[i];
            }
        }

        rewardLearner.learn(nextState, new double[]{nextReward});
        physicsLearner.learn(previousState, nextState);
        updateCounters();
        behaviourLearner.value(action,nextState);
        return Utils.checkAction(numActions, action);
    }

    @Override
    public String getDebugString(int indent) {
        debugString.setLength(0);
        String ind = Utils.makeIndent(indent);

        return debugString.
                append(ind).append("Brain : \n").
                append(super.getDebugString(indent)).
                append(ind).append("- physics learner : ").append(physicsLearner.getDebugString()).append('\n').
                append(ind).append("- reward learner : ").append(rewardLearner.getDebugString()).append('\n').
                append(ind).append("- behaviour learner : ").append(behaviourLearner.getDebugString()).append('\n').
                append(ind).append("- soul : \n").append(soul.getDebugString(indent + 4)).append('\n').
                toString();
    }

    @Override
    public void stop() {
        running = false;
        physicsLearner.stop();
        rewardLearner.stop();
        soul.stop();
        behaviourLearner.stop();
    }

}
