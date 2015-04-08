package jurls.reinforcementlearning.domains.grid;

import java.util.Arrays;

public class RandomAgent implements Agent {
    private double[] action;
    private double[] sensor;

    @Override
    public int step(double reward) {
        Arrays.fill(action, 0);
        action[(int)(Math.random()*action.length)] = 1.0;
        return 0;
    }

    @Override    public double[] getSensor() { return sensor;    }
    @Override    public double[] getAction() { return action;     }

    @Override
    public void init(World world) {
        int actions = world.getNumActions();
        int sensors = world.getNumSensors();
        action = new double[actions];
        sensor = new double[sensors];
    }
    
}
