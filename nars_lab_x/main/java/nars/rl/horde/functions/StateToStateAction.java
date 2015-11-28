package nars.rl.horde.functions;

import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;

/**
 * Created by me on 5/3/15.
 */
public interface StateToStateAction<A> extends Serializable {
    RealVector stateAction(RealVector s, A a);

    double vectorNorm();

    int vectorSize();
}
