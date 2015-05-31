package nars.rl.lstm;

public interface IAgentSupervised 
{
	void clear();
	double[] learn(double[] input, double[] target_output, final boolean requireOutput) throws Exception;
	double[] predict(double[] input, final boolean requireOutput) throws Exception;
}
