package nars.rl.lstm;


import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AbstractTraining {
    public int batchsize = 400000000;

    public AbstractTraining(Random random, final int inputDimension, final int outputDimension) {
        this.random = random;
        this.inputDimension = inputDimension;
        this.outputDimension = outputDimension;
    }

    public double EvaluateFitnessSupervised(AgentSupervised agent) throws Exception {

        List<Interaction> interactions = this.GenerateInteractions(tests);

        double fit = 0;
        double max_fit = 0;

        for (Interaction inter : interactions) {

            if (inter.do_reset)
                agent.clear();

            if (inter.target_output == null)
                agent.predict(inter.observation, false);
            else {
                double[] actual_output = null;

                if (validation_mode == true)
                    actual_output = agent.predict(inter.observation, true);
                else
                    actual_output = agent.learn(inter.observation, inter.target_output, true);

                if (util.argmax(actual_output) == util.argmax(inter.target_output))
                    fit++;

                max_fit++;
            }
        }
        return fit/max_fit;
    }

    public void supervised(AgentSupervised agent) throws Exception {
        List<Interaction> interactions = this.GenerateInteractions(tests);

        List<AgentSupervised.NonResetInteraction> agentNonResetInteraction = new ArrayList<>();

        for (Interaction inter : interactions) {

            if (inter.do_reset) {
                agentExecuteNonResetInteractionsAndFlush(agent, agentNonResetInteraction);

                agent.clear();
            }

            AgentSupervised.NonResetInteraction newInteraction = new AgentSupervised.NonResetInteraction();
            newInteraction.observation = inter.observation;
            newInteraction.target_output = inter.target_output;
            agentNonResetInteraction.add(newInteraction);

            if( agentNonResetInteraction.size() > batchsize ) {
                agentExecuteNonResetInteractionsAndFlush(agent, agentNonResetInteraction);
            }

        }

        agentExecuteNonResetInteractionsAndFlush(agent, agentNonResetInteraction);
    }

    private void agentExecuteNonResetInteractionsAndFlush(AgentSupervised agent, final List<AgentSupervised.NonResetInteraction> nonResetInteractions) throws Exception {
        agent.learnBatch(nonResetInteractions, false);

        nonResetInteractions.clear();
    }


    public final static class Interaction {
        public double[] observation;
        public double[] target_output;
        public boolean do_reset;

        @Override
        public String toString() {
            return ArrayUtils.toString(observation) + " " +
                    ArrayUtils.toString(target_output) + " " +
                    do_reset;
        }
    }

    public int getInputDimension() {
        return inputDimension;
    }

    public int getOutputDimension() {
        return outputDimension;
    }

    protected Random random;
    protected int tests; // need to be set by GenerateInteractions()
    protected boolean validation_mode = false;
    protected abstract List<Interaction> GenerateInteractions(int tests);

    private final int inputDimension;
    private final int outputDimension;
}
