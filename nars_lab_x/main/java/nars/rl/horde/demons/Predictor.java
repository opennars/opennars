package nars.rl.horde.demons;

import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;

/**
 * Created by me on 5/3/15.
 */
public interface Predictor extends Serializable {
    double predict(RealVector x);
}
