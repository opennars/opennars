package nars.rl.horde.functions;

import nars.rl.horde.LinearLearner;
import nars.rl.horde.demons.Predictor;
import nars.rl.horde.math.VectorPool;
import nars.rl.horde.math.VectorPools;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * GQ eligibility trace algorithm
 */
public class GQ implements Predictor, LinearLearner {
    private static final long serialVersionUID = -4971665888576276439L;

    public final RealVector v;
    protected final double alpha_v;
    protected final double alpha_w;
    protected final double beta_tp1;
    protected final double lambda_t;
    protected final RealVector w;
    protected final Traces e;
    protected double delta_t;

    public GQ(double alpha_v, double alpha_w, double beta, double lambda, int nbFeatures) {
        this(alpha_v, alpha_w, beta, lambda, nbFeatures, new ATraces());
    }

    public GQ(double alpha_v, double alpha_w, double beta, double lambda, int nbFeatures, Traces prototype) {
        this.alpha_v = alpha_v;
        this.alpha_w = alpha_w;
        beta_tp1 = beta;
        lambda_t = lambda;
        e = prototype.newTraces(nbFeatures);
        v = new ArrayRealVector(nbFeatures);
        w = new ArrayRealVector(nbFeatures);
    }

    protected double initEpisode() {
        e.clear();
        return 0.0;
    }

    public double update(RealVector x_t, double rho_t, double r_tp1, RealVector x_bar_tp1, double z_tp1) {
        if (x_t == null)
            return initEpisode();
        VectorPool pool = VectorPools.pool(x_t);
        delta_t = r_tp1 + beta_tp1 * z_tp1 + (1 - beta_tp1) * v.dotProduct(x_bar_tp1) - v.dotProduct(x_t);
        e.update((1 - beta_tp1) * lambda_t * rho_t, x_t);
        RealVector delta_e = pool.newVector(e.vect()).mapMultiplyToSelf(delta_t);
        ArrayRealVector tdCorrection = pool.newVector();
        if (x_bar_tp1 != null)
            tdCorrection.combineToSelf(0, 1, x_bar_tp1).mapMultiplyToSelf((1 - beta_tp1) * (1 - lambda_t) * e.vect().dotProduct(w));
        v.combineToSelf(1, alpha_v, pool.newVector(delta_e).combineToSelf(1, -1, tdCorrection));
        w.combineToSelf(1, alpha_w, delta_e.combineToSelf(1, -1, pool.newVector(x_t).mapMultiplyToSelf(w.dotProduct(x_t))));
        delta_e = null;
        pool.releaseAll();
        return delta_t;
    }

    @Override
    public double predict(RealVector x) {
        return v.dotProduct(x);
    }

    @Override
    public RealVector weights() {
        return v;
    }

    @Override
    public void resetWeight(int index) {
        v.setEntry(index, 0);
        e.vect().setEntry(index, 0);
        w.setEntry(index, 0);
    }

    @Override
    public double error() {
        return delta_t;
    }


    public Traces traces() {
        return e;
    }
}
