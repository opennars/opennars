/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.reinforcementlearning.domains;

/**
 * 
 * @author thorsten
 */
public class Point {
	public double x, y, vx, vy, decayx, decayy;

	public Point(double x, double y, double vx, double vy, double decayx,
			double decayy) {
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vy = vy;
		this.decayx = decayx;
		this.decayy = decayy;
	}
}
