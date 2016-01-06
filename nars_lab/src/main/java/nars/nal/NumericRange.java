package nars.nal;

public class NumericRange {

	private static void vectorize(double[] qIn, int i, double conceptPriority,
			int i0, int i1, int quant) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	double min;
	double max;
	private double value;
	private boolean autoRange;

	public NumericRange() {
		min = max = Double.NaN;
		autoRange = true;
	}

	public NumericRange(double center) {
		min = max = center;
		set(center);
	}

	// first input
	// System.out.println(this + "autorange update min: " + value + " -> " +
	// min);
	// System.out.println(this + "autorange update max: " + value + " -> " +
	// min);
	// hard limit to range
	// d = Math.min(d, 1.0);
	// System.out.println(v + " " + pp + " " + d);
	public NumericRange(double center, double radius) {
		min = center - radius;
		max = center + radius;
		set(center);
	}
	/** normalize to proportional value in range 0..1 */
	/** denormalize to range */
	/**
	 * proportional value normalized to 0..1 then divided into uniform discrete
	 * steps
	 */
	/**
	 * shrinks the distance between min and max around a target value to
	 * increase the 'contrast' gradually over time as seen by whatever uses this
	 * result
	 */

	public void set(double value) {
		this.value = value;
		if (autoRange) {
			if ((Double.isNaN(min)) && !(Double.isNaN(value))) {
				// first input
				min = value;
				max = value;
			} else {
				if (value < min) {
					// System.out.println(this + "autorange update min: " +
					// value + " -> " + min);
					min = value;
				}
				if (value > max) {
					// System.out.println(this + "autorange update max: " +
					// value + " -> " + min);
					max = value;
				}
			}
		}
	}

	public double get() {
		return value;
	}

	public double max() {
		return max;
	}

	public double min() {
		return min;
	}

	public double proportion() {
		return proportion(get());
	}

	/** normalize to proportional value in range 0..1 */
	public double proportion(double v) {
		if (max == min) {
			return 0.5;
		}
		if (Double.isNaN(v)) {
			return 0.5;
		}
		return (v - min) / (max - min);
	}

	/** denormalize to range */
	public double unproportion(double p) {
		return (p * (max - min)) + min;
	}

	/**
	 * proportional value normalized to 0..1 then divided into uniform discrete
	 * steps
	 */
	public int proportionDiscrete(double v, int steps) {
		double p = proportion(v);
		// hard limit to range
		p = Math.min(Math.max(p, 0), 1.0);
		return (int) (Math.round(p * (steps - 1)));
	}

	public void vectorize(double[] target, int index, int steps) {
		vectorize(target, index, get(), steps);
	}

	public void vectorize(double[] target, int index, double v, int steps) {
		int p = proportionDiscrete(v, steps);
		target[index + p] = 1;
	}

	public void vectorizeSmooth(double[] target, int index, double v, int steps) {
		set(v);
		v = proportion(v);
		if (Double.isNaN(v)) {
			v = 0.5;
		} else {
			v = Math.min(1, v);
			v = Math.max(0, v);
		}
		double stepScale = 1.0 / (steps - 1);
		for (int p = 0; p < steps; p++) {
			double pp = (p) * stepScale;
			double d = 1.0 - Math.abs(pp - v) / stepScale;
			d = Math.max(d, 0);
			// d = Math.min(d, 1.0);
			// System.out.println(v + " " + pp + " " + d);
			target[index + p] = d;
		}
	}

	private NumericRange add(double v) {
		value += v;
		if (value < min) {
			value = min;
		}
		if (value > max) {
			value = max;
		}
		return this;
	}

	/**
	 * shrinks the distance between min and max around a target value to
	 * increase the 'contrast' gradually over time as seen by whatever uses this
	 * result
	 */
	public void adaptiveContrast(double rate, double target) {
		double range = (max - min) * rate; // rate used here is in a slightly
											// different context than below; we
											// may want two different
											// parameters. this is the amount to
											// shrink the range window around
											// the target, and the below rate is
											// how quickly the window changes.

		double targetMin = target - range / 2.0;
		double targetMax = target + range / 2.0;
		min = (1.0 - rate) * min + (rate) * targetMin;
		max = (1.0 - rate) * max + (rate) * targetMax;
	}
}
