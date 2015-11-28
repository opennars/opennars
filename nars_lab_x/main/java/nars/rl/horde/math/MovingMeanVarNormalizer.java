package nars.rl.horde.math;


import static nars.rl.horde.math.MovingAverage.timeStepsToDiscount;


public class MovingMeanVarNormalizer implements Normalizer, MeanVar {
    private static final long serialVersionUID = -1340053804929435288L;
    private double mean = 0.0;
    private double var = 1.0;
    private double c = 0.0;
    private final int trackingSpeed;
    private final double alpha;

    public MovingMeanVarNormalizer(int trackingSpeed) {
        this.trackingSpeed = trackingSpeed;
        this.alpha = 1 - timeStepsToDiscount(trackingSpeed);
    }

    @Override
    final public double normalize(double x) {
        if (var == 0.0)
            return 0.0;
        return ((x - mean) / Math.sqrt(var)) * c;
    }

    @Override
    public void update(double x) {
        double delta = x - mean;
        mean = mean + alpha * delta;
        var = var + alpha * ((x - mean) * (x - mean) - var);
        c = c + alpha * (1 - c);
    }

    @Override
    public double mean() {
        return mean;
    }

    @Override
    public double var() {
        return var;
    }

    @Override
    public MovingMeanVarNormalizer newInstance() {
        return new MovingMeanVarNormalizer(trackingSpeed);
    }
}
