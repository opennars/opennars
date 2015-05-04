package nars.rl.horde.functions;


import nars.rl.horde.HordeAgent;
import nars.rl.horde.Policy;
import nars.rl.horde.math.VectorPool;
import nars.rl.horde.math.VectorPools;
import org.apache.commons.math3.linear.RealVector;

public class GreedyGQ<A> implements HordeAgent.OffPolicyLearner<A> {

    private static final long serialVersionUID = 7017521530598253457L;

    protected final GQ gq;

    protected final Policy<A> target;
    protected final Policy<A> behaviour;
    protected final StateToStateAction<A> toStateAction;

    public double rho_t;
    private final A[] actions;

    private final RealVector prototype;

    @SuppressWarnings("unchecked")
    public GreedyGQ(GQ gq, A[] actions, StateToStateAction<A> toStateAction, Policy<A> target, Policy<A> behaviour) {
        this.gq = gq;
        this.target = target;
        this.behaviour = behaviour;
        this.toStateAction = toStateAction;
        this.actions = actions;
        prototype = gq.e.prototype();
    }

    public double update(RealVector x_t, A a_t, double r_tp1, double gamma_tp1, double z_tp1, RealVector x_tp1,
                         A a_tp1) {
        rho_t = 0.0;
        if (a_t != null && x_t != null /*!Vectors.isNull(x_t)*/) {
            target.update(x_t);
            behaviour.update(x_t);
            rho_t = target.pi(a_t) / behaviour.pi(a_t);
        }
        //assert Utils.checkValue(rho_t);
        VectorPool pool = VectorPools.pool(prototype, gq.v.getDimension());
        RealVector sa_bar_tp1 = pool.newVector();
        //if (!Vectors.isNull(x_t) && !Vectors.isNull(x_tp1)) {
        if (x_t != null && x_tp1 != null) {
            target.update(x_tp1);
            for (A a : actions) {
                double pi = target.pi(a);
                if (pi == 0)
                    continue;
                sa_bar_tp1.combineToSelf(1, pi, toStateAction.stateAction(x_tp1, a));
            }
        }
        RealVector phi_stat = x_t != null ? toStateAction.stateAction(x_t, a_t) : null;
        double delta_t = gq.update(phi_stat, rho_t, r_tp1, sa_bar_tp1, z_tp1);
        pool.releaseAll();
        return delta_t;
    }

    public RealVector theta() {
        return gq.v;
    }

    public double gamma() {
        return 1 - gq.beta_tp1;
    }

    public GQ gq() {
        return gq;
    }

    @Override
    public Policy<A> targetPolicy() {
        return target;
    }

    @Override
    public void learn(RealVector x_t, A a_t, RealVector x_tp1, A a_tp1, double reward) {
        update(x_t, a_t, reward, gamma(), 0, x_tp1, a_tp1);
    }

    @Override
    public A proposeAction(RealVector x_t) {
        return Policy.decide(target, x_t);
    }


    @Override
    public GQ predictor() {
        return gq;
    }
}
