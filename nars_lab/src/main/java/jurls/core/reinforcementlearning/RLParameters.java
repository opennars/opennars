/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.reinforcementlearning;

import com.google.common.util.concurrent.AtomicDouble;

/**
 * 
 * @author thorsten
 */
public class RLParameters {

	/** learning rate */
	public final AtomicDouble alpha;

	/** farsight */
	public final AtomicDouble gamma;

	/**
	 * lambda
	 * "A value of λ=1.0 effectively makes algorithm run an online Monte Carlo in which the effects of all future interactions are fully considered in updating each Q-value of an episode."
	 */
	public final AtomicDouble lambda;

	/** randomness */
	public final AtomicDouble epsilon;

	public RLParameters(double alpha, double gamma, double lambda,
			double epsilon) {
		this.alpha = new AtomicDouble(alpha);
		this.gamma = new AtomicDouble(gamma);
		this.lambda = new AtomicDouble(lambda);
		this.epsilon = new AtomicDouble(epsilon);
	}

	public double getAlpha() {
		return alpha.get();
	}

	public void setAlpha(double alpha) {
		this.alpha.set(alpha);
	}

	public double getGamma() {
		return gamma.get();
	}

	public void setGamma(double gamma) {
		this.gamma.set(gamma);
	}

	public double getLambda() {
		return lambda.get();
	}

	public void setLambda(double lambda) {
		this.lambda.set(lambda);
	}

	public double getEpsilon() {
		return epsilon.get();
	}

	public void setEpsilon(double epsilon) {
		this.epsilon.set(epsilon);
	}
}
