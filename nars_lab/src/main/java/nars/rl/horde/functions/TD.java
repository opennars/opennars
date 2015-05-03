package nars.rl.horde.functions;

import nars.rl.horde.demons.OnPolicyTD;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;


public class TD implements OnPolicyTD {
    private static final long serialVersionUID = -3640476464100200081L;
    protected final double alpha_v;
    protected final double gamma;

    final public ArrayRealVector v;

    protected double delta_t;
    protected double v_t;

    public TD(double alpha_v, int nbFeatures) {
        this(Double.NaN, alpha_v, nbFeatures);
    }

    public TD(double gamma, double alpha_v, int nbFeatures) {
        this.alpha_v = alpha_v;
        this.gamma = gamma;
        v = new ArrayRealVector(nbFeatures);
    }

    protected double initEpisode() {
        v_t = 0;
        delta_t = 0;
        return delta_t;
    }

    @Override
    public double update(RealVector x_t, RealVector x_tp1, double r_tp1) {
        return update(x_t, x_tp1, r_tp1, gamma);
    }

    public double update(RealVector x_t, RealVector x_tp1, double r_tp1, double gamma_tp1) {
        if (x_t == null)
            return initEpisode();
        v_t = v.dotProduct(x_t);
        delta_t = r_tp1 + gamma_tp1 * v.dotProduct(x_tp1) - v_t;
        v.combineToSelf(1, alpha_v * delta_t, x_t);
        return delta_t;
    }

    @Override
    public double predict(RealVector phi) {
        return v.dotProduct(phi);
    }

    public double gamma() {
        return gamma;
    }

    @Override
    public ArrayRealVector weights() {
        return v;
    }

    @Override
    public void resetWeight(int index) {
        v.setEntry(index, 0);
    }

    @Override
    public double error() {
        return delta_t;
    }

    @Override
    public double prediction() {
        return v_t;
    }

    public double alpha() {
        return alpha_v;
    }
}
