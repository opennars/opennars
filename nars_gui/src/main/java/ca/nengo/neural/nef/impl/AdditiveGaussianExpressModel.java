/**
 * 
 */
package ca.nengo.neural.nef.impl;

import Jama.Matrix;
import ca.nengo.math.impl.GaussianPDF;
import ca.nengo.model.Resettable;
import ca.nengo.neural.nef.ExpressModel;
import ca.nengo.util.MU;

/**
 * An ExpressModel that adds random noise and interpolated static distortion to DIRECT values
 * as a model of spiking effects. Assumes Gaussian spike-related variability
 * which is independent across decoded values. Autocorrelation over time is assumed to
 * be zero by default but this can be set via setR(...). Note that noise is also
 * filtered by PSC dynamics at the Termination. Autocorrelation is meant to model the
 * unfiltered spectrum.
 * 
 * @author Bryan Tripp
 */
public abstract class AdditiveGaussianExpressModel implements ExpressModel, Resettable {

    private final int myDim;
    private final GaussianPDF myPDF;
    private float[][] myPreviousNoiseSamples;
    private float[][] myR; //autocorrelation for each output
    private float[][] myX; //we multiply new and previous samples by these coefficients to model myR

    /**
     * @param dim Number of outputs of the DecodedOrigin
     */
    public AdditiveGaussianExpressModel(int dim) {
        myDim = dim;
        myPDF = new GaussianPDF();
        reset(false);
    }

    /**
     * @param R Autocorrelation for each input. The first element of each array is the 1-step autocorrelation;
     * 		the kth is the k-step autocorrelation
     */
    public void setR(float[][] R) {
        if (R.length != myDim) {
            throw new IllegalArgumentException("Expected an array of autocorrelations for each output");
        }

        myR = R;
        myX = new float[myDim][];
        myPreviousNoiseSamples = new float[myDim][];

        for (int i = 0; i < R.length; i++) {
            myX[i] = getCoefficients(R[i]);
            myPreviousNoiseSamples[i] = new float[myX[i].length];
        }
    }

    //get coefficients to generate autocorrelations ...
    private static float[] getCoefficients(float[] R) {
        double[][] cov = new double[R.length][]; //covariance matrix
        for (int i = 0; i < R.length; i++) {
            cov[i] = new double[R.length];
            for (int j = 0; j < R.length; j++) {
                int d = Math.abs(i-j);
                cov[i][j] = (i == j) ? 1 : R[d-1];
            }
        }
        Matrix COV = new Matrix(cov);
        return MU.convert(COV.chol().getL().getArray()[R.length-1]);
    }

    /**
     * @return Autocorrelation for each output
     */
    public float[][] getR() {
        return myR;
    }

    public void reset(boolean randomize) {
        if (myPreviousNoiseSamples != null) {
            for (int i = 0; i < myPreviousNoiseSamples.length; i++) {
                for (int j = 0; j < myPreviousNoiseSamples[i].length; j++) {
                    myPreviousNoiseSamples[i][j] = 0;
                }
            }
        }
    }

    /**
     * @see ca.nengo.neural.nef.ExpressModel#getOutput(float, float[], float[])
     */
    public float[] getOutput(float startTime, float[] state, float[] directOutput) {
        float[] noise = getNoise(state, directOutput);
        float[] distortion = getDistortion(state, directOutput);
        float[] result = new float[directOutput.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = directOutput[i] + noise[i] + distortion[i];
        }
        return result;
    }

    /**
     * Note: Override this for alternative additive noise, e.g. correlated across outputs.
     * 
     * @param state The value represented by the associated NEFEnsemble
     * @param directOutput DIRECT mode output values of an Origin
     * @return Noise to be added to DIRECT mode values
     */
    public float[] getNoise(float[] state, float[] directOutput) {
        float[] SD = getNoiseSD(state, directOutput);

        float[] noise = new float[directOutput.length];
        for (int i = 0; i < noise.length; i++) {
            if (myR == null) {
                noise[i] = SD[i] * myPDF.sample()[0];
            } else {
                int len = myPreviousNoiseSamples[i].length;
                System.arraycopy(myPreviousNoiseSamples[i], 1, myPreviousNoiseSamples[i], 0, len-1);
                myPreviousNoiseSamples[i][len-1] = myPDF.sample()[0];

                noise[i] = SD[i] * MU.prod(myPreviousNoiseSamples[i], myX[i]);
            }
        }

        return noise;
    }

    /**
     * @param state The value represented by the associated NEFEnsemble
     * @param directOutput DIRECT mode output values of an Origin
     * @return Standard deviation of noise to be added to each DIRECT output value
     */
    public abstract float[] getNoiseSD(float[] state, float[] directOutput);

    /**
     * @param state The value represented by the associated NEFEnsemble
     * @param directOutput DIRECT mode output values of an Origin
     * @return Static distortion error to be added to each DIRECT output value
     */
    public abstract float[] getDistortion(float[] state, float[] directOutput);

}
