/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "ProbeImpl.java". Description: 
"Collects information from Probeable objects"

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

package ca.nengo.util.impl;

import ca.nengo.model.Node;
import ca.nengo.model.Probeable;
import ca.nengo.model.SimulationException;
import ca.nengo.model.Units;
import ca.nengo.util.Probe;
import ca.nengo.util.TimeSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Collects information from <code>Probeable</code> objects.</p> 
 * 
 * @author Bryan Tripp
 */
public class ProbeImpl implements Probe<String>, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	private Probeable myTarget;
	private String myStateName;
	private boolean myRecord;
	private float[] myTimes;
	private List<float[]> myValues;
	private Units[] myUnits;
	private float mySamplingPeriod = -1;
	private float myLastSampleTime = -100000;
	private String myEnsembleName = null;
	private ProbeTask myProbeTask;

	/**
	 * @see ca.nengo.util.Probe#connect(java.lang.String, ca.nengo.model.Probeable, java.lang.String, boolean)
	 */
	public void connect(String ensembleName, Probeable target,
			String stateName, boolean record) throws SimulationException {
		myEnsembleName = ensembleName;
		myTarget = target;
		myStateName = stateName;
		myRecord = record;

		//if the state is bad, we want to throw an exception now
		myTarget.getHistory(myStateName);  

		reset();
		
		myProbeTask = new ProbeTask(target, this);
	}

	/**
	 * @see ca.nengo.util.Probe#connect(Probeable, String, boolean)
	 */
	public void connect(Probeable target, String stateName, boolean record) throws SimulationException {
		connect(null, target, stateName, record);
	}
	
	/**
	 * @see ca.nengo.util.Probe#reset() 
	 */
	public void reset() {
		myUnits = null; //will be reset on first doCollect()
		myTimes = new float[1000];
		myValues = new ArrayList<float[]>(1000);
	}
	
	/**
	 * @see ca.nengo.util.Probe#collect(float)
	 */
	public void collect(float time) {
		if (mySamplingPeriod > 0) { 
			if (time >= myLastSampleTime + mySamplingPeriod) {
				doCollect();
				myLastSampleTime = time;
			}
		} else {
			doCollect();
		}
	}
	
	private void doCollect() {
		if (myTarget == null) {
			throw new IllegalStateException("This Recorder has not been connected to a Probeable");
		}
		
		TimeSeries stepData;
		try {
			stepData = myTarget.getHistory(myStateName);
		} catch (SimulationException e) {
			throw new RuntimeException("Target appears not to have the state " 
					+ myStateName + ", although this problem should have been detected on connect()", e);
		}
		
		float[] times = stepData.getTimes();
		float[][] values = stepData.getValues();
		int len = times.length;		
		
		if (myRecord) {
			if (myValues.size() + len >= myTimes.length) {
				grow();
			}		
			System.arraycopy(times, 0, myTimes, myValues.size(), len); //don't move this to after the values update			
		} else {
			myTimes = times;
			myValues = new ArrayList<float[]>(10);
		}

        myValues.addAll(Arrays.asList(values).subList(0, len));
		
		if (myUnits == null) {
			myUnits = stepData.getUnits();
		}
	}
	
	private void grow() {
		float[] newTimes = new float[myTimes.length + 1000];
		System.arraycopy(myTimes, 0, newTimes, 0, myTimes.length);
		myTimes = newTimes;
	}
	
	/**
	 * @see ca.nengo.util.Probe#getData()
	 */
	public TimeSeries getData() {
		float[] times = new float[myValues.size()];
		System.arraycopy(myTimes, 0, times, 0, myValues.size());
		
		float[][] values = myValues.toArray(new float[0][]);
		
		TimeSeriesImpl result = new TimeSeriesImpl(times, values, (myUnits == null) ? new Units[]{Units.UNK} : myUnits);
		result.setName(((myTarget instanceof Node) ? ((Node) myTarget).name()+ ':' : "") + myStateName);
		return result;
	}

	/**
	 * @see ca.nengo.util.Probe#setSamplingRate(float)
	 */
	public void setSamplingRate(float rate) {
		mySamplingPeriod = 1f / rate;
	}

	/**
	 * @see ca.nengo.util.Probe#getTarget()
	 */
	public Probeable getTarget() {
		return myTarget;
	}

	/**
	 * @see ca.nengo.util.Probe#getStateName()
	 */
	public String getStateName() {
		return myStateName;
	}

	/**
	 * @see ca.nengo.util.Probe#isInEnsemble()
	 */
	public boolean isInEnsemble() {
		if (myEnsembleName != null)
			return true;
		else
			return false;
	}

	/**
	 * @see ca.nengo.util.Probe#getEnsembleName()
	 */
	public String getEnsembleName() {
		return myEnsembleName;
	}

	/**
	 * @see ca.nengo.util.Probe#getProbeTask()
	 */
	public ProbeTask getProbeTask(){
		return myProbeTask;
	}
}
