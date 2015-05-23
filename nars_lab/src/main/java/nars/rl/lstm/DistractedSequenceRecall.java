package nars.rl.lstm;

import nars.rl.lstm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DistractedSequenceRecall {

	int tests = 1000;
	int observation_dimension = 10;
	int action_dimension = 4;
	boolean validation_mode = false;
	Random r;
	
	public DistractedSequenceRecall(Random r) {
		this.r = r;
	}
	
	class Interaction {
		double[] observation;
		double[] target_output;
		boolean do_reset;
	}
	
	private List<Interaction> GenerateInteractions(int tests) {
		List<Interaction> result = new ArrayList<Interaction>();
		for (int test = 0; test < tests; test++) {
			int[] seq = new int[22];
			int target1 = r.nextInt(4);
			int target2 = r.nextInt(4);
			for (int t = 0; t < 22; t++) {
				seq[t] = r.nextInt(4)+4;//+4 so as not to overlap with target symbols
			}
			int loc1 = r.nextInt(22);
			int loc2 = r.nextInt(22);
			while (loc1 == loc2)
				loc2 = r.nextInt(22);
			if (loc1 > loc2) {
				int temp = loc1;
				loc1 = loc2;
				loc2 = temp;
			}
			seq[loc1] = target1;
			seq[loc2] = target2;
			
			for (int t = 0; t < seq.length; t++) {
				double[] input = new double[observation_dimension];
				input[seq[t]] = 1.0;
				
				Interaction inter = new Interaction();
				if (t == 0)
					inter.do_reset = true;
				inter.observation = input;
				result.add(inter);
			}
			//final 2 steps
			double[] input1 = new double[observation_dimension];
			input1[8] = 1.0;
			double[] target_output1 = new double[action_dimension];
			target_output1[target1] = 1.0;
			Interaction inter1 = new Interaction();
			inter1.observation = input1;
			inter1.target_output = target_output1;
			result.add(inter1);
			
			double[] input2 = new double[observation_dimension];
			input2[9] = 1.0;
			double[] target_output2 = new double[action_dimension];
			target_output2[target2] = 1.0;
			Interaction inter2 = new Interaction();
			inter2.observation = input2;
			inter2.target_output = target_output2;
			result.add(inter2);
		}
		return result;
	}
	
	public double EvaluateFitnessSupervised(IAgentSupervised agent) throws Exception {
		
		List<Interaction> interactions = this.GenerateInteractions(tests);
		
		double fit = 0;
		double max_fit = 0;
		
		for (Interaction inter : interactions) {
			
			if (inter.do_reset)
				agent.Reset();
			
			if (inter.target_output == null)
				agent.Next(inter.observation);
			else {
				double[] actual_output = null;

				if (validation_mode == true)
					actual_output = agent.Next(inter.observation);
				else
					actual_output = agent.Next(inter.observation, inter.target_output);

				if (util.argmax(actual_output) == util.argmax(inter.target_output))
					fit++;
				
				max_fit++;
			}
		}
		return fit/max_fit;
	}
	

	public int GetActionDimension() {
		return action_dimension;
	}

	public int GetObservationDimension() {
		return observation_dimension;
	}

}
