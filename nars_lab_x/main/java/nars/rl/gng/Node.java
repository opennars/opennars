package nars.rl.gng;



/**
 * Created by Scadgek on 11/3/2014.
 */
public class Node extends ArrayRealVector implements Named<String> {

    private double localError;
    public final int id;
    private double localDistanceSq; //caches square of last tested distance

    public Node(int id, int dimensions) {
        super(dimensions);
        this.id = id;
        localError = 0;
    }

    @Override
    public boolean equals(Object other) {
        return (this == other) || ((Node)other).id == id;
    }

    @Override
    public int hashCode() {
        //return Objects.hash(id);
        return (id+1)*37;
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

    public double getDistanceSq(final double[] x) {
        double s = 0;
        final double[] y = getDataRef();
        for (int i = 0; i < getDimension(); i++) {
            final double d = y[i] - x[i];
            s += d*d;
        }
        return s;
    }



    public double getDistance(final double[] x) {
        return Math.sqrt(getDistanceSq(x));
    }

    /** 0 < rate < 1.0 */
    public void update(final double rate, final double[] x) {
        final double[] d = getDataRef();
        final double ir = (1.0 - rate);
        for (int i = 0; i < d.length; i++) {
            d[i] =  (ir * d[i] ) + (rate * x[i]);
        }
    }

    public void add(final double[] x) {
        final double[] d = getDataRef();
        for (int i = 0; i < d.length; i++) {
            d[i] += x[i];
        }
    }

    @Override
    public String toString() {
        return id + ": " + super.toString();
    }

    public double updateDistanceSq(double[] x) {
        return (this.localDistanceSq = getDistanceSq(x));
    }

    public double getLocalDistanceSq() {
        return localDistanceSq;
    }
    public double getLocalDistance() { return Math.sqrt(localDistanceSq); }

    @Override
    public String name() {
        return Integer.toString(id);
    }

    public double getDistanceSq(Node b) {
        return getDistanceSq(b.getDataRef());
    }

    //    public double distanceTo(double[] x) {
//        double retVal = 0;
//        for (int i = 0; i < x.length; i++) {
//            retVal += Math.pow(x[i] - weights[i], 2);
//        }
//        return Math.sqrt(retVal);
//    }
}