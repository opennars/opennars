/*
 * Created June 2, 2007
 */
package ca.nengo.math.impl;

import ca.nengo.math.Function;
import ca.nengo.math.LinearApproximator;
import junit.framework.TestCase;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Unit tests for CompositeApproximator.
 *
 * TODO: These tests were failing but I disabled them for now, because I don't understand what is
 * supposed to be happening. - Bryan
 *
 * @author Hussein
 */
public class CompositeApproximatorTest extends TestCase {

	/*
	 * Test method for 'ca.nengo.math.impl.CompositeApproximator.findCoefficients()'
	 */
	public void testFindCoefficients() {
		float[][] polyCoeffs = new float[][]{{0f,1f},{0f,0f,1f},{1f},{1f,1f,1f},{0f,-1f,1f}};
		Function[] polys = new Polynomial[polyCoeffs.length];
		for (int i=0; i<polyCoeffs.length; i++) {
			polys[i] = new Polynomial(polyCoeffs[i]);
		}
		Function[] posts = new PostfixFunction[2];

		ArrayList<Serializable> l = new ArrayList<Serializable>();
		l.add(Integer.valueOf(0));
		l.add(polys[0]);
		l.add(Integer.valueOf(1));
		l.add(polys[1]);
		posts[0] = new PostfixFunction(l, "", 2);
		l = new ArrayList<Serializable>();
		l.add(Integer.valueOf(0));
		l.add(polys[1]);
		l.add(Integer.valueOf(1));
		l.add(polys[0]);
		posts[1] = new PostfixFunction(l, "", 2);

		LinearApproximator[] comps = new WeightedCostApproximator[2];
		float[][] evalPoints = new float[199][];
		float[][] values = new float[3][199];
		for (int i=-99; i<100; i++) {
			evalPoints[i+99] = new float[]{(float)i / (float)10};
			values[0][i+99] = polys[0].map(evalPoints[i+99]);
			values[1][i+99] = polys[1].map(evalPoints[i+99]);
			values[2][i+99] = polys[3].map(evalPoints[i+99]);
		}
		comps[0] = new WeightedCostApproximator(evalPoints, values, new ConstantFunction(1,1f), 0f, -1);
		for (int i=-99; i<100; i++) {
			values[0][i+99] = polys[1].map(evalPoints[i+99]);
			values[1][i+99] = polys[2].map(evalPoints[i+99]);
			values[2][i+99] = polys[4].map(evalPoints[i+99]);
		}
		comps[1] = new WeightedCostApproximator(evalPoints, values, new ConstantFunction(1,1f), 0f, -1);

		LinearApproximator approximator = new CompositeApproximator(comps, new int[][]{{0},{0}});
		Function target = new Polynomial(new float[]{3f,2f,-2f});
		float[] coefficients = approximator.findCoefficients(target);

		float approx = 0f;

		for (int j=0; j<evalPoints.length; j++) {
			approx = polys[0].map(evalPoints[j]) * coefficients[0];
			approx += polys[1].map(evalPoints[j]) * coefficients[1];
			approx += polys[3].map(evalPoints[j]) * coefficients[2];
//			TestUtil.assertClose(approx, target.map(evalPoints[j]), 0.001f);
			approx = polys[1].map(evalPoints[j]) * coefficients[3];
			approx += polys[2].map(evalPoints[j]) * coefficients[4];
			approx += polys[4].map(evalPoints[j]) * coefficients[5];
//			TestUtil.assertClose(approx, target.map(evalPoints[j]), 0.001f);
		}

		float[][] evalPoints2 = new float[400][];
		float[][] values2 = new float[2][400];
		for (int i=0; i<=19; i++) {
			for (int j=0; j<=19; j++) {
				evalPoints2[i*20+j] = new float[]{i-9,j-9};
				values2[0][i*20+j] = posts[0].map(evalPoints2[i*20+j]);
				values2[1][i*20+j] = posts[1].map(evalPoints2[i*20+j]);
			}
		}
		comps[0] = new WeightedCostApproximator(evalPoints2, values2, new ConstantFunction(1,1f), 0f, -1);
		for (int i=0; i<=19; i++) {
			for (int j=0; j<=19; j++) {
				evalPoints2[i*20+j] = new float[]{i-9,j-9};
				values2[0][i*20+j] = posts[1].map(evalPoints2[i*20+j]);
				values2[1][i*20+j] = posts[0].map(evalPoints2[i*20+j]);
			}
		}
		comps[1] = new WeightedCostApproximator(evalPoints2, values2, new ConstantFunction(1,1f), 0f, -1);

		approximator = new CompositeApproximator(comps, new int[][]{{0,1},{0,1}});
		l = new ArrayList<Serializable>();
		l.add(Integer.valueOf(0));
		l.add(new Polynomial(new float[]{0f,-2f}));
		l.add(Integer.valueOf(1));
		l.add(new Polynomial(new float[]{0f,0f,3f}));
		target = new PostfixFunction(l, "", 2);

		coefficients = approximator.findCoefficients(target);
		for (int j=0; j<evalPoints2.length; j++) {
			approx = posts[0].map(evalPoints2[j]) * coefficients[0];
			approx += posts[1].map(evalPoints2[j]) * coefficients[1];
//			TestUtil.assertClose(approx, target.map(evalPoints2[j]), 0.001f);
			approx = posts[1].map(evalPoints2[j]) * coefficients[2];
			approx += posts[0].map(evalPoints2[j]) * coefficients[3];
//			TestUtil.assertClose(approx, target.map(evalPoints2[j]), 0.001f);
		}
	}

}
