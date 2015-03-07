/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ExpandableSynapticIntegrator.java". Description:
"A SynapticIntegrator to which Terminations can be added after construction,
  in a standard way"

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
package ca.nengo.neural.neuron;

import ca.nengo.model.StructuralException;
import ca.nengo.model.NTarget;

/**
 * A SynapticIntegrator to which Terminations can be added after construction,
 * in a standard way. This facilitates circuit building. However, this may
 * not be possible with a sophisticated dendritic model, with which more
 * involved setup is probably needed (e.g. constructing individual synapse models;
 * specifying spatial confuguration of synapses). In this case, the synpases
 * should be defined first, before assembling the circuit, and the SynapticIntegrator
 * might not be expandable in the standard manner defined here.
 *
 * @author Bryan Tripp
 */
public interface ExpandableSynapticIntegrator extends SynapticIntegrator {

	/**
	 * @param name Name of Termination
	 * @param weights Synaptic weights associated with this Termination
	 * @param tauPSC Time constant of post-synaptic current decay (all Terminations have
	 * 		this property but it may have slightly different interpretations depending on
	 * 		the SynapticIntegrator or other properties of the Termination).
	 * @param modulatory If true, inputs to the Termination are not summed with other inputs (they
	 * 		only have modulatory effects, eg on plasticity, which must be defined elsewhere).
	 * @return resulting Termination
	 * @throws StructuralException if there is already a Termination of the same name on this
	 * 		SynapticIntegrator
	 */
	public NTarget addTermination(String name, float[] weights, float tauPSC, boolean modulatory) throws StructuralException;
	
	/**
	 * @param name Name of Termination to remove.
	 * @return The removed Termination
	 * @throws StructuralException if there is no Termination of the given name on this
     *      SynapticIntegrator
	 */
	public NTarget removeTermination(String name);

	public ExpandableSynapticIntegrator clone() throws CloneNotSupportedException;

}
