package nars.rl.lstm;

import java.util.Arrays;
import java.util.Random;

public class SimpleLSTM implements IAgentSupervised
{
	
	private double init_weight_range = 0.1;
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
	
	public static double learningRate = 0.07;//0.07

	private double[] sumF;
	private double[] actF;
	private double[] sumG;
	private double[] actG;
	private double[] actH;
	private double[] full_hidden;
	private double[] output;
	private double[] deltaOutput;
	private double[] deltaH;

	public SimpleLSTM(Random r, int input_dimension, int output_dimension, int cell_blocks)
	{
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
				weightsF[j][i] = (r.nextDouble() * 2 - 1) * init_weight_range;
				weightsG[j][i] = (r.nextDouble() * 2 - 1) * init_weight_range;
			}
		}
		
		weightsOut = new double[output_dimension][cell_blocks + 1];
		
		for (int j = 0; j < cell_blocks + 1; j++) {
			for (int k = 0; k < output_dimension; k++)
				weightsOut[k][j] = (r.nextDouble() * 2 - 1) * init_weight_range;
		}
	}
	
	public void Reset()
	{
		for (int c = 0; c < context.length; c++)
			context[c] = 0.0;
		//reset accumulated partials
		for (int c = 0; c < cell_blocks; c++) {
			for (int i = 0; i < full_input_dimension; i++) {
				this.dSdG[c][i] = 0;
				this.dSdF[c][i] = 0;
			}
		}
	}
	
	public double[] Next(double[] input)
	{
		return Next(input, null);
	}
	
	public void Display()
	{
		System.out.println("==============================");
		System.out.println("DAGate: todo...");
		System.out.println("\n==============================");
	}
	
	public double[] Next(double[] input, double[] target_output) {
		
		//setup input vector
		double[] full_input = new double[full_input_dimension];
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
			actF[j] = F.Activate(sumF[j]);
			actG[j] = G.Activate(sumG[j]);
			actH[j] = actF[j] * context[j] + (1 - actF[j]) * actG[j];
		}
		
		//prepare hidden layer plus bias
		Arrays.fill(full_hidden, 0);

		loc = 0;
		for (int j = 0; j < cell_blocks; j++)
			full_hidden[loc++] = actH[j];
		full_hidden[loc++] = 1.0; //bias
		
		//calculate output

		Arrays.fill(output, 0);
		for (int k = 0; k < output_dimension; k++)
		{
			for (int j = 0; j < cell_blocks + 1; j++)
				output[k] += weightsOut[k][j] * full_hidden[j];
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
			
			for (int i = 0; i < full_input_dimension; i++) {
				
				double prevdSdF = dSdF[j][i];
				double prevdSdG = dSdG[j][i];
				double in = full_input[i];
				
				dSdG[j][i] = ((1 - f)*dg*in) + (f*prevdSdG);
				dSdF[j][i] = ((h_- g)*df*in) + (f*prevdSdF);
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
				deltaOutput[k] = (target_output[k] - output[k]) * SCALE_OUTPUT_DELTA;
				for (int j = 0; j < cell_blocks; j++) {
					deltaH[j] += deltaOutput[k] * weightsOut[k][j];
					weightsOut[k][j] += deltaOutput[k] * actH[j] * learningRate;
				}
				//bias
				weightsOut[k][cell_blocks] += deltaOutput[k] * 1.0 * learningRate;
			}
			
			//input to hidden
			for (int j = 0; j < cell_blocks; j++) {
				for (int i = 0; i < full_input_dimension; i++) {
					weightsF[j][i] += deltaH[j] * dSdF[j][i] * learningRate;
					weightsG[j][i] += deltaH[j] * dSdG[j][i] * learningRate;
				}
			}
		}
		
		//////////////////////////////////////////////////////////////
		
		//roll-over context to next time step
		for (int j = 0; j < cell_blocks; j++) {
			context[j] = actH[j];
		}
		
		//give results
		return output;
	}
}


