package jurls.reinforcementlearning.domains.grid;


/*
    Simplest one-dimensional grid task
    
    In this task, the agent's goal is to activate the same pattern as
    it sees in its sensor field.
*/
public class Grid1DSimple implements World {
    private final int size;
    
    private final double VISUALIZE_PERIOD;
    private final double REWARD_MAGNITUDE;
    private final double JUMP_FRACTION;
    private final double ENERGY_COST_FACTOR;
    private final double MATCH_REWARD_FACTOR;

    private double focusPosition;
    private final double focusVelocity;
        
    private double[] action;

    private final int totalTime;    
    private int time;
    
    private final double noise;

    public Grid1DSimple(int size, int totalTime, double noise, double focusVelocity) {
        time = 1;
        this.size = size;
        VISUALIZE_PERIOD = Math.pow(10, 4);
        ENERGY_COST_FACTOR = 0.5;
        MATCH_REWARD_FACTOR = size*1.1;
        REWARD_MAGNITUDE = 1;
        JUMP_FRACTION = 0.0;
        this.noise = noise;
        this.focusVelocity = focusVelocity;

        focusPosition = size/2;
        this.totalTime = totalTime;
    }

    
    @Override    public String getName()    {     return "Grid1D";    }
    @Override    public int getNumSensors() {     return size;    }
    @Override    public int getNumActions() {     return size;    }
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
        if (focusPosition > size) focusPosition = focusPosition - size;
        if (focusPosition < 0) focusPosition = size + focusPosition;
        
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
                  
        double match = 0;        
        double energyCost = 0;
        for (int i = 0; i < size; i++) {
            match += Math.abs( sensor[i] * action[i] );
            energyCost += action[i];
        }
        
        
        double reward = REWARD_MAGNITUDE * ((MATCH_REWARD_FACTOR * match) - (energyCost * ENERGY_COST_FACTOR));
        
        
        
        double min=0, max=0;
        for (int i = 0; i < size; i++) {
            double exp = 3.0; //sharpen
            sensor[i] = Math.pow(1.0 / (1.0 + Math.abs( (i)-focusPosition)),exp) + (Math.random()*noise);
            if (sensor[i] < 0)
                sensor[i] = 0;
            if (i == 0) {
                min = max = sensor[0];
            }
            else {
                if (sensor[i] < min) min = sensor[i];
                if (sensor[i] > max) max = sensor[i];
            }
            
        }
        /*
        //normalize
        for (int i = 0; i < size; i++) {
            sensor[i] = (max-min)*(sensor[i] - min);
        }
        */        
                
        return reward;        
    }
        

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < size; i++) {
            char c;
            c = i == (int) focusPosition ? 'O' : '.';
            s += c;
        }
        s += "\n";
        for (int i = 0; i < size; i++) {
            char c;
            c = action[i] > 0 ? 'X' : '.';
            s += c;
        }
        s += "\n";
        return s;
    }
    
    /*
    def visualize(self, agent):
        """ Show what's going on in the world """
        if (this.display_state):
            state_image = ['.'] * (this.num_sensors + this.num_actions + 2)
            state_image[this.simple_state] = 'O'
            state_image[this.num_sensors:this.num_sensors + 2] = '||'
            action_index = np.where(this.action > 0.1)[0]
            if action_index.size > 0:
                for i in range(action_index.size):
                    state_image[this.num_sensors + 2 + action_index[i]] = 'x'
            print(''.join(state_image))
            
        if (this.timestep % this.VISUALIZE_PERIOD) == 0:
            print("world age is %s timesteps " % this.timestep)    
    */

    /*
    
    def set_agent_parameters(self, agent):
    """ Turn a few of the knobs to adjust BECCA for this world """
    # Prevent the agent from forming any groups
    #agent.reward_min = -100.
    #agent.reward_max = 100.
    pass
     */
    
    public static void main(String[] args) throws Exception {
        //Class<? extends Agent> a = BeccaAgent.class;
        //Class<? extends Agent> a = QLAgent.class;
        Class<? extends Agent> a = RandomAgent.class;
        
        new Simulation(a, new Grid1DSimple(16, 11990000, 0.05, 0.01));
        
    }
}
