/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "WriteToDiskSimulatorListener.java". Description: 
"A method of writing to disk values being tracked by a probe."

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
 * Created on 16-Dec-2010
 */

package ca.nengo.sim.impl;

import ca.nengo.sim.SimulatorEvent;
import ca.nengo.sim.SimulatorListener;
import ca.nengo.util.Probe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * A method of writing to disk values being tracked by a probe.
 * 
 * This class is designed to be used in cases where a simulation must run for
 * a long period of time, and it is likely that the amount of data being stored
 * will cause issues with the proper running of Nengo. By attaching a
 * WriteToDiskSimulatorListener to a simulator instance, progress is saved to disk
 * after each recordInterval.
 * 
 * Example usage (Python syntax):
 *   probe_error = network.getSimulator().addProbe("error",error.X,True)
 *   file_error = File("output/error.csv")
 *   listener_error = WriteToDiskSimulatorListener(file_error,probe_error,0.005)
 *   network.simulator.addSimulatorListener(listener_error)
 * 
 * @author Trevor Bekolay
 */
public class WriteToDiskSimulatorListener implements SimulatorListener {
	private File myFile;
	private Probe myTargetProbe;
	private float myRecordInterval;
	private float myLastInterval;
	private long myStartTime;
	private BufferedWriter myWriter;
	
	/**
	 * @param file The file that progress will be saved to. If it already exists, it will be overwritten.
	 * @param targetProbe The Probe from which data will be collected.
	 * @param recordInterval How often data will be written to disk. To record every timestep, use 0.0.
	 */
	public WriteToDiskSimulatorListener(File file, Probe targetProbe, float recordInterval) {
		try {
			myFile = file;
			myWriter = new BufferedWriter(new FileWriter(myFile));
			myTargetProbe = targetProbe;
			myRecordInterval = recordInterval;
			myLastInterval = myRecordInterval;
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("IO Exception in WriteToDiskSimulatorListener: " + e);
		}
	}
	
	/**
	 * @param event The SimulatorEvent corresponding to the current state of the simulator.
	 */
	public void processEvent(SimulatorEvent event) {
		try {
	        if (event.getType() == SimulatorEvent.Type.STARTED) {
	        	myLastInterval = myRecordInterval;
	        	myStartTime = Calendar.getInstance().getTimeInMillis();
	        	myWriter = new BufferedWriter(new FileWriter(myFile));
	        } else if (event.getType() == SimulatorEvent.Type.STEP_TAKEN) {
	            float[] times = myTargetProbe.getData().getTimes();
	            
	            if (times[times.length - 1] >= myLastInterval) {
	                float[][] data = myTargetProbe.getData().getValues();
	
	                myWriter.write(Float.toString(times[times.length-1]));
	                for (int i=0; i < data[0].length; i++) {
	                	myWriter.write(',' + Float.toString(data[times.length-1][i]));
	                }
	                myWriter.newLine();
	                myWriter.flush();
	                myLastInterval += myRecordInterval;
	            }
	        } else if (event.getType() == SimulatorEvent.Type.FINISHED) {
	            myWriter.close();
	
	            long finishTime = Calendar.getInstance().getTimeInMillis();
	            System.out.println("Simulation finished - Elapsed time: " + (finishTime - myStartTime) / 1000.0 + " seconds.");
	        }
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("IO Exception in WriteToDiskSimulatorListener:  " + e);
		}
	}
}
