package nars.rl.lstm;

public abstract class Neuron
{
	public static Neuron Factory(NeuronType neuron_type)
	{
		switch (neuron_type) {
			case Sigmoid:
				return new SigmoidNeuron();
			case Tanh:
				return new TanhNeuron();
			case Identity:
				return new IdentityNeuron();
			default:
				throw new RuntimeException("ERROR: unknown neuron type");
		}
	}
	
	abstract public double Activate(double x);
	abstract public double Derivative(double x);
}
