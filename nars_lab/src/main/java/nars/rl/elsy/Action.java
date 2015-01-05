package nars.rl.elsy;

import java.io.Serializable;

/**
 * Each instance of this class is responsible for executing
 * one specified action of the Agent. 
 * @author Elser http://www.elsy.gdan.pl
 */
public abstract class Action implements Serializable{
	private static final long serialVersionUID = 1L;
	/**
	 * Here you implement what the agent should do,
	 * when performing the action. 
	 */
	public abstract int execute();
}
