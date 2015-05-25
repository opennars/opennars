package nars.rl.lstm;


import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Random;

public abstract class AbstractTraining {
    Random random;


    public AbstractTraining(Random random) {
        this.random = random;
    }

    protected abstract List<Interaction> GenerateInteractions(int tests);


    public double EvaluateFitnessSupervised(IAgentSupervised agent) throws Exception {

        List<Interaction> interactions = this.GenerateInteractions(tests);

        double fit = 0;
        double max_fit = 0;

        for (Interaction inter : interactions) {

            if (inter.do_reset)
                agent.clear();

            if (inter.target_output == null)
                agent.predict(inter.observation);
            else {
                double[] actual_output = null;

                if (validation_mode == true)
                    actual_output = agent.predict(inter.observation);
                else
                    actual_output = agent.learn(inter.observation, inter.target_output);

                if (util.argmax(actual_output) == util.argmax(inter.target_output))
                    fit++;

                max_fit++;
            }

            //System.out.println(inter);
        }
        return fit/max_fit;
    }


    public final static class Interaction {
        double[] observation;
        double[] target_output;
        boolean do_reset;

        @Override
        public String toString() {
            return ArrayUtils.toString(observation) + " " +
                    ArrayUtils.toString(target_output) + " " +
                    do_reset;
        }
    }

    protected int tests; // need to be set by GenerateInteractions()
    protected boolean validation_mode = false;
}
