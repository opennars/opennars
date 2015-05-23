package nars.rl.lstm;

import org.apache.commons.math3.util.FastMath;

public class TanhNeuron extends Neuron
{
	@Override
	final public double Activate(final double x) {
		return FastMath.tanh(x);
	}

	@Override
	final public double Derivative(final double x) {
		double coshx = FastMath.cosh(x);
		double denom = (FastMath.cosh(2.0*x) + 1.0);
		return 4.0 * coshx * coshx / (denom * denom);
	}

	
}
