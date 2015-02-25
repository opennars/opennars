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
package ca.nengo.neural.plasticity.impl;

import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.Node;
import ca.nengo.model.PlasticNodeTarget;
import ca.nengo.model.StructuralException;

import java.util.Arrays;

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
 * @author Trevor Bekolay
 * @author Jonathan Lai
 */
public abstract class ModulatedPlasticGroupTarget extends PlasticGroupTarget {

    private static final long serialVersionUID = 1L;
    protected String myModTermName;
    protected float[] myModInput;
    protected float[] myFilteredModInput;

    /**
     * @param node The parent Node
     * @param name Name of this Termination
     * @param nodeTerminations Node-level Terminations that make up this Termination. Must be
     *        all LinearExponentialTerminations
     * @throws StructuralException If dimensions of different terminations are not all the same
     */
    public ModulatedPlasticGroupTarget(Node node, String name, PlasticNodeTarget[] nodeTerminations) throws StructuralException {
        super(node, name, nodeTerminations);
    }

    /**
     * @return Name of the Termination from which modulatory input is drawn (can be null if not used)
     */
    public String getModTermName() {
        return myModTermName;
    }

    /**
     * @param name Name of the Termination from which modulatory input is drawn (can be null if not used)
     */
    public void setModTermName(String name) {
        myModTermName = name;
    }

    /**
     * @see ca.nengo.model.Resettable#reset(boolean)
     */
    @Override
    public void reset(boolean randomize) {
        super.reset(randomize);
        if (myModInput != null) {
        	Arrays.fill(myModInput, 0.0f);
        }
    }

    /**
     * @param name Name of the termination from which modulatory input is drawn
     * @param state The state to set
     * @param time Current time
     * @throws StructuralException if modulatory termination does not exist
     */
    public void setModTerminationState(String name, InstantaneousOutput state, float time) throws StructuralException {
        if (myModTermName == null) {
            throw new StructuralException("Modulatory termination name not set in PESTermination");
        }

        if (!name.equals(myModTermName)) { return; }
        
        if (myModInput == null) {
        	myModInput = new float[state.getDimension()];
        }
        float integrationTime = 0.001f;
        updateRaw(myModInput, state, integrationTime);
        
        float tauPSC = getNodeTerminations()[0].getTau();
        if (myFilteredModInput == null) {
        	myFilteredModInput = new float[state.getDimension()];
        }
        updateFiltered(myModInput, myFilteredModInput, tauPSC, integrationTime);
    }
    
    @Override
    public ModulatedPlasticGroupTarget clone(Node node) throws CloneNotSupportedException {
    	ModulatedPlasticGroupTarget result = (ModulatedPlasticGroupTarget)super.clone(node);
    	result.myModInput = (myModInput != null) ? myModInput.clone() : null;
    	return result;
    }
}
