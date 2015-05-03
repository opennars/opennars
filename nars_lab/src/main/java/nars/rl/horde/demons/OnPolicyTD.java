package nars.rl.horde.demons;

import nars.rl.horde.LinearLearner;
import org.apache.commons.math3.linear.RealVector;

/**
 * Created by me on 5/3/15.
 */
public interface OnPolicyTD extends Predictor, LinearLearner {
    double update(RealVector x_t, RealVector x_tp1, double r_tp1);

    double prediction();
}
