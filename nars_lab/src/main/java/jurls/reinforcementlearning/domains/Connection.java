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
public class Connection {

	public final double length;
	public final Point p1, p2;

	public Connection(double length, Point p1, Point p2) {
		this.length = length;
		this.p1 = p1;
		this.p2 = p2;
	}
}
