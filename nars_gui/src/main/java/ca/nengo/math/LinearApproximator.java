/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "LinearApproximator.java". Description:
"Finds coefficients on a set of functions so that their linear combination approximates
  a target Function"

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
 * Created on 5-Jun-2006
 */
package ca.nengo.math;

import java.io.Serializable;

/**
 * <p>Finds coefficients on a set of functions so that their linear combination approximates
 * a target Function. In other words, finds a_i so that sum(a_i * f_i(x)) roughly equals
 * t(x) in some sense, where f_i are the functions to be combined and t is a target function,
 * both over some range of the variable x.</p>
 *
 * <p>Can be used to find decoding vectors and synaptic weights.</p>
 *
 * @author Bryan Tripp
 */
public interface LinearApproximator extends Serializable, Cloneable {

	/**
	 * Note: more information is needed than the arguments provide (for example
	 * the functions that are to be combined to estimate the target). These other
	 * data are object properties. This enables re-use of calculations based on these
	 * data, for estimating multiple functions.
	 *
	 * @param target Function to approximate
	 * @return coefficients on component functions which result in an approximation of the
	 * 		target
	 */
	public float[] findCoefficients(Function target);

	/**
	 * @return Valid clone
	 * @throws CloneNotSupportedException if clone can't be made
	 */
	public LinearApproximator clone() throws CloneNotSupportedException;

	/**
	 * @return Points at which target functions are evaluated. Each row (or float[]) corresponds to
	 *  	a single evaluation point. These points should usually be uniformly distributed, because
	 *  	the sum of error at these points is treated as an integral over the domain of interest.
	 */
	public float[][] getEvalPoints();

	/**
	 * @return The values of component functions at the evaluation points. The first dimension
	 * 		makes up the list of functions, and the second the values of these functions at each
	 * 		evaluation point.
	 */
	public float[][] getValues();

}
