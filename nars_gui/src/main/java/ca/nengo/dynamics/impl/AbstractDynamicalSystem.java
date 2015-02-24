/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "AbstractDynamicalSystem.java". Description:
"Base implementation of DynamicalSystem"

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
 * Created on 30-Mar-07
 */
package ca.nengo.dynamics.impl;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.model.Units;

/**
 * Base implementation of DynamicalSystem.
 *
 * @author Bryan Tripp
 */
public abstract class AbstractDynamicalSystem implements DynamicalSystem {

	private static final long serialVersionUID = 1L;

	private float[] myState;

	/**
	 * Arbitrary dynamical system
	 *
	 * @param state Initial state
	 */
	public AbstractDynamicalSystem(float[] state) {
		myState = state;
	}

	/**
	 * @see ca.nengo.dynamics.DynamicalSystem#f(float, float[])
	 */
	public abstract float[] f(float t, float[] u);

	/**
	 * @see ca.nengo.dynamics.DynamicalSystem#g(float, float[])
	 */
	public abstract float[] g(float t, float[] u);

	/**
	 * @see ca.nengo.dynamics.DynamicalSystem#getInputDimension()
	 */
	public abstract int getInputDimension();

	/**
	 * @see ca.nengo.dynamics.DynamicalSystem#getOutputDimension()
	 */
	public abstract int getOutputDimension();

	/**
	 * Returns Units.UNK by default.
	 *
	 * @see ca.nengo.dynamics.DynamicalSystem#getOutputUnits(int)
	 */
	public Units getOutputUnits(int outputDimension) {
		return Units.UNK;
	}

	/**
	 * @see ca.nengo.dynamics.DynamicalSystem#getState()
	 */
	public float[] getState() {
		return myState;
	}

	/**
	 * @see ca.nengo.dynamics.DynamicalSystem#setState(float[])
	 */
	public void setState(float[] state) {
		myState = state;
	}

	@Override
	public DynamicalSystem clone() throws CloneNotSupportedException {
		AbstractDynamicalSystem result = (AbstractDynamicalSystem) super.clone();
		result.myState = myState.clone();
		return result;
	}

}
