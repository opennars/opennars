/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "InstantaneousOutput.java". Description:
"An output from an Origin at an instant in time"

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
 * Created on May 17, 2006
 */
package ca.nengo.model;

import java.io.Serializable;

/**
 * <p>An output from an Origin at an instant in time. This is the medium we use to pass
 * information around a neural circuit.</p>
 *
 * <p>Note that an Ensemble or Neuron may have multiple Origins and can therefore produce
 * multiple outputs simultaneously. For example, one Origin of an Ensemble might produce
 * spiking outputs, another the decoded estimates of variables it represents, and others
 * decoded functions of these variables.</p>
 *
 * <p>Note that the methods for getting output values from an InstantaneousOuput are not
 * defined here, but on subinterfaces.</p>
 *
 * @author Bryan Tripp
 */
public interface InstantaneousOutput extends Serializable, Cloneable {

	/**
	 * @return Units in which output is expressed.
	 */
	public Units getUnits();

	/**
	 * @return Dimension of output
	 */
	public int getDimension();

	/**
	 * @return Time at which output is produced.
	 */
	public float getTime();

	/**
	 * @return Valid clone
	 * @throws CloneNotSupportedException if clone can't be made
	 */
	public InstantaneousOutput clone() throws CloneNotSupportedException;

}

