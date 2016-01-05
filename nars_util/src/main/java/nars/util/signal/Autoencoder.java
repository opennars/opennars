package nars.util.signal;

import java.util.Random;

/**
 * Denoising Autoencoder (from DeepLearning.net)
 * 
 * TODO parameter for activation function (linear, sigmoid, etc..)
 */
public class Autoencoder {

	public int n_visible;
	public int n_hidden;
	public double[][] W;
	public double[] hbias;
	public double[] vbias;
	public Random rng;
	private double[] tilde_x;
	private double[] y;
	private double[] z;
	private double[] L_vbias;
	private double[] L_hbias;

	public double uniform(double min, double max) {
		return rng.nextDouble() * (max - min) + min;
	}

	/*
	 * public double binomial(final int n, final double p) { if (p < 0 || p > 1)
	 * { return 0; }
	 * 
	 * int c = 0; double r;
	 * 
	 * for (int i = 0; i < n; i++) { r = rng.nextDouble(); if (r < p) { c++; } }
	 * 
	 * return c; }
	 */

	public static double sigmoid(double x) {
		return 1.0 / (1.0 + Math.pow(Math.E, -x));
	}

	public Autoencoder(int n_visible, int n_hidden) {
		this(n_visible, n_hidden, null, null, null, null);
	}

	public Autoencoder(int n_visible, int n_hidden, double[][] W,
			double[] hbias, double[] vbias, Random rng) {
		this.n_visible = n_visible;
		this.n_hidden = n_hidden;

		this.rng = rng == null ? new Random() : rng;

		if (W == null) {
			this.W = new double[n_hidden][n_visible];
			double a = 1.0 / this.n_visible;

			for (int i = 0; i < this.n_hidden; i++) {
				for (int j = 0; j < this.n_visible; j++) {
					this.W[i][j] = uniform(-a, a);
				}
			}
		} else {
			this.W = W;
		}

		if (hbias == null) {
			this.hbias = new double[n_hidden];
			for (int i = 0; i < this.n_hidden; i++) {
				this.hbias[i] = 0;
			}
		} else {
			this.hbias = hbias;
		}

		if (vbias == null) {
			this.vbias = new double[n_visible];
			for (int i = 0; i < this.n_visible; i++) {
				this.vbias[i] = 0;
			}
		} else {
			this.vbias = vbias;
		}
	}

	public void addNoise(double[] x, double[] tilde_x, double maxNoiseAmount,
			double corruptionRate) {
		for (int i = 0; i < n_visible; i++) {
			if ((corruptionRate > 0) && (rng.nextFloat() < corruptionRate)) {
				tilde_x[i] = 0;
			} else if (maxNoiseAmount > 0) {
				double nx = x[i] + (rng.nextFloat() - 0.5) * maxNoiseAmount;
				if (nx < 0)
					nx = 0;
				if (nx > 1)
					nx = 1;
				tilde_x[i] = nx;
			}
		}
	}

	// Encode
	public double[] encode(double[] x, double[] y, boolean sigmoid,
			boolean normalize) {

		double[][] W = this.W;

		if (y == null)
			y = new double[n_hidden];

		int nv = n_visible;
		int nh = n_hidden;
		double[] hbias = this.hbias;

		double max = 0, min = 0;
		for (int i = 0; i < nh; i++) {
			double yi = hbias[i];
			double[] wi = W[i];

			for (int j = 0; j < nv; j++) {
				yi += wi[j] * x[j];
			}

			if (sigmoid)
				yi = sigmoid(yi);

			if (i == 0)
				max = min = yi;
			else {
				if (yi > max)
					max = yi;
				if (yi < min)
					min = yi;
			}
			y[i] = yi;

		}

		if ((normalize) && (max != min)) {
			for (int i = 0; i < nh; i++) {
				y[i] = (y[i] - min) / (max - min);
			}
		}

		return y;
	}

	// Decode
	public void get_reconstructed_input(double[] y, double[] z) {
		double[][] w = W;

		double[] vbias = this.vbias;
		int nv = n_visible;
		int nh = n_hidden;

		for (int i = 0; i < nv; i++) {
			double zi = vbias[i];

			for (int j = 0; j < nh; j++) {
				zi += w[j][i] * y[j];
			}

			zi = sigmoid(zi);

			z[i] = zi;
		}
	}

	public double[] getOutput() {
		return y;
	}

	public double train(double[] x, double learningRate, double noiseLevel,
			double corruptionRate, boolean sigmoid) {
		if ((tilde_x == null) || (tilde_x.length != n_visible)) {
			tilde_x = new double[n_visible];
			z = new double[n_visible];
			L_vbias = new double[n_visible];
		}
		if (y == null || y.length != n_hidden) {
			y = new double[n_hidden];
			L_hbias = new double[n_hidden];
		}

		double[][] W = this.W;
		double[] L_hbias = this.L_hbias;
		double[] L_vbias = this.L_vbias;
		double[] vbias = this.vbias;

		if (noiseLevel > 0) {
			addNoise(x, tilde_x, noiseLevel, corruptionRate);
		} else {
			tilde_x = x;
		}

		double[] tilde_x = this.tilde_x;

		encode(tilde_x, y, sigmoid, true);

		get_reconstructed_input(y, z);

		double error = 0;

		double[] zz = z;
		// vbias
		for (int i = 0; i < n_visible; i++) {

			double lv = L_vbias[i] = x[i] - zz[i];

			error += lv * lv; // square of difference

			vbias[i] += learningRate * lv;
		}

		error /= n_visible;

		int n = n_visible;
		double[] y = this.y;
		double[] hbias = this.hbias;
		int nh = n_hidden;

		// hbias
		for (int i = 0; i < nh; i++) {
			L_hbias[i] = 0;
			for (int j = 0; j < n; j++) {
				L_hbias[i] += W[i][j] * L_vbias[j];
			}
			double yi = y[i];
			L_hbias[i] *= yi * (1 - yi);
			hbias[i] += learningRate * L_hbias[i];
		}

		// W
		for (int i = 0; i < nh; i++) {
			double yi = y[i];
			double lhb = L_hbias[i];
			for (int j = 0; j < n; j++) {
				W[i][j] += learningRate * (lhb * tilde_x[j] + L_vbias[j] * yi);
			}
		}

		return error;
	}

	public double[] reconstruct(double[] x, double[] z) {
		double[] y = new double[n_hidden];

		encode(x, y, true, false);
		get_reconstructed_input(y, z);

		return z;
	}

	/**
	 * finds the index of the highest output value, or returns a random one if
	 * none are
	 */
	public int getMax() {

		double m = Double.MIN_VALUE;
		int best = -1;
		for (int i = 0; i < y.length; i++) {
			double Y = y[i];
			if (Y > m) {
				m = Y;
				best = i;
			}
		}
		if (best == -1)
			return (int) (Math.random() * y.length);
		return best;
	}
}
