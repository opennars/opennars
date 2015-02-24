package ca.nengo.math.impl;

/**
 * <p>A Function that produces a fixed sequence of outputs, independent of input.</p>
 *
 *
 *
 * @author Daniel Rasmussen
 */
public class FixedSignalFunction extends AbstractFunction {

	private static final long serialVersionUID = 1L;

	private int myDimension;
	private float[][] mySignal;
	private int myIndex;

	/**
	 * @param signal sequence defining output (each element is a (potentially) multidimensional output)
	 * @param dimension Dimension of signal on which to base Function output
	 * @param reportdimension value this function will report as its input dimension (this is only needed
	 * 			for compatibility with other components, since this function takes no input)
	 */
	public FixedSignalFunction(float[][] signal, int dimension, int reportdimension) {
		super(reportdimension);
		mySignal = signal;
		myDimension = dimension;
		myIndex = 0;
	}
	public FixedSignalFunction(float[][] signal, int dimension) {
		super(1);
		mySignal = signal;
		myDimension = dimension;
		myIndex = 0;
	}

	public float[][] getSignal() {
		return mySignal;
	}

	public void setSignal(float[][] signal) {
		mySignal = signal;
	}

	/**
	 * @return Dimension of series on which to base Function output
	 */
	public int getSeriesDimension() {
		return myDimension;
	}

	/**
	 * @param dim Dimension of series on which to base Function output
	 */
	public void setSeriesDimension(int dim) {
		if (dim < 0 || dim >= mySignal[0].length) {
			throw new IllegalArgumentException("Dimension must be between 0 and " + (mySignal[0].length-1));
		}
		myDimension = dim;
	}

	/**
	 * @see ca.nengo.math.impl.AbstractFunction#map(float[])
	 */
	public float map(float[] from) {
		float output = mySignal[myIndex][myDimension];
		myIndex = (myIndex+1) % mySignal.length;
		                        
		return output;
	}
}
