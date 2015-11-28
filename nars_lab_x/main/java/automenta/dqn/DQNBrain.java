package automenta.dqn;

/**
 * Created by me on 8/4/15.
 */
public class DQNBrain {


    /* An agent is in state0 and does action0
         environment then assigns reward0 and provides new state, state1
         Experience nodes store all this information, which is used in the
         Q-learning update step */
    public static class Experience {
        final int state0, state1, action0;
        final double reward;

        public Experience(int state0, int action0, int state1, double reward) {
            this.reward = reward;
            this.state0 = state0;
            this.state1 = state1;
            this.action0 = action0;
        }
    }

    public DQNBrain(int inputs, int actions) {
    }

    public void backward(double reward) {


    }

    public double[] forward(double[] input) {
        return null;

    }

}
