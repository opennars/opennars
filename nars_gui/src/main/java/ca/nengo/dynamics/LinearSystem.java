/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "LinearSystem.java". Description: 
"A linear dynamical system, which may or may not be time-varying"

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
 * Created on 7-Jun-2006
 */
package ca.nengo.dynamics;

/**
 * <p>A linear dynamical system, which may or may not be time-varying. We use 
 * the state-space model of linear systems, which consist of four (possibly 
 * time-varying) matrices.</p>
 * 
 * TODO: ref chen
 * 
 * <p>The distinction between linear and non-linear dynamical systems is 
 * important, because many assumptions that hold for linear systems do not hold 
 * in general. For this reason, only linear systems can be used in some situations, 
 * and we need this interface to enforce their use.</p>
 *  
 * @author Bryan Tripp
 */
public interface LinearSystem extends DynamicalSystem {

	/**
	 * @param t Simulation time
	 * @return The dynamics matrix at the given time 
	 */
	public float[][] getA(float t);
	
	/**
	 * @param t Simulation time
	 * @return The input matrix at the given time 
	 */
	public float[][] getB(float t);
	
	/**
	 * @param t Simulation time
	 * @return The output matrix at the given time 
	 */
	public float[][] getC(float t);

	/**
	 * @param t Simulation time
	 * @return The passthrough matrix at the given time 
	 */
	public float[][] getD(float t);
	
}
