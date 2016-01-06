/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import com.google.common.util.concurrent.AtomicDouble;

/**
 * 
 * @author thorsten
 */
public class ApproxParameters {

	/** learning rate */
	public final AtomicDouble alpha = new AtomicDouble();

	public final AtomicDouble momentum = new AtomicDouble();

	public ApproxParameters(double alpha, double momentum) {
		this.alpha.set(alpha);
		this.momentum.set(momentum);
	}

	public double getAlpha() {
		return alpha.get();
	}

	public void setAlpha(double alpha) {
		this.alpha.set(alpha);
	}

	public double getMomentum() {
		return momentum.get();
	}

	public void setMomentum(double momentum) {
		this.momentum.set(momentum);
	}
}
