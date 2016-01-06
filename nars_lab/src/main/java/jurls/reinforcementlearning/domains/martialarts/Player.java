/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.reinforcementlearning.domains.martialarts;

/**
 * 
 * @author thorsten
 */
public class Player {
	public int minX = Integer.MAX_VALUE;
	public int maxX = Integer.MIN_VALUE;
	public int minY = Integer.MAX_VALUE;
	public int maxY = Integer.MIN_VALUE;
	public int x = 0;
	public boolean mirror = false;
	public int moveImageIndex = 0;
	public int moveIndex = -1;
	public Move[] moves = null;
}
