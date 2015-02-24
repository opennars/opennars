package ca.nengo.math.impl;

import ca.nengo.math.PDF;

/**
 * <p>A Function that produces outputs drawn from a given distribution.</p>
 *
 *
 *
 * @author Daniel Rasmussen
 */
public class PDFFunction extends AbstractFunction {

	private static final long serialVersionUID = 1L;

	private PDF myPDF;

	/**
	 * @param signal sequence defining output (each element is a (potentially) multidimensional output)
	 * @param dimension Dimension of signal on which to base Function output
	 */
	public PDFFunction(PDF pdf) {
		super(1);
		myPDF = pdf;
	}

	/**
	 * @return TimeSeries from which to obtain Function of time
	 */
	public PDF getPDF() {
		return myPDF;
	}
	
	/**
	 * @param pdf input PDF
	 */
	public void setPDF(PDF pdf) {
		myPDF = pdf;
	}

	/**
	 * @see ca.nengo.math.impl.AbstractFunction#map(float[])
	 */
	public float map(float[] from) {
		return myPDF.sample()[0];
	}
}

