/*
 * Created on 14-Nov-07
 */
package ca.nengo.io;

import ca.nengo.TestUtil;
import ca.nengo.model.Units;
import ca.nengo.plot.Plotter;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.SpikePatternImpl;
import ca.nengo.util.impl.TimeSeriesImpl;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * Unit tests for DelimitedFileExporter. 
 *  
 * @author Bryan Tripp
 */
public class DelimitedFileExporterTest extends TestCase {

	private DelimitedFileExporter myExporter;
	private File myFile;
	
	protected void setUp() throws Exception {
		super.setUp();
		myExporter = new DelimitedFileExporter();
		myFile = new File("./delimited_file_exporter_test.txt");
	}

	public void testExportTimeSeriesFile() throws IOException {
		TimeSeries ts = new TimeSeriesImpl(new float[]{1, 2, 3}, 
				new float[][]{new float[]{4, 7}, new float[]{5, 8}, new float[]{6, 9}}, 
				Units.uniform(Units.UNK, 2));
		myExporter.export(ts, myFile);
		float[][] imported = myExporter.importAsMatrix(myFile);
		TestUtil.assertClose(imported[0][0], 1, .0001f);
		TestUtil.assertClose(imported[1][0], 2, .0001f);
		TestUtil.assertClose(imported[0][1], 4, .0001f);
		TestUtil.assertClose(imported[0][2], 7, .0001f);
	}

	public void testExportTimeSeriesFileFloat() throws IOException {
		TimeSeries ts = new TimeSeriesImpl(new float[]{1, 2, 3}, 
				new float[][]{new float[]{4, 7}, new float[]{5, 8}, new float[]{6, 9}}, 
				Units.uniform(Units.UNK, 2));
		myExporter.export(ts, myFile, .5f);
		TimeSeries filtered = Plotter.filter(ts, .5f);
		float[][] imported = myExporter.importAsMatrix(myFile);
		TestUtil.assertClose(imported[0][0], filtered.getTimes()[0], .0001f);
		TestUtil.assertClose(imported[1][0], filtered.getTimes()[1], .0001f);
		TestUtil.assertClose(imported[0][1], filtered.getValues()[0][0], .0001f);
		TestUtil.assertClose(imported[0][2], filtered.getValues()[0][1], .0001f);
	}

	public void testExportSpikePatternFile() throws IOException {
		SpikePatternImpl pattern = new SpikePatternImpl(2);
		pattern.addSpike(0, 1);
		pattern.addSpike(0, 2);
		pattern.addSpike(1, 3);
		pattern.addSpike(1, 4);
		pattern.addSpike(1, 5);

		myExporter.export(pattern, myFile);
		float[][] imported = myExporter.importAsMatrix(myFile);
		TestUtil.assertClose(imported[0][0], 1, .0001f);
		TestUtil.assertClose(imported[0][1], 2, .0001f);
		TestUtil.assertClose(imported[1][0], 3, .0001f);
		TestUtil.assertClose(imported[1][1], 4, .0001f);
		TestUtil.assertClose(imported[1][2], 5, .0001f);
	}

	public void testExportFloatArrayArrayFile() throws IOException {
		float[][] matrix = new float[][]{new float[]{1}, new float[]{-1.2f, .0000000001f}};
		myExporter.export(matrix, myFile);
		float[][] imported = myExporter.importAsMatrix(myFile);
		TestUtil.assertClose(matrix[0][0], imported[0][0], .0000000000001f);
		TestUtil.assertClose(matrix[1][0], imported[1][0], .0000000000001f);
		TestUtil.assertClose(matrix[1][1], imported[1][1], .0000000000001f);
	}

}
