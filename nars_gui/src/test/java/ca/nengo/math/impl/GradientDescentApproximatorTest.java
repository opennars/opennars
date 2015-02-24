package ca.nengo.math.impl;

import ca.nengo.TestUtil;
import ca.nengo.math.Function;
import ca.nengo.math.impl.GradientDescentApproximator.Constraints;
import junit.framework.TestCase;

//import ca.nengo.model.Units;
//import ca.nengo.plot.Plotter;
//import ca.nengo.util.MU;
//import ca.nengo.util.impl.TimeSeries1DImpl;

/**
 * Unit tests for GradientDescentApproximator. 
 * 
 * @author Bryan Tripp
 */
public class GradientDescentApproximatorTest extends TestCase {

	public GradientDescentApproximatorTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testFindCoefficients() {
		float[] frequencies = new float[]{1, 5, 8};
		float[] amplitudes = new float[]{.1f, .2f, .3f};
		float[] phases = new float[]{0, -1, 1};
		
		float[][] evalPoints = new float[100][];
		for (int i = 0; i < evalPoints.length; i++) {
			evalPoints[i] = new float[]{(float) i / (float) evalPoints.length};
		}
		
		Function target = new FourierFunction(frequencies, amplitudes, phases);
		float[][] values = new float[frequencies.length][];
		for (int i = 0; i < frequencies.length; i++) {
			Function component = new FourierFunction(new float[]{frequencies[i]}, new float[]{1}, new float[]{phases[i]});
			values[i] = new float[evalPoints.length];
			for (int j = 0; j < evalPoints.length; j++) {
				values[i][j] = component.map(evalPoints[j]);
			}
		}
		
		GradientDescentApproximator.Constraints constraints = new GradientDescentApproximator.Constraints() {
			private static final long serialVersionUID = 1L;
			public boolean correct(float[] coefficients) {
				boolean allCorrected = true;
				for (int i = 0; i < coefficients.length; i++) {
					if (coefficients[i] < 0) {
						coefficients[i] = 0;
					} else {
						allCorrected = false;
					}
				}
				return allCorrected;
			}
			public Constraints clone() throws CloneNotSupportedException {
				return (Constraints) super.clone();
			}
		};
		
		GradientDescentApproximator approximator = new GradientDescentApproximator(evalPoints, values, constraints, true);
		float[] coefficients = approximator.findCoefficients(target);
		
		float approx;
		for (int j = 0; j < evalPoints.length; j++) {
			approx = 0f;
			for (int i = 0; i < frequencies.length; i++) {
				approx += coefficients[i] * values[i][j];
			}
			TestUtil.assertClose(approx, target.map(evalPoints[j]), 0.0001f);
		}
		
//		float[] estimate = MU.prod(MU.transpose(values), coefficients);
//		Plotter.plot(target, 0, .01f, .99f, "Ideal");
//		Plotter.plot(new TimeSeries1DImpl(MU.prod(evalPoints, new float[]{1}), estimate, Units.UNK), "Estimate");
//		
//		try {
//			Thread.sleep(1000*15);
//		} catch (InterruptedException e) {}
	}
	
	/*
	 * Test method for get- and setMaxIterations
	 */
	public void testMaxIterations() {
		
		GradientDescentApproximator.Constraints constraints = new GradientDescentApproximator.Constraints() {
			private static final long serialVersionUID = 1L;
			public boolean correct(float[] coefficients) {
				return true;
			}
			public Constraints clone() throws CloneNotSupportedException {
				return (Constraints) super.clone();
			}			
		};
		
		GradientDescentApproximator approximator = new GradientDescentApproximator(new float[][]{{1f},{2f},{3f}}, new float[][]{{1f},{2f},{3f}}, constraints, true);
		assertEquals(1000, approximator.getMaxIterations());
		approximator.setMaxIterations(500);
		assertEquals(500, approximator.getMaxIterations());
		
	}
	
	/*
	 * Test method for get- and setTolerance
	 */
	public void testTolerance() {
		
		GradientDescentApproximator.Constraints constraints = new GradientDescentApproximator.Constraints() {
			private static final long serialVersionUID = 1L;
			public boolean correct(float[] coefficients) {
				return true;
			}
			public Constraints clone() throws CloneNotSupportedException {
				return (Constraints) super.clone();
			}
		};
		
		GradientDescentApproximator approximator = new GradientDescentApproximator(new float[][]{{1f},{2f},{3f}}, new float[][]{{1f},{2f},{3f}}, constraints, true);
		assertEquals(.000000001f, approximator.getTolerance());
		approximator.setTolerance(.000001f);
		assertEquals(.000001f, approximator.getTolerance());
		
	}
	
}
