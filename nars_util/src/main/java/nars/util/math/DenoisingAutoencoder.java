package nars.util.math;


import nars.util.data.XORShiftRandom;

import java.util.Random;

/** Denoising Autoencoder (from DeepLearning.net) */
public class DenoisingAutoencoder {

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

    public double uniform(final double min, final double max) {
        return rng.nextDouble() * (max - min) + min;
    }

    public double binomial(final int n, final double p) {
        if (p < 0 || p > 1) {
            return 0;
        }

        int c = 0;
        double r;

        for (int i = 0; i < n; i++) {
            r = rng.nextDouble();
            if (r < p) {
                c++;
            }
        }

        return c;
    }

    final public static double sigmoid(final double x) {
        return 1.0 / (1.0 + Math.pow(Math.E, -x));
    }
    
    public DenoisingAutoencoder(int n_visible, int n_hidden) {
        this(n_visible, n_hidden, null, null, null, null);
    }

    public DenoisingAutoencoder(int n_visible, int n_hidden, double[][] W, double[] hbias, double[] vbias, Random rng) {
        this.n_visible = n_visible;
        this.n_hidden = n_hidden;

        if (rng == null) {
            this.rng = new XORShiftRandom();
        } else {
            this.rng = rng;
        }

        if (W == null) {
            this.W = new double[this.n_hidden][this.n_visible];
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
            this.hbias = new double[this.n_hidden];
            for (int i = 0; i < this.n_hidden; i++) {
                this.hbias[i] = 0;
            }
        } else {
            this.hbias = hbias;
        }

        if (vbias == null) {
            this.vbias = new double[this.n_visible];
            for (int i = 0; i < this.n_visible; i++) {
                this.vbias[i] = 0;
            }
        } else {
            this.vbias = vbias;
        }
    }

    public void get_corrupted_input(double[] x, double[] tilde_x, double p) {
        for (int i = 0; i < n_visible; i++) {
            if (x[i] == 0) {
                tilde_x[i] = 0;
            } else {
                tilde_x[i] = binomial(1, p);
            }
        }
    }

    // Encode
    public void encode(double[] x, double[] y, boolean sigmoid, boolean normalize) {
        double max=0, min=0;
        for (int i = 0; i < n_hidden; i++) {
            y[i] = 0;
            for (int j = 0; j < n_visible; j++) {
                y[i] += W[i][j] * x[j];
            }
            y[i] += hbias[i];
            
            if (sigmoid)
                y[i] = sigmoid(y[i]);
            
            if (i == 0)
                max = min = y[i];
            else {
                if (y[i] > max) max = y[i];
                if (y[i] < min) min = y[i];
            }
                
        }
        if (normalize) {
            for (int i = 0; i < n_hidden; i++) {
                y[i] = (y[i]-min)/(max-min);
            }            
        }
    }

    // Decode
    public void get_reconstructed_input(double[] y, double[] z) {
        for (int i = 0; i < n_visible; i++) {
            z[i] = 0;
            for (int j = 0; j < n_hidden; j++) {
                z[i] += W[j][i] * y[j];
            }
            z[i] += vbias[i];
            z[i] = sigmoid(z[i]);
        }
    }

    public void train(double[] x, double lr, double corruption_level) {
        if ((tilde_x == null) || (tilde_x.length!=n_visible)) {
            tilde_x = new double[n_visible];
            y = new double[n_hidden];
            z = new double[n_visible];

            L_vbias = new double[n_visible];
            L_hbias = new double[n_hidden];            
        }

        if (corruption_level > 0) {        
            get_corrupted_input(x, tilde_x, 1 - corruption_level);
        }
        else {
            tilde_x = x;
        }
        encode(tilde_x, y, true, false);
        get_reconstructed_input(y, z);

        // vbias
        for (int i = 0; i < n_visible; i++) {
            L_vbias[i] = x[i] - z[i];
            vbias[i] += lr * L_vbias[i];
        }

        // hbias
        for (int i = 0; i < n_hidden; i++) {
            L_hbias[i] = 0;
            for (int j = 0; j < n_visible; j++) {
                L_hbias[i] += W[i][j] * L_vbias[j];
            }
            L_hbias[i] *= y[i] * (1 - y[i]);
            hbias[i] += lr * L_hbias[i];
        }

        // W
        for (int i = 0; i < n_hidden; i++) {
            for (int j = 0; j < n_visible; j++) {
                W[i][j] += lr * (L_hbias[i] * tilde_x[j] + L_vbias[j] * y[i]);
            }
        }
    }

    public void reconstruct(double[] x, double[] z) {
        double[] y = new double[n_hidden];

        encode(x, y, true, false);
        get_reconstructed_input(y, z);
    }

}
