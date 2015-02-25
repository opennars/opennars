/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "DataUtils.java". Description: 
"Tools manipulating TimeSeries and SpikePattern data.
  
  TODO: test; remove Plotter.filter() references
  
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
 * Created on 14-Nov-07
 */
package ca.nengo.util;

import ca.nengo.dynamics.Integrator;
import ca.nengo.dynamics.impl.EulerIntegrator;
import ca.nengo.dynamics.impl.LTISystem;
import ca.nengo.dynamics.impl.SimpleLTISystem;
import ca.nengo.model.Group;
import ca.nengo.model.Node;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.neuron.impl.SpikingNeuron;
import ca.nengo.util.impl.SpikePatternImpl;
import ca.nengo.util.impl.TimeSeries1DImpl;
import ca.nengo.util.impl.TimeSeriesImpl;

import java.util.Arrays;

/**
 * Tools manipulating TimeSeries and SpikePattern data.
 * 
 * TODO: test; remove Plotter.filter() references
 * 
 * @author Bryan Tripp
 */
public class DataUtils {

	/**
	 * @param series A TimeSeries to which to apply a 1-D linear filter
	 * @param tau Filter time constant
	 * @return Filtered TimeSeries
	 */
	public static TimeSeries filter(TimeSeries series, float tau) {
		float[] times = series.getTimes();
		float meanStepSize = (times[times.length-1] - times[1]) / times.length;
		float integrationStepSize = Math.min(meanStepSize/2f, tau/2f);
		
		Integrator integrator = new EulerIntegrator(integrationStepSize);
		
		int dim = series.getDimension();
		float[] A = new float[dim];
		float[][] B = new float[dim][];
		float[][] C = new float[dim][];
		for (int i = 0; i < dim; i++) {
			A[i] = -1f / tau;
			B[i] = new float[dim];
			B[i][i] = 1f;
			C[i] = new float[dim];
			C[i][i] = 1f / tau;
		}		
		LTISystem filter = new SimpleLTISystem(A, B, C, new float[dim], series.getUnits());
		
		return integrator.integrate(filter, series);		
	}
	
	/**
	 * @param series An n-dimensional TimeSeries
	 * @param dim Index (less than n-1) of dimension to extract 
	 * @return One-dimensional TimeSeries composed of extracted dimension
	 */
	public static TimeSeries extractDimension(TimeSeries series, int dim) {
		if (dim < 0 || dim >= series.getDimension()) {
			throw new IllegalArgumentException("Dimension " + dim 
					+ " is out of range; should be between 0 and " + (series.getDimension()-1));
		}
		
		return new TimeSeries1DImpl(series.getTimes(), MU.transpose(series.getValues())[dim], series.getUnits()[dim]);
	}
	
	/**
	 * @param series Any TimeSeries
	 * @param start Beginning of extracted portion of series
	 * @param end End of extracted portion of series 
	 * @return A TimeSeries that includes any samples in the given TimeSeries between the start and end times
	 */
	public static TimeSeries extractTime(TimeSeries series, float start, float end) {
		MU.VectorExpander times = new MU.VectorExpander();
		MU.MatrixExpander values = new MU.MatrixExpander();
		
		float[] originalTimes = series.getTimes();
		float[][] originalValues = series.getValues();
		for (int i = 0; i < originalTimes.length; i++) {
			if (originalTimes[i] >= start && originalTimes[i] <= end) {
				times.add(originalTimes[i]);
				values.add(originalValues[i]);
			}
		}
		
		return new TimeSeriesImpl(times.toArray(), values.toArray(), series.getUnits());
	}
	
	/**
	 * Draws one of every <code>period</code> samples from a given TimeSeries.
	 *   
	 * @param series Any TimeSeries 
	 * @param period The sub-sampling period
	 * @return New TimeSeries composed of one of every <code>period</code> samples in the original 
	 */
	public static TimeSeries subsample(TimeSeries series, int period) {
		MU.VectorExpander times = new MU.VectorExpander();
		MU.MatrixExpander values = new MU.MatrixExpander();
		
		float[] originalTimes = series.getTimes();
		float[][] originalValues = series.getValues();
		for (int i = 0; i < originalTimes.length; i = i + period) {
			times.add(originalTimes[i]);
			values.add(originalValues[i]);
		}
		
		return new TimeSeriesImpl(times.toArray(), values.toArray(), series.getUnits());
	}
	
	/**
	 * Extracts spikes of selected neurons from a given SpikePattern. 
	 * 
	 * @param pattern Any SpikePattern
	 * @param start Neuron number at which to start extraction 
	 * @param interval Spikes are taken from one every <code>interval</code> neurons 
	 * @param end Neuron number at which to end extraction
	 * @return Spikes from selected neurons in the original pattern
	 */
	public static SpikePattern subset(SpikePattern pattern, int start, int interval, int end) {
		int[] indices = MU.round(MU.makeVector(start, interval, end));
		System.out.println(MU.toString(new float[][]{MU.makeVector(start, interval, end)}, 10));
		return subset(pattern, indices);
	}

	/**
	 * Extracts spikes of selected neurons from a given SpikePattern. 
	 * 
	 * @param pattern Any SpikePattern
	 * @param indices Indices of neurons in original pattern from which to extract spikes
	 * @return Spikes from selected neurons in the original pattern
	 */
	public static SpikePattern subset(SpikePattern pattern, int[] indices) {
		SpikePatternImpl result = new SpikePatternImpl(indices.length);
		
		for (int i = 0; i < indices.length; i++) {
			float[] spikeTimes = pattern.getSpikeTimes(indices[i]);
			for (int j = 0; j < spikeTimes.length; j++) {
				result.addSpike(i, spikeTimes[j]);
			}
		}
		return result;
	}
	
	/**
	 * Attempts to sort a SpikePattern by properties of the associated neurons. 
	 * 
	 * @param pattern A SpikePattern
	 * @param group Ensemble from which spikes come
	 * @return A SpikePattern that is re-ordered according to neuron properties, if possible
	 */
	public static SpikePattern sort(SpikePattern pattern, Group group) {
		ComparableNodeWrapper[] wrappers = new ComparableNodeWrapper[group.getNodes().length];
		for (int i = 0; i < wrappers.length; i++) {
			wrappers[i] = new ComparableNodeWrapper(group, i);
		}		
		Arrays.sort(wrappers);
		
		int[] sortedIndices = new int[wrappers.length];
		for (int i = 0; i < wrappers.length; i++) {
			sortedIndices[i] = wrappers[i].getIndex();
		}
		
		return subset(pattern, sortedIndices);
	}

	/**
	 * For sorting. We try to extract encoding vectors and certain properties of 
	 * common SpikeGenerators for ordering. 
	 */
	private static class ComparableNodeWrapper implements Comparable<ComparableNodeWrapper> {
		
		private final int myIndex;
		private float myFirstDimEncoder;
		private float myBias;
		
		public ComparableNodeWrapper(Group group, int nodeIndex) {
			myIndex = nodeIndex;
			
			myBias = 1;
			Node node = group.getNodes()[nodeIndex];
			if (node instanceof SpikingNeuron) {
				myBias = ((SpikingNeuron) node).getBias();
			}
			
			myFirstDimEncoder = 1;
			if (group instanceof NEFGroup) {
				myFirstDimEncoder = ((NEFGroup) group).getEncoders()[nodeIndex][0];
			}
		}
		
		/**
		 * @return The index of the underlying Node within its Ensemble
		 */
		public int getIndex() {
			return myIndex;
		}
		
		/**
		 * @return A number that summarizes the position of the underlying node 
		 * 		in a global ordering
		 */
		public float getOrderingMetric() {
			return myFirstDimEncoder * myBias;
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(ComparableNodeWrapper o) {
			int result = 0;			

			ComparableNodeWrapper c = o;
			if (getOrderingMetric() > c.getOrderingMetric()) {
				result = 1;
			} else if (getOrderingMetric() < c.getOrderingMetric()) {
				result = -1;
			}
			return result;
		}		
	}

}
