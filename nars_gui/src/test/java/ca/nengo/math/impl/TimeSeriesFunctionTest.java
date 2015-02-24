/**
 *
 */
package ca.nengo.math.impl;

import ca.nengo.TestUtil;
import ca.nengo.math.Function;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.plot.Plotter;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeries1DImpl;
import junit.framework.TestCase;

/**
 * Unit tests for TimeSeriesFunction.
 *
 * @author Bryan Tripp
 */
public class TimeSeriesFunctionTest extends TestCase {

	/**
	 * @param arg0
	 */
	public TimeSeriesFunctionTest(String arg0) {
		super(arg0);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Test method for {@link ca.nengo.math.impl.TimeSeriesFunction#map(float[])}.
	 * @throws StructuralException
	 */
	public void testMap() throws StructuralException {
		//this is code that was giving Lloyd some trouble (not really a unit test) ...
		TimeSeries ts = new TimeSeries1DImpl(new float[]{ 0.0f, 0.5f, 1.0f},
				new float[]{0.0f, 0.5f, 1.0f}, Units.UNK);
		Function f = new TimeSeriesFunction(ts , 0);
//		FunctionInput input = new FunctionInput("input", new Function[]{f}, Units.UNK);

		//now for a little test
		TestUtil.assertClose(.2f, f.map(new float[]{.2f}), .00001f);
	}

    //functional test
    public static void main(String[] args) {
        Function f = new SineFunction(10);
        TimeSeries series = TimeSeriesFunction.makeSeries(f, 0, .001f, 1, Units.UNK);
        Plotter.plot(f, 0, .001f, 1, "function output");
        Plotter.plot(series, "time series");

        Function f2 = new TimeSeriesFunction(series, 0);
        Plotter.plot(f2, 0, .0005f, 1, "derived function");
    }
}
