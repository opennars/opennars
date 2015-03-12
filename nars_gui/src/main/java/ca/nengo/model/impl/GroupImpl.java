/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "EnsembleImpl.java". Description:
"Default implementation of Ensemble.

  Origins or Terminations can be set up on Nodes before they are grouped into an
  Ensemble"

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
 * Created on 31-May-2006
 */
package ca.nengo.model.impl;

import ca.nengo.math.PDF;
import ca.nengo.math.impl.IndicatorPDF;
import ca.nengo.model.*;
import ca.nengo.neural.neuron.impl.ExpandableSpikingNeuron;
import ca.nengo.util.ScriptGenException;

import java.util.*;

/**
 * <p>Default implementation of Ensemble.</p>
 *
 * <p>Origins or Terminations can be set up on Nodes before they are grouped into an
 * Ensemble. After Nodes are added to an Ensemble, no Origins or Terminations should
 * be added to them directly. Terminations can be added with EnsembleImpl.addTermination(...)
 * If a Termination is added directly to a Node after the Node is added to the
 * Ensemble, the Termination will not appear in Ensemble.getTerminations()</p>
 *
 * TODO: test
 *
 * @author Bryan Tripp
 */
public class GroupImpl extends AbstractGroup implements ExpandableNode {

	private static final long serialVersionUID = 1L;

	protected ExpandableNode[] myExpandableNodes;
	protected Map<String, NTarget> myExpandedTargets;

	/**
	 * @param name Name of Ensemble
	 * @param nodes Nodes that make up the Ensemble
	 */
	public GroupImpl(String name, Node[] nodes) {
		super(name, nodes);

		myExpandableNodes = findExpandable(nodes);
		myExpandedTargets = new LinkedHashMap<String, NTarget>(10);
	}

	/**
	 * @param name Name of Ensemble
	 * @param factory Factory class that will create nodes
	 * @param n Number of nodes to create
	 * @throws StructuralException if any problem halts construction
	 */
	public GroupImpl(String name, NodeFactory factory, int n) throws StructuralException {
		super(name, make(factory, n));

		myExpandableNodes = findExpandable(this.getNodes());
		myExpandedTargets = new LinkedHashMap<String, NTarget>(10);
	}

	private static Node[] make(NodeFactory factory, int n) throws StructuralException {
		Node[] result = new Node[n];

		for (int i = 0; i < n; i++) {
			result[i] = factory.make("node " + i);
		}

		return result;
	}

	//finds neurons with expandable synaptic integrators
	private static ExpandableNode[] findExpandable(Node[] nodes) {
		ArrayList<ExpandableNode> result = new ArrayList<ExpandableNode>(nodes.length * 2);

		for (Node node : nodes) {
			if (node instanceof ExpandableNode) {
				result.add((ExpandableNode) node);
			}
		}

		return result.toArray(new ExpandableNode[result.size()]);
	}

    /**
     * @see ca.nengo.model.Node#getTarget(java.lang.String)
     */
    @Override
    public NTarget getTarget(String name) throws StructuralException {
        return myExpandedTargets.containsKey(name) ?
                myExpandedTargets.get(name) : super.getTarget(name);
    }

	/**
	 * @see ca.nengo.model.Group#getTargets()
	 */
	public NTarget[] getTargets() {
		ArrayList<NTarget> result = new ArrayList<NTarget>(10);
		result.addAll(myExpandedTargets.values());

		NTarget[] composites = super.getTargets();
        Collections.addAll(result, composites);

		return result.toArray(new NTarget[result.size()]);
	}

	/**
	 * This Ensemble does not support SimulationMode.DIRECT.
	 *
	 * @see ca.nengo.model.Group#setMode(ca.nengo.model.SimulationMode)
	 */
	@Override
    public void setMode(SimulationMode mode) {
		super.setMode(mode);
	}

	/**
	 * @param weights Each row is used as a 1 by m matrix of weights in a new termination on the nth expandable node
	 *
	 * @see ca.nengo.model.ExpandableNode#addTarget(java.lang.String, float[][], float, boolean)
	 */
    public synchronized NTarget addTarget(String name, float[][] weights, float tauPSC, boolean modulatory) throws StructuralException {
    	return addTarget(name, weights, new IndicatorPDF(tauPSC, tauPSC), null, modulatory);
	}
    
    /**
	 * @param weights Each row is used as a 1 by m matrix of weights in a new termination on the nth expandable node
	 * @param tauPSC PDF from which psc time constants will be sampled
	 *
	 * @see ca.nengo.model.ExpandableNode#addTarget(java.lang.String, float[][], float, boolean)
	 */
    public synchronized NTarget addTarget(String name, float[][] weights, PDF tauPSC, PDF delays, boolean modulatory) throws StructuralException {
    	for(NTarget t : getTargets()) {
        	if(t.getName().equals(name))
        		throw new StructuralException("The ensemble already contains a termination named " + name);
        }
    	
		if (myExpandableNodes.length != weights.length) {
			throw new StructuralException(weights.length + " sets of weights given for "
					+ myExpandableNodes.length + " expandable nodes");
		}

		int dimension = weights[0].length;

		NTarget[] components = new NTarget[myExpandableNodes.length];
		for (int i = 0; i < myExpandableNodes.length; i++) {
			if (weights[i].length != dimension) {
				throw new StructuralException("Equal numbers of weights are needed for termination onto each node");
			}
			
			if(delays == null)
				components[i] = myExpandableNodes[i].addTarget(name, new float[][]{weights[i]}, tauPSC.sample()[0], modulatory);
			else {
				if(myExpandableNodes[i] instanceof ExpandableSpikingNeuron) 
					components[i] = ((ExpandableSpikingNeuron)myExpandableNodes[i]).addDelayedTermination(name, 
							new float[][]{weights[i]}, tauPSC.sample()[0], delays.sample()[0], modulatory);
				else
					throw new StructuralException("Cannot specify delays for non-ExpandableSpikingNeuron");
			}
		}

		GroupTarget result = new GroupTarget(this, name, components);
		myExpandedTargets.put(name, result);

		fireVisibleChangeEvent();

		return result;
	}

	/**
	 * @throws StructuralException if Termination does not exist
	 * @see ca.nengo.model.ExpandableNode#removeTarget(java.lang.String)
	 */
	@Override
    public synchronized NTarget removeTarget(String name) throws StructuralException {
		if (myExpandedTargets.containsKey(name)) {
		    NTarget result = myExpandedTargets.remove(name);
			for (ExpandableNode myExpandableNode : myExpandableNodes) {
				myExpandableNode.removeTarget(name);
			}

			fireVisibleChangeEvent();
			return result;
		} else if (getTarget(name) != null) {
			return super.removeTarget(name);
		}
		throw new StructuralException("Termination " + name + " does not exist");
	}

	/**
	 * @see ca.nengo.model.ExpandableNode#getDimension()
	 */
    public int getDimension() {
		return myExpandableNodes.length;
	}

	@Override
	public GroupImpl clone() throws CloneNotSupportedException {
		GroupImpl result = (GroupImpl) super.clone();

		result.myExpandableNodes = new ExpandableNode[myExpandableNodes.length];
		for (int i = 0; i < myExpandableNodes.length; i++) {
			result.myExpandableNodes[i] = myExpandableNodes[i].clone();
		}
		
		result.myExpandedTargets = new LinkedHashMap<String, NTarget>(10);
		for (Map.Entry<String, NTarget> stringTerminationEntry : myExpandedTargets.entrySet()) {
			result.myExpandedTargets.put(stringTerminationEntry.getKey(),
                    stringTerminationEntry.getValue().clone(result));
		}

		return result;
	}
	
	public void reset(boolean randomize) {
		super.reset(randomize);
		
		for(NTarget t: myExpandedTargets.values())
			t.reset(randomize);
	}

	public Node[] getChildren() {
		return new Node[0];
	}

	public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
		return "";
	}
}
