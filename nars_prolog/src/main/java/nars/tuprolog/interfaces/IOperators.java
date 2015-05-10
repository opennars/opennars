package nars.tuprolog.interfaces;
public interface IOperators {
	
	/**
	 * Creates a new operate. If the operate is already provided,
	 * it replaces it with the new one
	 */
	void opNew(String name, String type, int prio);
	

	
}
