package nars.rl.lstm;

public class SigmoidNeuron extends Neuron
{
	@Override
	public double Activate(double x) {
		return 1 / (1 + Math.exp(-x));
	}

	@Override
	public double Derivative(double x) {
		double act = Activate(x);
		return act * (1 - act);
	}

	
}
