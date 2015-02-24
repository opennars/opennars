/*
 * Created on 25-Jul-2006
 */
package ca.nengo.math.impl;

import ca.nengo.math.PDF;
import ca.nengo.plot.Plotter;
import junit.framework.TestCase;

public class ExponentialPDFTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testNothing() {
    }

    //functional test ...
    public static void main(String[] args) {
        PDF pdf = new ExponentialPDF(.1f);
        Plotter.plot(pdf, 0, .001f, .5f, "aagg");

        float binSize = .05f;
        float[] bins = new float[8];
        for (int i = 0; i < 10000; i++) {
            float foo = pdf.sample()[0];
            int bin = (int) Math.floor(foo / binSize);
            if (bin <= 7) {
                bins[bin] += 1;
            }
        }

        Plotter.plot(bins, "bins");
    }
}
