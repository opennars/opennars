package nars.rl.lstm.opencl;

import nars.rl.lstm.DistractedSequenceRecall;
import nars.rl.lstm.SimpleLSTM;
import objenome.util.random.XORShiftRandom;

import java.util.Random;

public class TestCL {
	public static void main(String[] args) throws Exception {
		
		System.out.println("Test of SimpleLSTM\n");
		
		Random r = new XORShiftRandom(1234);
		DistractedSequenceRecall task = new DistractedSequenceRecall(r);

		int cell_blocks = 8;
		double learningRate = 0.07;
		LSTMCL slstm = new LSTMCL(r,
				task.getInputDimension(),
				task.getOutputDimension(),
				cell_blocks,
				learningRate);
		
		for (int epoch = 0; epoch < 5000; epoch++) {
			double fit = task.EvaluateFitnessSupervised(slstm);
			if (epoch % 10 == 0)
				System.out.println("["+epoch+"] error = " + (1 - fit));
		}
		System.out.println("done.");
	}

}
