package nars.rl.lstm;

public class TanhNeuron extends Neuron
{
	@Override
	public double Activate(double x) {
		return Math.tanh(x);
	}

	@Override
	public double Derivative(double x) {
		double coshx = Math.cosh(x);
		double denom = (Math.cosh(2*x) + 1);
		return 4 * coshx * coshx / (denom * denom);
	}

	
}
