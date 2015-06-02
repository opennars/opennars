/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "Node.java". Description:
"A part of a Network that can be run independently (eg a Neuron)"

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
package ca.nengo.model;

import ca.nengo.util.ScriptGenException;
import ca.nengo.util.VisiblyChanges;
import nars.util.data.id.Named;

import java.io.Serializable;
import java.util.HashMap;

/**
 * A part of a Network that can be run independently (eg a Neuron). Normally
 * a source of Sources and/or Targets.
 *
 * @author Bryan Tripp
 */
public interface Node<N extends Node> extends Serializable, Resettable, SimulationMode.ModeConfigurable, VisiblyChanges, Cloneable, Named<String> {

    public final static Node[] EMPTY = new Node[0];

    /**
	 * @return Name of Node (must be unique in a Network)
	 */
	public String name();

	/**
	 * @param name The new name
	 * @throws StructuralException if name already exists?
	 */
	public void setName(String name) throws StructuralException;

	/**
	 * Runs the Node (including all its components), updating internal state and outputs as needed.
	 * Runs should be short (eg 1ms), because inputs can not be changed during a run, and outputs
	 * will only be communicated to other Nodes after a run.
	 *
	 * @param startTime simulation time at which running starts (s)
	 * @param endTime simulation time at which running ends (s)
	 * @throws SimulationException if a problem is encountered while trying to run
	 */
	public void run(float startTime, float endTime) throws SimulationException;

	/**
	 * @return Sets of ouput channels (eg spiking outputs, gap junctional outputs, etc.)
	 */
	@Deprecated public NSource[] getSources();

	/**
	 * @param name Name of an Origin on this Node
	 * @return The named Origin if it exists
	 * @throws StructuralException if the named Origin does not exist
	 */
	public NSource getSource(String name) throws StructuralException;

	/**
	 * @return Sets of input channels (these have the same dimension as corresponding Origins
	 * 		to which they are connected).
	 */
    @Deprecated public NTarget[] getTargets();

	/**
	 * @param name Name of a Termination onto this Node
	 * @return The named Termination if it exists
	 * @throws StructuralException if the named Termination does not exist
	 */
	public NTarget getTarget(String name) throws StructuralException;

    final static Node[] emptyNodeArray = new Node[0];


	
    /**
     * @param scriptData Map of class parent and prefix data for generating python script
     * @return Python script for generating the node
     * @throws ScriptGenException if the node cannot be generated in script
     */
	public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException;

	/**
	 * @return User-specified documentation for the Node, if any
	 */
	public String getDocumentation();

	/**
	 * @param text New user-specified documentation for the Node
	 */
	public void setDocumentation(String text);
	
	/**
	 * @return An independent copy of the Node
	 * @throws CloneNotSupportedException if clone can't be made
	 */
	public Node clone() throws CloneNotSupportedException;

    @Deprecated default public Node[] getNodes() {
        return emptyNodeArray;
    }
}
