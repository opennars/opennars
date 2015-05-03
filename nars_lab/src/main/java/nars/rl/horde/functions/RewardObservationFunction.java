package nars.rl.horde.functions;


import org.apache.commons.math3.linear.RealVector;

public class RewardObservationFunction<A> implements RewardFunction, HordeUpdatable<A> {
    private static final long serialVersionUID = -5930168576876015871L;
    protected double reward;
    private final int observationIndex;
    private final String label;

    public RewardObservationFunction(int index, String label) {
        this.label = label;
        //observationIndex = legend.indexOf(label);
        observationIndex = index;
        if (observationIndex < 0)
            throw new RuntimeException(label + " not found in the legend");
    }

    public void update(RealVector o) {
        reward = o != null ? o.getEntry(observationIndex) : 0.0;
    }

    @Override
    public double reward() {
        return reward;
    }


    @Override
    public void update(RealVector o_tp1, RealVector x_t, A a_t, RealVector x_tp1) {
        update(o_tp1);
    }
}
