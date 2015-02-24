package jurls.reinforcementlearning.domains.grid;


public interface World {

    public String getName();
    
    public int getNumSensors();
    public int getNumActions();

    public boolean isActive();
    
    /**     
     * @param actions input actions
     * @param sensors outpt sensors
     * @return reward
     */
    public double step(double[] action, double[] sensor);
}
