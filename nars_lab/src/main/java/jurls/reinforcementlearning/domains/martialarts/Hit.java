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
public class Hit {
	public final int x;
	public final int y;
	public final int reward;
	public int progress = 0;

	public Hit(int x, int y, int reward) {
		this.x = x;
		this.y = y;
		this.reward = reward;
	}
}
