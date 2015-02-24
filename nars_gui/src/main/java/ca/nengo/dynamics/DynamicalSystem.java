/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "DynamicalSystem.java". Description:
"A state-space model of a continuous-time dynamical system"

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
 * Created on 1-Jun-2006
 */
package ca.nengo.dynamics;

import ca.nengo.model.Units;

import java.io.Serializable;

/**
 * <p>A state-space model of a continuous-time dynamical system. The system can
 * be linear or non-linear, and autonomous or time-varying.</p>
 *
 * <p>While a DynamicalSystem can be time-varying, it must be immutable. That
 * is, its properties can change over simulation time, but not over run time.</p>
 *
 * TODO: units here or in subinterface?
 * TODO: reference Chen
 *
 * @author Bryan Tripp
 */
public interface DynamicalSystem extends Serializable, Cloneable {

	/**
	 * The dynamic equation.
	 *
	 * @param t Time
	 * @param u Input vector
	 * @return 1st derivative of state vector
	 */
	public float[] f(float t, float[] u);

	/**
	 * The output equation.
	 *
	 * @param t Time
	 * @param u Input vector
	 * @return Output vector
	 */
	public float[] g(float t, float[] u);

	/**
	 * @return State vector
	 */
	public float[] getState();

	/**
	 * @param state New state vector
	 */
	public void setState(float[] state);

	/**
	 * @return Dimension of input vector
	 */
	public int getInputDimension();

	/**
	 * @return Dimension of output vector
	 */
	public int getOutputDimension();

	/**
	 * @param outputDimension Numbered from 0
	 * @return Units of output in the given dimension
	 */
	public Units getOutputUnits(int outputDimension);

	/**
	 * @return An identical copy of this system which references an independent
	 *   copy of the state variables
	 * @throws CloneNotSupportedException if something causes clone not to work
	 */
	public DynamicalSystem clone() throws CloneNotSupportedException;

}
