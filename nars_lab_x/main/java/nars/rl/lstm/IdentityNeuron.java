package nars.rl.lstm;

public class IdentityNeuron extends Neuron
{
	@Override
	final public double Activate(double x)
	{
		return x;
	}

	@Override
	final public double Derivative(double x) {
		return 1.0;
	}
}

