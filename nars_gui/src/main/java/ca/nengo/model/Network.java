/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "Network.java". Description:
"A neural circuit, consisting of Nodes such as Ensembles and ExternalInputs"

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
package ca.nengo.model;

import ca.nengo.sim.Simulator;
import ca.nengo.util.ScriptGenException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 * <p>A neural circuit, consisting of Nodes such as Ensembles and ExternalInputs. A Network is the
 * usual object of a simulation. If you are new to this code, what you probably want to
 * do is create some Neurons, group them into Ensembles, connect the Ensembles in a Network,
 * and run the Network in a Simulator.</p>
 *
 * <p>Note: Multiple steps are needed to add a Projection between Ensembles. First, an Origin
 * must be created on the presynaptic Ensemble, and a Termination with the same dimensionality
 * must be created on the post-synaptic Ensemble. Then the Origin and Termination can be connected
 * with the method addProjection(Origin, Termination). We don't do this in one step (ie
 * automatically create the necessary Origin and Termination as needed) because there are various
 * ways of doing so, and in fact some types of Origins and Terminations can only be created in the
 * course of constructing the Ensemble. Creation of an Origin or Termination can also be a complex process.
 * Rather than try to abstract these varied procedures into something that can be driven from the Network
 * level, we just assume here that the necessary Origins and Terminations exist, and provide a method
 * for connecting them.</p>
 *
 * @author Bryan Tripp
 */
public interface Network<K, N extends Node> extends Group<N>, Probeable {

	/**
	 * @param node Node to add to the Network
	 * @throws StructuralException if the Network already contains a Node of the same name
	 */
	public void addNode(N node) throws StructuralException;


	/**
	 * @param name Name of Node to remove
	 * @return Named node
	 * @throws StructuralException if named Node does not exist in network
	 */
	public N getNode(K name) throws StructuralException;

	/**
     * @param name Name of Node to remove
     * @throws StructuralException if named Node does not exist in network
	 */
	public N removeNode(K name);


	/**
	 * Connects an Origin to a Termination. Origins and Terminations belong to
	 * Ensembles (or ExternalInputs). Both the Origin and Termination must be set up
	 * before calling this method. The way to do this will depend on the Ensemble.
	 *
	 * @param source Origin (data source) of Projection.
	 * @param target Termination (data destination) of Projection.
	 * @return The created Projection
	 * @throws StructuralException if the given Origin and Termination have different dimensions,
	 * 		or if there is already an Origin connected to the given Termination (note that an
	 * 		Origin can project to multiple Terminations though).
	 */
	public Projection addProjection(NSource source, NTarget target) throws StructuralException;

	/**
	 * @return All Projections in this Network
	 */
	public Projection[] getProjections();

	/**
	 * @param target Termination of Projection to remove
	 * @throws StructuralException if there exists no Projection between the specified
	 * 		Origin and Termination
	 */
	public void removeProjection(NTarget target) throws StructuralException;

	/**
	 * Declares the given Origin as available for connection outside the Network
	 * via getOrigins(). This Origin should not be connected within	this Network.
	 *
	 * @param source An Origin within this Network that is to connect to something
	 * 		outside this Network
	 * @param name Name of the Origin as it will appear outside this Network
	 */
	public void exposeOrigin(NSource source, String name);


	/**
	 * @param insideSource Origin inside the network
	 * @return Name of the exposed origin given the inner origin. null if no
	 *   such origin is exposed.
	 */
	public String getExposedOriginName(NSource insideSource);

	/**
	 * Undoes exposeOrigin(x, x, name).
	 *
	 * @param name Name of Origin to unexpose.
	 * @throws StructuralException if Origin does not exist
	 */
	public void hideOrigin(String name) throws StructuralException;

	/**
	 * Declares the given Termination as available for connection from outside the Network
	 * via getTerminations(). This Termination should not be connected within this Network.
	 *
	 * @param target A Termination within this Network that is to connect to something
	 * 		outside this Network
	 * @param name Name of the Termination as it will appear outside this Network
	 */
	public void exposeTermination(NTarget target, String name);

	/**
	 * Undoes exposeTermination(x, x, name).
	 *
	 * @param name Name of Termination to unexpose.
	 */
	public void hideTermination(String name);

	/**
	 * @param insideTarget Termination inside the network
	 * @return Name of the exposed termination given the inner termination or
     *   null if no such termination is exposed.
	 */
	public String getExposedTerminationName(NTarget insideTarget);

	/**
	 * Declares the given Probeable state as being available for Probing from outside this
	 * Network.
	 *
	 * @param probeable A Probeable within this Network.
	 * @param stateName A state of the given Probeable
	 * @param name A new name with which to access this state via Network.getHistory
	 * @throws StructuralException if Probeable not in the Network
	 */
	public void exposeState(Probeable probeable, String stateName, String name) throws StructuralException;

	/**
	 * Undoes exposeState(x, x, name).
	 *
	 * @param name Name of state to unexpose.
	 */
	public void hideState(String name);

	/**
	 * @param simulator The Simulator used to run this Network
	 */
	public void setSimulator(Simulator simulator);

	/**
	 * @return The Simulator used to run this Network
	 */
	public Simulator getSimulator();
	
	/**
	 * Metadata is non-critical information about the Network (eg UI layout) that the user doesn't
	 * access directly.
	 *
	 * (Note: if there is a need for user-accessible metadata, Network could extend Configurable, but this doesn't
	 * seem to be necessary.)
	 *
	 * @param key Name of a metadata item
	 * @return Value of a metadata item
	 */
	public Object getMetaData(String key);

	/**
	 * @param key Name of a metadata item
	 * @param value Value of the named metadata item
	 */
	public void setMetaData(String key, Object value);
	

    /**
     * @param scriptData Map of class parent and prefix data for generating python script
     * @return Python script for generating special or template ensembles and terminations in the network
     * @throws ScriptGenException if the node cannot be generated in script
     */
	public String toPostScript(HashMap<String, Object> scriptData);

	public void addStepListener(StepListener listener);
	public void removeStepListener(StepListener listener);
	public void fireStepListeners(float time);


}
