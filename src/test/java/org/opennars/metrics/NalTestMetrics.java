package org.opennars.metrics;

import org.opennars.core.NALTest;

/**
 * Computes the metrics of NAL-tests.
 *
 * Metrics are numeric values which indicate how fast NARS could solve problems
 */
public class NalTestMetrics {
    public static void main(String[] args) {
        // number of samples was guessed and not computed with probability theory
        // TODO< maybe we need to compute it with probability theory to make sure that we test enough and not to much for a given error margin >
        int numberOfSamples = 50;

        // we are only in multistep problems interested
        NALTest.directories = new String[]{"/nal/multi_step/", "/nal/application/"};
        NALTest.numberOfSamples = numberOfSamples;

        NALTest.runTests(NALTest.class);


        int here = 5;
        // TODO< compute median of samples >
    }
}
