/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "PoissonSpikeGenerator.java". Description:
"A phenomenological SpikeGenerator that produces spikes according to a Poisson
  process with a rate that varies as a function of current"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU
Public License license (the GPL License), in which case the provisions of GPL
License are applicable  instead of those above. If you wish to allow use of your
version of this file only under the terms of the GPL License and not to allow
others to use your version of this file under the MPL, indicate your decision
by deleting the provisions above and replace  them with the notice and other
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
 */

package ca.nengo.neural.neuron.impl;


import ca.nengo.math.Function;
import ca.nengo.math.PDFTools;
import ca.nengo.math.impl.AbstractFunction;
import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.SimulationMode;
import ca.nengo.model.Units;
import ca.nengo.model.impl.RealOutputImpl;
import ca.nengo.neural.impl.SpikeOutputImpl;
import ca.nengo.neural.neuron.SpikeGenerator;
import ca.nengo.util.MU;

/**
 * Rate Function Spike Generator
 * 
 * This class generates spikes based on a user defined function.
 * Initially, spikes should be generated without any sort of random distribution
 * (eventually this may change)
 * Modified version of LIFSpikeGenerator, original code taken from other
 * SpikeGenerator classes.
 * 
 * @author Bryan Tripp
 */

public class RateFunctionSpikeGenerator implements SpikeGenerator {

    private static final long serialVersionUID = 1L;

    private static final SimulationMode[] ourSupportedModes =
        new SimulationMode[]{SimulationMode.DEFAULT, SimulationMode.CONSTANT_RATE, SimulationMode.RATE};

    private Function myRateFunction;
    private SimulationMode myMode;

    private float myVoltage;
    private final boolean smooth;

    /**
     * @param rateFunction Maps input current to spiking rate
     */
    public RateFunctionSpikeGenerator(Function rateFunction) {
        this(rateFunction,false);
    }


    /**
     * @param rateFunction Maps input current to spiking rate
     * @param smooth Apply smoothing?
     */
    public RateFunctionSpikeGenerator(Function rateFunction, boolean smooth) {
        setRateFunction(rateFunction);
        myMode = SimulationMode.DEFAULT;
        myVoltage=0;
        this.smooth=smooth;
    }

    /**
     * @return Function that maps input current to spiking rate
     */
    public Function getRateFunction() {
        return myRateFunction;
    }

    /**
     * @param function Function that maps input current to spiking rate
     */
    public void setRateFunction(Function function) {
        if (function.getDimension() != 1) {
            throw new IllegalArgumentException("Function must be one-dimensional (mapping from driving current to rate)");
        }
        myRateFunction = function;
    }

    /**
     * @see ca.nengo.neural.neuron.SpikeGenerator#run(float[], float[])
     */
    public InstantaneousOutput run(float[] time, float[] current) {
        InstantaneousOutput result = null;

        //calculates rate as a function of current
        if (myMode.equals(SimulationMode.CONSTANT_RATE)) {
            result = new RealOutputImpl(new float[]{myRateFunction.map(new float[]{current[0]})}, Units.SPIKES_PER_S, time[time.length-1]);
        } else if (myMode.equals(SimulationMode.RATE)) {
            float totalTimeSpan = time[time.length-1] - time[0];
            float ratePerSecond = myRateFunction.map(new float[]{MU.mean(current)});
            float ratePerStep = totalTimeSpan * ratePerSecond;
            float numSpikes = ratePerStep;

            result = new RealOutputImpl(new float[]{numSpikes / totalTimeSpan}, Units.SPIKES_PER_S, time[time.length-1]);
        } else if (smooth) {
            boolean spike = false;
            for (int i = 0; i < time.length - 1; i++) {
                float timeSpan = time[i+1] - time[i];
                float rate = myRateFunction.map(new float[]{current[i]});

                myVoltage+=timeSpan*rate;
                if (myVoltage>1) {
                    myVoltage-=1;
                    spike=true;
                }
            }

            result = new SpikeOutputImpl(new boolean[]{spike}, Units.SPIKES, time[time.length-1]);

        } else { //default mode is spiking mode
            boolean spike = false;
            for (int i = 0; i < time.length - 1 && !spike; i++) {
                float timeSpan = time[i+1] - time[i];

                float rate = myRateFunction.map(new float[]{current[i]});
                double probNoSpikes = Math.exp(-rate*timeSpan);
                spike = (PDFTools.random() > probNoSpikes);
            }

            result = new SpikeOutputImpl(new boolean[]{spike}, Units.SPIKES, time[time.length-1]);
        }

        return result;
    }

    /**
     * @see ca.nengo.model.SimulationMode.ModeConfigurable#getMode()
     */
    public SimulationMode getMode() {
        return myMode;
    }

    /**
     * @see ca.nengo.model.SimulationMode.ModeConfigurable#setMode(ca.nengo.model.SimulationMode)
     */
    public void setMode(SimulationMode mode) {
        myMode = SimulationMode.getClosestMode(mode, ourSupportedModes);
    }



    /**
     * useless method for current implementations
     * 
     * @see ca.nengo.model.Resettable#reset(boolean)
     */
    public void reset(boolean randomize) {

    }

    @Override
    public SpikeGenerator clone() throws CloneNotSupportedException {
        RateFunctionSpikeGenerator result = (RateFunctionSpikeGenerator) super.clone();
        result.myRateFunction = myRateFunction.clone();
        return result;
    }



    /**
     * Spike generator using the rate function PoiraziDendriteSigmoid documented
     * in Poirazi et al.,2003
     */
    public static class PoiraziDendriteSigmoidFactory implements SpikeGeneratorFactory
    {
        private static final long serialVersionUID = 1L;

        public SpikeGenerator make() {
            Function ps = new PoiraziDendriteSigmoid();
            return new RateFunctionSpikeGenerator(ps);
        }
    }

    /**
     * Function from Poirazi et al.,2003
     */
    public static class PoiraziDendriteSigmoid extends AbstractFunction
    {
        private static final long serialVersionUID = 1L;

        /**
         * 1D function
         */
        public PoiraziDendriteSigmoid() {
            super(1);
        }

        //sigmoid function
        //In terms of active synapses, firing rate of each subunit can be predicted by the formula...
        //... s(n) = 1/(1+exp((3.6-n)/0.20)) + 0.30n + 0.0114n^2 (Poirazi et al.,2003)
        @Override
        public float map(float[] from) {
            float n = (float) ((4.5*(from[0])) + 4.5);
            float answer;

            answer = (float)((1/(1+Math.exp((3.6-n)/0.2))) + (0.30*n) + 0.0114*(n*n));

            if (answer<0)
            {
                answer = 0;
            }
            return answer;
        }

    }

    /**
     * Currently unused, but could act as a model of the soma
     */
    public static class PoiraziSomaSigmoid extends AbstractFunction
    {
        private static final long serialVersionUID = 1L;

        /**
         * 1D function
         */
        public PoiraziSomaSigmoid() {
            super(1);
        }

        //output function
        @Override
        public float map(float[] from) {
            //x is going to be scaled to allow firing rates between 0 and 70Hz, as seen in Figure 5 of Poirazi et al.,2003
            //According to Schwindt el al., 1997: Layer 5 pyramidal neurons exhibit firing rates averaging between 10 and 74Hz
            float x = (float) ((37.5*from[0])+37.5);
            float answer;

            answer = (float)((0.96*x)/(1+(1509*Math.exp(-0.26*x))))+5;


            if (answer<0)
            {
                answer = 0;
            }
            return answer;

        }
    }


}

















































