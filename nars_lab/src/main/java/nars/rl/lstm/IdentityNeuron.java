package nars.rl.lstm;

public class IdentityNeuron extends Neuron
{
	@Override
	public double Activate(double x) 
	{
		return x;
	}

	@Override
	public double Derivative(double x) {
		// TODO Auto-generated method stub
		return 1;
	}
}

