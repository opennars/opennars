/*
 * Created June 12, 2007
 */
package ca.nengo.math.impl;

import ca.nengo.TestUtil;
import ca.nengo.math.Function;
import ca.nengo.model.Units;
import ca.nengo.plot.Plotter;
import ca.nengo.util.TimeSeries1D;
import ca.nengo.util.impl.TimeSeries1DImpl;
import junit.framework.TestCase;

/*
 * Unit tests for PolynomialCurveFitter
 */
public class PolynomialCurveFitterTest extends TestCase {

	/*
	 * Test method for 'ca.nengo.math.impl.PolynomialCurveFitter.getOrder()'
	 */
	public void testGetOrder() {
		PolynomialCurveFitter pcf = new PolynomialCurveFitter(6);
		assertEquals(6, pcf.getOrder());
	}

	/*
	 * Test method for 'ca.nengo.math.impl.PolynomialCurveFitter.fit()'
	 */
	public void testFindCoefficients() {
		Function target = new Polynomial(new float[]{1f,4f,-3f,0.5f,0.01f});
		PolynomialCurveFitter pcf = new PolynomialCurveFitter(3);
		float[][] values = new float[2][10];

		for (int i=0; i<values[0].length; i++) {
			values[0][i] = -9 + i * 2;
			values[1][i] = target.map(new float[]{values[0][i]});
		}

		Function fitted = pcf.fit(values[0], values[1]);

//		Plotter.plot(target, -10, 0.05f, 10, "target");
//		Plotter.plot(fitted, -10, 0.05f, 10, "fitted");

		float targetVal = 0f;
		float fittedVal = 0f;
		for (int i=-8; i<9; i=i+2) {
			targetVal = target.map(new float[]{i});
			fittedVal = fitted.map(new float[]{i});
			TestUtil.assertClose(targetVal, fittedVal, 10f);
		}

		pcf = new PolynomialCurveFitter(2);

		float[] examplex = new float[]{1f, 2f, 3f, 4f};
		float[] exampley = new float[]{3f, 2f, 1f, 2f};
		fitted = pcf.fit(examplex, exampley);

		float[] x = new float[50];
		float[] y = new float[50];
		float dx = 0.1f;
		for (int i = 0; i < x.length; i++) {
			x[i] = i*dx;
			y[i] = fitted.map(new float[]{x[i]});
		}

//		TimeSeries1D approx = new TimeSeries1DImpl(x, y, Units.UNK);
//		TimeSeries1D actual = new TimeSeries1DImpl(examplex, exampley, Units.UNK);

		for (int i = 0; i < examplex.length; i++) {
			TestUtil.assertClose(exampley[i], fitted.map(new float[]{examplex[i]}), 0.5f);
		}

	}

    public static void main(String[] args) {
        PolynomialCurveFitter fitter = new PolynomialCurveFitter(2);

        float[] examplex = new float[]{1f, 2f, 3f, 4f};
        float[] exampley = new float[]{3f, 2f, 1f, 2f};
        Function f = fitter.fit(examplex, exampley);

        float[] x = new float[50];
        float[] y = new float[50];
        float dx = 0.1f;
        for (int i = 0; i < x.length; i++) {
            x[i] = i*dx;
            y[i] = f.map(new float[]{x[i]});
        }

        TimeSeries1D approx = new TimeSeries1DImpl(x, y, Units.UNK);
        TimeSeries1D actual = new TimeSeries1DImpl(examplex, exampley, Units.UNK);
        Plotter.plot(approx, actual, "polynomial");
    }
}
