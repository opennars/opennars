/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "PassthroughNode.java". Description:
"A Node that passes values through unaltered.

  This can be useful if an input to a Network is actually routed to multiple destinations,
  but you want to handle this connectivity within the Network rather than expose multiple
  terminations"

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
 * Created on 24-May-07
 */
package ca.nengo.model.impl;

import ca.nengo.model.*;
import ca.nengo.neural.SpikeOutput;
import ca.nengo.util.MU;
import ca.nengo.util.ScriptGenException;
import ca.nengo.util.VisiblyChanges;
import ca.nengo.util.VisiblyChangesUtils;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

import java.util.*;

/**
 * <p>A Node that passes values through unaltered.</p>
 *
 * <p>This can be useful if an input to a Network is actually routed to multiple destinations,
 * but you want to handle this connectivity within the Network rather than expose multiple
 * terminations.</p>
 *
 * @author Bryan Tripp
 */
public class DirectNode implements Node<Node> {

	//implementation note: this class doesn't nicely extend AbstractNode

	private static final Logger ourLogger = LogManager.getLogger(DirectNode.class);

	/**
	 * Default name for a termination
	 */
	public static final String TERMINATION = "termination";


	/**
	 * Default name for an origin
	 */
	public static final String ORIGIN = "origin";

	private static final long serialVersionUID = 1L;

	private String myName;
	private final int myDimension; //TODO: clean this up (can be obtained from transform)
	private Map<String, ObjectTarget<InstantaneousOutput>> myTerminations;
	private BasicSource myOrigin;
	private String myDocumentation;
	private transient ArrayList<VisiblyChanges.Listener> myListeners;

	/**
	 * Constructor for a simple passthrough with single input.
	 *
	 * @param name Node name
	 * @param dimension Dimension of data passing through
	 */
	public DirectNode(String name, int dimension) {
		myName = name;
		myDimension = dimension;
		myTerminations = new HashMap(10);
		myTerminations.put(TERMINATION, new ObjectTarget(this, TERMINATION, dimension,InstantaneousOutput.class));
		myOrigin = new BasicSource(this, ORIGIN, dimension, Units.UNK);
		reset(false);
	}

	/**
	 * Constructor for a summing junction with multiple inputs.
	 *
	 * @param name Node name
	 * @param dimension Dimension of data passing through
	 * @param termDefinitions Name of each Termination (TERMINATION is used for the single-input case)
	 * 		and associated transform
	 */
	public DirectNode(String name, int dimension, Map<String, float[][]> termDefinitions) {
		myName = name;
		myDimension = dimension;
		myTerminations = new HashMap(10);

		Iterator<String> it = termDefinitions.keySet().iterator();
		while (it.hasNext()) {
			String termName = it.next();
			float[][] termTransform = termDefinitions.get(termName);
			myTerminations.put(termName, new ObjectTarget(this, termName, dimension, termTransform));
		}
		myOrigin = new BasicSource(this, ORIGIN, dimension, Units.UNK);
		reset(false);
	}

	/**
	 * @see ca.nengo.model.Node#name()
	 */
	public String name() {
		return myName;
	}

	/**
	 * @param name The new name
	 */
	public void setName(String name) throws StructuralException {
		VisiblyChangesUtils.nameChanged(this, name(), name, myListeners);
		myName = name;
	}

	/**
	 * @see ca.nengo.model.Node#getSource(java.lang.String)
	 */
	public NSource getSource(String name) throws StructuralException {
		if (ORIGIN.equals(name)) {
			return myOrigin;
		} else {
			throw new StructuralException("Unknown origin: " + name);
		}
	}

	/**
	 * @see ca.nengo.model.Node#getSources()
	 */
	public NSource[] getSources() {
		return new NSource[]{myOrigin};
	}

	/**
	 * @see ca.nengo.model.Node#getTarget(java.lang.String)
	 */
	public NTarget getTarget(String name) throws StructuralException {
		if (myTerminations.containsKey(name)) {
			return myTerminations.get(name);
		} else {
			throw new StructuralException("Unknown termination: " + name);
		}
	}

	/**
	 * @see ca.nengo.model.Node#getTargets()
	 */
	public NTarget[] getTargets() {
        Collection<ObjectTarget<InstantaneousOutput>> var = myTerminations.values();
        return var.toArray(new ObjectTarget[var.size()]);
	}

	/**
	 * @see ca.nengo.model.Node#run(float, float)
	 */
	public void run(float startTime, float endTime) throws SimulationException {
		if (myTerminations.size() == 1) {
			myOrigin.accept(myTerminations.values().iterator().next().get());
		} else {
			float[] values = new float[myDimension];
			Iterator<ObjectTarget<InstantaneousOutput>> it = myTerminations.values().iterator();
			while (it.hasNext()) {
				ObjectTarget<InstantaneousOutput> termination = it.next();
				InstantaneousOutput io = termination.get();
				if (io instanceof RealSource) {
					values = MU.sum(values, ((RealSource) io).getValues());
				} else if (io instanceof SpikeOutput) {
					boolean[] spikes = ((SpikeOutput) io).getValues();
					for (int i = 0; i < spikes.length; i++) {
						if (spikes[i]) {
                            values[i] += 1f/(endTime - startTime);
                        }
					}
				} else if (io == null) {
					throw new SimulationException("Null input to Termination " + termination.getName());
				} else {
					throw new SimulationException("Output type unknown: " + io.getClass().getName());
				}
			}
			myOrigin.accept(new RealOutputImpl(values, Units.UNK, endTime));
		}
	}

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
		float time = 0;
		//try {
			if (myOrigin.get() != null) {
                myOrigin.get().getTime();
            }
/*		} catch (SimulationException e) {
			ourLogger.warn("Exception getting time from existing output during reset", e);
		}*/
		myOrigin.accept(new RealOutputImpl(new float[myOrigin.getDimensions()], Units.UNK, time));
		myOrigin.reset(randomize);
	}

	/**
	 * @see ca.nengo.model.SimulationMode.ModeConfigurable#getMode()
	 */
	public SimulationMode getMode() {
		return SimulationMode.DEFAULT;
	}

	/**
	 * Does nothing (only DEFAULT mode is supported).
	 *
	 * @see ca.nengo.model.SimulationMode.ModeConfigurable#setMode(ca.nengo.model.SimulationMode)
	 */
	public void setMode(SimulationMode mode) {
	}

	/**
	 * @see ca.nengo.model.Node#getDocumentation()
	 */
	public String getDocumentation() {
		return myDocumentation;
	}

	/**
	 * @see ca.nengo.model.Node#setDocumentation(java.lang.String)
	 */
	public void setDocumentation(String text) {
		myDocumentation = text;
	}

	/**
	 * @see ca.nengo.util.VisiblyChanges#addChangeListener(ca.nengo.util.VisiblyChanges.Listener)
	 */
	public void addChangeListener(Listener listener) {
		if (myListeners == null) {
			myListeners = new ArrayList<Listener>(2);
		}
		myListeners.add(listener);
	}

	/**
	 * @see ca.nengo.util.VisiblyChanges#removeChangeListener(ca.nengo.util.VisiblyChanges.Listener)
	 */
	public void removeChangeListener(Listener listener) {
		myListeners.remove(listener);
	}

	@Override
	public Node clone() throws CloneNotSupportedException {
		DirectNode result = (DirectNode) super.clone();

		result.myOrigin = new BasicSource(result, FunctionInput.ORIGIN_NAME, myDimension, myOrigin.getUnits());
		result.myOrigin.setNoise(myOrigin.getNoise().clone());
		//try {
			result.myOrigin.accept(myOrigin.get());
		/*} catch (SimulationException e) {
			throw new CloneNotSupportedException("Problem copying origin values: " + e.getMessage());
		}*/

		result.myTerminations = new HashMap(10);
		for (ObjectTarget oldTerm : myTerminations.values()) {
			ObjectTarget newTerm = new ObjectTarget(result, oldTerm.getName(),
					oldTerm.getDimensions(), MU.clone(oldTerm.getTransform()));
			result.myTerminations.put(newTerm.getName(), newTerm);
		}

		result.myListeners = new ArrayList<Listener>(5);

		return result;
	}

    public Node[] getChildren() {
		return new Node[0];
	}

	public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
		return "";
	}

}
