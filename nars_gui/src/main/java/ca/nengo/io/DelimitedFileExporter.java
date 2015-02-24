/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "DelimitedFileExporter.java". Description:
"Exports TimeSeries, SpikePattern, and float[][] data to delimited text files"

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
package ca.nengo.io;

import ca.nengo.plot.Plotter;
import ca.nengo.util.MU;
import ca.nengo.util.SpikePattern;
import ca.nengo.util.TimeSeries;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Exports TimeSeries, SpikePattern, and float[][] data to delimited text files.
 *
 * @author Bryan Tripp
 */
public class DelimitedFileExporter {

	private final String myColumnDelim;
	private final String myRowDelim;

	/**
	 * Uses default column delimiter ", " and row delimiter "\r\n".
	 */
	public DelimitedFileExporter() {
		myColumnDelim = ", ";
		myRowDelim = "\r\n";
	}

	/**
	 * @param columnDelim String used to delimit items within a matrix row
	 * @param rowDelim String used to delimit rows of a matrix
	 */
	public DelimitedFileExporter(String columnDelim, String rowDelim) {
		myColumnDelim = columnDelim;
		myRowDelim = rowDelim;
	}

	/**
	 * Exports a TimeSeries with times in the first column and data from each dimension in subsequent columns.
	 *
	 * @param series TimeSeries to export
	 * @param file File to which to export the TimeSeries
	 * @throws IOException if there's a problem writing to disk
	 */
	public void export(TimeSeries series, File file) throws IOException {
		float[][] values = MU.transpose(series.getValues());
		float[][] timesAndValues = new float[values.length + 1][];
		timesAndValues[0] = series.getTimes();
		System.arraycopy(values, 0, timesAndValues, 1, values.length);

		export(MU.transpose(timesAndValues), file);
	}

	/**
	 * Exports a TimeSeries as a matrix with times in the first column and data from each dimension
	 * in subsequent rows.
	 *
	 * @param series TimeSeries to export
	 * @param file File to which to export the TimeSeries
	 * @param tau Time constant with which to filter data
	 * @throws IOException if there's a problem writing to disk
	 */
	public void export(TimeSeries series, File file, float tau) throws IOException {
		TimeSeries filtered = Plotter.filter(series, tau);

		float[][] values = MU.transpose(filtered.getValues());
		float[][] timesAndValues = new float[values.length + 1][];
		timesAndValues[0] = filtered.getTimes();
		System.arraycopy(values, 0, timesAndValues, 1, values.length);

		export(MU.transpose(timesAndValues), file);
	}

	/**
	 * Exports a SpikePattern as a matrix with spikes times of each neuron in a different row.
	 *
	 * @param pattern SpikePattern to export
	 * @param file File to which to export the SpikePattern
	 * @throws IOException if there's a problem writing to disk
	 */
	public void export(SpikePattern pattern, File file) throws IOException {
		int n = pattern.getNumNeurons();
		float[][] times = new float[n][];
		for (int i = 0; i < n; i++) {
			times[i] = pattern.getSpikeTimes(i);
		}
		export(times, file);
	}

	/**
	 * Exports a matrix with rows and columns delimited as specified in the constructor.
	 *
	 * @param matrix The matrix to export
	 * @param file File to which to export the matrix
	 * @throws IOException if there's a problem writing to disk
	 */
	public void export(float[][] matrix, File file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		for (float[] element : matrix) {
			for (int j = 0; j < element.length; j++) {
				writer.write(String.valueOf(element[j]));
				if (j < element.length - 1) {
                    writer.write(myColumnDelim);
                }
			}
			writer.write(myRowDelim);
		}

		writer.flush();
		writer.close();
	}

	/**
	 * Imports a delimited file as a matrix. Assumes that rows are delimited as lines, and
	 * items in a row are delimited with one or more of the following: comma, colon, semicolon,
	 * space, tab.
	 *
	 * @param file File from which to load matrix
	 * @return Matrix from file
	 * @throws IOException if there's a problem writing to disk
	 */
	public float[][] importAsMatrix(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));

		List<float[]> rows = new ArrayList<float[]>(100);
		for (String line; (line = reader.readLine()) != null; ) {
			StringTokenizer tok = new StringTokenizer(line, ",;: \t", false);
			float[] row = new float[tok.countTokens()];
			for (int i = 0; i < row.length; i++) {
				row[i] = Float.parseFloat(tok.nextToken());
			}
			rows.add(row);
		}

		return rows.toArray(new float[0][]);
	}


}
