/*
 * Created on 25-Jul-2006
 */
package ca.nengo.model.impl;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.dynamics.impl.EulerIntegrator;
import ca.nengo.dynamics.impl.SimpleLTISystem;
import ca.nengo.math.impl.GaussianPDF;
import ca.nengo.model.Noise;
import ca.nengo.model.Units;
import ca.nengo.plot.Plotter;
import ca.nengo.util.MU;
import junit.framework.TestCase;

public class NoiseFactoryTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testNothing() {
    }

    //functional test ...
    public static void main(String[] args) {
        float tau = .01f;
        DynamicalSystem dynamics = new SimpleLTISystem(new float[]{-1f/tau}, new float[][]{new float[]{1f/tau}}, MU.I(1), new float[1], new Units[]{Units.UNK});
        Integrator integrator = new EulerIntegrator(.0001f);
//      Noise noise = NoiseFactory.makeRandomNoise(1000, new GaussianPDF(0, 1));
        Noise noise = NoiseFactory.makeRandomNoise(1000, new GaussianPDF(0, 1), dynamics, integrator);
//      Noise noise = NoiseFactory.makeNullNoise(1);
//      Noise noise = NoiseFactory.makeExplicitNoise(new Function[]{new FourierFunction(1, 10, 1, -1)});

        float elapsedTime = .001f;
        int steps = 1000;
        float[] output = new float[steps];
        for (int i = 0; i < steps; i++) {
            output[i] = noise.getValue(i*elapsedTime, (i+1)*elapsedTime, 1);
        }

        Plotter.plot(output, "noise");
    }
}
