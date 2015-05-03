package nars.rl.horde.math;


public class IncMeanVarNormalizer implements Normalizer, MeanVar {
    private static final long serialVersionUID = -7117059874975759612L;
    private final int minNbUpdate;
    private final IncrementalAverage average = new IncrementalAverage();

    public IncMeanVarNormalizer() {
        this(5);
    }

    public IncMeanVarNormalizer(int minNbUpdate) {
        this.minNbUpdate = minNbUpdate;
    }

    @Override
    final public double normalize(double x) {
        if (average.nbSample() < minNbUpdate)
            return 0.0;
        if (average.var() == 0.0)
            return 0.0;
        return (x - average.mean()) / Math.sqrt(average.var());
    }

    @Override
    public void update(double x) {
        average.update(x);
    }

    @Override
    public IncMeanVarNormalizer newInstance() {
        return new IncMeanVarNormalizer(minNbUpdate);
    }

    @Override
    public double mean() {
        return average.mean();
    }

    @Override
    public double var() {
        return average.var();
    }

    public IncrementalAverage average() {
        return average;
    }
}
