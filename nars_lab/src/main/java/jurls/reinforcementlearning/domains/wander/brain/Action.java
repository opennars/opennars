package jurls.reinforcementlearning.domains.wander.brain;

import java.io.Serializable;

/**
 * Each instance of this class is responsible for executing one specified action
 * of the Agent.
 * 
 * @author Elser http://www.elsy.gdan.pl
 */
public abstract class Action implements Serializable {
	/**
	 * Here you implement what the agent should do, when performing the action.
	 */
	public abstract void execute();
}
