package jurls.reinforcementlearning.domains.wander;

import nars.Video;

import java.awt.*;

public class Obstacle {
	private static final double MIN_SIZE = World.SIZE / 10;
	private static final double MAX_SIZE = World.SIZE / 5;
	public double x;
	public double y;
	public double r;
	public Color c;

	public static double d(double from, double to) {
		if (to < from) {
			return 0;
		}
		return (to - from) * (Math.random()) + from;
	}

	public Obstacle() {
		x = d(-World.SIZE, World.SIZE);
		y = d(-World.SIZE, World.SIZE);
		r = d(MIN_SIZE, MAX_SIZE);
		c = new Color(Video.colorHSB(
				0.7f + 0.1f * (float) ((r - MIN_SIZE) / (MAX_SIZE - MIN_SIZE)),
				0.65f, 0.85f));
	}

	public boolean circleCollides(double x2, double y2, double r2) {
		double dist = getDistanceSq(x2, y2);
		return dist - (r2 * r2) < (r * r);
	}

	public boolean pointCollides(double x2, double y2) {
		return circleCollides(x2, y2, 0);
	}

	private double getDistanceSq(double x2, double y2) {
		double xd = (x2 - x);
		double yd = (y2 - y);
		double dist = (xd * xd + yd * yd);
		return dist;
	}
	private double getDistance(double x2, double y2) {
		return Math.sqrt(getDistanceSq(x2, y2));
	}

}
