/*
 * Created on 26-May-2006
 */
package ca.nengo.model.impl;

import ca.nengo.TestUtil;
import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.neural.impl.PreciseSpikeOutputImpl;
import ca.nengo.neural.impl.SpikeOutputImpl;
import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Unit tests for LinearExponentialTermination.
 * 
 * @author Bryan Tripp
 */
public class LinearExponentialTargetTest extends TestCase {

    private final static Logger ourLogger = LogManager.getLogger(LinearExponentialTargetTest.class);

    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * Test method for 'ca.bpt.cn.model.impl.LinearExponentialTermination.getName()'
     */
    public void testGetName() {
        String name = "test";
        LinearExponentialTarget let = new LinearExponentialTarget(null, name, new float[0], 0f);
        assertEquals(name, let.getName());
    }

    /*
     * Test method for 'ca.bpt.cn.model.impl.LinearExponentialTermination.getDimensions()'
     */
    public void testGetDimensions() {
        LinearExponentialTarget let = new LinearExponentialTarget(null, "test", new float[1], 0f);
        assertEquals(1, let.getDimensions());
        let = new LinearExponentialTarget(null, "test", new float[2], 0f);
        assertEquals(2, let.getDimensions());
    }

    /*
     * Test method for 'ca.bpt.cn.model.impl.LinearExponentialTermination.getProperty(String)'
     */
    public void testGetProperty() throws StructuralException {
        LinearExponentialTarget let = new LinearExponentialTarget(null, "test", new float[1], 1.5f);

        TestUtil.assertClose(1.5f, let.getTau(), 1e-5f);

        let.setTau(2.5f);
        TestUtil.assertClose(2.5f, let.getTau(), 1e-5f);

        assertFalse(let.getModulatory());
        let.setModulatory(true);
        assertTrue(let.getModulatory());
    }

    /*
     * Test method for 'ca.bpt.cn.model.impl.LinearExponentialTermination.reset(boolean)'
     */
    public void testReset() throws SimulationException {
        LinearExponentialTarget let = new LinearExponentialTarget(null, "test", new float[]{2f}, 1f);
        let.apply(new RealOutputImpl(new float[]{1f}, Units.ACU, 0));

        float current = let.updateCurrent(false, 1f, 0f);
        assertTrue(current > 1.99f);

        let.reset(false);

        current = let.updateCurrent(false, 0, 0);
        assertTrue(current < .01f);
    }

    /*
     * Test method for 'ca.bpt.cn.model.impl.LinearExponentialTermination.setValues(InstantaneousOutput)'
     */
    public void testSetValues() throws SimulationException {
        LinearExponentialTarget let = new LinearExponentialTarget(null, "test", new float[]{1f, 2f, 3f}, 1f);

        try {
            let.apply(new SpikeOutputImpl(new boolean[]{true}, Units.SPIKES, 0));
            fail("Should have thrown exception because dimension of input is 1 (should be 3)");
        } catch (SimulationException e) {} //exception is expected

        let.apply(new SpikeOutputImpl(new boolean[]{true, false, true}, Units.SPIKES, 0));
        float current = let.updateCurrent(true, 0, 0);
        assertClose(4f, current, .01f);

        let.reset(false);

        let.apply(new RealOutputImpl(new float[]{1f, .1f, .01f}, Units.SPIKES_PER_S, 0));
        current = let.updateCurrent(false, 1, 0);
        assertClose(1.23f, current, .001f);
    }

    /*
     * Test method for 'ca.bpt.cn.model.impl.LinearExponentialTermination.updateCurrent(boolean, float, float)'
     */
    public void testUpdateCurrent() throws SimulationException {
        float tol = .0001f;
        float tauPSC = .01f;
        LinearExponentialTarget let = new LinearExponentialTarget(null, "test", new float[]{1f}, tauPSC);
        assertClose(0, let.updateCurrent(false, 0, 0), tol);

        let.apply(new SpikeOutputImpl(new boolean[]{false}, Units.SPIKES, 0));
        assertClose(0, let.updateCurrent(true, 0, 0), tol);

        let.apply(new RealOutputImpl(new float[]{0f}, Units.SPIKES_PER_S, 0));
        assertClose(0, let.updateCurrent(false, 1f, 0), tol);

        let.apply(new SpikeOutputImpl(new boolean[]{true}, Units.SPIKES, 0));
        assertClose(1f/tauPSC, let.updateCurrent(true, 0, 0), tol);
        assertClose(0, let.updateCurrent(false, 0, tauPSC), tol); //which illustrates that we need time steps << tauPSC

        let.reset(false);

        //decay a spike to 0
        let.apply(new SpikeOutputImpl(new boolean[]{true}, Units.SPIKES, 0));
        float current = let.updateCurrent(true, 0, 0);
        for (int i = 0; i < 150; i++) {
            current = let.updateCurrent(false, 0, tauPSC/10f);
        }
        ourLogger.debug("current: " + current);
        assertClose(0f, current, tol);

        //low-pass filter constant rate input
        let.apply(new RealOutputImpl(new float[]{10f}, Units.SPIKES_PER_S, 0));
        current = 0;
        for (int i = 0; i < 120; i++) {
            float lastCurrent = current;
            current = let.updateCurrent(false, tauPSC/10f, tauPSC/10f);
            if (i % 20 == 0) {
                assertTrue(current > lastCurrent);
            }
        }
        ourLogger.debug("current: " + current);
        assertClose(10f, current, tol);
    }

    public void testPreciseUpdateCurrent() throws SimulationException {
        float tauPSC = .01f;
        int steps=9;
        float time=0.001f;

        LinearExponentialTarget let = new LinearExponentialTarget(null, "test", new float[]{1f}, tauPSC);
        PreciseSpikeOutputImpl spikeAt0=new PreciseSpikeOutputImpl(new float[]{0f},Units.SPIKES,0);
        PreciseSpikeOutputImpl spikeAtHalf=new PreciseSpikeOutputImpl(new float[]{time/2},Units.SPIKES,0);
        PreciseSpikeOutputImpl spikeAt1=new PreciseSpikeOutputImpl(new float[]{time},Units.SPIKES,0);
        PreciseSpikeOutputImpl spikeNever=new PreciseSpikeOutputImpl(new float[]{-1f},Units.SPIKES,0);

        SpikeOutputImpl simpleSpikeYes=new SpikeOutputImpl(new boolean[]{true},Units.SPIKES,0);
        SpikeOutputImpl simpleSpikeNo=new SpikeOutputImpl(new boolean[]{false},Units.SPIKES,0);



        let.reset(false);
        float[] currents1=getCurrents(let,simpleSpikeYes,time,steps);
        let.reset(false);
        float[] currents2=getCurrents(let,spikeAt0,time,steps);

        assertTrue(currents1[0]==currents2[0]);
        assertTrue(currents1[1]==currents2[1]);
        assertTrue(currents1[steps-1]==currents2[steps-1]);


        let.reset(false);
        currents1=getCurrents(let,simpleSpikeNo,time,steps);
        currents1=getCurrents(let,simpleSpikeYes,time,steps);

        let.reset(false);
        currents2=getCurrents(let,spikeAt1,time,steps);
        currents2=getCurrents(let,spikeNever,time,steps);

        assertTrue(currents1[1]==currents2[1]);
        assertTrue(currents1[steps-1]==currents2[steps-1]);


        steps=1;
        let.reset(false);
        currents1=getCurrents(let,spikeAt0,time,steps);
        let.reset(false);
        currents2=getCurrents(let,spikeAtHalf,time,steps);

        assertTrue(currents1[0]>currents2[1]);
        assertTrue(currents2[1]>currents1[1]);

    }

    public void testGetWeights()
    {
        float[] weights = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        LinearExponentialTarget term = new LinearExponentialTarget(null, "test", weights, 0.0f);

        float[] retweights = term.getWeights();

        assertTrue(weights.length == retweights.length);

        for(int i = 0; i < weights.length; i++) {
            assertTrue(weights[i] == retweights[i]);
        }
    }

    public void testSetWeights()
    {
        float[] weights = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        float[] newweights = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
        LinearExponentialTarget term = new LinearExponentialTarget(null, "test", weights, 0.0f);
        term.setWeights(newweights, true);

        float[] retweights = term.getWeights();

        for(int i = 0; i < retweights.length; i++) {
            assertTrue(newweights[i] == retweights[i]);
        }

        float[] badweights = new float[]{0.5f};
        term.setWeights(badweights, true);

        for(int i = 0; i < retweights.length; i++) {
            assertTrue(newweights[i] == retweights[i]);
        }

        term.reset(false);
        retweights = term.getWeights();

        for(int i = 0; i < retweights.length; i++) {
            assertTrue(newweights[i] == retweights[i]);
        }
    }

    private float[] getCurrents(LinearExponentialTarget let, InstantaneousOutput values, float time, int steps)
    throws SimulationException {
        let.apply(values);

        float dt=time/steps;
        float[] currents=new float[steps+1];

        currents[0]=let.updateCurrent(true,0,0);
        for (int i=0; i<steps; i++) {
            currents[i+1]=let.updateCurrent(false,dt,dt);
        }
        return currents;
    }

    //approximate assertEquals for floats
    private void assertClose(float target, float value, float tolerance) {
        assertTrue(value > target - tolerance && value < target + tolerance);
    }



}
