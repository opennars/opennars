/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.becca;


import nars.util.signal.Autoencoder;

/**
 * Ziptie implemented as a raw internal Autoencoder
 */
public class AEZiptie extends Ziptie {

    protected final Autoencoder a;
    double learningRate = 0.02;
    double noiseLevel = 0.05;
    protected double error;
    protected final int numCables;
    protected final int numBundles;

    public AEZiptie(int numInputs, int numOutputs) {
        numCables = numInputs;
        numBundles = numOutputs;
        a = new Autoencoder(numInputs, numOutputs);
    }
    
    public double getWeight(int cable, int bundle) {
        return a.W[bundle][cable];
    }
    public double getMembership(int cable, int bundle) {
        double w = getWeight(cable, bundle);
        if (w > 0) return w;
        if (w > 1) return 1;
        return 0;
    }
    
    @Override
    public double[] in(double[] signal, double[] result) {
        error = a.train(signal, learningRate, noiseLevel, 0.05, false);
        return a.getOutput();
    }
    
    
}
