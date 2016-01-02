package jurls.reinforcementlearning.domains.grid;

/*
    Simplest one-dimensional grid task
    
    In this task, the agent's goal is to activate the same pattern as
    it sees in its sensor field.

    It has 3 outputs: move left, move right, or neither.  it is shown its position in a copy of the sensor field

*/
public class Grid1DRelative implements World {
    private final int size;
    
    private final double VISUALIZE_PERIOD;
    private final double REWARD_MAGNITUDE;
    private final double JUMP_FRACTION;
    private final double ENERGY_COST_FACTOR;
    private final double MATCH_REWARD_FACTOR;

    private double position;
    private double focusPosition;
    private final double focusVelocity;
        
    private double[] action;

    private final int totalTime;    
    private int time;
    
    private final double noise;

    public Grid1DRelative(int size, int totalTime, double noise, double focusVelocity) {
        time = 1;
        this.size = size;
        VISUALIZE_PERIOD = Math.pow(10, 4);
        ENERGY_COST_FACTOR = 0.01;
        MATCH_REWARD_FACTOR = 1.0;
        REWARD_MAGNITUDE = 1;
        JUMP_FRACTION = 0.002;
        this.noise = noise;
        this.focusVelocity = focusVelocity;

        focusPosition = size/2;
        this.totalTime = totalTime;
    }

    
    @Override    public String getName()    {     return "Grid1D";    }
    @Override    public int getNumSensors() {     return size*2;    }
    @Override    public int getNumActions() {     return 2;    }
    @Override    public boolean isActive()  {     return time < totalTime;   }

    double[] action2 = null;
    
    @Override
    public double step(double[] action, double[] sensor) {

        time++;
        
        this.action = action;
        
        //# At random intervals, jump to a random position in the world
        if (Math.random() < JUMP_FRACTION) {
            focusPosition = size * Math.random();
        }
        else {            
            focusPosition += focusVelocity;
        }
        
        //# Ensure that the world state falls between 0 and 9
        if (focusPosition > size) focusPosition = 0;
        if (focusPosition < 0) focusPosition = size-1;
        
        /*        
        # Assign basic_feature_input elements as binary. 
        # Represent the presence or absence of the current position in the bin.
        */

        //blur the action
        /*if (action2 == null) action2 = new double[action.length];
        for (int i = 0; i < action2.length; i++) {
            action2[i] = action[i];
            if (i > 0) action2[i] += 0.5 * action[i-1];
            if (i < action2.length-1) action2[i] += 0.5 * action[i+1];
        } */           
            
        
        /*if (action[0] > 0.5) {
            //nothing
        }*/
        if ((action[0] > 0.5) && !(action[1] > 0.5)) {
            position--;
        }
        if ((action[1] > 0.5) && !(action[0] > 0.5)) {
            position++;
        }
        if (position < 0) position = size-1;
        if (position >= size) position = 0;
        
        double match = 0;        
        double energyCost = 0;
        
        match = 1.0 / (1.0 + Math.abs(position - focusPosition));
        
        if (action[0] > 0.25)
            energyCost += 1.0;
        if (action[1] > 0.25)
            energyCost += 1.0;
        
        
        double reward = REWARD_MAGNITUDE * ((MATCH_REWARD_FACTOR * match) - (energyCost * ENERGY_COST_FACTOR));
        
        
        
        double min=0, max=0;
        for (int i = 0; i < size; i++) {
            double exp = 2.0; //sharpen
            sensor[i] = Math.pow(1.0 / (1.0 + Math.abs( (i)-focusPosition)),exp) + (Math.random()*noise);
            if (sensor[i] < 0.2)
                sensor[i] = 0;
            if (i == 0) {
                min = max = sensor[0];
            }
            else {
                if (sensor[i] < min) min = sensor[i];
                if (sensor[i] > max) max = sensor[i];
            }
         
            //show agent's own location
            sensor[i+size] = (i == position) ? 1.0 : 0.0;
        }
  
                
        return reward;        
    }
        
    
    public static void main(String[] args) throws Exception {
        Class<? extends Agent> a = RandomAgent.class;
        //Class<? extends Agent> a = QLAgent.class;
        
        new Simulation(a, new Grid1DRelative(8, 11990000, 0.05, 0.01));
        
    }
}
