/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.brain;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import jurls.core.LearnerAndActor;
import jurls.core.approximation.ApproxParameters;
import jurls.core.approximation.Generator;
import jurls.core.approximation.ParameterizedFunctionGenerator;
import jurls.core.brain.NeuroMap.InputOutput;
import jurls.core.reinforcementlearning.EpsilonGreedyActionSelector;
import jurls.core.reinforcementlearning.QZeroAgent;
import jurls.core.reinforcementlearning.QZeroParameters;
import jurls.core.utils.MatrixImage;
import jurls.core.utils.Utils;

/**
 *
 * @author thorsten
 */
public class Brain implements LearnerAndActor {

    final transient StringBuilder debugString = new StringBuilder();
    private double[] behaviourLearned;
    private MatrixImage rewardChart;
    private MatrixImage physicsChart;
    private MatrixImage behaviourChart;
    private MatrixImage soulChart;
    
    @Override
    public String getDebugString() {
        debugString.setLength(0);
        return debugString.append("physics learner : ").append( physicsLearner.getIterations()).append('\n').append("reward learner : ").append(rewardLearner.getIterations()).append('\n').append("soul : ").append(soul.getNumInternalIterations()).append('\n').append("behaviour learner : ").append( behaviourLearner.getIterations()).toString();
    }

    public static class Parameters {

        public double epsilon;

        public Parameters(double epsilon) {
            this.epsilon = epsilon;
        }
    }

    private final NeuroMap physicsLearner; //region 1
    private final NeuroMap rewardLearner; // region 2
    private final QZeroAgent soul; // region 3
    private final NeuroMap behaviourLearner; // region 4
    //private NeuroMap stateToAgentMiddleLayer; // region 5

    private final double[] stateMin;
    private final double[] stateMax;
    private final int numActions;
    private int numIterations = 0;
    private final Parameters parameters;
    private final Random random = new Random();

    public Brain(
            final int numStateElements,
            final int numFeatures,
            final int numActions,
            final QZeroParameters qZeroParameters,
            final EpsilonGreedyActionSelector.Parameters egParameters,
            final ParameterizedFunctionGenerator parameterizedFunctionGenerator,
            final Parameters parameters
    ) {
        this.parameters = parameters;
        this.numActions = numActions;

        
        

        physicsLearner = new NeuroMap(
                numStateElements + 1, numStateElements,
                parameterizedFunctionGenerator,
                1000000
        );
        rewardLearner = new NeuroMap(
                numStateElements, 1,
                parameterizedFunctionGenerator,
                1000000
        );
        behaviourLearner = new NeuroMap(
                numStateElements, 1,
                parameterizedFunctionGenerator,
                10
        );
        soul = new QZeroAgent(
                qZeroParameters,
                parameterizedFunctionGenerator,
                new EpsilonGreedyActionSelector(egParameters),
                new double[numStateElements],
                numActions
        );
        stateMin = new double[numStateElements];
        stateMax = new double[numStateElements];

        new Thread() {
            
            private double[] nextInputState;
            private double[] r;
            private double[] previousState;
            private double[] previousStateLearned;

            @Override
            public void run() {
                while (true) {
                    if ((previousState==null) || (previousState.length!=numStateElements))
                        previousState = new double[numStateElements];

                    for (int i = 0; i < numStateElements; ++i) {
                        final double stateMin1 = stateMin[i];
                        previousState[i]
                                = Math.random() * (stateMax[i] - stateMin1) + stateMin1;
                    }

                    final int previousAction = Utils.checkAction(numActions, behaviourLearner.value(previousState, previousStateLearned));
                    
                    InputOutput nextLearning = behaviourLearner.newMemory();
                    double[] nextState = nextLearning.input;
                    double[] nextActionArray = nextLearning.output;
                    {


                        nextState = physicsLearner.value(Utils.join(previousState, previousAction, nextInputState), nextState);

                        r = rewardLearner.value(nextState, r);   assert(r.length == 1);

                        final double reward = r[0];

                        int nextAction = soul.learnAndAction(nextState, reward, previousState, previousAction);                  
                        if (nextActionArray == null) nextActionArray = new double[1];
                        nextActionArray[0] = nextAction;
                    }
        
                    nextLearning.input = nextState;
                    nextLearning.output = nextActionArray;
                    
                    numIterations++;
                }
            }
        }.start();
        
        newStatusWindow();
    }
    
    protected void newStatusWindow() {
        JPanel j = new JPanel(new GridLayout(1, 0, 4, 4));
        
        j.add(soulChart = new MatrixImage(300, 300));        
        j.add(rewardChart = new MatrixImage(150, 300));        
        j.add(physicsChart = new MatrixImage(150, 300));
        j.add(behaviourChart = new MatrixImage(150, 300));
        
        JFrame x = new JFrame(this.toString());
        x.getContentPane().add(j);
        x.pack();
        x.show();
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

        //TODO this doesnt re-use rewardLearner's arrays if they exist. this can be done by restruturing how this function is called
        rewardLearner.learn(nextState, nextReward);
        
        
        //TODO this doesnt (completely) re-use rewardLearner's arrays if they exist. this can be done by restruturing how this function is called
        InputOutput physicsLearning = physicsLearner.newMemory();
        physicsLearning.input = Utils.join(previousState, previousAction, physicsLearning.input);
        physicsLearning.output = nextState;        

        
        soulChart.draw(soul.parameterizedFunction, -1, +1);
        rewardChart.draw(rewardLearner, -1, +1, 300);
        physicsChart.draw(physicsLearner, -1, +1, 300);
        behaviourChart.draw(behaviourLearner, -1, +1, 300);
        
        
        if (Math.random() < parameters.epsilon) {
            return random.nextInt(numActions);
        } else {
            return Utils.checkAction(numActions,                    
                    behaviourLearned = behaviourLearner.value(nextState, behaviourLearned));
        }
        
    }

    @Override
    public int getNumInternalIterations() {
        return numIterations;
    }
}
