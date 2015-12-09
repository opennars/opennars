package nars.util;

import java.util.function.DoubleConsumer;

/**
 * DoubleSummaryStatistics in java.util can't be cleared
 * wtf
 * anyway we'll add stdev calcluation to this and it will
 * serve as a lighter weight replacement for Apache
 * Commons Math SummaryStatistics which also is undesirable
 *
 */
public class DoubleSummaryReusableStatistics implements DoubleConsumer {
    private long count = 0L;
    private double sum = 0.0;
    //private double sumCompensation; // Low order bits of sum
//    private double simpleSum; // Used to compute right sum for non-finite inputs
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;

    /**
     * Construct an empty instance with zero count, zero sum,
     * {@code Double.POSITIVE_INFINITY} min, {@code Double.NEGATIVE_INFINITY}
     * max and zero average.
     */
    public DoubleSummaryReusableStatistics() {
        clear();
    }

    public void clear() {
        count = 0;
        sum = 0;
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
    }
    /**
     * Records another value into the summary information.
     *
     * @param value the input value
     */
    @Override
    public final void accept(double value) {
        ++count;
        //simpleSum += value;
        sum += value;
        //sumWithCompensation(value);
        if (min > value) min = value;
        if (max < value) max = value;
    }

//    /**
//     * Incorporate a new double value using Kahan summation /
//     * compensated summation.
//     */
//    private final void sumWithCompensation(double value) {
//        double tmp = value - sumCompensation;
//        double velvel = sum + tmp; // Little wolf of rounding error
//        sumCompensation = (velvel - sum) - tmp;
//        sum = velvel;
//    }

    /**
     * Return the count of values recorded.
     *
     * @return the count of values
     */
    public final long getCount() {
        return count;
    }

    /**
     * Returns the sum of values recorded, or zero if no values have been
     * recorded.
     *
     * If any recorded value is a NaN or the sum is at any point a NaN
     * then the sum will be NaN.
     *
     * <p> The value of a floating-point sum is a function both of the
     * input values as well as the order of addition operations. The
     * order of addition operations of this method is intentionally
     * not defined to allow for implementation flexibility to improve
     * the speed and accuracy of the computed result.
     *
     * In particular, this method may be implemented using compensated
     * summation or other technique to reduce the error bound in the
     * numerical sum compared to a simple summation of {@code double}
     * values.
     *
     * @apiNote Values sorted by increasing absolute magnitude tend to yield
     * more accurate results.
     *
     * @return the sum of values, or zero if none
     */
    public final double getSum() {
        return sum;

//        // Better error bounds to add both terms as the final sum
//        double tmp =  sum + sumCompensation;
//        if (Double.isNaN(tmp) && Double.isInfinite(simpleSum))
//            // If the compensated sum is spuriously NaN from
//            // accumulating one or more same-signed infinite values,
//            // return the correctly-signed infinity stored in
//            // simpleSum.
//            return simpleSum;
//        else
//            return tmp;
    }

    /**
     * Returns the minimum recorded value, {@code Double.NaN} if any recorded
     * value was NaN or {@code Double.POSITIVE_INFINITY} if no values were
     * recorded. Unlike the numerical comparison operators, this method
     * considers negative zero to be strictly smaller than positive zero.
     *
     * @return the minimum recorded value, {@code Double.NaN} if any recorded
     * value was NaN or {@code Double.POSITIVE_INFINITY} if no values were
     * recorded
     */
    public final double getMin() {
        return min;
    }

    /**
     * Returns the maximum recorded value, {@code Double.NaN} if any recorded
     * value was NaN or {@code Double.NEGATIVE_INFINITY} if no values were
     * recorded. Unlike the numerical comparison operators, this method
     * considers negative zero to be strictly smaller than positive zero.
     *
     * @return the maximum recorded value, {@code Double.NaN} if any recorded
     * value was NaN or {@code Double.NEGATIVE_INFINITY} if no values were
     * recorded
     */
    public final double getMax() {
        return max;
    }

    /**
     * Returns the arithmetic mean of values recorded, or zero if no
     * values have been recorded.
     *
     * If any recorded value is a NaN or the sum is at any point a NaN
     * then the average will be code NaN.
     *
     * <p>The average returned can vary depending upon the order in
     * which values are recorded.
     *
     * This method may be implemented using compensated summation or
     * other technique to reduce the error bound in the {@link #getSum
     * numerical sum} used to compute the average.
     *
     * @apiNote Values sorted by increasing absolute magnitude tend to yield
     * more accurate results.
     *
     * @return the arithmetic mean of values, or zero if none
     */
    public final double getAverage() {
        return getCount() > 0 ? getSum() / getCount() : 0.0d;
    }

    /**
     * {@inheritDoc}
     *
     * Returns a non-empty string representation of this object suitable for
     * debugging. The exact presentation format is unspecified and may vary
     * between implementations and versions.
     */
    @Override
    public String toString() {
        return String.format(
                "%s{n=%d, sum=%f, min=%f, avg=%f, max=%f}",
                getClass().getSimpleName(),
                getCount(),
                getSum(),
                getMin(),
                getAverage(),
                getMax());
    }

    public final double normalize(double n) {
        double min = getMin();
        double max = getMax();
        if (min == max) return n;

        return (n - min) / (max-min);
    }

}
