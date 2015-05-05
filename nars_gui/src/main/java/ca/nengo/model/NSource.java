/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "Origin.java". Description:
"An source of information in a circuit model"

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


import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <p>An source of information in a circuit model. Origins arise from Ensembles,
 * ExternalInputs, and individual Neurons (although the latter Origins are mainly used
 * internally within Ensembles, ie an Ensemble typically combines Neuron Origins into
 * Ensemble Origins).</p>
 *
 * <p>An Origin object will often correspond loosely to the anatomical origin of a neural
 * projection in the brain. However, there is not a strict correspondance. In particular,
 * an Origin object may relate specifically to a particular decoding of
 * activity in an Ensemble. For example, suppose a bundle of axons bifurcates and
 * terminates in two places. This would be modelled with two Origin objects if the
 * postsynaptic Ensembles received different functions of the variables represented by the
 * presynaptic Ensemble. So, an Origin is best thought about as a source of information
 * in a certain form, rather than an anatomical source of axons.</p>
 *
 * @author Bryan Tripp
 */
public interface NSource<V> extends Serializable, Cloneable, Consumer<V>, Supplier<V> {

	/**
	 * @return Name of this Origin (unique in the scope of a source of Origins, eg a Neuron or
	 * 		Ensemble)
	 */
	public String getName();

	/**
	 * @return Dimensionality of information coming from this Origin (eg number of
	 * 		axons, or dimension of decoded function of variables represented by the
	 * 		Ensemble)
	 */
	public int getDimensions();

	/**
	 * @return Instantaneous output from this Origin.
	 * @throws SimulationException if there is any problem retrieving values
	 */
    @Override
	public V get();
	
	/**
	 * @param Instantaneous output from this Origin.
	 */
    @Override
	public void accept(V val);
	
	public void setRequiredOnCPU(boolean val);
	
	public boolean getRequiredOnCPU();

	/**
	 * @return The Node to which the Origin belongs
	 */
	public Node getNode();

	/**
	 * @return Valid clone
	 * @throws CloneNotSupportedException if clone cannot be made
	 */
	public NSource clone() throws CloneNotSupportedException;
	
	/**
	 * Clone method that changes necessary parameters to point to a new parent,
	 * for use in cloning ensembles, etc.
	 * @param e New parent ensemble
	 * @return A clone of the origin for the new parent ensemble
	 * @throws CloneNotSupportedException if clone cannot be made
	 */
	public NSource clone(Node node) throws CloneNotSupportedException;

}
