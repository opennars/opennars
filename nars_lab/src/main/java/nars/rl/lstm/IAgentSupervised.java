package nars.rl.lstm;

public interface IAgentSupervised 
{
	void clear();
	double[] learn(double[] input, double[] target_output) throws Exception;
	double[] predict(double[] input) throws Exception;
}
