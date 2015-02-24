/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "Noise.java". Description:
"An model of noise that can be explicitly injected into a circuit (e.g"

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
 * Created on 24-May-07
 */
package ca.nengo.model;

import java.io.Serializable;

/**
 * <p>An model of noise that can be explicitly injected into a circuit (e.g. added to
 * an Origin). </p>
 *
 * <p>Noise may be cloned across independent dimensions of a Noisy. This means that
 * either 1) noise parameters can't be changed after construction, or 2) parameters
 * must be shared or propagated across clones. </p>
 *
 * @author Bryan Tripp
 */
public interface Noise extends Cloneable, Resettable, Serializable {

	/**
	 * How do we refer to the dimension?
	 */
	public static final String DIMENSION_PROPERTY = "dimension";

	/**
	 * @param startTime Simulation time at which step starts
	 * @param endTime Simulation time at which step ends
	 * @param input Value which is to be corrupted by noise
	 * @return The noisy values (inputs corrupted by noise)
	 */
	public float getValue(float startTime, float endTime, float input);

	/**
	 * @return Valid clone
	 */
	public Noise clone();

	/**
	 * An object that implements this interface is subject to Noise.
	 *
	 * @author Bryan Tripp
	 */
	public interface Noisy {

		/**
		 * @param noise New noise model
		 */
		public void setNoise(Noise noise);

		/**
		 * @return Noise with which the object is to be corrupted
		 */
		public Noise getNoise();

	}

}
