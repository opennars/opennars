package nars.rl.lstm;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SimpleLSTM extends AgentSupervised {
	
	private double init_weight_range = 0.1;
	public double learningRate;//0.07


	private int full_input_dimension;
	private int output_dimension;
	private int cell_blocks;
	private Neuron F;
	private Neuron G;
	
	private double [] context;
	
	private double [][] weightsF;
	private double [][] weightsG;
	private double [][] weightsOut;
	
	//partials (Need this for each output? Need to remind myself..)
	private double [][] dSdF;
	private double [][] dSdG;
	
	private NeuronType neuron_type_F = NeuronType.Sigmoid;
	private NeuronType neuron_type_G = NeuronType.Sigmoid;
	
	private double SCALE_OUTPUT_DELTA = 1.0;
	

	private double[] sumF;
	private double[] actF;
	private double[] sumG;
	private double[] actG;
	private double[] actH;
	private double[] full_hidden;
	private double[] output;
	private double[] deltaOutput;
	private double[] deltaH;
	private double[] full_input;

	public SimpleLSTM(Random r, int input_dimension, int output_dimension, int cell_blocks, final double initLearningRate)
	{
		this.learningRate = initLearningRate;
		this.output_dimension = output_dimension;
		this.cell_blocks = cell_blocks;
		
		context = new double[cell_blocks];
		
		full_input_dimension = input_dimension + cell_blocks + 1; //+1 for bias
		
		F = Neuron.Factory(neuron_type_F);
		G = Neuron.Factory(neuron_type_G);
		
		weightsF = new double[cell_blocks][full_input_dimension];
		weightsG = new double[cell_blocks][full_input_dimension];
		
		dSdF = new double[cell_blocks][full_input_dimension];
		dSdG = new double[cell_blocks][full_input_dimension];
		
		for (int i = 0; i < full_input_dimension; i++) {
			for (int j = 0; j < cell_blocks; j++) {
				weightsF[j][i] = (r.nextDouble() * 2d - 1d) * init_weight_range;
				weightsG[j][i] = (r.nextDouble() * 2d - 1d) * init_weight_range;
			}
		}
		
		weightsOut = new double[output_dimension][cell_blocks + 1];
		
		for (int j = 0; j < cell_blocks + 1; j++) {
			for (int k = 0; k < output_dimension; k++)
				weightsOut[k][j] = (r.nextDouble() * 2d - 1d) * init_weight_range;
		}
	}
	
	public void clear()
	{

		Arrays.fill(context, 0.0);

		//reset accumulated partials
		for (int c = 0; c < cell_blocks; c++) {
			Arrays.fill(this.dSdG[c], 0.0);
			Arrays.fill(this.dSdF[c], 0.0);
		}

	}
	
	public double[] predict(double[] input, final boolean requireOutput)
	{
		return learn(input, null, requireOutput);
	}
	
	public static void Display()
	{
		System.out.println("==============================");
		System.out.println("DAGate: todo...");
		System.out.println("\n==============================");
	}

	// requireOutput is unused
	public double[] learn(double[] input, double[] target_output, final boolean requireOutput) {

		final double learningRate = this.learningRate;
		final int cell_blocks = this.cell_blocks;
		final int full_input_dimension = this.full_input_dimension;

		//setup input vector


		if ((full_input == null) || (full_input.length != full_input_dimension)) {
			full_input = new double[full_input_dimension];
		}
		final double[] full_input = this.full_input;

		int loc = 0;
		for (int i = 0; i < input.length; ) {
			full_input[loc++] = input[i++];
		}
		for (int c = 0; c < context.length; ) {
			full_input[loc++] = context[c++];
		}
		full_input[loc++] = 1.0; //bias

		//cell block arrays
		if ((sumF == null) || (sumF.length!=cell_blocks)) {
			sumF = new double[cell_blocks];
			actF = new double[cell_blocks];
			sumG = new double[cell_blocks];
			actG = new double[cell_blocks];
			actH = new double[cell_blocks];
			full_hidden = new double[cell_blocks + 1];
			output = new double[output_dimension];
		}
		else {
			Arrays.fill(sumF, 0);
			Arrays.fill(actF, 0);
			Arrays.fill(sumG, 0);
			Arrays.fill(actG, 0);
			Arrays.fill(actH, 0);
		}
		final double[] full_hidden = this.full_hidden;

		//inputs to cell blocks
		for (int i = 0; i < full_input_dimension; i++)
		{
			final double fi = full_input[i];

			for (int j = 0; j < cell_blocks; j++)
			{
				sumF[j] += weightsF[j][i] * fi;
				sumG[j] += weightsG[j][i] * fi;
			}
		}
		
		for (int j = 0; j < cell_blocks; j++) {
			final double actfj = actF[j] = F.Activate(sumF[j]);
			final double actgj = actG[j] = G.Activate(sumG[j]);


			actH[j] = actfj * context[j] + (1 - actfj) * actgj;
		}
		
		//prepare hidden layer plus bias
		Arrays.fill(full_hidden, 0);


		System.arraycopy(actH, 0, full_hidden, 0, cell_blocks);
		full_hidden[cell_blocks] = 1.0; //bias
		
		//calculate output

		for (int k = 0; k < output_dimension; k++)
		{
			double s = 0;
			double wk[] = weightsOut[k];
			for (int j = 0; j < cell_blocks + 1; j++)
				s += wk[j] * full_hidden[j];

			output[k] = s;
			//output not squashed
		}

		//////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////
		//BACKPROP
		//////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////
		
		//scale partials
		for (int j = 0; j < cell_blocks; j++) {
			
			double f = actF[j];
			double df = F.Derivative(sumF[j]);
			double g = actG[j];
			double dg = G.Derivative(sumG[j]);
			double h_ = context[j]; //prev value of h

			final double[] dsg = dSdG[j];
			final double[] dsf = dSdF[j];

			for (int i = 0; i < full_input_dimension; i++) {
				
				double prevdSdF = dsf[i];
				double prevdSdG = dsg[i];
				double in = full_input[i];
				
				dsg[i] = ((1 - f)*dg*in) + (f*prevdSdG);
				dsf[i] = ((h_- g)*df*in) + (f*prevdSdF);
			}
		}
		
		if (target_output != null) {
			
			//output to hidden

			if ((deltaOutput == null) || (deltaOutput.length!=output_dimension)) {
				deltaOutput = new double[output_dimension];
				deltaH = new double[cell_blocks];
			}
			else {
				Arrays.fill(deltaOutput, 0);
				Arrays.fill(deltaH, 0);
			}

			for (int k = 0; k < output_dimension; k++) {
				final double dok  = deltaOutput[k] = (target_output[k] - output[k]) * SCALE_OUTPUT_DELTA;

				final double[] wk = weightsOut[k];

				for (int j = 0; j < cell_blocks; j++) {

					deltaH[j] += dok * wk[j];
					wk[j] += dok * actH[j] * learningRate;
				}
				//bias
				wk[cell_blocks] += dok * 1.0 * learningRate;
			}
			
			//input to hidden
			for (int j = 0; j < cell_blocks; j++) {
				final double dhj = deltaH[j];
				final double[] dsj = dSdF[j];
				final double[] dsd = dSdG[j];
				final double[] wfj = weightsF[j];
				final double[] wgj = weightsG[j];

				for (int i = 0; i < full_input_dimension; i++) {
					wfj[i] += dhj * dsj[i] * learningRate;
					wgj[i] += dhj * dsd[i] * learningRate;
				}
			}
		}
		
		//////////////////////////////////////////////////////////////
		
		//roll-over context to next time step
		System.arraycopy(actH, 0, context, 0, cell_blocks);
		
		//give results
		return output;
	}

	@Override
	public double[] learnBatch(List<NonResetInteraction> interactions, boolean requireOutput) throws Exception {
		throw new RuntimeException("TODO");
	}

	public void setLearningRate(double learningRate) {
		this.learningRate = learningRate;
	}
}


