package nars.rl.lstm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DistractedSequenceRecall extends AbstractTraining {
	static final int observation_dimension = 10;
	static final int action_dimension = 4;


	public DistractedSequenceRecall(Random r) {
		super(r, observation_dimension, action_dimension);

		tests = 1000;
	}

	protected List<Interaction> GenerateInteractions(int tests) {
		this.tests = tests;

		List<Interaction> result = new ArrayList<>();
		for (int test = 0; test < tests; test++) {
			int[] seq = new int[22];
			int target1 = random.nextInt(4);
			int target2 = random.nextInt(4);
			for (int t = 0; t < 22; t++) {
				seq[t] = random.nextInt(4) + 4;//+4 so as not to overlap with target symbols
			}
			int loc1 = random.nextInt(22);
			int loc2 = random.nextInt(22);
			while (loc1 == loc2)
				loc2 = random.nextInt(22);
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
}