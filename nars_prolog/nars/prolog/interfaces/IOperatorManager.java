package nars.prolog.interfaces;
public interface IOperatorManager {
	
	/**
	 * Creates a new operator. If the operator is already provided,
	 * it replaces it with the new one
	 */
	void opNew(String name, String type, int prio);
	
	/**
	 * @return a copy of the current istance
	 */
	IOperatorManager clone();
	
}
