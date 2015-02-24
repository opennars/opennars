/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "EulerIntegrator.java". Description:
"Euler's method of numerical integration: x(t+h) ~ x(t) + hx'(t)

  TODO: test
  TODO: should there be some means for aborting early (aside from exceptions, e.g"

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
 * Created on 1-Jun-2006
 */
package ca.nengo.dynamics.impl;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.Units;
import ca.nengo.util.MU;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.LinearInterpolatorND;
import ca.nengo.util.impl.TimeSeriesImpl;

/**
 * Euler's method of numerical integration: x(t+h) ~ x(t) + h*x'(t)
 *
 * TODO: test
 * TODO: should there be some means for aborting early (aside from exceptions, e.g. if output converges to constant)?
 *
 * @author Bryan Tripp
 */
public class EulerIntegrator implements Integrator {

	private static final long serialVersionUID = 1L;

	//shrink factor to avoid possible very small step at end due to float comparison
	//TODO: solve this problem more robustly
	private static final float SHRINK = .99999f;

	private float h;

	/**
	 * @param stepSize Timestep size (dt)
	 */
	public EulerIntegrator(float stepSize) {
		h = stepSize;
	}

	/**
	 * Uses default step size of .0001
	 */
	public EulerIntegrator() {
		this(.0001f);
	}

	/**
	 * @return get Timestep size
	 */
	public float getStepSize() {
		return h;
	}

	/**
	 * @param stepSize Timestep size
	 */
	public void setStepSize(float stepSize) {
		h = stepSize;
	}

	/**
	 * Linear interpolation is performed between given input points.
	 *
	 * @see ca.nengo.dynamics.Integrator#integrate(ca.nengo.dynamics.DynamicalSystem, ca.nengo.util.TimeSeries)
	 */
	public TimeSeries integrate(DynamicalSystem system, TimeSeries input) {
		float[] inTimes = input.getTimes();
		float timespan = inTimes[inTimes.length-1] - inTimes[0];
		int steps = (int) Math.ceil(timespan*SHRINK / h);

		LinearInterpolatorND interpolator = new LinearInterpolatorND(input);

		float[] times = new float[steps+1];
		float[][] values = new float[steps+1][];
		times[0] = inTimes[0];
		values[0] = system.g(times[0], input.getValues()[0]);

		float t = inTimes[0];
		for (int i = 1; i <= steps; i++) {
			float dt = (i < steps) ? h : (inTimes[inTimes.length-1] - t);
			t = t + dt;
			times[i] = t;

			float[] u = interpolator.interpolate(t);
			float[] dxdt = system.f(t, u);
			system.setState(MU.sum(system.getState(), MU.prod(dxdt, dt)));
			values[i] = system.g(t, u);
		}

		Units[] units = new Units[system.getOutputDimension()];
		for (int i = 0; i < units.length; i++) {
			units[i] = system.getOutputUnits(i);
		}

		return new TimeSeriesImpl(times, values, units);
	}

	@Override
	public Integrator clone() throws CloneNotSupportedException {
		return (Integrator) super.clone();
	}


}
