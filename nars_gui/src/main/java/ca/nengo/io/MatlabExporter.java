///*
//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the License.
//You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
//WARRANTY OF ANY KIND, either express or implied. See the License for the specific
//language governing rights and limitations under the License.
//
//The Original Code is "MatlabExporter.java". Description:
//"A tool for exporting data to Matlab .mat files"
//
//The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.
//
//Alternatively, the contents of this file may be used under the terms of the GNU
//Public License license (the GPL License), in which case the provisions of GPL
//License are applicable  instead of those above. If you wish to allow use of your
//version of this file only under the terms of the GPL License and not to allow
//others to use your version of this file under the MPL, indicate your decision
//by deleting the provisions above and replace  them with the notice and other
//provisions required by the GPL License.  If you do not delete the provisions above,
//a recipient may use your version of this file under either the MPL or the GPL License.
//*/
//
//package ca.nengo.io;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import ca.nengo.plot.Plotter;
//import ca.nengo.util.MU;
//import ca.nengo.util.SpikePattern;
//import ca.nengo.util.TimeSeries;
//
//import com.jmatio.io.MatFileWriter;
//import com.jmatio.types.MLArray;
//import com.jmatio.types.MLDouble;
//
///**
// * <p>A tool for exporting data to Matlab .mat files. Use like this:</p>
// *
// * <p><code>
// * MatlabExport me = new MatlabExport();<br>
// * me.add("series1", series1);<br>
// * ... <br>
// * me.add("series1", series1);<br>
// * me.write(new File("c:\\foo.mat"));<br>
// * </code></p>
// *
// * @author Bryan Tripp
// */
//public class MatlabExporter {
//
//	private Map<String, MLArray> myData;
//
//	/**
//	 * Export data to Matlab .mat files
//	 */
//	public MatlabExporter() {
//		myData = new HashMap<String, MLArray>(10);
//	}
//
//	/**
//	 * @param name Matlab variable name
//	 * @param data Data to be stored in Matlab variable
//	 */
//	public void add(String name, TimeSeries data) {
//		add(name+"_time", new float[][]{data.getTimes()});
//		add(name, data.getValues());
//	}
//
//	/**
//	 * Filters TimeSeries data with given time constant (this is usually a good
//	 * idea for spike output, which is a sum of impulses).
//	 *
//	 * TODO: this filter is prohibitively slow for large datasets
//	 *
//	 * @param name Matlab variable name
//	 * @param data Data to be stored in Matlab variable
//	 * @param tau Time constant of filter to apply to data
//	 */
//	public void add(String name, TimeSeries data, float tau) {
//		TimeSeries filtered = Plotter.filter(data, tau);
//		add(name+"_time", new float[][]{filtered.getTimes()});
//		add(name+"_data", filtered.getValues());
//	}
//
//	/**
//	 * @param name Matlab variable name
//	 * @param pattern Spike times for a group of neurons
//	 */
//	public void add(String name, SpikePattern pattern) {
//		int n = pattern.getNumNeurons();
//		int maxSpikes = 0;
//		for (int i = 0; i < n; i++) {
//			float[] times = pattern.getSpikeTimes(i);
//			if (times.length > maxSpikes) {
//                maxSpikes = times.length;
//            }
//		}
//		float[][] timesMatrix = new float[n][];
//		for (int i = 0; i < n; i++) {
//			timesMatrix[i] = new float[maxSpikes];
//			float[] times = pattern.getSpikeTimes(i);
//			System.arraycopy(times, 0, timesMatrix[i], 0, times.length);
//		}
//		add(name, timesMatrix);
//	}
//
//	/**
//	 * @param name Matlab variable name
//	 * @param data A matrix
//	 */
//	public void add(String name, float[][] data) {
//		if (!MU.isMatrix(data)) {
//			throw new IllegalArgumentException("Data must be a matrix (same number of columns in each row)");
//		}
//		name=makeVariableNameValid(name);
//		MLDouble mld = new MLDouble(name, MU.convert(data));
//		myData.put(name, mld);
//	}
//
//	/**
//	 * @param name original possibly invalid name
//	 * @return valid name that Matlab can use
//	 */
//	public static String makeVariableNameValid(String name) {
//		// replace all invalid characters with underscores
//		name=name.replaceAll("[^a-zA-Z0-9_]","_");
//
//		// make sure the variable name starts with a letter
//		if (name.length()==0) {
//			name="data";
//		} else if (name.matches("[^a-zA-Z].*")) {
//			name="data"+name;
//		}
//		return name;
//	}
//
//	/**
//	 * Clears all variables
//	 */
//	public void removeAll() {
//		myData.clear();
//	}
//
//	/**
//	 * Writes to given destination the data that have been added to this exporter.
//	 *
//	 * @param destination File to which data are to be written (should have extension .mat)
//	 * @throws IOException if there's a problem writing to disk
//	 */
//	public void write(File destination) throws IOException {
//		new MatFileWriter(destination, myData.values());
//	}
//
//}
