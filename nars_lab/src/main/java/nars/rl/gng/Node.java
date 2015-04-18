package nars.rl.gng;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.Objects;

/**
 * Created by Scadgek on 11/3/2014.
 */
public class Node extends ArrayRealVector {

    private double localError;
    public final int id;

    public Node(int id, int dimensions) {
        super(dimensions);
        this.id = id;
        localError = 0;
    }

    @Override
    public boolean equals(Object other) {
        return ((Node)other).id == id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** create a node from two existing nodes */
    public Node(int id, Node maxErrorNode, Node maxErrorNeighbour) {
        this(id, maxErrorNode.getDimension());
        setLocalError(maxErrorNode.getLocalError());
        double[] d= getDataRef();
        for (int i = 0; i < getDimension(); i++) {
            d[i] =  (maxErrorNode.getEntry(i) + maxErrorNeighbour.getEntry(i)) / 2;
        }
    }

    public Node randomizeUniform(double min, double max) {
        for (int i = 0; i < getDimension(); i++) {
            setEntry(i, Math.random() * (max-min) + min);
        }
        return this;
    }

//    public double[] getWeights() {
//        return weights;
//    }
//
//    public void setWeights(double[] weights) {
//        this.weights = weights;
//    }

    public double getLocalError() {
        return localError;
    }

    public void setLocalError(double localError) {
        this.localError = localError;
    }

    public double getDistanceSq(double[] x) {
        double s = 0;
        double[] y = getDataRef();
        for (int i = 0; i < getDimension(); i++) {
            double d = y[i] - x[i];
            s += d*d;
        }
        return s;
    }

    public double getDistance(double[] x) {
        return Math.sqrt(getDistanceSq(x));
    }

    public void update(double m, double[] x) {
        final double[] d = getDataRef();
        for (int i = 0; i < getDimension(); i++) {
            d[i] += m * (d[i] - x[i]);
        }
    }

//    public double distanceTo(double[] x) {
//        double retVal = 0;
//        for (int i = 0; i < x.length; i++) {
//            retVal += Math.pow(x[i] - weights[i], 2);
//        }
//        return Math.sqrt(retVal);
//    }
}