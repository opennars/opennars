/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "RK45Integrator.java". Description: 
"A variable-timestep Integrator, which uses the Dormand-Prince 4th and 5th-order 
  Runge-Kutta formulae"

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
 * Created on 29-Mar-07
 */
package ca.nengo.dynamics.impl;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.Units;
import ca.nengo.util.MU;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.LinearInterpolatorND;
import ca.nengo.util.impl.TimeSeriesImpl;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * <p>A variable-timestep Integrator, which uses the Dormand-Prince 4th and 5th-order 
 * Runge-Kutta formulae.</p> 
 * 
 * <p>This code is adapted from a GPL Octave implementation by Marc Compere (see 
 * http://users.powernet.co.uk/kienzle/octave/matcompat/scripts/ode_v1.11/ode45.m)</p>
 * 
 * <p>See also Dormand & Prince, 1980, J Computational and Applied Mathematics 6(1), 19-26.</p>
 * 
 * TODO: should re-use initial time step estimate from last integration if available
 * 
 * @author Bryan Tripp
 */
public class RK45Integrator implements Integrator {
	
	private static final long serialVersionUID = 1L;

	private static final Logger ourLogger = LogManager.getLogger(RK45Integrator.class);
	
	//The Dormand-Prince 4(5) coefficients:
	private static final float[][] a = new float[][] {
		new float[]{0},
		new float[]{1f/5f},
		new float[]{3f/40f, 9f/40f},
		new float[]{44f/45f, -56f/15f, 32f/9f},
		new float[]{19372f/6561f, -25360f/2187f, 64448f/6561f, -212f/729f},
		new float[]{9017f/3168f, -355f/33f, 46732f/5247f, 49f/176f, -5103f/18656f},
		new float[]{35f/384f, 0f, 500f/1113f, 125f/192f, -2187f/6784f, 11f/84f}
	};
    
    private static final float[] b4 = new float[]{5179f/57600f, 0f, 7571f/16695f, 393f/640f, -92097f/339200f, 187f/2100f, 1f/40f};
    private static final float[] b5 = new float[]{35f/384f, 0, 500f/1113f, 125f/192f, -2187f/6784f, 11f/84f, 0f};
    
    private static final float[] c = new float[] {0f, 1f/5f, 3f/10f, 4f/5f, 8f/9f, 1f, 1f}; //sums of a[0] to a[6]
        
    //Note: for this value Compere references p.91 of Ascher & Petzold, Computer Methods for Ordinary Differential Equations and Differential-Agebraic Equations, 
    //  Society for Industrial and Applied Mathematics (SIAM), Philadelphia, 1998
    private final double myPow = 1f/6f;
    private float myTolerance;
    
    /**
     * @param tolerance Error tolerance
     */
    public RK45Integrator(float tolerance) {
    	myTolerance = tolerance;
    }
    
    /**
     * Uses default error tolerance of 1e-6
     */
    public RK45Integrator() {
    	this(1e-6f);
    }
    
	/**
     * @return Error tolerance
     */
    public float getTolerance() {
    	return myTolerance;
    }
    
    /**
     * @param tolerance Error tolerance
     */
    public void setTolerance(float tolerance) {
    	myTolerance = tolerance;
    }
	
    /**
     * @see ca.nengo.dynamics.Integrator#integrate(ca.nengo.dynamics.DynamicalSystem, ca.nengo.util.TimeSeries)
     */
	public TimeSeries integrate(DynamicalSystem system, TimeSeries input) {		
		MU.VectorExpander times = new MU.VectorExpander();
		MU.MatrixExpander values = new MU.MatrixExpander();		
		LinearInterpolatorND interpolator = new LinearInterpolatorND(input);		

		float t0 = input.getTimes()[0];
		float tfinal = input.getTimes()[input.getTimes().length - 1];		
		float hmax = (tfinal - t0) / 2.5f;
		float hmin = (tfinal - t0) / 1e9f;
		float h = (tfinal - t0) / 100f; //initial guess at step size
		float t = t0;
		float[] x = system.getState();
		
		times.add(t);
		values.add(x);

		float[][] k = new float[7][]; //7 stages for each step (although one of them is shared by adjacent steps) 
		for (int i = 0; i < k.length; i++) {
			k[i] = new float[x.length];
		}

		//Compute the first stage prior to the main loop (subsequently the first stage is assigned from the previous 
		//step's last stage).
		float[] u = interpolator.interpolate(t);
		k[0] = system.f(t, u);
		
		while (t < tfinal && h >= hmin) {
			if (t + h > tfinal) h = tfinal - t;

			for (int j = 0; j < 6; j++) {
				float stageTime = t + c[j+1]*h;
				u = interpolator.interpolate(stageTime);
								
				float[] ka = new float[x.length];
				for (int q = 0; q < x.length; q++) {
					for (int r = 0; r <= j; r++) {
						ka[q] += k[r][q] * a[j+1][r]; //note: this doesn't look like a matrix-vector product because our k is transposed 
					}
				}
				system.setState(MU.sum(x, MU.prod(ka, h)));
				k[j+1] = system.f(stageTime, u);
			}
			
			float[][] kt = MU.transpose(k);
			float[] x4 = MU.sum(x, MU.prod(MU.prod(kt, b4), h)); //4th order estimate
			float[] x5 = MU.sum(x, MU.prod(MU.prod(kt, b5), h)); //5th order estimate
			
			float[] gamma1 = MU.difference(x5, x4); //truncation error
			
			float delta = MU.pnorm(gamma1, -1); //actual error
			float tau = myTolerance * Math.max(MU.pnorm(x, -1), 1f); //allowable error

			//Update the solution only if the error is acceptable
			if (delta <= tau) {
				t = t + h;
				x = x5;
				times.add(t);
				system.setState(x);
				values.add(system.g(t, u));
				k[0] = k[6]; //re-use last stage as first stage of next step
			}
			
			//Update step size
			if (delta == 0f) delta = 1e-16f;
			if ( !(delta >= 0) && !(delta < 0) ) {
				h = h / 2f;
			} else {
				boolean hWasAlreadyMinimum = (h == hmin); 
				h = Math.min(hmax, 0.8f * h * (float) Math.pow(tau/delta, myPow));
				if (h < hmin && !hWasAlreadyMinimum) h = hmin; //give it one more chance at hmin
			}
		}
		
		if (t < tfinal) {
			ourLogger.warn("Step size grew too small -- integration aborted.");
		}
		
		Units[] units = new Units[system.getOutputDimension()];
		for (int i = 0; i < units.length; i++) {
			units[i] = system.getOutputUnits(i);
		}
		
		return new TimeSeriesImpl(times.toArray(), values.toArray(), units);
	}

	@Override
	public Integrator clone() throws CloneNotSupportedException {
		return (Integrator) super.clone();
	}
	
}
