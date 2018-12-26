package org.opennars.metrics;

import org.opennars.core.NALTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Computes the metrics of NAL-tests.
 *
 * Metrics are numeric values which indicate how fast NARS could solve problems
 */
public class NalTestMetrics {
    public static double computeMetric(final Map<String, List<Double>> scores) {
        double metric = 0;

        // compute median of (valid) samples
        for (List<Double> iValues : scores.values()) {
            // remove infinities because they indicate failed tests and would mess up the metric
            final List<Double> valuesWithoutInfinities = removeInfinities(iValues);

            final double medianOfThisTest = calcMedian(valuesWithoutInfinities);

            metric += medianOfThisTest;
        }

        // average of all medians should be fine
        metric /= scores.values().size();

        return metric;
    }

    // helper
    public static List<Double> removeInfinities(final List<Double> values) {
        List<Double> result = new ArrayList<>();

        for (double iValue : values) {
            if (iValue != Double.POSITIVE_INFINITY) {
                result.add(iValue);
            }
        }

        return result;
    }

    // helper
    public static double calcMedian(final List<Double> values) {
        return values.get(values.size()/2);
    }

    public static void main(String[] args) {
        // number of samples was guessed and not computed with probability theory
        // TODO< maybe we need to compute it with probability theory to make sure that we test enough and not to much for a given error margin >
        int numberOfSamples = 50;

        // we are only in multistep problems interested
        NALTest.directories = new String[]{"/nal/multi_step/", "/nal/application/"};
        NALTest.numberOfSamples = numberOfSamples;

        NALTest.runTests(NALTest.class);

        final double metric = computeMetric(NALTest.scores);
        System.out.println("metric=" + Double.toString(metric));
        int debugHere = 5;
    }
}
