package nars.rl.horde.demons;

import nars.rl.horde.LinearLearner;
import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;

/**
 * @param <A> action type
 */
public interface Demon<A> extends Serializable {
    void update(RealVector x_t, A a_t, RealVector x_tp1);

    LinearLearner learner();
}
