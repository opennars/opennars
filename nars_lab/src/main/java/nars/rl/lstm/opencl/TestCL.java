package nars.rl.lstm.opencl;

import nars.rl.lstm.DistractedSequenceRecall;
import nars.util.data.random.XORShiftRandom;

import java.util.Random;

public class TestCL {
	public static void main(String[] args) throws Exception {
		
		System.out.println("Test of SimpleLSTM openCL\n");
		
		Random r = new XORShiftRandom(1234);
		DistractedSequenceRecall task = new DistractedSequenceRecall(r);

		task.batchsize = 1;

		int cell_blocks = 8;
		double learningRate = 0.07;
		LSTMCL slstm = new LSTMCL(r,
				task.getInputDimension(),
				task.getOutputDimension(),
				cell_blocks,
				learningRate);
		
		for (int epoch = 0; epoch < 5000; epoch++) {
			if (epoch % 50 == 0) {
				double fit = task.EvaluateFitnessSupervised(slstm);

				System.out.println("["+epoch+"] error = " + (1 - fit));
			}
			else {
				task.supervised(slstm);
			}
		}
		System.out.println("done.");
	}

}
