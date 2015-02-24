/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "Function.java". Description:
"A mathematical function from an n-D space to a 1-D space"

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
 * Created on May 16, 2006
 */
package ca.nengo.math;

import java.io.Serializable;

/**
 * <p>A mathematical function from an n-D space to a 1-D space. For simplicity we always
 * map to a 1-D space, and model maps to n-D spaces with n Functions.</p>
 *
 * <p>Instances of Function are immutable once they are created (ie their parameters
 * do not change over time).</p>
 *
 * @author Bryan Tripp
 */
public interface Function extends Serializable, Cloneable {

	/**
	 * @return Dimension of the space that the Function maps from
	 */
	public int getDimension();

	/**
	 * @param from Must have same length as getDimension()
	 * @return result of function operation on arg
	 */
	public float map(float[] from);

	/**
	 * @param from An array of arguments; each element must have length getDimension().
	 * @return Array of results of function operation on each arg
	 */
	public float[] multiMap(float[][] from);

	/**
	 * @return Valid clone
	 * @throws CloneNotSupportedException if clone can't be made
	 */
	public Function clone() throws CloneNotSupportedException;

}
