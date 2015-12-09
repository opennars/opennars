/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 * @author thorsten
 */
public class WeightedInterpolationFunction implements ParameterizedFunction {

    private final double gravity = 0.00001;
    private final double decay = 0.1;
    private final int numPoints;
    private final double power;
    private double minOutput = Double.POSITIVE_INFINITY;
    private double maxOutput = Double.NEGATIVE_INFINITY;
    private final ArrayRealVector minInput = new ArrayRealVector(numberOfInputs());
    private final ArrayRealVector maxInput = new ArrayRealVector(numberOfInputs());
    private final Point[] points;

//    @Override
//    public double getParameter(int i) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void setParameter(int i, double v) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    private class Point {

        final ArrayRealVector xs = new ArrayRealVector(numberOfInputs());
        final ArrayRealVector velocity = new ArrayRealVector(numberOfInputs());
        double y = 0;

        public Point() {
            for (int i = 0; i < numberOfInputs(); ++i) {
                xs.setEntry(i, Math.random());
            }
        }
    }

    public WeightedInterpolationFunction(int numInputs, int numPoints, double power) {
        this.numPoints = numPoints;
        this.power = power;
        minInput.set(Double.POSITIVE_INFINITY);
        maxInput.set(Double.NEGATIVE_INFINITY);
        points = new Point[numPoints];
        for (int i = 0; i < numPoints; ++i) {
            points[i] = new Point();
        }
    }

    private double weight(ArrayRealVector a, ArrayRealVector b) {
        double d = a.getL1Distance(b);
        return Math.pow(1 / (1 + d), power);
    }

    private void adjustBorders(double[] xs) {
        for (int i = 0; i < numberOfInputs(); ++i) {
            if (xs[i] < minInput.getEntry(i)) {
                minInput.setEntry(i, xs[i]);
            }
            if (xs[i] > maxInput.getEntry(i)) {
                maxInput.setEntry(i, xs[i]);
            }
        }
    }

    @Override
    public double value(double[] xs) {
        adjustBorders(xs);

        ArrayRealVector xs3 = new ArrayRealVector(xs);
        double y = 0;
        double sumOfWeights = 0;

        for (Point p : points) {
            sumOfWeights += weight(p.xs, xs3);
        }

        if (sumOfWeights == 0) {
            sumOfWeights = 1;
        }

        for (Point p : points) {
            y += weight(p.xs, xs3) * p.y;
        }

        y /= sumOfWeights;

        if (y > maxOutput) {
            maxOutput = y;
        }

        if (y < minOutput) {
            minOutput = y;
        }

        return y;
    }

    @Override
    public void parameterGradient(double[] output, double... xs) {

    }

    @Override
    public void addToParameters(double[] deltas) {

    }

    @Override
    public void learn(double[] xs, double y) {
        adjustBorders(xs);

        ArrayRealVector xs3 = new ArrayRealVector(xs);

        double min = Double.POSITIVE_INFINITY;
        Point nearest = null;

        for (Point p : points) {
            double d = p.xs.getL1Distance(xs3);
            if (d < min) {
                min = d;
                nearest = p;
            }
        }

        nearest.xs.setSubVector(0, xs3);
        nearest.y = y;

        //move the points away from each other to get a homogenous distribution
        for (Point a : points) {
            for (Point b : points) {
                if (a != b) {
                    ArrayRealVector d = b.xs.subtract(a.xs);
                    double l = d.getL1Norm();
                    if (l == 0) {
                        l = 1;
                    }
                    d.mapMultiplyToSelf(gravity / l / l / l);
                    b.velocity.setSubVector(0, b.velocity.add(d));
                }
            }
        }

        for (Point p : points) {
            p.velocity.mapMultiplyToSelf(decay);
            p.xs.setSubVector(0, p.xs.add(p.velocity));

            for (int i = 0; i < numberOfInputs(); ++i) {
                if (p.xs.getEntry(i) > maxInput.getEntry(i)) {
                    p.xs.setEntry(i, maxInput.getEntry(i));
                    p.velocity.setEntry(i, 0);
                }
                if (p.xs.getEntry(i) < minInput.getEntry(i)) {
                    p.xs.setEntry(i, minInput.getEntry(i));
                    p.velocity.setEntry(i, 0);
                }
            }
            
            p.y = value(p.xs.getDataRef());
        }
    }

    @Override
    public int numberOfParameters() {
        return numPoints;
    }

    @Override
    public int numberOfInputs() {
        return 0;
    }

    @Override
    public double maxOutputDebug() {
        return maxOutput;
    }

    @Override
    public double minOutputDebug() {
        return minOutput;
    }

}
