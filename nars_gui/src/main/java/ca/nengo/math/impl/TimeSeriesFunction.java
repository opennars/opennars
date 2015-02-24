/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "TimeSeriesFunction.java". Description:
"A Function based on interpolation of a TimeSeries.

  A TimeSeriesFunction can be used to apply the results of a simulation as
  input to other simulations"

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
 * Created on 14-May-07
 */
package ca.nengo.math.impl;

import ca.nengo.math.Function;
import ca.nengo.model.Units;
import ca.nengo.util.InterpolatorND;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.TimeSeries1D;
import ca.nengo.util.impl.LinearInterpolatorND;
import ca.nengo.util.impl.TimeSeries1DImpl;

/**
 * <p>A Function based on interpolation of a TimeSeries.</p>
 *
 * <p>A TimeSeriesFunction can be used to apply the results of a simulation as
 * input to other simulations. </p>
 *
 * TODO: unit tests
 * TODO: this could be made more efficient for n-D series by wrapping them in 1D,
 * 		so interpolation is only done as needed
 *
 * @author Bryan Tripp
 */
public class TimeSeriesFunction extends AbstractFunction {

	private static final long serialVersionUID = 1L;

	private int myDimension;
	private TimeSeries myTimeSeries;
	private transient InterpolatorND myInterpolator;

	/**
	 * @param series TimeSeries from which to obtain Function of time
	 * @param dimension Dimension of series on which to base Function output
	 */
	public TimeSeriesFunction(TimeSeries series, int dimension) {
		super(1);
		setTimeSeries(series);
		setSeriesDimension(dimension);
	}

	/**
	 * @return TimeSeries from which to obtain Function of time
	 */
	public TimeSeries getTimeSeries() {
		return myTimeSeries;
	}

	/**
	 * @param series TimeSeries from which to obtain Function of time
	 */
	public void setTimeSeries(TimeSeries series) {
		myTimeSeries = series;
		myInterpolator = new LinearInterpolatorND(series);
	}

	/**
	 * @return Dimension of series on which to base Function output
	 */
	public int getSeriesDimension() {
		return myDimension;
	}

	/**
	 * @param dim Dimension of series on which to base Function output
	 */
	public void setSeriesDimension(int dim) {
		if (dim < 0 || dim >= myTimeSeries.getDimension()) {
			throw new IllegalArgumentException("Dimension must be between 0 and " + (myTimeSeries.getDimension() -1));
		}
		myDimension = dim;
	}

	/**
	 * @see ca.nengo.math.impl.AbstractFunction#map(float[])
	 */
	public float map(float[] from) {
		if (myInterpolator == null) {
            myInterpolator = new LinearInterpolatorND(myTimeSeries);
        }
		return myInterpolator.interpolate(from[0])[myDimension];
	}

	/**
	 * @param function A 1-dimensional Function
	 * @param start Start time
	 * @param increment Time step
	 * @param end End time
	 * @param units Units of Function output
	 * @return A TimeSeries consisting of values output by the given function over the given time range
	 */
	public static TimeSeries1D makeSeries(Function function, float start, float increment, float end, Units units) {
		int steps = (int) Math.ceil((end - start) / increment);
		float[] times = new float[steps];
		float[] values = new float[steps];
		for (int i = 0; i < steps; i++) {
			times[i] = start + i * increment;
			values[i] = function.map(new float[]{times[i]});
		}
		return new TimeSeries1DImpl(times, values, units);
	}
}
