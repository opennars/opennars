package nars.rl.horde.functions;

import nars.rl.horde.demons.OnPolicyTD;
import nars.rl.horde.math.VectorPool;
import nars.rl.horde.math.VectorPools;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;


public class GTDLambda implements OnPolicyTD, GVF {
    protected final double gamma;
    final public double alpha_v;
    public final double alpha_w;
    protected final double lambda;
    private double gamma_t;

    final public ArrayRealVector v;

    protected final ArrayRealVector w;
    private final Traces e;
    protected double v_t;

    protected double delta_t;
    private double correction;

    public GTDLambda(double lambda, double gamma, double alpha_v, double alpha_w, int nbFeatures) {
        this(lambda, gamma, alpha_v, alpha_w, nbFeatures, new ATraces());
    }

    public GTDLambda(double lambda, double gamma, double alpha_v, double alpha_w, int nbFeatures, Traces prototype) {
        this.alpha_v = alpha_v;
        this.gamma = gamma;
        this.lambda = lambda;
        this.alpha_w = alpha_w;
        v = new ArrayRealVector(nbFeatures);
        w = new ArrayRealVector(nbFeatures);
        e = prototype.newTraces(nbFeatures);
    }

    @Override
    public double update(double pi_t, double b_t, RealVector x_t, RealVector x_tp1, double r_tp1, double gamma_tp1,
                         double z_tp1) {
        if (x_t == null)
            return initEpisode(gamma_tp1);
        VectorPool pool = VectorPools.pool(e.vect());
        v_t = v.dotProduct(x_t);
        delta_t = r_tp1 + (1 - gamma_tp1) * z_tp1 + gamma_tp1 * v.dotProduct(x_tp1) - v_t;
        // Update traces
        e.update(gamma_t * lambda, x_t);
        double rho_t = pi_t / b_t;
        e.vect().mapMultiplyToSelf(rho_t);
        // Compute correction
        ArrayRealVector correctionVector = pool.newVector();
        if (x_tp1 != null) {
            correction = e.vect().dotProduct(w);
            correctionVector.combineToSelf(1, correction * gamma_tp1 * (1 - lambda), x_tp1);
        }
        // Update parameters
        RealVector deltaE = pool.newVector(e.vect()).mapMultiplyToSelf(delta_t);
        v.combineToSelf(1, alpha_v, pool.newVector(deltaE).combineToSelf(1, -1, correctionVector));
        w.combineToSelf(1, alpha_w, deltaE.combineToSelf(1, -w.dotProduct(x_t), x_t));
        deltaE = null;
        gamma_t = gamma_tp1;
        pool.releaseAll();
        return delta_t;
    }

    protected double initEpisode(double gamma_tp1) {
        gamma_t = gamma_tp1;
        e.clear();
        v_t = 0;
        return 0;
    }

    @Override
    public void resetWeight(int index) {
        v.setEntry(index, 0);
        e.vect().setEntry(index, 0);
    }

    @Override
    public double update(RealVector x_t, RealVector x_tp1, double r_tp1) {
        return update(1, 1, x_t, x_tp1, r_tp1, gamma, 0);
    }

    @Override
    public double update(double pi_t, double b_t, RealVector x_t, RealVector x_tp1, double r_tp1) {
        return update(pi_t, b_t, x_t, x_tp1, r_tp1, gamma, 0);
    }

    public double update(double pi_t, double b_t, RealVector x_t, RealVector x_tp1, double r_tp1, double gamma_tp1) {
        return update(pi_t, b_t, x_t, x_tp1, r_tp1, gamma_tp1, 0);
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
    public ArrayRealVector secondaryWeights() {
        return w;
    }


    public Traces traces() {
        return e;
    }

    @Override
    public double error() {
        return delta_t;
    }

    @Override
    public double prediction() {
        return v_t;
    }

    public double correction() {
        return correction;
    }
}
