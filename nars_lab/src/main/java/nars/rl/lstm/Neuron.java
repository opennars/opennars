package nars.rl.lstm;

import nars.rl.lstm.IdentityNeuron;

public abstract class Neuron
{
	public static Neuron Factory(NeuronType neuron_type)
	{
		if (neuron_type == NeuronType.Sigmoid)
			return new SigmoidNeuron();
		else if (neuron_type == NeuronType.Identity)
			return new IdentityNeuron();
		else if (neuron_type == NeuronType.Tanh)
			return new TanhNeuron();
		else
			System.out.println("ERROR: unknown neuron type");
		return null;
	}
	
	abstract public double Activate(double x);
	abstract public double Derivative(double x);
}
