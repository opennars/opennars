package jurls.reinforcementlearning.domains.grid;


public class Grid2DSimple implements World {
    private final int size;
    
    private final double VISUALIZE_PERIOD;
    private final double REWARD_MAGNITUDE;
    private final double JUMP_FRACTION;
    private final double ENERGY_COST_FACTOR;
    private final double MATCH_REWARD_FACTOR;

        
    private double[] action;

    private final int totalTime;    
    private int time;
    
    private final double noise;
    private final int w;
    private final int h;
    private double focusPositionW;
    private double focusPositionH;

    public Grid2DSimple(int x, int y, int totalTime, double noise, double focusVelocity) {
        time = 1;
        w = x;
        h = y;
        size = x * y;
        VISUALIZE_PERIOD = Math.pow(10, 4);
        ENERGY_COST_FACTOR = 1.0;
        MATCH_REWARD_FACTOR = size*1.0;
        REWARD_MAGNITUDE = 1;
        JUMP_FRACTION = 0.1;
        this.noise = noise;
        
        this.totalTime = totalTime;
    }

    
    @Override    public String getName()    {     return "Grid1D";    }
    @Override    public int getNumSensors() {     return size;    }
    @Override    public int getNumActions() {     return size;    }
    @Override    public boolean isActive()  {     return time < totalTime;   }

    double[] action2 = null;

    double get(double[] d, int x, int y) {
        return d[y * h + x];
    }
    void set(double[] d, int x, int y, double v) {
        d[y * h + x] = v;
    }
    
    @Override
    public double step(double[] action, double[] sensor) {

        time++;
        
        this.action = action;
        
        //# At random intervals, jump to a random position in the world
        if (Math.random() < JUMP_FRACTION) {
            focusPositionW = w * Math.random();
            focusPositionH = h * Math.random();
        }
        
        
    
                  
        double match = 0;        
        double energyCost = 0;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                double a = get(action, x, y);
                if (a > 0) {                    
                    double dx = Math.abs(x - focusPositionW);
                    double dy = Math.abs(y - focusPositionH);
                    match += a * 1.0/(1.0+Math.sqrt(dx*dx + dy*dy));
                    energyCost += a;                    
                }
            }
        }
        
        double reward = REWARD_MAGNITUDE * ((MATCH_REWARD_FACTOR * match) - (energyCost * ENERGY_COST_FACTOR));
        
        /*if (reward!=0)
            System.out.println(match + " " + energyCost + " -> " + reward);*/
        
        
        
        double exp = 3.0; //sharpen
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                double dx = Math.abs(x - focusPositionW);
                double dy = Math.abs(y - focusPositionH);
                double distScale = 1.0/(1.0+Math.sqrt(dx*dx + dy*dy));
                double v = Math.pow(distScale, exp);
                if (v < 0.1) v = 0;
                set(sensor, x, y, v);
            }
        }
        
                
        return reward;        
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
        Class<? extends Agent> a = RandomAgent.class;
        //Class<? extends Agent> a = QLAgent.class;
        
        new Simulation(a, new Grid2DSimple(4,4, 11990000, 0.01, 0.005));
        
    }
}
