package nars.rl.horde.math;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;


public interface VectorPool {
    ArrayRealVector newVector();

    ArrayRealVector newVector(RealVector v);

    void releaseAll();
}
