/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "SimulationException.java". Description: 
"A problem encountered while trying to run a simulation.
     
  @author Bryan Tripp"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU 
Public License license (the GPL License), in which case the provisions of GPL 
License are applicable  instead of those above. If you wish to allow use of your 
version of this file only under the terms of the GPL License and not to allow 
others to use your version of this file under the MPL, indicate your decision 
by deleting the provisions above and replace  them with the notice and other 
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
*/

/*
 * Created on May 23, 2006
 */
package ca.nengo.model;

import ca.nengo.NengoException;

/**
 * A problem encountered while trying to run a simulation.
 *    
 * @author Bryan Tripp
 */
public class SimulationException extends NengoException {

	private static final long serialVersionUID = 1L;
	
	private String myEnsembleName; 

	/**
	 * @param message Text explanation of the exception. 
	 */
	public SimulationException(String message) {
		super(message);
	}

	/**
	 * @param cause Another throwable that indicates a problem underlying this 
	 * 		exception.  
	 */
	public SimulationException(Throwable cause) {
		super(cause); 
	}

	/**
	 * @param message Text explanation of the exception. 
	 * @param cause Another throwable that indicates a problem underlying this 
	 * 		exception.  
	 */
	public SimulationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Adds ensemble name to message. 
	 */
	public String getMessage() {
		String message = super.getMessage();
		
		if (myEnsembleName != null) {
			message = message + " (ensemble: " + myEnsembleName + ')';
		}
		
		return message;
	}
	
	/**
	 * @param name Name of the ensemble in which the exception occured
	 */
	public void setEnsemble(String name) {
		myEnsembleName = name;
	}

}
