package automenta.dqn;

/**
 * Created by me on 8/4/15.
 */
public class DQNAgent {

    private final int inputs;
    private final int actions;
    private final DQNBrain brain;


    DQNAgent(int inputs, int actions) {

        this.inputs = inputs;
        this.actions = actions;




        this.brain = new DQNBrain(inputs, actions);

        /*
        var num_actions = actions.length;
        var temporal_window = 1; // amount of temporal memory. 0 = agent lives in-the-moment :)
        var network_size = num_inputs*temporal_window + num_actions*temporal_window + num_inputs;
        */

        // the value function network computes a value of taking any of the possible actions
        // given an input state. Here we specify one explicitly the hard way
        // but user could also equivalently instead use opt.hidden_layer_sizes = [20,20]
        // to just insert simple relu hidden layers.
        /*
        var layer_defs = [];
        layer_defs.push({type:'input', out_sx:1, out_sy:1, out_depth:network_size});
        layer_defs.push({type:'fc', num_neurons: 50, activation:'relu'});
        layer_defs.push({type:'fc', num_neurons: 50, activation:'relu'});
        layer_defs.push({type:'regression', num_neurons:num_actions});
        */

        /*

        // options for the Temporal Difference learner that trains the above net
        // by backpropping the temporal difference learning rule.

        opt.temporal_window = temporal_window;
        opt.experience_size = 30000;
        opt.start_learn_threshold = 1000;
        opt.gamma = 0.7;
        opt.learning_steps_total = 200000;
        opt.learning_steps_burnin = 3000;
        opt.epsilon_min = 0.05;
        opt.epsilon_test_time = 0.05;

        opt.tdtrainer_options = {learning_rate:0.001, momentum:0.0, batch_size:64, l2_decay:0.01};


        */

    }

    public double[] forward(double[] input) {
        return this.brain.forward(input);
    }
    public void backward(double reward) {
        this.brain.backward(reward);
    }


}
