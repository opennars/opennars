/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "EnsembleTermination.java". Description:
"A Termination that is composed of Terminations onto multiple Nodes"

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

import ca.nengo.model.*;

/**
 * <p>A Termination that is composed of Terminations onto multiple Nodes.
 * The dimensions of the Terminations onto each Node must be the same.</p>
 *
 * <p>Physiologically, this might correspond to a set of n axons passing into
 * a neuron pool. Each neuron in the pool receives synaptic connections
 * from as many as n of these axons (zero weight is equivalent to no
 * connection). Sometimes we deal with this set of axons only in terms
 * of the branches they send to one specific Neuron (a Node-level Termination)
 * but here we deal with all branches (an Ensemble-level Termination).
 * In either case the spikes transmitted by the axons are the same.</p>
 *
 * TODO: test
 *
 * @author Bryan Tripp
 */
public class GroupTarget implements Target {

	private static final long serialVersionUID = 1L;

	private Node myNode;
	private String myName;
	private Target[] myNodeTargets;

	/**
	 * @param node The parent Node
	 * @param name Name of this Termination
	 * @param nodeTargets Node-level Terminations that make up this Termination
	 * @throws StructuralException If dimensions of different terminations are not all the same
	 */
	public GroupTarget(Node node, String name, Target[] nodeTargets) throws StructuralException {
		checkSameDimension(nodeTargets, name);

		myNode = node;
		myName = name;
		myNodeTargets = nodeTargets;
	}

	private static void checkSameDimension(Target[] targets, String name) throws StructuralException {
		int dim = targets[0].getDimensions();
		for (int i = 1; i < targets.length; i++) {
			if (targets[i].getDimensions() != dim) {
				throw new StructuralException("All Terminations " + name + " must have the same dimension");
			}
		}
	}

	/**
	 * @see ca.nengo.model.Target#getName()
	 */
    public String getName() {
		return myName;
	}

	/**
	 * @see ca.nengo.model.Target#getDimensions()
	 */
    public int getDimensions() {
		return myNodeTargets[0].getDimensions();
	}

	/**
	 * @see ca.nengo.model.Target#setValues(ca.nengo.model.InstantaneousOutput)
	 */
    public void setValues(InstantaneousOutput values) throws SimulationException {
		if (values.getDimension() != getDimensions()) {
			throw new SimulationException("Input to this Termination must have dimension " + getDimensions());
		}

		for (Target myNodeTarget : myNodeTargets) {
			myNodeTarget.setValues(values);
		}
	}

	/**
	 * @return Latest input to the underlying terminations.
	 */
	public InstantaneousOutput get(){
		return myNodeTargets[0].get();
	}

	/**
	 * Returns true if more than half of node terminations are modulatory.
	 * @see ca.nengo.model.Target#getModulatory()
	 */
    public boolean getModulatory() {
		int nModulatory = 0;
		for (Target myNodeTarget : myNodeTargets) {
			if (myNodeTarget.getModulatory()) {
                nModulatory++;
            }
		}
		return nModulatory > myNodeTargets.length/2;
	}

	/**
	 * Returns the average.
	 *
	 * @see ca.nengo.model.Target#getTau()
	 */
    public float getTau() {
		float sumTau = 0;
		for (Target myNodeTarget : myNodeTargets) {
			sumTau += myNodeTarget.getTau();
		}
		return sumTau / myNodeTargets.length;
	}

	/**
	 * @see ca.nengo.model.Target#setModulatory(boolean)
	 */
    public void setModulatory(boolean modulatory) {
		for (Target myNodeTarget : myNodeTargets) {
			myNodeTarget.setModulatory(modulatory);
		}
	}

	/**
	 * @see ca.nengo.model.Target#setTau(float)
	 */
    public void setTau(float tau) throws StructuralException {
		float[] oldValues = new float[myNodeTargets.length];

		for (int i = 0; i < myNodeTargets.length; i++) {
			oldValues[i] = myNodeTargets[i].getTau();
			try {
				myNodeTargets[i].setTau(tau);
			} catch (StructuralException e) {
				//roll back changes
				for (int j = 0; j < i; j++) {
					myNodeTargets[j].setTau(oldValues[j]);
				}
				throw new StructuralException(e);
			}
		}
	}

	/**
	 * @see ca.nengo.model.Target#getNode()
	 */
    public Node getNode() {
		return myNode;
	}

	/**
	 * @return Array with all of the underlying node terminations
	 */
	public Target[] getNodeTerminations(){
		return myNodeTargets;
	}

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
    public void reset(boolean randomize) {
		for (Target myNodeTarget : myNodeTargets) {
			myNodeTarget.reset(randomize);
		}
	}

	@Override
	public GroupTarget clone() throws CloneNotSupportedException {
		return this.clone(myNode);
	}
	
	public GroupTarget clone(Node node) throws CloneNotSupportedException {
		GroupTarget result = (GroupTarget) super.clone();
		result.myNode = node;
		result.myName = myName;

		// get terminations for nodes in new ensemble
		result.myNodeTargets = myNodeTargets.clone();
		if (node instanceof Group || node instanceof Network) {
			try {
				if (node instanceof Group) {
					Group group = (Group)node;
					for (int i = 0; i < result.myNodeTargets.length; i++){
						result.myNodeTargets[i] = group.getNodes()[i].getTermination(myNodeTargets[i].getName());
					}
				}
				if (node instanceof Network) {
					Network network = (Network)node;
					for (int i = 0; i < result.myNodeTargets.length; i++){
						result.myNodeTargets[i] = network.getNodes()[i].getTermination(myNodeTargets[i].getName());
					}
				}
			} catch (StructuralException e) {
				throw new CloneNotSupportedException("Error cloning EnsembleTermination: " + e.getMessage());
			}
		}
		else {
			throw new CloneNotSupportedException("Error cloning EnsembleTermination: Wrong node type.");
		}
		return result;
	}

}
