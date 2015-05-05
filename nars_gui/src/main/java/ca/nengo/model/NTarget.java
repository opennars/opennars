/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "Termination.java". Description:
"An destination for information in a circuit model"

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
 * Created on May 18, 2006
 */
package ca.nengo.model;


import java.io.Serializable;
import java.util.function.Supplier;

/**
 * <p>A destination for information in a circuit model. A Termination is normally associated
 * with a neural Ensemble or an individual Neuron, although other terminations could be modelled
 * (eg muscles).</p>
 *
 * <p>Terminations onto neural Ensembles can be modelled in two ways. First, a Termination can
 * model a set of axons that end at an Ensemble. In this case the dimension of the Termination
 * equals the number of axons. Associated with each Neuron in the Ensemble will be synaptic weights
 * (possibly zero) corresponding to each axon (i.e. each dimension of the Termination).<p>
 *
 * <p>Alternatively, in a connection between two NEFEnsembles, a termination may have a smaller
 * number of dimensions that summarize activity in all the axons. In this case, each dimension
 * of the termination corresponds to a dimension of a represented vector or function. Synaptic
 * weights are not stored anywhere explicitly. Synaptic weights are instead decomposed into
 * decoding vectors, a transformation matrix, and encoding vectors. The decoding vectors are
 * associated with the sending Ensemble. The encoding vectors are associated with the receiving
 * ensemble. The transformation matrix is a property of the projection, but it happens that we
 * keep it with the receiving Ensemble, for various reasons. See Eliasmith & Anderson, 2003 for
 * related theory.</p>
 *
 * <P>Note that in each case, a corresponding Origin and Termination have the same dimensionality,
 * and that this is the dimensionality associated with the Origin. The receiving Ensemble is responsible
 * for the weight matrix in the first case, and for the transformation matrix in the second case,
 * which transform inputs into dimensions that the receiving Ensemble can use.</p>
 *
 * <p>Note also that the second method is more efficient when the number of neurons in each ensemble
 * is much larger than the number of dimensions in represented variables, as is typical.</p>
 *
 * TODO: should probably extract properties-related methods into another interface (Configurable?)
 *   possibly supporting types
 *
 * @author Bryan Tripp
 */
public interface NTarget<V> extends Serializable, Resettable, Cloneable, Supplier<V> {



	/**
	 * @return Name of this Termination (unique in the scope of the object the which the Termination
	 * 		is connected, eg the Neuron or Ensemble).
	 */
	public String getName();

	/**
	 * @return Dimensionality of information entering this Termination (eg number of
	 * 		axons, or dimension of decoded function of variables represented by sending
	 * 		Ensemble)
	 */
	public int getDimensions();

	/**
	 * @param values InstantaneousOutput (eg from another Ensemble) to apply to this Termination.
	 * @throws SimulationException if the given values have the wrong dimension
	 */
	public void apply(V values) throws SimulationException;

    default public boolean applies(V value) { return true; }

	/**
	 * @return The Node to which this Termination belongs
	 */
	public Node getNode();

	/**
	 * @return Time constant of dominant dynamics
	 */
	public float getTau();

	/**
	 * @param tau Time constant of dominant dynamics
	 * @throws StructuralException if the time constant cannot be changed
	 */
	public void setTau(float tau) throws StructuralException;

	/**
	 * @return Whether the Termination is modulatory, in the sense of neuromodulation, ie true if
	 * 		input via this Termination is not summed to drive a node, but influences node activity
	 * 		in some other way
	 */
	public boolean getModulatory();

	/**
	 * @param modulatory True if the Termination is to be modulatory
	 */
	public void setModulatory(boolean modulatory);
	
	/**
	 * @return Latest input to the termination.
	 */
    @Override
	public V get();


	/**
	 * @return Valid clone
	 * @throws CloneNotSupportedException if clone can't be made
	 */
	public NTarget<V> clone() throws CloneNotSupportedException;
	
	/**
	 * Clone method that changes necessary parameters to point to a new parent,
	 * for use in cloning ensembles, etc.
	 * @param node New parent node
	 * @return A clone of the termination for the new parent ensemble
	 * @throws CloneNotSupportedException if clone cannot be made
	 */
	public NTarget<V> clone(Node node) throws CloneNotSupportedException;



    /**
     * Standard name of the post-synaptic current time constant property (most Terminations
     * have this property).
     */
//	public static final String TAU_PSC = "tauPSC";

    /**
     * A modulatory termination does not induce current directly but may influence membrane properties or
     * excitability: Boolean(true) means modulatory; Boolean(false) means not modulatory.
     */
//	public static final String MODULATORY = "MODULATORY";

    /**
     * Standard name of synaptic weights property (a float[][])
     */
//	public static final String WEIGHTS = "WEIGHTS";

    /**
     * A property value for Terminations that are composed of multiple underlying
     * Terminations. This property value indicates that different underlying Terminations
     * report different values for the requested property.
     */
//	public static final String MIXED_VALUE = "MIXED VALUE";
}
