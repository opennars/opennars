package nars.rl.horde.math;


import nars.Global;

import java.io.Serializable;
import java.util.Random;

public class MinMaxNormalizer implements Normalizer {

    public static class Range implements Serializable {
        private static final long serialVersionUID = 3076267038250925863L;
        private double min;
        private double max;

        public Range() {
            this(Double.MAX_VALUE, -Double.MAX_VALUE);
        }


        public Range(Range range) {
            this(range.min, range.max);
        }

        public Range(double min, double max) {
            this.min = min;
            this.max = max;
        }

        public double bound(double value) {
            return Math.max(min, Math.min(max, value));
        }

        public double choose(Random random) {
            return random.nextFloat() * length() + min;
        }

        public boolean in(double value) {
            return value >= min && value <= max;
        }

        @Override
        public boolean equals(Object object) {
            if (super.equals(object))
                return true;
            Range other = (Range) object;
            return Global.equals(min, other.min) && Global.equals(max, other.max);
        }

        @Override
        public int hashCode() {
            return (int)(min * 31 + max);
        }

        public double length() {
            return max - min;
        }

        public double[] sample(final int nbValue) {
            double[] values = new double[nbValue];
            double sampleSize = (max - min) / nbValue;
            for (int i = 0; i < values.length; i++)
                values[i] = i * sampleSize + min;
            return values;
        }

        public double center() {
            return min + (length() / 2);
        }

        @Override
        public String toString() {
            return String.format("[%f,%f]", min, max);
        }

        public void update(double value) {
            min = Math.min(value, min);
            max = Math.max(value, max);
        }

        public double min() {
            return min;
        }

        public double max() {
            return max;
        }

        public void reset() {
            min = Double.MAX_VALUE;
            max = -Double.MAX_VALUE;
        }
    }

    private static final long serialVersionUID = 4495161964136798707L;
    public final static double MIN = -1;
    public final static double MAX = 1;
    private int nbUpdate = 0;
    private final Range targetRange;
    private final Range valueRange = new Range();

    public MinMaxNormalizer() {
        this(new Range(MIN, MAX));
    }

    public MinMaxNormalizer(Range range) {
        this.targetRange = range;
    }

    @Override
    public double normalize(double x) {
        if (valueRange.length() == 0 || nbUpdate == 0)
            return 0;
        double scaled = Math.max(0.0, Math.min(1.0, (x - valueRange.min()) / valueRange.length()));
        return scaled * targetRange.length() + targetRange.min();
    }

    public float normalize(float x) {
        return (float) normalize((double) x);
    }

    @Override
    public void update(double x) {
        valueRange.update(x);
        nbUpdate++;
    }

    @Override
    public MinMaxNormalizer newInstance() {
        return new MinMaxNormalizer(targetRange);
    }

    public void reset() {
        valueRange.reset();
        nbUpdate = 0;
    }

    public Range targetRange() {
        return targetRange;
    }

    public Range valueRange() {
        return valueRange;
    }
}
