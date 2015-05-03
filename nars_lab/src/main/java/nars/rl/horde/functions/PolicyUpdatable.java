package nars.rl.horde.functions;

import nars.rl.horde.Policy;
import org.apache.commons.math3.linear.RealVector;


public class PolicyUpdatable<A> implements HordeUpdatable<A> {
    private final Policy<A> policy;

    public PolicyUpdatable(Policy<A> policy) {
        this.policy = policy;
    }

    @Override
    public void update(RealVector o_tp1, RealVector x_t, A a_t, RealVector x_tp1) {

        policy.update(x_t);
    }

    public Policy<A> policy() {
        return policy;
    }
}
