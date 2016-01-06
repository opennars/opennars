/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.kif;

import java.io.IOException;

/**
 * 
 * @author me
 */
public interface KIFInference {

	/**
	 * ************************************************************* Add an
	 * assertion.
	 * 
	 * @param formula
	 *            asserted formula in the KIF syntax
	 * @return answer to the assertion (in the XML syntax)
	 * @throws IOException
	 *             should not normally be thrown
	 */
	String assertFormula(String formula);

	/**
	 * ************************************************************* Submit a
	 * query.
	 * 
	 * @param formula
	 *            query in the KIF syntax
	 * @param timeLimit
	 *            time limit for answering the query (in seconds)
	 * @param bindingsLimit
	 *            limit on the number of bindings
	 * @return answer to the query (in the XML syntax)
	 * @throws IOException
	 *             should not normally be thrown
	 */
	String submitQuery(String formula, int timeLimit, int bindingsLimit);

	/**
	 * ************************************************************* Terminate
	 * this instance of Vampire. <font color='red'><b>Warning:</b></font>After
	 * calling this functions no further assertions or queries can be done.
	 * 
	 * @throws IOException
	 *             should not normally be thrown
	 */
	void terminate();

}
