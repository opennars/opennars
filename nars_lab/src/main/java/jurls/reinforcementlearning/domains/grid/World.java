package jurls.reinforcementlearning.domains.grid;

public interface World {

	String getName();

	int getNumSensors();
	int getNumActions();

	boolean isActive();

	/**
	 * @param actions
	 *            input actions
	 * @param sensors
	 *            outpt sensors
	 * @return reward
	 */
	double step(double[] action, double[] sensor);
}
