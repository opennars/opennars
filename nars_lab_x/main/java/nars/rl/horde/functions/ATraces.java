package nars.rl.horde.functions;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Accumulating traces
 */
public class ATraces implements Traces {
    private static final long serialVersionUID = 6241887723527497111L;
    public static final OpenMapRealVector DefaultPrototype = new OpenMapRealVector(0);
    public static final double DefaultThreshold = 1e-8;

    protected final double threshold;
    protected final RealVector prototype;

    protected RealVector vector;


    public ATraces() {
        this(DefaultPrototype);
    }

    public ATraces(RealVector prototype) {
        this(prototype, DefaultThreshold);
    }

    public ATraces(RealVector prototype, double threshold) {
        this(prototype, threshold, 0);
    }

    protected ATraces(RealVector prototype, double threshold, int size) {
        this.prototype = prototype;
        vector = prototype.copy(); //size > 0 ? prototype.copy() : null;
        this.threshold = threshold;
    }

    @Override
    public ATraces newTraces(int size) {
        return new ATraces(prototype, threshold, size);
    }

    @Override
    public void update(double lambda, RealVector phi) {
        updateVector(lambda, phi);
        adjustUpdate();
        if (clearRequired(phi, lambda))
            clearBelowThreshold();
        //assert Vectors.checkValues(vector);
    }

    protected void adjustUpdate() {
    }

    protected void updateVector(double lambda, RealVector phi) {
        if (vector.getDimension()!=phi.getDimension())
            vector = phi.copy();
        else {
            vector.mapMultiplyToSelf(lambda);
            vector.combineToSelf(1, 1, phi);
        }
    }

    private boolean clearRequired(RealVector phi, double lambda) {
        if (threshold == 0)
            return false;
        if (vector instanceof ArrayRealVector)
            return false;
        return true;
    }

    protected void clearBelowThreshold() {
        throw new RuntimeException("clearBelowThreshold: not implemented yet");
    }


//  protected void clearBelowThreshold() {
//
//    OpenMapRealVector v = (OpenMapRealVector) vector;
//    //double[] values = v.get
//
//    Iterator<RealVector.Entry> e = v.iterator();
//
//
//    int[] indexes = OpenMapRealVector.activeIndexes;
//    int i = 0;
//    while (i < OpenMapRealVector.nonZeroElements()) {
//      final double absValue = Math.abs(values[i]);
//      if (absValue <= threshold)
//        OpenMapRealVector.removeEntry(indexes[i]);
//      else
//        i++;
//    }
//  }
//  private RealVectorChangingVisitor clearBelowThresholdVisitor = new RealVectorChangingVisitor() {
//    @Override
//    public void start(int dimension, int start, int end) {
//
//    }
//
//    @Override
//    public double visit(int i, double v) {
//      if (Math.abs(v) <= threshold)
//      return 0;
//    }
//
//    @Override
//    public double end() {
//      return 0;
//    }
//  };

    @Override
    public RealVector vect() {
        return vector;
    }

    @Override
    public void clear() {
        vector = new OpenMapRealVector();
    }


    public RealVector prototype() {
        return prototype;
    }
}
