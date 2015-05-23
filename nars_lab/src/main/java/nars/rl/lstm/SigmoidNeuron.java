package nars.rl.lstm;

import org.apache.commons.math3.util.FastMath;

public class SigmoidNeuron extends Neuron
{
	@Override
	final public double Activate(final double x) {
		return 1.0 / (1.0 + FastMath.exp(-x));
	}

	@Override
	final public double Derivative(final double x) {
		double act = Activate(x);
		return act * (1.0 - act);
	}

	
}
