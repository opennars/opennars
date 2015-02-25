/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "BiasTermination.java". Description:
"Created on 24-Apr-07"

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
 * Created on 24-Apr-07
 */
package ca.nengo.neural.nef.impl;

import ca.nengo.dynamics.Integrator;
import ca.nengo.dynamics.LinearSystem;
import ca.nengo.model.Node;
import ca.nengo.model.StructuralException;

/**
 * Termination which is somehow used in the Bias process?
 *
 * TODO: Figure out where this is used and why.
 */
public class BiasTarget extends DecodedTarget {

	private static final long serialVersionUID = 1L;

	private float[] myBiasEncoders;
	private String myBaseName;
	private boolean myIsEnabled;

	/**
	 * @param node Parent node
	 * @param name Termination name
	 * @param baseName Original termination name?
	 * @param dynamics Linear system that defines dynamics
	 * @param integrator Integrator to integrate dynamics
	 * @param biasEncoders biased encoders?
	 * @param interneurons Is parent a population of interneurons...?
	 * @throws StructuralException if DecodedTermination can't be made
	 */
	public BiasTarget(Node node, String name, String baseName, LinearSystem dynamics,
                      Integrator integrator, float[] biasEncoders, boolean interneurons) throws StructuralException {
		super(node, name, new float[][]{new float[]{interneurons ? -1 : 1}}, dynamics, integrator);
		myBiasEncoders = biasEncoders;
		myBaseName = baseName;
		myIsEnabled = true;
	}

	/**
	 * @return Underlying termination name
	 */
	public String getBaseTerminationName() {
		return myBaseName;
	}

	/**
	 * @return biased encoders?
	 */
	public float[] getBiasEncoders() {
		return myBiasEncoders;
	}

	/**
	 * @param enable If true, the Termination is enabled; if false, it is disabled (so that inputs have no effect)
	 */
	public void setEnabled(boolean enable) {
		//TODO: should this be pulled up to Termination? DecodedTermination?
		myIsEnabled = enable;
	}

	/**
	 * @return True if this Termination is enabled
	 */
	public boolean isEnabled() {
		return myIsEnabled;
	}

	@Override
	public float[] getOutput() {
		float[] result = super.getOutput();
		if (!myIsEnabled) {
			result = new float[result.length];
		}
		return result;
	}

}
