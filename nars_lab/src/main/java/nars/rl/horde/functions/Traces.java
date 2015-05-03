package nars.rl.horde.functions;

import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;

/**
 * Created by me on 5/3/15.
 */
public interface Traces extends Serializable {
    Traces newTraces(int size);

    void update(double lambda, RealVector phi);

    void clear();

    RealVector vect();

    RealVector prototype();

}
