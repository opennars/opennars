package nars.rl.lstm;

public abstract class AgentSupervised {
	public final static class NonResetInteraction {
		public double[] observation;
		public double[] target_output;
	}

	public abstract void clear();
	public abstract double[] learn(double[] input, double[] target_output, final boolean requireOutput) throws Exception;
	public abstract double[] predict(double[] input, final boolean requireOutput) throws Exception;
}
