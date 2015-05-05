package nars.tuprolog.interfaces;
public interface IOperatorManager {
	
	/**
	 * Creates a new operate. If the operate is already provided,
	 * it replaces it with the new one
	 */
	void opNew(String name, String type, int prio);
	
	/**
	 * @return a copy of the current istance
	 */
	IOperatorManager clone();
	
}
