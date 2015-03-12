/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ExpandableNode.java". Description:
"A Node to which Terminations can be added after construction, in a standard manner"

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

/**
 *
 */
package ca.nengo.model;

/**
 * A Node to which Terminations can be added after construction, in a standard manner.
 * Note that a given Node might provide additional methods for adding Terminations if more
 * customization is needed.
 *
 * @author Bryan Tripp
 */
public interface ExpandableNode extends Node<Node> {

	/**
	 * @return Output dimension of Terminations onto this Node
	 */
	public int getDimension();

	/**
	 * Adds a new Termination onto this Node.
	 *
	 * @param name Unique name for the Termination (in the scope of this Node)
	 * @param weights Connection weights. Length must equal getDimension(). Each component
	 * 		must have length equal to the dimension of the Origin that will connect to this Termination.
	 * @param tauPSC Time constant with which incoming signals are filtered. (All Terminations have
	 * 		this property, but it may have slightly different interpretations per implementation.)
	 * @param modulatory If true, inputs to the Termination are not summed with other inputs (they
	 * 		only have modulatory effects, eg on plasticity, which must be defined elsewhere).
	 * @return resulting Termination
	 * @throws StructuralException if length of weights doesn't equal getDimension(),
	 * 		or if there are different numbers of weights given in different rows.
	 */
	public NTarget addTarget(String name, float[][] weights, float tauPSC, boolean modulatory) throws StructuralException;

	/**
	 * @param name Name of Termination to remove.
	 * @return The removed Termination
	 * @throws StructuralException if the Termination doesn't exist
	 */
	public NTarget removeTarget(String name) throws StructuralException;
	
	public ExpandableNode clone() throws CloneNotSupportedException;
}
