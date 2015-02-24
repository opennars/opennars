/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "FourierFunction.java". Description: 
"A Function that is composed of a finite number of sinusoids.
  
  @author Bryan Tripp"

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

/*
 * Created on 6-Jun-2006
 */
package ca.nengo.math.impl;

import ca.nengo.math.Function;

import java.util.Random;

/**
 * A Function that is composed of a finite number of sinusoids.
 * 
 * @author Bryan Tripp
 */
public class FourierFunction implements Function {

	private static final long serialVersionUID = 1L;
	
	private float[][] myFrequencies;
	private float[] myAmplitudes;
	private float[][] myPhases;

    private float myFundamental;
    private float myCutoff;
    private float myRms;
    private long mySeed;
	
	/**
	 * Creates a 1-dimensional function composed of explicitly defined sinusoids. 
	 * 
	 * @param frequencies Explicit list of frequencies of sinusoidal components of the 
	 * 		function (Hz)
	 * @param amplitudes The amplitude of each component 
	 * @param phases The phase lead of each component (from -.5 to .5)
	 */
	public FourierFunction(float[] frequencies, float[] amplitudes, float[] phases) {
		set(new float[][]{frequencies}, amplitudes, new float[][]{phases});
	}
	
	/**
	 * Creates an n-dimensional function composed of explicitly defined sinusoids. 
	 * 
	 * @param frequencies Lists of frequencies (length n; ith members define frequencies of ith component along each dimension)  
	 * @param amplitudes The amplitude of each component
	 * @param phases Lists of phases (length n; ith members define phases of ith component along each dimension)
	 */
	public FourierFunction(float[][] frequencies, float[] amplitudes, float[][] phases) {
		set(frequencies, amplitudes, phases);
	}
	/**
	 * Creates a 1-dimensional band-limited noise function with specified parameters. 
	 *  
	 * @param fundamental The fundamental frequency (Hz), i.e., frequency step size.
	 * @param cutoff The high-frequency limit (Hz)
	 * @param rms The root-mean-squared function amplitude
	 * @param seed Random seed
	 * @param type The type of noise: 0 = white; 1 = pink; 
	 */
	public FourierFunction(float fundamental, float cutoff, float rms, long seed, int type) {
		int n = (int) Math.floor(cutoff / fundamental);
		
		float[][] frequencies = new float[][]{new float[n]};
		float[] amplitudes = new float[n];
		float[][] phases = new float[][]{new float[n]};
		Random random = new Random(seed); 

        myFundamental = fundamental;
        myCutoff = cutoff;
        myRms = rms;
        mySeed = seed;
		
		for (int i = 0; i < n; i++) {
			frequencies[0][i] = fundamental * (i+1);
			
			if (type == 1) {
				amplitudes[i] = random.nextFloat() * fundamental / frequencies[0][i]; //decreasing amplitude = pink noise
			} else if (type == 0) {
				amplitudes[i]	= random.nextFloat(); //constant amplitude = white noise
			} else {
				throw new IllegalArgumentException("FourierFunction noise type is invalid");
			}
			phases[0][i] = -.5f + 2f * random.nextFloat();
		}
		
		//find amplitude over one period and rescale to specified amplitude 
		int samplePoints = 500;
		float dx = (1f / fundamental) / samplePoints;
		double sumSquared = 0;
		for (int i = 0; i < samplePoints; i++) {
			float val = getValue(new float[]{i*dx}, frequencies, amplitudes, phases);
			sumSquared += val * val;
		}
		double unscaledRMS = Math.sqrt(sumSquared / samplePoints);
		
		for (int i = 0; i < n; i++) {
			amplitudes[i] = amplitudes[i] * rms / (float) unscaledRMS;
		}
				set(frequencies, amplitudes, phases);
	}
	/**
	 * Creates a 1-dimensional band-limited pink noise function with specified parameters. 
	 *  
	 * @param fundamental The fundamental frequency (Hz), i.e., frequency step size.
	 * @param cutoff The high-frequency limit (Hz)
	 * @param rms The root-mean-squared function amplitude
	 * 	@param seed Random seed
	 */
	public FourierFunction(float fundamental, float cutoff, float rms, long seed) {
		this(fundamental, cutoff, rms, seed, 1);
	}
	
	private void set(float[][] frequencies, float[] amplitudes, float[][] phases) {
		if (frequencies.length != phases.length) {
			throw new IllegalArgumentException("Lists of frequencies and phases must have same dimension");
		}
		
		if (frequencies[0].length != amplitudes.length || phases[0].length != amplitudes.length) {
			throw new IllegalArgumentException("Frequencies, amplitudes, and phases must have same length in each dimension");
		}
		
		myFrequencies = frequencies;
		myAmplitudes = amplitudes;
		myPhases = phases;
	}
	
	/**
	 * @see ca.nengo.math.Function#getDimension()
	 */
	public int getDimension() {
		return myFrequencies.length;
	}
	
	/**
	 * @return Number of frequency components  
	 */
	public int getComponents() {
		return myFrequencies[0].length;
	}
	
	/**
	 * @return Lists of frequencies (length n; ith members define frequencies of ith component along each dimension)
	 */
	public float[][] getFrequencies() {
		return myFrequencies;
	}
	
	/**
	 * @param frequencies Lists of frequencies (length n; ith members define frequencies of ith component along each dimension)
	 */
	public void setFrequencies(float[][] frequencies) {
		set(frequencies, getAmplitudes(), getPhases());
	}
	
	/**
	 * @return The amplitude of each component
	 */
	public float[] getAmplitudes() {
		return myAmplitudes;
	}
	
	/**
	 * @param amplitudes The amplitude of each component
	 */
	public void setAmplitudes(float[] amplitudes) {
		set(getFrequencies(), amplitudes, getPhases());
	}
	
	/**
	 * @return Lists of phases (length n; ith members define phases of ith component along each dimension)
	 */
	public float[][] getPhases() {
		return myPhases;
	}

	/**
	 * @param phases Lists of phases (length n; ith members define phases of ith component along each dimension)
	 */
	public void setPhases(float[][] phases) {
		set(getFrequencies(), getAmplitudes(), phases);
	}
	
	/**
	 * @see ca.nengo.math.Function#map(float[])
	 */
	public float map(float[] from) {
		return getValue(from, myFrequencies, myAmplitudes, myPhases);
	}
	
	/**
	 * @see ca.nengo.math.Function#multiMap(float[][])
	 */
	public float[] multiMap(float[][] from) {
		float[] result = new float[from.length];
		
		for (int i = 0; i < from.length; i++) {
			result[i] = getValue(from[i], myFrequencies, myAmplitudes, myPhases);
		}
		
		return result;
	}

    /**
     * @return The fundamental frequency used to generate the function if it was provided.
     */
    public float getFundamental() {
        return myFundamental;
    }

    /**
     * @return The cutoff frequency used to generate the function if it was provided.
     */
    public float getCutoff() {
        return myCutoff;
    }

    /**
     * @return The rms amplitude used to generate the function if it was provided.
     */
    public float getRms() {
        return myRms;
    }

    /**
     * @return The seed used to generate the function if it was provided.
     */
    public long getSeed() {
        return mySeed;
    }


	private static float getValue(float[] x, float[][] f, float[] a, float[][] p) {
		float result = 0f;
		
		for (int i = 0; i < f[0].length; i++) {
			float component = 1;
			for (int j = 0; j < x.length; j++) {
				component = component * (float) Math.sin(2d * Math.PI * (f[j][i] * x[j] + p[j][i]));
			}
			result += a[i] * component;
		}
		
		return result;
	}
	
	@Override
	public Function clone() throws CloneNotSupportedException {
		return new FourierFunction(myFrequencies.clone(), myAmplitudes.clone(), myPhases.clone());		
	}
	
}
