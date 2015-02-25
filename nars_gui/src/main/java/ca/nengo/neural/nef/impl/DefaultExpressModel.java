/**
 * 
 */
package ca.nengo.neural.nef.impl;

import ca.nengo.math.Function;
import ca.nengo.math.impl.LinearCurveFitter;
import ca.nengo.model.SimulationException;
import ca.nengo.model.SimulationMode;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.util.MU;

/**
 * An ExpressModel that determines simplified noise and distortion models from simulations. Noise
 * variance & autocorrelation are assumed constant per output and are determined from an example simulation.
 * Distortion is interpolated from example simulations. For 1D ensembles, distortion is interpolated from
 * samples in the encoded domain. For higher-dimensional ensembles, distortion is treated
 * as a function of radial distance from zero, taken from samples in the first dimension.
 * 
 * @author Bryan Tripp
 */
public class DefaultExpressModel extends AdditiveGaussianExpressModel {

    private NEFGroup myEnsemble;
    private DecodedSource myOrigin;
    private boolean myRadial = false;
    private Function[] myInterpFunctions = null;
    private float[] myNoiseSD;

    /**
     * @param origin The DecodedOrigin for which spike effects are to be modelled
     * @throws SimulationException
     */
    public DefaultExpressModel(DecodedSource origin) throws SimulationException {
        super(origin.getDimensions());

        if (!NEFGroup.class.isAssignableFrom(origin.getNode().getClass())) {
            throw new IllegalArgumentException("Expected DecodedOrigin on NEFEnsemble");
        }

        myEnsemble = (NEFGroup) origin.getNode();
        myOrigin = origin;
        update();
    }

    public void update() throws SimulationException {
        float[][] R = setNoise(myOrigin); //R = autocorrelation
        super.setR(R);

        myRadial = myEnsemble.getDimension() > 1;

        float[] r; //r = radius
        if (myRadial) {
            r = MU.makeVector(0, .1f, 2.5f);
        } else {
            r = MU.makeVector(-2.5f, .1f, 2.5f);
        }

        //run simulations to get distortion samples for interpolation ...
        float[][] input = MU.uniform(r.length, myEnsemble.getDimension(), 0);
        for (int i = 0; i < r.length; i++) {
            input[i][0] = r[i] * myEnsemble.getRadii()[0];
        }
        float[][] directOutput = NEFUtil.getOutput(myOrigin, input, SimulationMode.DIRECT);
        float[][] constantRateOutput = NEFUtil.getOutput(myOrigin, input, SimulationMode.CONSTANT_RATE);
        float[][] difference = MU.transpose(MU.difference(constantRateOutput, directOutput));

        //make the interpolated distortion functions (note these are normalized to radius 1; we map this online in case radius changes) ...
        LinearCurveFitter cf = new LinearCurveFitter();
        myInterpFunctions = new Function[myOrigin.getDimensions()];
        for (int i = 0; i < myInterpFunctions.length; i++) {
            myInterpFunctions[i] = cf.fit(r, difference[i]);
        }
    }

    private float[][] setNoise(DecodedSource origin) {
        int nSamples = 500;

        float input0 = .5f; //our example input will be [.5; 0; 0; ...], because [0] may not have typical enough firing rates
        float[][] input = MU.uniform(nSamples, myEnsemble.getDimension(), 0);
        for (int i = 0; i < nSamples; i++) {
            input[i][0] = input0;
        }

        float[][] defaultModeOutput = MU.transpose(NEFUtil.getOutput(origin, input, SimulationMode.DEFAULT));

        myNoiseSD = new float[origin.getDimensions()];
        float[][] noiseR = new float[myNoiseSD.length][];
        for (int i = 0; i < myNoiseSD.length; i++) {
            float mean = MU.mean(defaultModeOutput[i]);
            myNoiseSD[i] = (float) Math.sqrt(MU.variance(defaultModeOutput[i], mean));

            float varSum = 0;
            float[] covarSum = new float[5]; //# of autocorrelation steps
            for (int j = 0; j < defaultModeOutput[i].length-covarSum.length; j++) {
                float centred = defaultModeOutput[i][j] - mean;
                varSum += centred*centred;
                for (int k = 0; k < covarSum.length; k++) {
                    covarSum[k] += centred*(defaultModeOutput[i][j+k+1]-mean);
                }
            }
            noiseR[i] = MU.prod(covarSum, 1f/varSum);
        }

        return noiseR;
    }

    /**
     * @see ca.nengo.neural.nef.impl.AdditiveGaussianExpressModel#getNoiseSD(float[])
     */
    public float[] getNoiseSD(float[] state, float[] directValues) {
        return myNoiseSD;
    }

    /**
     * @see ca.nengo.neural.nef.impl.AdditiveGaussianExpressModel#getDistortion(float[])
     */
    public float[] getDistortion(float[] state, float[] directValues) {
        float radius;
        if (myRadial) {
            float[] radii = myEnsemble.getRadii();
            float[] normState = new float[state.length];
            for (int i = 0; i < state.length; i++) {
                normState[i] = state[i] / radii[i];
            }
            radius = MU.pnorm(normState, 2);
        } else {
            radius = state[0] / myEnsemble.getRadii()[0];
        }

        float[] distortionError = new float[myInterpFunctions.length];
        for (int i = 0; i < distortionError.length; i++) {
            distortionError[i] = myInterpFunctions[i].map(new float[]{radius});
        }
        return distortionError;
    }

}
