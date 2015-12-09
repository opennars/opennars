package jurls.reinforcementlearning.domains.grid;

/*
    Simplest one-dimensional grid task
    
    In this task, the agent's goal is to activate the same pattern as
    it sees in its sensor field.
*/
public class Minimal1 implements World {    
            
    private double[] action;

    private final int totalTime;    
    private int time;
    private final double frequency;
    

    public Minimal1(int totalTime, double frequency) {
        time = 1;
        this.totalTime = totalTime;
        this.frequency = frequency;
    }

    
    @Override    public String getName()    {     return getClass().toString();    }
    @Override    public int getNumSensors() {     return 1;    }
    @Override    public int getNumActions() {     return 1;    }
    @Override    public boolean isActive()  {     return time < totalTime;   }

    @Override
    public double step(double[] action, double[] sensor) {
        sensor[0] = Math.sin(  (time) * frequency/3.14159)*0.5 + 0.5;
        if (sensor[0] > 0.75) sensor[0] = 1.0;
        if (sensor[0] < 0.25) sensor[0] = 0;
        time++;
        return (1.0 - Math.abs(action[0] - sensor[0])) - 0.5;
    }
        

    
    public static void main(String[] args) throws Exception {
        //Class<? extends Agent> a = HubAgent.class;
        //final Class<? extends Agent> a = BeccaAgent.class;
        //Class<? extends Agent> a = QLAgent.class;
        
        Class<? extends Agent> r = RandomAgent.class;
        
        
        
        int duration = 9000000;
        double freq = 0.5;
        
        new Thread(() -> {
            try {
                new Simulation(r, new Minimal1(duration, freq));
            } catch (Exception ex) {                }
        }).start();
        /*
        new Thread(new Runnable() {
            @Override
            public void run() { 
                try {
                    new Simulation(r, new Minimal1(duration, freq));
                } catch (Exception ex) {                }
            }        
        }).start();
        */
    }
}
