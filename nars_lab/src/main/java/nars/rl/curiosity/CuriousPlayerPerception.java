package nars.rl.curiosity;

import nars.rl.elsy.Perception;



public abstract class CuriousPlayerPerception extends Perception {
	private static double rMin = 0.02;
	private static double rMax = 0.325;
	private static double rSlope = 650;
	private static double pMin = 0.048;
	private static double pMax = 0.370;
	private static double pSlope = 200;
	private static double offset = 0;
	private Curiosity curiosity;
	private double novelty;
	private Perception foreseePerc = this;

        @Override
	public double getReward() {
		this.novelty = curiosity.getAvgError();
		double wundtCurve = wundtCurve(this.novelty);
		//System.out.println(wundtCurve);
		return wundtCurve;
	}

	public double wundtCurve(double n) {
		double R = func(n, rMin, rMax, rSlope);
		double P = func(n, pMin, pMax, pSlope);
		return R - P + offset;
	}

	private double func(double n, double min, double max, double slope) {
		return max / (1 + Math.exp(-slope * (n - min)));
	}
	
        @Override
	public void start() {
		super.start();
		if (foreseePerc != this) {
			foreseePerc.start();
		}
	}
	
        @Override
	public void perceive() {
		super.perceive();
		if (getOutput()!=null && foreseePerc != this) {
			foreseePerc.perceive();
		}
	}

	public Curiosity getCuriosity() {
		return curiosity;
	}

	public void setCuriosity(Curiosity curiosity) {
		this.curiosity = curiosity;
	}

	public double getNovelty() {
		return novelty;
	}
	
	public double[] getForeseeOutput() {
		return foreseePerc.getOutput();
	}

	public Perception getForeseePerc() {
		return foreseePerc;
	}

	public void setForeseePerc(Perception foreseePerc) {
		this.foreseePerc = foreseePerc;
		//this.foreseePerc.setInputPerception(this);
	}

	public static double getPMax() {
		return pMax;
	}

	public static void setPMax(double max) {
		pMax = max;
	}

	public static double getPMin() {
		return pMin;
	}

	public static void setPMin(double min) {
		pMin = min;
	}

	public static double getRMax() {
		return rMax;
	}

	public static void setRMax(double max) {
		rMax = max;
	}

	public static double getRMin() {
		return rMin;
	}

	public static void setRMin(double min) {
		rMin = min;
	}

	public static double getPSlope() {
		return pSlope;
	}

	public static void setPSlope(double slope) {
		pSlope = slope;
	}

	public static double getRSlope() {
		return rSlope;
	}

	public static void setRSlope(double slope) {
		rSlope = slope;
	}

	public static double getOffset() {
		return offset;
	}

	public static void setOffset(double offset) {
		CuriousPlayerPerception.offset = offset;
	}
}
