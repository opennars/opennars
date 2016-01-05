package karpathy;

import nars.util.data.random.XorShift128PlusRandom;

import java.util.Random;

/**
 * adapted from deepqlearn.js
 * https://github.com/karpathy/convnetjs/blob/master/build/deepqlearn.js
 */
public class DeepQBrain {

	private final int net_inputs;
	private final int num_states; // TODO numIns
	private final int num_actions; // TODO num_outs
	private final int window_size;
	private final float[] state_window;
	private final float[] action_window;
	private final float[] reward_window;
	private final float[] net_window;
	private float[] last_input_array;
	private final boolean learning;
	private final float latest_reward;
	private float epsilon;
	private int forward_passes;
	private final int age;
	private final Object[] experience;
	private final Net value_net;
	private final TrainSGD train;

	// in number of time steps, of temporal memory
	// the ACTUAL input to the net will be (x,a) temporal_window times, and
	// followed by current x
	// so to have no information from previous time step going into value
	// function, set to 0.
	int temporal_window = 1;
	// size of experience replay memory
	int experience_size = 30000;

	// number of examples in experience replay memory before we begin learning
	float start_learn_threshold = (float) Math.floor(Math.min(
			experience_size * 0.1, 1000));

	// gamma is a crucial parameter that controls how much plan-ahead the agent
	// does. In [0,1]
	float gamma = 0.8f;

	// number of steps we will learn for
	int learning_steps_total = 100000;

	// how many steps of the above to perform only random actions (in the
	// beginning)?
	int learning_steps_burnin = 3000;

	// what epsilon value do we bottom out on? 0.0 => purely deterministic
	// policy at end
	float epsilon_min = 0.05f;

	// what epsilon to use at test time? (i.e. when learning is disabled)
	float epsilon_test_time = 0.01f;

	// // advanced feature. Sometimes a random action should be biased towards
	// some values
	// // for example in flappy bird, we may want to choose to not flap more
	// often
	// if(typeof opt.random_action_distribution !== 'undefined') {
	// // this better sum to 1 by the way, and be of length this.num_actions
	// this.random_action_distribution = opt.random_action_distribution;
	// if(this.random_action_distribution.length !== num_actions) {
	// console.log('TROUBLE. random_action_distribution should be same length as
	// num_actions.');
	// }
	// var a = this.random_action_distribution;
	// var s = 0.0; for(var k=0;k<a.length;k++) { s+= a[k]; }
	// if(Math.abs(s-1.0)>0.0001) { console.log('TROUBLE.
	// random_action_distribution should sum to 1!'); }
	// } else {
	// this.random_action_distribution = [];
	// }

	public DeepQBrain(int num_states, int num_actions) {
		this.num_states = num_states;
		this.num_actions = num_actions;

		// states that go into neural net to predict optimal action look as
		// x0,a0,x1,a1,x2,a2,...xt
		// this variable controls the size of that temporal window. Actions are
		// encoded as 1-of-k hot vectors
		net_inputs = num_states * temporal_window + num_actions
				* temporal_window + num_states;
		window_size = Math.max(temporal_window, 2); // must be at least 2, but
													// if we want more context
													// even more
		state_window = new float[window_size];
		action_window = new float[window_size];
		reward_window = new float[window_size];
		net_window = new float[window_size];

		// // create [state -> value of all possible actions] modeling net for
		// the value function
		// //var layer_defs = [];
		// if(typeof opt.layer_defs !== 'undefined') {
		// // this is an advanced usage feature, because size of the input to
		// the network, and number of
		// // actions must check out. This is not very pretty Object Oriented
		// programming but I can't see
		// // a way out of it :(
		// layer_defs = opt.layer_defs;
		// if(layer_defs.length < 2) { console.log('TROUBLE! must have at least
		// 2 layers'); }
		// if(layer_defs[0].type !== 'input') { console.log('TROUBLE! first
		// layer must be input layer!'); }
		// if(layer_defs[layer_defs.length-1].type !== 'regression') {
		// console.log('TROUBLE! last layer must be input regression!'); }
		// if(layer_defs[0].out_depth * layer_defs[0].out_sx *
		// layer_defs[0].out_sy !== this.net_inputs) {
		// console.log('TROUBLE! Number of inputs must be num_states *
		// temporal_window + num_actions * temporal_window + num_states!');
		// }
		// if(layer_defs[layer_defs.length-1].num_neurons !== this.num_actions)
		// {
		// console.log('TROUBLE! Number of regression neurons should be
		// num_actions!');
		// }
		// } else {

		// default architecture
		value_net = new Net();
		// create a very simple neural net by default
		value_net.add(new Net.Input(1, 1, net_inputs)); // {type:'input',
														// out_sx:1, out_sy:1,
														// out_depth:this.net_inputs});

		/*
		 * if(typeof opt.hidden_layer_sizes !== 'undefined') { // allow user to
		 * specify this via the option, for convenience var hl =
		 * opt.hidden_layer_sizes; for(var k=0;k<hl.length;k++) {
		 * layer_defs.push({type:'fc', num_neurons:hl[k], activation:'relu'});
		 * // relu by default } }
		 */

		// value function output
		value_net.add(new Net.Regression(num_actions));

		// and finally we need a Temporal Difference Learning trainer!
		train = new TrainSGD(value_net);
		train.learning_rate = 0.01f;
		train.momentum = 0;
		train.batch_size = 64;
		train.l2_decay = 0.01f;

		// experience replay
		experience = new Object[0];

		// various housekeeping variables
		age = 0; // incremented every backward()
		forward_passes = 0; // incremented every forward()
		epsilon = 1.0f; // controls exploration exploitation tradeoff. Should be
						// annealed over time
		latest_reward = 0;
		last_input_array = new float[1];

		// this.average_reward_window = new cnnutil.Window(1000, 10);
		// this.average_loss_window = new cnnutil.Window(1000, 10);
		learning = true;
	}

	public final Random rng = new XorShift128PlusRandom(1);

	public int getActionRandom() {
		return rng.nextInt(num_actions);
		/*
		 * random_action: function() { // a bit of a helper function. It returns
		 * a random action // we are abstracting this away because in future we
		 * may want to // do more sophisticated things. For example some actions
		 * could be more // or less likely at "rest"/default state.
		 * if(this.random_action_distribution.length === 0) { return
		 * convnetjs.randi(0, this.num_actions); } else { // okay, lets do some
		 * fancier sampling: var p = convnetjs.randf(0, 1.0); var cumprob = 0.0;
		 * for(var k=0;k<this.num_actions;k++) { cumprob +=
		 * this.random_action_distribution[k]; if(p < cumprob) { return k; } } }
		 * },
		 */
	}

	public int policy(float[] state /* # inputs */) {
		// compute the value of doing any action in this state
		// and return the argmax action and its value
		DenseTensor svol = new DenseTensor(state);

		DenseTensor action_values = value_net.forward(svol);
		int maxk = 0;
		float[] av = action_values.data;
		float maxval = av[0];
		for (int k = 1; k < num_actions; k++) {
			float avk = av[k];
			if (avk > maxval) {
				maxk = k;
				maxval = avk;
			}
		}
		return maxk;
		// return {action:maxk, value:maxval};
	}

	/**
	 * // It's a concatenation of last window_size (x,a) pairs and current state
	 * x
	 */
	float[] getNetInput(float[] xt) {
		// OLD: return s = (x,a,x,a,x,a,xt) state vector.

		int stateSize = xt.length;
		int size = xt.length + window_size * (stateSize + num_actions);
		float[] w = new float[size];
		/*
		 * 
		 * var w = []; w = w.concat(xt); // start with current state // and now
		 * go backwards and append states and actions from history
		 * temporal_window times var n = this.window_size; for(var
		 * k=0;k<this.temporal_window;k++) { // state w =
		 * w.concat(this.state_window[n-1-k]); // action, encoded as 1-of-k
		 * indicator vector. We scale it up a bit because // we dont want weight
		 * regularization to undervalue this information, as it only exists once
		 * var action1ofk = new float[this.num_actions); for(var
		 * q=0;q<this.num_actions;q++) action1ofk[q] = 0.0;
		 * action1ofk[this.action_window[n-1-k]] = 1.0*this.num_states; w =
		 * w.concat(action1ofk); }
		 */
		return w;

	}

	public int forward(float[] input_array) {

		// compute forward (behavior) pass given the input neuron signals from
		// body
		forward_passes++;
		last_input_array = input_array;

		int action;

		float[] net_input;
		if (forward_passes > temporal_window) {
			// we have enough to actually do something reasonable
			net_input = getNetInput(input_array);
			epsilon = learning
					? (float) Math.min(1.0, Math.max(epsilon_min, 1.0
							- (age - learning_steps_burnin)
							/ (learning_steps_total - learning_steps_burnin)))
					: epsilon_test_time;
			float rf = rng.nextFloat();
			action = rf < epsilon ? getActionRandom() : policy(net_input);
		} else {
			// pathological case that happens first few iterations
			// before we accumulate window_size inputs
			net_input = null;
			action = getActionRandom();
		}

		// CONTINUE HERE
		// // remember the state and action we took for backward pass
		// //Util.arrayPush(net_window, net_input);
		// this.net_window.shift();
		// this.net_window.push(net_input);
		// this.state_window.shift();
		// this.state_window.push(input_array);
		// this.action_window.shift();
		// this.action_window.push(action);

		return action;
	}

	/*
	 * backward: function(reward) { this.latest_reward = reward;
	 * this.average_reward_window.add(reward); this.reward_window.shift();
	 * this.reward_window.push(reward);
	 * 
	 * if(!this.learning) { return; }
	 * 
	 * // various book-keeping this.age += 1;
	 * 
	 * // it is time t+1 and we have to store (s_t, a_t, r_t, s_{t+1}) as new
	 * experience // (given that an appropriate number of state measurements
	 * already exist, of course) if(this.forward_passes > this.temporal_window +
	 * 1) { var e = new Experience(); var n = this.window_size; e.state0 =
	 * this.net_window[n-2]; e.action0 = this.action_window[n-2]; e.reward0 =
	 * this.reward_window[n-2]; e.state1 = this.net_window[n-1];
	 * if(this.experience.length < this.experience_size) {
	 * this.experience.push(e); } else { // replace. finite memory! var ri =
	 * convnetjs.randi(0, this.experience_size); this.experience[ri] = e; } }
	 * 
	 * // learn based on experience, once we have some samples to go on // this
	 * is where the magic happens... if(this.experience.length >
	 * this.start_learn_threshold) { var avcost = 0.0; for(var k=0;k <
	 * this.tdtrainer.batch_size;k++) { var re = convnetjs.randi(0,
	 * this.experience.length); var e = this.experience[re]; var x = new
	 * convnetjs.Vol(1, 1, this.net_inputs); x.w = e.state0; var maxact =
	 * this.policy(e.state1); var r = e.reward0 + this.gamma * maxact.value; var
	 * ystruct = {dim: e.action0, val: r}; var loss = this.tdtrainer.train(x,
	 * ystruct); avcost += loss.loss; } avcost =
	 * avcost/this.tdtrainer.batch_size; this.average_loss_window.add(avcost); }
	 * },
	 * 
	 * 
	 * 
	 * visSelf: function(elt) { elt.innerHTML = ''; // erase elt first
	 * 
	 * // elt is a DOM element that this function fills with brain-related
	 * information var brainvis = document.createElement('div');
	 * 
	 * // basic information var desc = document.createElement('div'); var t =
	 * ''; t += 'experience replay size: ' + this.experience.length + '<br>'; t
	 * += 'exploration epsilon: ' + this.epsilon + '<br>'; t += 'age: ' +
	 * this.age + '<br>'; t += 'average Q-learning loss: ' +
	 * this.average_loss_window.get_average() + '<br />'; t += 'smooth-ish
	 * reward: ' + this.average_reward_window.get_average() + '<br />';
	 * desc.innerHTML = t; brainvis.appendChild(desc);
	 * 
	 * elt.appendChild(brainvis); } }
	 * 
	 * global.Brain = Brain; })(deepqlearn);
	 * 
	 * (function(lib) { "use strict"; if (typeof module === "undefined" ||
	 * typeof module.exports === "undefined") { window.deepqlearn = lib; // in
	 * ordinary browser attach library to window } else { module.exports = lib;
	 * // in nodejs } })(deepqlearn);
	 */
}
