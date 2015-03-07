/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "ProbeableOrigin.java". Description: 
"An Origin that obtains output from an underlying Probeable object.
   
  As an example of use, suppose a Neuron has a SynapticIntegrator with a complex 
  dendritic morphology, and that it is desired to model a gap junction between 
  one of these dendrites and a dendrite on another Neuron"

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

package ca.nengo.model.impl;

import ca.nengo.model.*;

/**
 * <p>An Origin that obtains output from an underlying Probeable object.</p>
 *  
 * <p>As an example of use, suppose a Neuron has a SynapticIntegrator with a complex 
 * dendritic morphology, and that it is desired to model a gap junction between 
 * one of these dendrites and a dendrite on another Neuron. If the SynapticIntegrator
 * can provide gap-junctional Origins, there is no problem. But it might not (for 
 * example the implementor of the SynapticIntegrator may not have anticipated this
 * usage). However, if the SynapticIntegrator is Probeable and can be probed for the  
 * appropriate state variables, eg ion concentrations in the compartment of interest, 
 * then this class (ProbeableOrigin) provides a convenient way to model an Origin
 * that outputs the probed information.</p>
 * 
 * <p>For a Neuron, if multi-dimensional state is to be output, it is generally better 
 * to create multiple one-dimensional Outputs than to creat one multi-dimensional Output. 
 * Reasons for this include the following: </p>
 *    
 * <ul><li>As with all Origins, all the output values at a given instant have  
 * the same units. So, if you want to output states with different units, you must use  
 * separate Origins.</li>
 * 
 * <li>Ensembles may combine identically-named Ouputs of different Neurons  
 * into a single Ensemble-level Output (with the same dimension as the number of Neurons that 
 * have that Output). This doesn't work well with multi-dimensional Neuron Outputs. So, if your 
 * Neurons will be grouped into an Ensemble, it's better to stick with 1-D Outputs. The  
 * other option (which seems more convoluted) is to make sure that each Neuron's n-D Output 
 * has a distinct name (ie distinct from the names of the correspoding Outputs of other Neurons 
 * in the same Ensemble). Incorporating a number into the name is one way to do this.</li><ul>
 * 
 * <p>For these reasons, this class supports only 1-dimensional Output, as a way to keep you 
 * out of trouble. This limits its usefulness with Probeables that are Ensembles, but such
 * Probeables probably already provide the needed Outputs anyway.</p>
 * 
 * <p>If you really do want a Neuron to serve as a multi-dimensional Origin, you can do that, 
 * but not with this class. </p> 
 * 
 * @author Bryan Tripp
 */
public class ProbeableSource implements NSource<InstantaneousOutput> {

	private static final long serialVersionUID = 1L;

	private final Node myNode;
	private final Probeable myProbeable;
	private final String myStateVariable;
	private final int myDimension;
	private final String myName;
	private Units myUnits;
	private boolean myRequiredOnCPU;

	/**
	 * @param node The parent node
	 * @param probeable The Probeable from which to obtain state variables to output
	 * @param state State variable to output
	 * @param dimension Index of the dimension of the specified state variable that is to be output
	 * @param name Name of this Origin
	 * @throws StructuralException if there is a problem running an initial Probeable.getHistory() to
	 * 		ascertain the units.
	 */
	public ProbeableSource(Node node, Probeable probeable, String state, int dimension, String name) throws StructuralException {
		myNode = node;
		myProbeable = probeable;
		myStateVariable = state;
		myDimension = dimension;
		myName = name;

		try {
			myUnits = probeable.getHistory(state).getUnits()[dimension];
		} catch (SimulationException e) {
			throw new StructuralException("Problem getting pre-simulation history in order to find state variable units", e);
		}
	}

	/**
	 * @see ca.nengo.model.NSource#getName()
	 */
	public String getName() {
		return myName;
	}

	/**
	 * @return 1
	 * @see ca.nengo.model.NSource#getDimensions()
	 */
	public int getDimensions() {
		return 1;
	}

	/**
	 * @return The final value in the TimeSeries for the state variable that is retrieved
	 * 		from the underlying Probeable
	 */
	public InstantaneousOutput get() {
        try {
            float[] times = myProbeable.getHistory(myStateVariable).getTimes();
            float[][] series = myProbeable.getHistory(myStateVariable).getValues();

            float result = 0;

            if (series.length > 0) {
                result = series[series.length - 1][myDimension];
            }

            return new RealOutputImpl(new float[]{result}, myUnits, times[times.length - 1]);
        }
        catch (SimulationException e) {
            e.printStackTrace();
        }
        return null;
    }

	public void accept(InstantaneousOutput values) {
		throw new RuntimeException("Not implemented for probeable origins");
	}

	/**
	 * @see ca.nengo.model.NSource#getNode()
	 */
	public Node getNode() {
		return myNode;
	}
	
	public void setRequiredOnCPU(boolean val){
        myRequiredOnCPU = val;
    }
    
    public boolean getRequiredOnCPU(){
        return myRequiredOnCPU;
    }

	@Override
	public ProbeableSource clone() throws CloneNotSupportedException {
		return (ProbeableSource)super.clone();
	}
	
	public ProbeableSource clone(Node node) throws CloneNotSupportedException {
		return this.clone();
	}
}
