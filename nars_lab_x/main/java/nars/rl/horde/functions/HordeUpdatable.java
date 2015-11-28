package nars.rl.horde.functions;

import org.apache.commons.math3.linear.RealVector;

public interface HordeUpdatable<A> {
    void update(RealVector o_tp1, RealVector x_t, A a_t, RealVector x_tp1);
}
