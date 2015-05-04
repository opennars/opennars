package nars.rl.horde;

import nars.rl.horde.demons.Predictor;
import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;

public class HordeAgent<A> {

    public interface Control<A> extends Serializable {
        A proposeAction(RealVector x);
    }

    public interface ControlLearner<A> extends Control<A> {
        A step(RealVector x_t, A a_t, RealVector x_tp1, double r_tp1);

        Policy<A> policy();
    }

    public interface OffPolicyLearner<A> extends Control<A> {
        void learn(RealVector x_t, A a_t, RealVector x_tp1, A a_tp1, double reward);

        Policy<A> targetPolicy();

        Predictor predictor();
    }

    public interface OffPolicyTD extends Predictor, LinearLearner {
        double update(double pi_t, double b_t, RealVector x_t, RealVector x_tp1, double r_tp1);

        double prediction();

        RealVector secondaryWeights();
    }

    public interface Projector {
        /**
         * Project an observation. If the observation is null, it should return a
         * non-null vector representing an absorbing state.
         *
         * @param obs observation to project
         * @return a non-null vector
         */
        RealVector project(RealVector obs);

        /**
         * @return size of the vector after projection
         */
        int vectorSize();

        /**
         * @return the expected norm of the vector after projection
         */
        double vectorNorm();

    }

    public static class Direct implements Projector {

        private RealVector obs;

        @Override
        public RealVector project(RealVector obs) {
            this.obs= obs;
            return obs;
        }

        @Override
        public int vectorSize() {
            return obs.getDimension();
        }

        @Override
        public double vectorNorm() {
            return obs.getNorm();
        }
    }

    private static final long serialVersionUID = -8430893512617299110L;

    protected final ControlLearner<A> control;
    protected final Projector projector;
    protected RealVector x_t;
    private final Horde<A> horde;

    public HordeAgent(ControlLearner<A> control, Projector projector, Horde<A> horde) {
        this.control = control;
        this.projector = projector;
        this.horde = horde;
    }

    public A getAtp1(RealVector o_tp1, A a, double r_tp1) {
        /*if (step.isEpisodeStarting())
            x_t = null;*/
        RealVector x_tp1 = projector.project(o_tp1);
        A a_tp1 = control.step(x_t, a, x_tp1, r_tp1);
        horde.update(o_tp1, x_t, a, x_tp1);
        x_t = x_tp1;
        return a_tp1;
    }

    public ControlLearner<A> control() {
        return control;
    }

    public Projector projector() {
        return projector;
    }

    public Horde<A> horde() {
        return horde;
    }

    public Policy<A> behaviourPolicy() {
        return control.policy();
    }
}
