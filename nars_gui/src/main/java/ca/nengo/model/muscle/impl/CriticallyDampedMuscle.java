/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "CriticallyDampedMuscle.java". Description:
"A simple, phenomenological muscle model in which activation-force dynamics are
  modelled with a linear 2nd-order low-pass filter (see e.g"

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
 * Created on 4-Apr-07
 */
package ca.nengo.model.muscle.impl;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.impl.LTISystem;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;

/**
 * <p>A simple, phenomenological muscle model in which activation-force dynamics are
 * modelled with a linear 2nd-order low-pass filter (see e.g. Winter, 1990,
 * Biomechanics and Motor Control of Human Movement).</p>
 *
 * <p>This type of model is most viable in isometric conditions.</p>
 *
 * @author Bryan Tripp
 */
public class CriticallyDampedMuscle extends SkeletalMuscleImpl {

	private static final long serialVersionUID = 1L;

	/**
	 * @param name Name of muscle
	 * @param cutoff Cutoff frequency of filter model (Hz)
	 * @param maxForce Cutoff force for muscle
	 * @throws StructuralException if there's an issue making dynamics
	 */
	public CriticallyDampedMuscle(String name, float cutoff, float maxForce) throws StructuralException {
		super(name, makeDynamics(cutoff, maxForce));
	}

	//returns a Butterworth filter with requested cutoff (Hz)
	private static DynamicalSystem makeDynamics(float cutoff, float maxForce) {
		float wc = 2 * (float) Math.PI * cutoff;
		float[][] A = new float[][]{new float[]{0, 1}, new float[]{-wc*wc, - (float) Math.sqrt(2) * wc}};
		float[][] B = new float[][]{new float[]{0}, new float[]{wc*wc}};
		float[][] C = new float[][]{new float[]{maxForce, 0}};
		float[][] D = new float[][]{new float[]{0, 0}};
		return new LTISystem(A, B, C, D, new float[]{0, 0}, new Units[]{Units.N});
	}

}
