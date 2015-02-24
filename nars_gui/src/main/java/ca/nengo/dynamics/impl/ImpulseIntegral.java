/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "ImpulseIntegral.java". Description: 
"A tool for finding the integral of the impulse response of an LTI system"

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
 * Created on 16-Jun-2006
 */
package ca.nengo.dynamics.impl;

import Jama.Matrix;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.Units;
import ca.nengo.util.MU;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeriesImpl;

/**
 * <p>A tool for finding the integral of the impulse response of an LTI system. 
 * The impulse response of an LTI system is the matrix D*d(t) + C*exp(A*t)*B, where
 * A,B,C,D are defined as usual and d(t) is an impulse. We are interested here in the 
 * integral of this matrix (which we may want so that we can normalize it somehow).</p>
 * 
 * <p>There are many ways to calculate e^At (see Moler & Van Loan, 2003). Here 
 * we use simulation, which is simple to implement, and numerically attractive when 
 * the result is needed at many t.</p>
 * 
 * @author Bryan Tripp
 */
public class ImpulseIntegral {


	/**
	 * @param system The system for which integrals of impulse responses are needed
	 * @return Integrals of impulse responses. This is a matrix with the same dimensions as the 
	 * 		passthrough	matrix of the system. Each column is the integral of the response to an 
	 * 		impulse at the corresponding input.  
	 */
	public static float[][] integrate(LTISystem system) {
		
		int inputs = system.getInputDimension();
		float[][] impulseIntegrals = new float[inputs][];
		for (int i = 0; i < inputs; i++) {
			impulseIntegrals[i] = integrate(system, i);
		}		
		
		return MU.transpose(impulseIntegrals);
	}
	
	private static float[] integrate(LTISystem system, int input) {
		double maxTimeConstant = 10000; //big enough to cover plasticity? 
		double minTimeConstant = .0001; //small enough to cover channel kinetics (including auditory neurons) 
		
		float[][] A = system.getA(0f);
		Matrix m = new Matrix(MU.convert(A));
		double[] eigenvalues = m.eig().getRealEigenvalues();
		double[] eigRange = range(eigenvalues, minTimeConstant, maxTimeConstant);
		
		//We will give an approximation of an impulse, then let the system decay for 10 X the longest time constant,  
		//then step in increments of 1/2 X the longest time constant until there is little change in the integral
		Integrator integrator = new EulerIntegrator(-1f / (10f * (float) eigRange[0]));
		
		float[] integral = new float[system.getOutputDimension()];
		
		system.setState(new float[eigenvalues.length]);
		
		float[] impulse = new float[system.getInputDimension()];
		float pulseWidth = (float) minTimeConstant; //prevents saturation
		float pulseHeight = 1f / pulseWidth;
		impulse[input] = pulseHeight;
		
		float[] zero = new float[system.getInputDimension()];
		Units[] units = new Units[system.getInputDimension()];		
		
		TimeSeries pulse = integrator.integrate(system, 
				new TimeSeriesImpl(new float[]{0f, pulseWidth}, new float[][]{impulse, impulse}, units));
		integral = integrate(pulse);		
		
		float decayTime = -10f / (float) eigRange[1];		
		TimeSeries decay = integrator.integrate(system, 
				new TimeSeriesImpl(new float[]{0f, decayTime}, new float[][]{zero, zero}, units)); //time-invariant, so we can start at 0
		float[] increment = integrate(decay);
		integral = MU.sum(integral, increment);
	
		float stepTime = -.5f / (float) eigRange[1];
		do {
			decay = integrator.integrate(system, 
					new TimeSeriesImpl(new float[]{0f, stepTime}, new float[][]{zero, zero}, units));
			increment = integrate(decay);
			integral = MU.sum(integral, increment);
		} while (substantialChange(integral, increment, .001f * stepTime / decayTime));

		return integral;
	}
	
	//make sure eigenvalues (real parts) are OK and return their range
	private static double[] range(double[] eigenvalues, double minTimeConstant, double maxTimeConstant) {
		double minEig = eigenvalues[0]; 
		double maxEig = eigenvalues[0];
		for (int i = 0; i < eigenvalues.length; i++) {
			if (eigenvalues[i] > -1d/maxTimeConstant) {
				throw new IllegalArgumentException("The system must have eigenvalues with real parts < " + (-1d/maxTimeConstant));
			}
			if (eigenvalues[i] < -1d/minTimeConstant) {
				throw new IllegalArgumentException("The system must have eigenvalues with real parts > " + (-1d/minTimeConstant));
			}
			
			if (eigenvalues[i] < minEig) {
				minEig = eigenvalues[i];
			}
			if (eigenvalues[i] > maxEig) {
				maxEig = eigenvalues[i];
			}
		}
		
		if (maxEig / minEig > 1000) {
			throw new IllegalArgumentException("This will take too long. We don't have an integrator for stiff systems yet");
		}

		return new double[]{minEig, maxEig};
	}
	
	//true if any element of the increment is > integral*proportion
	private static boolean substantialChange(float[] integral, float[] increment, float proportion) {
		boolean result = false;
		
		for (int i = 0; i < integral.length && !result; i++) {
			if (increment[i] > proportion*integral[i]) {
				result = true;
			}
		}
		
		return result;
	}
	
	//box time integral (it's assumed there are at least 2 time series entries)
	private static float[] integrate(TimeSeries series) {
		float[] result = new float[series.getDimension()];
		
		float[] times = series.getTimes();
		float[][] values = series.getValues();
		
		for (int i = 1; i < times.length; i++) {
			result = MU.sum(result, MU.prod(values[i-1], times[i]-times[i-1]));
		}
				
		return result;
	}
	
}
