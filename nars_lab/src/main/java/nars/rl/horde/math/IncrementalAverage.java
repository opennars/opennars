package nars.rl.horde.math;


public class IncrementalAverage implements MeanVar {
    private static final long serialVersionUID = -5821860314203393858L;
    private double mean = 0.0;
    private double var = 1.0;
    private int n = 0;
    private double m2 = 0.0;

    // http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#On-line_algorithm
    @Override
    public void update(double x) {
        n++;
        double delta = x - mean;
        mean = mean + delta / n;
        m2 = m2 + delta * (x - mean);
        if (n > 1)
            var = variance(1);
    }

    public int nbSample() {
        return n;
    }

    @Override
    public double mean() {
        return mean;
    }

    @Override
    public double var() {
        return var;
    }

    public double variance(int ddf) {
        assert ddf >= 0;
        return m2 / (n - ddf);
    }

    public double stdError() {
        return Math.sqrt(variance(1));
    }

    @Override
    public MeanVar newInstance() {
        return new IncrementalAverage();
    }
}
