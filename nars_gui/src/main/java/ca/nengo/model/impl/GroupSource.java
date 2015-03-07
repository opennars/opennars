/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "EnsembleOrigin.java". Description: 
"An Origin that is composed of the Origins of multiple Nodes"

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
import ca.nengo.neural.PreciseSpikeOutput;
import ca.nengo.neural.SpikeOutput;
import ca.nengo.neural.impl.PreciseSpikeOutputImpl;
import ca.nengo.neural.impl.SpikeOutputImpl;

/**
 * An Origin that is composed of the Origins of multiple Nodes. The dimension 
 * of this Origin equals the number of Nodes. All the Nodes must produce the 
 * same type of output (RealOutput or SpikeOutput) with the same Unit at the same 
 * time (these things can change in subsequent time steps, but they must change 
 * together for all Nodes). 
 *   
 * @author Bryan Tripp
 */
public class GroupSource implements NSource<InstantaneousOutput> {

	private static final long serialVersionUID = 1L;
	
	private Node myNode;
	private NSource<InstantaneousOutput>[] myNodeSources;
	private final String myName;
	private boolean myRequiredOnCPU;
	
	/**
	 * @param node The parent Node
	 * @param name Name of this Origin 
	 * @param nodeSources Origins on individual Nodes that are combined to make this
	 * 		Origin. Each of these is expected to have dimension 1, but this is not enforced. 
	 * 		Other dimensions are ignored. 
	 */
	public GroupSource(Node node, String name, NSource[] nodeSources) {
		myNode = node;
		myNodeSources = nodeSources;
		myName = name;
	}

	/**
	 * @see ca.nengo.model.NSource#getName()
	 */
	public String getName() {
		return myName;
	}

	/**
	 * @see ca.nengo.model.NSource#getDimensions()
	 */
	public int getDimensions() {
		return myNodeSources.length;
	}

	/**
	 * @return Array with all of the underlying node origins
	 */
	public NSource[] getNodeOrigins(){
		return myNodeSources;
	}

	/**
	 * @return A composite of the first-dimensional outputs of all the Node Origins
	 * 		that make up the EnsembleOrigin. Node Origins should normally have 
	 * 		dimension 1, but this isn't enforced here. All Node Origins must have 
	 * 		the same units, and must output the same type of InstantaneousOuput (ie 
	 * 		either SpikeOutput or RealOutput), otherwise an exception is thrown.   
	 * @see ca.nengo.model.NSource#get()
	 */
	public InstantaneousOutput get() {
		InstantaneousOutput result = null;
		
		Units units = myNodeSources[0].get().getUnits(); //must be same for all

        try {
            if (myNodeSources[0].get() instanceof RealSource) {
                result = composeRealOutput(myNodeSources, units);
            } else if (myNodeSources[0].get() instanceof PreciseSpikeOutput) {
                result = composePreciseSpikeOutput(myNodeSources, units);
            } else if (myNodeSources[0].get() instanceof SpikeOutput) {
                result = composeSpikeOutput(myNodeSources, units);
            }
            return result;
        }
        catch (SimulationException e) {
            e.printStackTrace();
        }
		return null;
	}
	
	public void accept(InstantaneousOutput values) {
		for(NSource source : myNodeSources){
			source.accept(values);
		}
	}
	
	private static RealSource composeRealOutput(NSource<InstantaneousOutput>[] sources, Units units) throws SimulationException {
		float[] values = new float[sources.length];
		
		for (int i = 0; i < sources.length; i++) {
			InstantaneousOutput o = sources[i].get();
			if ( !(o instanceof RealSource) ) {
				throw new SimulationException("Some of the Node Origins are not producing real-valued output");
			}
			if ( !o.getUnits().equals(units) ) {
				throw new SimulationException("Some of the Node Origins are producing outputs with non-matching units");
			}
			
			values[i] = ((RealSource) o).getValues()[0];
		}
		
		return new RealOutputImpl(values, units, sources[0].get().getTime());
	}
	
	private static SpikeOutput composeSpikeOutput(NSource<InstantaneousOutput>[] sources, Units units) throws SimulationException {
		boolean[] values = new boolean[sources.length];
		
		for (int i = 0; i < sources.length; i++) {
			InstantaneousOutput o = sources[i].get();
			if ( !(o instanceof SpikeOutput) ) {
				throw new SimulationException("Some of the Node Origins are not producing spiking output");
			}
			if ( !o.getUnits().equals(units) ) {
				throw new SimulationException("Some of the Node Origins are producing outputs with non-matching units");
			}
			
			values[i] = ((SpikeOutput) o).getValues()[0];
		}
		
		return new SpikeOutputImpl(values, units, sources[0].get().getTime());
	}

	private static PreciseSpikeOutput composePreciseSpikeOutput(NSource<InstantaneousOutput>[] sources, Units units) throws SimulationException {
		float[] values = new float[sources.length];
		
		for (int i = 0; i < sources.length; i++) {
			InstantaneousOutput o = sources[i].get();
			if ( !(o instanceof PreciseSpikeOutput) ) {
				throw new SimulationException("Some of the Node Origins are not producing precise spiking output");
			}
			if ( !o.getUnits().equals(units) ) {
				throw new SimulationException("Some of the Node Origins are producing outputs with non-matching units");
			}
			
			values[i] = ((PreciseSpikeOutput) o).getSpikeTimes()[0];
		}
		
		return new PreciseSpikeOutputImpl(values, units, sources[0].get().getTime());
	}
	
	public void setRequiredOnCPU(boolean val){
        myRequiredOnCPU = val;
    }
    
    public boolean getRequiredOnCPU(){
        return myRequiredOnCPU;
    }
	
	
	/**
	 * @see ca.nengo.model.NSource#getNode()
	 */
	public Node getNode() {
		return myNode;
	}

	/**
	 * Note: the clone references the same copies of the underlying node origins. This 
	 * will work if the intent is to duplicate an EnsembleOrigin on the same Ensemble. 
	 * More work is needed if this clone is part of an Ensemble clone, since the cloned
	 * EnsembleOrigin should then reference the new node origins, which we don't have 
	 * access to here.   
	 */
	@Override
	public GroupSource clone() throws CloneNotSupportedException {
		return this.clone(myNode);
	}
	
	public GroupSource clone(Node node) throws CloneNotSupportedException {
		if (!(node instanceof Group) && !(node instanceof Network)) {
			throw new CloneNotSupportedException("Error cloning EnsembleOrigin: Invalid node type");
		}
		
		try {
			GroupSource result = (GroupSource) super.clone();
			result.myNode = node;
			
			// get origins for nodes in new ensemble
			NSource[] sources = new NSource[myNodeSources.length];
			if (node instanceof Group) {
				Group group = (Group)node;
				for (int i = 0; i < myNodeSources.length; i++)
					sources[i] = group.getNodes()[i].getSource(myNodeSources[i].getName());
			}
			if (node instanceof Network) {
				Network network = (Network)node;
				for (int i = 0; i < myNodeSources.length; i++)
					sources[i] = network.getNodes()[i].getSource(myNodeSources[i].getName());
			}
			result.myNodeSources = sources;
			return result;
		} catch (StructuralException e) {
			throw new CloneNotSupportedException("Error cloning EnsembleOrigin: " + e.getMessage());
		} catch (CloneNotSupportedException e) {
			throw new CloneNotSupportedException("Error cloning EnsembleOrigin: " + e.getMessage());
		}
	}

}
