/*
 * Created on 16-Nov-07
 */
package ca.nengo.util;

import ca.nengo.TestUtil;
import ca.nengo.math.Function;
import ca.nengo.math.impl.SineFunction;
import ca.nengo.model.Network;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.FunctionInput;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.nef.NEFGroup;
import ca.nengo.model.nef.NEFGroupFactory;
import ca.nengo.model.nef.impl.NEFGroupFactoryImpl;
import ca.nengo.plot.Plotter;
import ca.nengo.util.impl.SpikePatternImpl;
import ca.nengo.util.impl.TimeSeriesImpl;
import junit.framework.TestCase;

/**
 * Unit tests for DataUtils. 
 *  
 * @author Bryan Tripp
 */
public class DataUtilsTest extends TestCase {

	private TimeSeries myOriginalSeries;
	private SpikePattern myOriginalPattern;
	private float myTolerance;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		float[] times = MU.makeVector(1, 1, 10);
		float[][] valuesT = new float[3][];
		valuesT[0] = MU.makeVector(.1f, .1f, 1);
		valuesT[1] = MU.makeVector(1.1f, .1f, 2);
		valuesT[2] = MU.makeVector(2.1f, .1f, 3);
		Units[] units = new Units[]{Units.ACU, Units.AVU, Units.M};
		myOriginalSeries = new TimeSeriesImpl(times, MU.transpose(valuesT), units);
		
		SpikePatternImpl pattern = new SpikePatternImpl(10);
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < i; j++) {
				pattern.addSpike(i, j);
			}
		}
		myOriginalPattern = pattern;
		
		myTolerance = .00001f;
	}

	public void testExtractDimension() {
		TimeSeries ts = DataUtils.extractDimension(myOriginalSeries, 0);
		assertEquals(myOriginalSeries.getTimes().length, ts.getTimes().length);
		TestUtil.assertClose(myOriginalSeries.getTimes()[0], ts.getTimes()[0], myTolerance);
		TestUtil.assertClose(myOriginalSeries.getTimes()[1], ts.getTimes()[1], myTolerance);

		assertEquals(myOriginalSeries.getValues().length, ts.getValues().length);
		assertEquals(1, ts.getValues()[0].length);
		TestUtil.assertClose(myOriginalSeries.getValues()[0][0], ts.getValues()[0][0], myTolerance);
		TestUtil.assertClose(myOriginalSeries.getValues()[1][0], ts.getValues()[1][0], myTolerance);
		
		ts = DataUtils.extractDimension(myOriginalSeries, 1);
		TestUtil.assertClose(myOriginalSeries.getValues()[0][1], ts.getValues()[0][0], myTolerance);
		TestUtil.assertClose(myOriginalSeries.getValues()[1][1], ts.getValues()[1][0], myTolerance);
	}

	public void testExtractTime() {
		TimeSeries ts = DataUtils.extractTime(myOriginalSeries, 3, 7);		
		assertEquals(5, ts.getTimes().length);
		TestUtil.assertClose(3, ts.getTimes()[0], myTolerance);
		TestUtil.assertClose(4, ts.getTimes()[1], myTolerance);
		
		assertEquals(5, ts.getValues().length);
		assertEquals(3, ts.getValues()[0].length);
		TestUtil.assertClose(.3f, ts.getValues()[0][0], myTolerance);
		TestUtil.assertClose(.4f, ts.getValues()[1][0], myTolerance);
		TestUtil.assertClose(1.3f, ts.getValues()[0][1], myTolerance);
	}

	public void testSubsample() {
		TimeSeries ts = DataUtils.subsample(myOriginalSeries, 2);
		assertEquals(5, ts.getTimes().length);
		TestUtil.assertClose(1, ts.getTimes()[0], myTolerance);
		TestUtil.assertClose(3, ts.getTimes()[1], myTolerance);
		
		assertEquals(5, ts.getValues().length);
		assertEquals(3, ts.getValues()[0].length);
		TestUtil.assertClose(.1f, ts.getValues()[0][0], myTolerance);
		TestUtil.assertClose(.3f, ts.getValues()[1][0], myTolerance);
		TestUtil.assertClose(1.1f, ts.getValues()[0][1], myTolerance);
	}

	public void testSubsetSpikePatternIntIntInt() {
		SpikePattern p = DataUtils.subset(myOriginalPattern, 2, 3, 5);
		assertEquals(2, p.getNumNeurons());
		assertEquals(2, p.getSpikeTimes(0).length);
		assertEquals(5, p.getSpikeTimes(1).length);
		TestUtil.assertClose(1, p.getSpikeTimes(0)[1], myTolerance);
	}

	public void testSubsetSpikePatternIntArray() {
		SpikePattern p = DataUtils.subset(myOriginalPattern, new int[]{9, 8, 7});
		assertEquals(3, p.getNumNeurons());
		assertEquals(9, p.getSpikeTimes(0).length);
		assertEquals(8, p.getSpikeTimes(1).length);
		assertEquals(7, p.getSpikeTimes(2).length);
		TestUtil.assertClose(1, p.getSpikeTimes(0)[1], myTolerance);
	}

	/**
	 * Note: this isn't run automatically but it's run from the main()
	 * 
	 * @throws StructuralException 
	 * @throws SimulationException 
	 */
	public void functionalTestSort() throws StructuralException, SimulationException {
		Network network = new NetworkImpl();
		
		FunctionInput input = new FunctionInput("input", new Function[]{new SineFunction(5)}, Units.UNK);
		network.addNode(input);
		
		NEFGroupFactory ef = new NEFGroupFactoryImpl();
		NEFGroup ensemble = ef.make("ensemble", 100, 1);
		ensemble.addDecodedTermination("input", MU.I(1), .005f, false);
		ensemble.collectSpikes(true);
		network.addNode(ensemble);
		
		network.addProjection(input.getOrigin(FunctionInput.ORIGIN_NAME), ensemble.getTermination("input"));
		network.run(0, 2);
		
		SpikePattern unsorted = ensemble.getSpikePattern();
		SpikePattern sorted = DataUtils.sort(unsorted, ensemble);
		
		Plotter.plot(unsorted);
		Plotter.plot(sorted);
	}

	public static void main(String[] args) {
		DataUtilsTest test = new DataUtilsTest();
		try {
			test.functionalTestSort();
		} catch (StructuralException e) {
			e.printStackTrace();
		} catch (SimulationException e) {
			e.printStackTrace();
		}
	}
	
}
